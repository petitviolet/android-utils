package net.petitviolet.library.util;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

public class ToastUtil {
    private static final String TAG = ToastUtil.class.getSimpleName();
    private static Context sContext;

    public static void setApplication(Application application) {
        sContext = application.getApplicationContext();
    }

    public static void show(int resId) {
        show(resId, Toast.LENGTH_SHORT);
    }

    public static void show(int resId, int duration) {
        show(sContext.getString(resId), duration);
    }

    public static void show(String message) {
        show(message, Toast.LENGTH_SHORT);
    }

    public static void show(String message, int duration) {
        if (sContext == null) {
            throw new NullPointerException("You should call setApplication at initializing application");
        }
        if (!TextUtils.isEmpty(message)) {
            Toast.makeText(sContext, message, duration).show();
        }
    }
}