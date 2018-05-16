package win.lioil.bluetooth.ui;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import win.lioil.bluetooth.R;
import win.lioil.bluetooth.Util;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE
                    , Manifest.permission.READ_EXTERNAL_STORAGE
                    , Manifest.permission.ACCESS_COARSE_LOCATION
                    , Manifest.permission.ACCESS_FINE_LOCATION};
            for (String str : permissions) {
                if (checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(permissions, 111);
                    return;
                }
            }
        }

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Util.toast(this, "本机没有找到蓝牙硬件或驱动");
            finish();
        } else {
            if (!adapter.isEnabled())
                adapter.enable();
        }
    }

    public void sendMsg(View view) {
        startActivity(new Intent(this, BtClientActivity.class));
    }

    public void receive(View view) {
        startActivity(new Intent(this, BtServerActivity.class));
    }
}
