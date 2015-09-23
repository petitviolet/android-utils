package net.petitviolet.util.sample;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import net.petitviolet.library.util.FixedTimesScheduledExecutorService;
import net.petitviolet.library.util.Logger;
import net.petitviolet.library.util.ToastUtil;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class FixedTimesScheduledESSample extends AppCompatActivity {
    @Bind(R.id.hello_world)
    TextView mHelloWorld;
    @Bind(R.id.hello_world2)
    TextView mHelloWorld2;
    private Handler mMainHandler;
    private FixedTimesScheduledExecutorService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mMainHandler = new Handler(Looper.getMainLooper());
        mService = new FixedTimesScheduledExecutorService();
    }

    @OnClick(R.id.runner_executor)
    void animateExecutor() {
        final Runnable biggerTask = () -> {
            float scaleX = mHelloWorld2.getScaleX() + 0.05f;
            float scaleY = mHelloWorld2.getScaleY() + 0.05f;
            updateScale(mHelloWorld2, scaleX, scaleY);
            String text = "x" + mHelloWorld2.getText().toString() + "x";
            updateText(mHelloWorld2, text);
        };
        final Runnable smallerTask = () -> {
            float scaleX = mHelloWorld2.getScaleX() - 0.05f;
            float scaleY = mHelloWorld2.getScaleY() - 0.05f;
            updateScale(mHelloWorld2, scaleX, scaleY);
            String text = mHelloWorld2.getText().toString();
            updateText(mHelloWorld2, text.substring(1, text.length() - 1));
        };
//        updateAlpha(mHelloWorld2, 0f);
        updateScale(mHelloWorld2, 1f, 1f);
        mService.scheduleAtFixedRate(biggerTask, 20, 0, 50, TimeUnit.MILLISECONDS, () -> {
            showToast("reverse");
        });
        mService.scheduleAtFixedRate(smallerTask, 20, 2000, 50, TimeUnit.MILLISECONDS, () -> {
            showToast("completed");
//            updateAlpha(mHelloWorld2, 0f);
            updateScale(mHelloWorld2, 1f, 1f);
        });
    }

    @OnClick(R.id.runner_animator)
    void animateAnimator() {
        PropertyValuesHolder biggerX = PropertyValuesHolder.ofFloat("scaleX", 1f, 2f);
        PropertyValuesHolder biggerY = PropertyValuesHolder.ofFloat("scaleY", 1f, 2f);
        biggerY = PropertyValuesHolder.ofObject("setText", (fraction, startValue, endValue) -> {
            Logger.d("fraction: " + fraction);
            Logger.d("start: " + startValue);
            Logger.d("end: " + endValue);
            return "x" + endValue + "x";
        }, "hoge");
        ObjectAnimator biggerAnim = ObjectAnimator.ofPropertyValuesHolder(mHelloWorld, biggerX, biggerY);
        biggerAnim.setDuration(1000);

        PropertyValuesHolder smallerX = PropertyValuesHolder.ofFloat("scaleX", 2f, 1f);
        PropertyValuesHolder smallerY = PropertyValuesHolder.ofFloat("scaleY", 2f, 1f);
        ObjectAnimator smallerAnim = ObjectAnimator.ofPropertyValuesHolder(mHelloWorld, smallerX, smallerY);
        smallerAnim.setDuration(1000);
        smallerAnim.setStartDelay(1000);
        biggerAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                showToast("bigger end");
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        smallerAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                showToast("smaller end");
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        AnimatorSet animSet = new AnimatorSet();
        animSet.playSequentially(biggerAnim, smallerAnim);
        animSet.start();
    }

    private void showToast(final String msg) {
        mMainHandler.post(() -> {
            ToastUtil.show(msg);
        });
    }

    private void updateScale(final View view, final float scaleX, final float scaleY) {
        mMainHandler.post(() -> {
            view.setScaleX(scaleX);
            view.setScaleY(scaleY);
        });
    }

    private void updateText(final TextView view, final String newText) {
        mMainHandler.post(() -> {
            view.setText(newText);
        });
    }

    private void updateAlpha(final View view, final float alpha) {
        mMainHandler.post(() -> {
            view.setAlpha(alpha);
        });
    }
}
