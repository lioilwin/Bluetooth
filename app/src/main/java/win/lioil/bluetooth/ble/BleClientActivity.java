package win.lioil.bluetooth.ble;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;
import java.util.UUID;

import win.lioil.bluetooth.R;
import win.lioil.bluetooth.util.Util;

public class BleClientActivity extends Activity {
    private static final String TAG = BleClientActivity.class.getSimpleName();
    private EditText mWriteET;
    private TextView mTips;

    private BleDevAdapter mBleDevAdapter;
    private BluetoothGatt mBluetoothGatt;
    private boolean isConnected = false;

    // 与服务端连接的Callback
    public BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            BluetoothDevice dev = gatt.getDevice();
            Log.i(TAG, String.format("onConnectionStateChange:%s,%s,%s,%s", dev.getName(), dev.getAddress(), status, newState));
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices(); //启动服务发现
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                isConnected = false;
                gatt.close();// 连接外围设备的数量有限(一般最多6个)，当不需要连接蓝牙设备的时候，必须调用BluetoothGatt#close释放资源
            }
            logTv(newState == 2 ? "与" + dev.getAddress() + "连接成功" : "与" + dev.getAddress() + "连接断开");
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG, String.format("onServicesDiscovered:%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), status));
            if (status == BluetoothGatt.GATT_SUCCESS) { //BLE服务发现成功
                isConnected = true;
                // 遍历获取BLE服务Services/Characteristics/Descriptors的全部UUID
                List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService service : services) {
                    List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                    StringBuilder allUUIDs = new StringBuilder(service.getUuid().toString());
                    for (BluetoothGattCharacteristic ch : characteristics) {
                        allUUIDs.append(",").append(ch.getUuid());
                        List<BluetoothGattDescriptor> descriptors = ch.getDescriptors();
                        for (BluetoothGattDescriptor desc : descriptors)
                            allUUIDs.append(",").append(desc.getUuid());
                    }
                    Log.i(TAG, "onServicesDiscovered.UUIDs=" + allUUIDs.toString());
                    logTv("发现服务UUIDs=[" + allUUIDs + "]");
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            UUID uuid = characteristic.getUuid();
            String valueStr = new String(characteristic.getValue());
            Log.i(TAG, String.format("onCharacteristicRead:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
            logTv("读取Characteristic[" + uuid + "]: " + valueStr);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            UUID uuid = characteristic.getUuid();
            String valueStr = new String(characteristic.getValue());
            Log.i(TAG, String.format("onCharacteristicWrite:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
            logTv("写入Characteristic[" + uuid + "]: " + valueStr);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            UUID uuid = characteristic.getUuid();
            String valueStr = new String(characteristic.getValue());
            Log.i(TAG, String.format("onCharacteristicChanged:%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr));
            logTv("通知Characteristic[" + uuid + "]: " + valueStr);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            UUID uuid = descriptor.getUuid();
            String valueStr = new String(descriptor.getValue());
            Log.i(TAG, String.format("onDescriptorRead:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
            logTv("读取Descriptor[" + uuid + "]: " + valueStr);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            UUID uuid = descriptor.getUuid();
            String valueStr = new String(descriptor.getValue());
            Log.i(TAG, String.format("onDescriptorWrite:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
            logTv("写入Descriptor[" + uuid + "]: " + valueStr);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bleclient);
        RecyclerView rv = findViewById(R.id.rv_ble);
        mWriteET = findViewById(R.id.et_write);
        mTips = findViewById(R.id.tv_tips);
        rv.setLayoutManager(new LinearLayoutManager(this));
        mBleDevAdapter = new BleDevAdapter(new BleDevAdapter.Listener() {
            @Override
            public void onItemClick(BluetoothDevice dev) {
                if (mBluetoothGatt != null && !mBluetoothGatt.getDevice().equals(dev))
                    mBluetoothGatt.disconnect(); // Android连接外围设备的数量有限(有些手机最多6个)，所以断开其它设备连接
                mBluetoothGatt = dev.connectGatt(BleClientActivity.this, false, mBluetoothGattCallback); // 连接蓝牙设备
            }
        });
        rv.setAdapter(mBleDevAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }
    }

    // 扫描BLE
    public void reScan(View view) {
        if (mBleDevAdapter.isScanning)
            Util.toast(this, "正在扫描...");
        else
            mBleDevAdapter.reScan();
    }

    // 读取数据->onCharacteristicRead
    public void read(View view) {
        if (!isConnected) {
            Util.toast(this, "没有连接");
            return;
        }
        BluetoothGattService service = mBluetoothGatt.getService(BleServerActivity.UUID_SERVICE);//通过UUID获取服务
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(BleServerActivity.UUID_CHAR_READ);//通过UUID获取可读的Characteristic
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    // 写入数据->onCharacteristicWrite
    public void write(View view) {
        if (!isConnected) {
            Util.toast(this, "没有连接");
            return;
        }
        String text = mWriteET.getText().toString();
        BluetoothGattService service = mBluetoothGatt.getService(BleServerActivity.UUID_SERVICE);//通过UUID获取服务
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(BleServerActivity.UUID_CHAR_WRITE);//通过UUID获取可写的Characteristic
        characteristic.setValue(text.getBytes()); //单次最多20个字节
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    // 设置Characteristic通知->onCharacteristicChanged
    public void setNotify(View view) {
        if (!isConnected) {
            Util.toast(this, "没有连接");
            return;
        }
        // 设置Characteristic通知
        BluetoothGattService service = mBluetoothGatt.getService(BleServerActivity.UUID_SERVICE);
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(BleServerActivity.UUID_CHAR_READ);//通过UUID获取需要通知的Characteristic
        mBluetoothGatt.setCharacteristicNotification(characteristic, true);

        // 向Characteristic的Descriptor属性写入通知开关，使蓝牙设备主动向手机发送数据
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BleServerActivity.UUID_DESC);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    private void logTv(final String msg) {
        if (isDestroyed())
            return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Util.toast(BleClientActivity.this, msg);
                mTips.append(msg + "\n\n");
            }
        });
    }
}