package win.lioil.bluetooth.bt;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;

import win.lioil.bluetooth.R;
import win.lioil.bluetooth.util.Util;

public class BtServerActivity extends Activity implements BtBase.Listener {
    private TextView mTips;
    private EditText mInputMsg;
    private EditText mInputFile;
    private TextView mLogs;
    private BtServer mServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btserver);
        mTips = findViewById(R.id.tv_tips);
        mInputMsg = findViewById(R.id.input_msg);
        mInputFile = findViewById(R.id.input_file);
        mLogs = findViewById(R.id.tv_log);
        mServer = new BtServer(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mServer.close();
    }

    public void sendMsg(View view) {
        if (mServer.isConnected(null)) {
            String msg = mInputMsg.getText().toString();
            if (TextUtils.isEmpty(msg))
                Util.toast(this, "消息不能空");
            else
                mServer.sendMsg(msg);
        } else
            Util.toast(this, "没有连接");
    }

    public void sendFile(View view) {
        if (mServer.isConnected(null)) {
            String filePath = mInputFile.getText().toString();
            if (TextUtils.isEmpty(filePath) || !new File(filePath).exists())
                Util.toast(this, "文件无效");
            else
                mServer.sendFile(filePath);
        } else
            Util.toast(this, "没有连接");
    }

    @Override
    public void socketNotify(int state, final Object obj) {
        if (isDestroyed())
            return;
        String msg = null;
        switch (state) {
            case BtBase.Listener.CONNECTED:
                BluetoothDevice dev = (BluetoothDevice) obj;
                msg = String.format("与%s(%s)连接成功", dev.getName(), dev.getAddress());
                mTips.setText(msg);
                break;
            case BtBase.Listener.DISCONNECTED:
                mServer.listen();
                msg = "连接断开,正在重新监听...";
                mTips.setText(msg);
                break;
            case BtBase.Listener.MSG:
                msg = String.format("\n%s", obj);
                mLogs.append(msg);
                break;
        }
        Util.toast(this, msg);
    }
}