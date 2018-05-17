package win.lioil.bluetooth.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;

import win.lioil.bluetooth.R;
import win.lioil.bluetooth.util.Util;
import win.lioil.bluetooth.bt.BtBase;
import win.lioil.bluetooth.bt.BtClient;
import win.lioil.bluetooth.util.BluetoothReceiver;

public class BtClientActivity extends Activity implements BtBase.Listener, BluetoothReceiver.Listener, DevAdapter.Listener {
    private TextView mTips;
    private EditText mInputMsg;
    private EditText mInputFile;
    private TextView mLogs;
    private BluetoothReceiver mBluetoothReceiver;
    private final DevAdapter mDevAdapter = new DevAdapter(this);
    private final BtClient mClient = new BtClient(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btclient);
        RecyclerView rv = findViewById(R.id.rv_bt);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(mDevAdapter);
        mTips = findViewById(R.id.tv_tips);
        mInputMsg = findViewById(R.id.input_msg);
        mInputFile = findViewById(R.id.input_file);
        mLogs = findViewById(R.id.tv_log);
        mBluetoothReceiver = new BluetoothReceiver(this, this);//注册蓝牙广播
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBluetoothReceiver);
        mClient.close();
    }

    @Override
    public void onItemClick(BluetoothDevice dev) {
        if (mClient.isConnected(dev))
            return;
        mClient.connect(dev);
        mTips.setText("正在连接...");
    }

    @Override
    public void foundDev(BluetoothDevice dev) {
        mDevAdapter.add(dev);
    }

    public void refresh(View view) {
        mDevAdapter.refresh();//刷新发现设备
    }

    public void sendMsg(View view) {
        if (mClient.isConnected(null)) {
            String msg = mInputMsg.getText().toString();
            if (TextUtils.isEmpty(msg))
                Util.toast(this, "消息不能空");
            else
                mClient.sendMsg(msg);
        } else
            Util.toast(this, "没有连接");
    }

    public void sendFile(View view) {
        if (mClient.isConnected(null)) {
            String filePath = mInputFile.getText().toString();
            if (TextUtils.isEmpty(filePath) || !new File(filePath).exists())
                Util.toast(this, "文件无效");
            else
                mClient.sendFile(filePath);
        } else
            Util.toast(this, "没有连接");
    }

    @Override
    public void socketNotify(int state, final Object obj) {
        String msg = null;
        switch (state) {
            case BtBase.Listener.CONNECTED:
                BluetoothDevice dev = (BluetoothDevice) obj;
                msg = String.format("与%s(%s)连接成功", dev.getName(), dev.getAddress());
                mTips.setText(msg);
                break;
            case BtBase.Listener.DISCONNECTED:
                msg = "连接断开";
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
