package win.lioil.bluetooth.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;

import win.lioil.bluetooth.Util;

/**
 * 服务端监听和连接线程，只连接一个设备
 */
public class BtServer extends Bt {
    private BluetoothServerSocket mSSocket;

    public BtServer(Listener listener) {
        super(listener);
    }

    public void listen() {
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            mSSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(SPP_TAG, SPP_UUID); //明文传输，无需配对
//            sSocket = adapter.listenUsingRfcommWithServiceRecord(SPP_TAG, SPP_UUID); //加密传输，必须配对
            Util.EXECUTOR.execute(new Runnable() {//子线程
                @Override
                public void run() {
                    try {
                        mSocket = mSSocket.accept(); // 监听连接
                        mSSocket.close(); // 关闭监听，只连接一个设备
                        loopRead();
                    } catch (Throwable e) {
                        close();
                    }
                }
            });
        } catch (Throwable e) {
            close();
        }
    }

    @Override
    public void close() {
        super.close();
        try {
            mSSocket.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}