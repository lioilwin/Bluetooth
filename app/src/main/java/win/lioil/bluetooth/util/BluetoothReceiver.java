package win.lioil.bluetooth.util;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * 监听蓝牙广播-各种状态
 */
public class BluetoothReceiver extends BroadcastReceiver {
    private static final String TAG = BluetoothReceiver.class.getSimpleName();
    private final Listener mListener;

    public BluetoothReceiver(Context cxt, Listener listener) {
        mListener = listener;
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//蓝牙开关状态
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//蓝牙开始搜索
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//蓝牙搜索结束

        filter.addAction(BluetoothDevice.ACTION_FOUND);//蓝牙发现新设备(未配对)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//设备配对状态改变
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);//设备建立连接
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);//设备断开连接

        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED); //BluetoothAdapter连接状态
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED); //BluetoothHeadset连接状态
        filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED); //BluetoothA2dp连接状态
        cxt.registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null)
            return;
        Log.i(TAG, "=== " + action);
        switch (action) {
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                Log.i(TAG, "STATE: " + state);
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                break;

            case BluetoothDevice.ACTION_FOUND:
                BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "BluetoothDevice: " + dev.getName() + ", " + dev.getAddress());
                mListener.foundDev(dev);
                break;
            case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "STATE: " + intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0));
                Log.i(TAG, "BluetoothDevice: " + dev.getName() + ", " + dev.getAddress());
                break;

            case BluetoothDevice.ACTION_ACL_CONNECTED:
                dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "BluetoothDevice: " + dev.getName() + ", " + dev.getAddress());
                break;
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "BluetoothDevice: " + dev.getName() + ", " + dev.getAddress());
                break;

            case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "STATE: " + intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, 0));
                Log.i(TAG, "BluetoothDevice: " + dev.getName() + ", " + dev.getAddress());
                break;
            case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED:
                dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "STATE: " + intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, 0));
                Log.i(TAG, "BluetoothDevice: " + dev.getName() + ", " + dev.getAddress());
                break;
            case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED:
                dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "STATE: " + intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, 0));
                Log.i(TAG, "BluetoothDevice: " + dev.getName() + ", " + dev.getAddress());
                break;
        }
    }

    public interface Listener {
        void foundDev(BluetoothDevice dev);
    }
}