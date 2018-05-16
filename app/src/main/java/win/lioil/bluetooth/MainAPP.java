package win.lioil.bluetooth;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

public class MainAPP extends Application {
    // app退出时，必须销毁static变量，以免内存泄漏
    public static Handler sHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void runUi(Runnable runnable) {
        sHandler.post(runnable);
    }
}
