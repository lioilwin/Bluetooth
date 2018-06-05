package win.lioil.bluetooth.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.UUID;

import win.lioil.bluetooth.R;
import win.lioil.bluetooth.util.Util;

/**
 * 作为BLE peripheral外围设备(GATT server服务端)
 */
public class BleServerActivity extends Activity {
    public static final UUID UUID_SERVICE = UUID.fromString("11111111-67d0-11e8-adc0-fa7ae01bbebc"); //自定义UUID
    public static final UUID UUID_CHAR_READ = UUID.fromString("22222222-67d0-11e8-adc0-fa7ae01bbebc");
    public static final UUID UUID_CHAR_WRITE = UUID.fromString("33333333-67d0-11e8-adc0-fa7ae01bbebc");
    public static final UUID UUID_DESC = UUID.fromString("44444444-67d0-11e8-adc0-fa7ae01bbebc");

    private static final String TAG = BleServerActivity.class.getSimpleName();
    private TextView mTips;
    public BluetoothLeAdvertiser mBluetoothLeAdvertiser; // BLE广播
    private BluetoothGattServer mBluetoothGattServer; // BLE服务端

    // BLE广播Callback
    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            logTv("BLE广播开启成功");
        }

        @Override
        public void onStartFailure(int errorCode) {
            logTv("BLE广播开启失败,errorCode:" + errorCode);
        }
    };

    // BLE服务端Callback
    private BluetoothGattServerCallback mBluetoothGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            Log.i(TAG, String.format("onConnectionStateChange:%s,%s,%s,%s", device.getName(), device.getAddress(), status, newState));
            logTv(newState == 2 ? "与" + device.getAddress() + "连接成功" : "与" + device.getAddress() + "连接断开");
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            Log.i(TAG, String.format("onServiceAdded:%s,%s", status, service.getUuid()));
            logTv(status == 0 ? "服务添加成功,UUID=" + service.getUuid() : "服务添加失败,UUID=" + service.getUuid());
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            Log.i(TAG, String.format("onCharacteristicReadRequest:%s,%s,%s,%s,%s", device.getName(), device.getAddress(), requestId, offset, characteristic.getUuid()));
            String response = "CHAR_" + (int) (Math.random() * 100); //模拟数据
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, response.getBytes());// 响应客户端
            logTv("客户端读取Characteristic: " + response);
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] requestBytes) {
            // 获取客户端发过来的数据
            String requestStr = new String(requestBytes);
            Log.i(TAG, String.format("onCharacteristicWriteRequest:%s,%s,%s,%s,%s,%s,%s,%s", device.getName(), device.getAddress(), requestId, characteristic.getUuid(),
                    preparedWrite, responseNeeded, offset, requestStr));
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, requestBytes);// 响应客户端
            logTv("客户端写入Characteristic: " + requestStr);
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            Log.i(TAG, String.format("onDescriptorReadRequest:%s,%s,%s,%s,%s", device.getName(), device.getAddress(), requestId, offset, descriptor.getUuid()));
            String response = "DESC_" + (int) (Math.random() * 100); //模拟数据
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, response.getBytes()); // 响应客户端
            logTv("客户端读取Descriptor: " + response);
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            // 获取客户端发过来的数据
            String valueStr = new String(value);
            Log.i(TAG, String.format("onDescriptorWriteRequest:%s,%s,%s,%s,%s,%s,%s,%s", device.getName(), device.getAddress(), requestId, descriptor.getUuid(),
                    preparedWrite, responseNeeded, offset, valueStr));
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);// 响应客户端
            logTv("客户端写入Descriptor: " + valueStr);

            // 通知客户端Characteristic变化(简单模拟，实际上应该用List保存客户端设备，以后真的变化再逐一通知)
            BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
            String response = "CHAR_" + (int) (Math.random() * 100); //模拟数据
            characteristic.setValue(response);
            mBluetoothGattServer.notifyCharacteristicChanged(device, characteristic, false);
            logTv("通知客户端Characteristic改变: " + response);
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            Log.i(TAG, String.format("onExecuteWrite:%s,%s,%s,%s", device.getName(), device.getAddress(), requestId, execute));
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            Log.i(TAG, String.format("onNotificationSent:%s,%s,%s", device.getName(), device.getAddress(), status));
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            Log.i(TAG, String.format("onMtuChanged:%s,%s,%s", device.getName(), device.getAddress(), mtu));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bleserver);
        mTips = findViewById(R.id.tv_tips);
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // ============启动BLE蓝牙广播(广告) =================================================================================
        //广播设置
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY) //低延迟
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH) //高信号强度
                .setConnectable(true) //能否连接
                .build();
        //广播数据
        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .addManufacturerData(666, new byte[]{23, 33}) //设备厂商数据，自定义
//                .addServiceData(new ParcelUuid(UUID_SERVICE), new byte[]{16, 17}) //服务数据，自定义
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(true)
                .build();
        mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        mBluetoothLeAdvertiser.startAdvertising(settings, advertiseData, advertiseData, mAdvertiseCallback);

        // 必须要开启BLE广播，其它设备才能扫描发现本设备BLE服务!
        // =============启动蓝牙服务端=====================================================================================
        BluetoothGattService service = new BluetoothGattService(UUID_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        //添加可读+通知characteristic
        BluetoothGattCharacteristic characteristicRead = new BluetoothGattCharacteristic(UUID_CHAR_READ,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ);
        characteristicRead.addDescriptor(new BluetoothGattDescriptor(UUID_DESC, BluetoothGattCharacteristic.PERMISSION_WRITE));
        service.addCharacteristic(characteristicRead);
        //添加可写characteristic
        BluetoothGattCharacteristic characteristicWrite = new BluetoothGattCharacteristic(UUID_CHAR_WRITE,
                BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE);
        service.addCharacteristic(characteristicWrite);
        if (bluetoothManager != null)
            mBluetoothGattServer = bluetoothManager.openGattServer(this, mBluetoothGattServerCallback);
        mBluetoothGattServer.addService(service);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothGattServer != null)
            mBluetoothGattServer.close();
        if (mBluetoothLeAdvertiser != null)
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
    }

    private void logTv(final String msg) {
        if (isDestroyed())
            return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Util.toast(BleServerActivity.this, msg);
                mTips.append(msg + "\n\n");
            }
        });
    }
}