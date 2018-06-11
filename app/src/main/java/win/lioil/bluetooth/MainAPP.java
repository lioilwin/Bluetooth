package win.lioil.bluetooth;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;

public class MainAPP extends Application {
    public static WeakReference<Handler> sHandlerWRF; // Handler弱引用, 避免内存泄漏

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void runUi(Runnable runnable) {
        if (sHandlerWRF == null || sHandlerWRF.get() == null)
            sHandlerWRF = new WeakReference<>(new Handler(Looper.getMainLooper()));
        sHandlerWRF.get().post(runnable);
    }
}
