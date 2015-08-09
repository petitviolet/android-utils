package net.petitviolet.library.util;

import android.util.Log;

import java.util.regex.Pattern;

public class Logger {

    public static void v(String msg) {
        Log.v(getTag(), msg);
    }

    public static void i(String msg) {
        Log.i(getTag(), msg);
    }

    public static void d(String msg) {
        Log.d(getTag(), msg);
    }

    public static void w(String msg) {
        Log.w(getTag(), msg);
    }

    public static void e(String msg) {
        Log.e(getTag(), msg);
    }

    private static String getTag() {
        final StackTraceElement trace = Thread.currentThread().getStackTrace()[4];
        final String cla = trace.getClassName();
        Pattern pattern = Pattern.compile("[\\.]+");
        final String[] splitedStr = pattern.split(cla);
        final String simpleClass = splitedStr[splitedStr.length - 1];
        final String mthd = trace.getMethodName();
        final int line = trace.getLineNumber();
        return simpleClass + "#" + mthd + ":" + line;
    }
}
