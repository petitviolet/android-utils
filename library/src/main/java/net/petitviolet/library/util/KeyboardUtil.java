package net.petitviolet.library.util;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class KeyboardUtil {
    private static final String TAG = KeyboardUtil.class.getSimpleName();

    public static void hide(Activity activity, View view) {
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        } else {
            activity.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    public static void hide(Activity activity) {
        hide(activity, null);
    }

    public static void show(final Context context, final EditText editText) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (context != null) {
                    InputMethodManager imm = (InputMethodManager) context
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
    }
}
