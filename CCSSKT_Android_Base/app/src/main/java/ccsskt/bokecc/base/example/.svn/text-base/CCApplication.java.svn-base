package ccsskt.bokecc.base.example;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.tencent.bugly.crashreport.CrashReport;

import java.lang.ref.WeakReference;

/**
 * 作者 ${郭鹏飞}.<br/>
 */
public class CCApplication extends Application {

    private static final String TAG = "CCApp";

    public static int mAppStatus = -1; // 表示 force_kill
    private static WeakReference<Context> context;

    @Override
    public void onCreate() {
        super.onCreate();
        if (context == null) {
            context = new WeakReference<Context>(this);
        }

        CrashReport.initCrashReport(this, "b7622b541a", true);
    }

    public static Context getContext() {
        return context == null ? null : context.get();
    }

    public static String getVersion() {
        try {
            PackageManager manager = context.get().getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.get().getPackageName(), 0);
            return "v" + info.versionName;
        } catch (Exception e) {
            return "v" + Config.APP_VERSION;
        }
    }

}
