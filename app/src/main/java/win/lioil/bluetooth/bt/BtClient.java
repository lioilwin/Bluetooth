package win.lioil.bluetooth.bt;

import android.bluetooth.BluetoothDevice;

import win.lioil.bluetooth.Util;

/**
 * 客户端，与服务端建立长连接
 */
public class BtClient extends Bt {
    public BtClient(Listener listener) {
        super(listener);
    }

    public void connect(BluetoothDevice dev) {
        close();
        try {
            mSocket = dev.createInsecureRfcommSocketToServiceRecord(SPP_UUID); //明文传输，无需配对
//            mSocket = dev.createRfcommSocketToServiceRecord(SPP_UUID); //加密传输，必须配对
            Util.EXECUTOR.execute(new Runnable() {//子线程循环读取
                @Override
                public void run() {
                    loopRead();
                }
            });
        } catch (Throwable e) {
            close();
        }
    }
}