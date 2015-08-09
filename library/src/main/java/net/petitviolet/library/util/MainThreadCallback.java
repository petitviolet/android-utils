package net.petitviolet.library.util;

import android.os.Handler;
import android.os.Looper;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public abstract class MainThreadCallback implements Callback {
    private static final String TAG = MainThreadCallback.class.getSimpleName();

    abstract public void onFail(final Exception error);

    abstract public void onSuccess(final String responseBody);

    public static class CanceledException extends IOException {
        public CanceledException(String message) {
            super(message);
        }
    }

    @Override
    public void onFailure(final Request request, final IOException e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                e.printStackTrace();
                if (e.getMessage().contains("Canceled") || e.getMessage().contains("Socket closed")) {
                    onFail(new CanceledException("Request has canceled: " + request.toString()));
                } else {
                    onFail(e);
                }
            }
        });
    }

    @Override
    public void onResponse(final Response response) throws IOException {
        if (!response.isSuccessful() || response.body() == null) {
            onFailure(response.request(), new IOException("Failed"));
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    onSuccess(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                    onFailure(response.request(), new IOException("Failed"));
                }
            }
        });
    }

    private void runOnUiThread(Runnable task) {
        new Handler(Looper.getMainLooper()).post(task);
    }
}