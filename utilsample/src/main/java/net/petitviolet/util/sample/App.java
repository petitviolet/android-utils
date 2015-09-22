package net.petitviolet.util.sample;

import android.app.Application;
import android.os.StrictMode;

import net.petitviolet.library.util.ToastUtil;

public class App extends Application {
    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDeath()
                .build();
        StrictMode.setThreadPolicy(policy);
        ToastUtil.setApplication(this);
    }
}