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


public class MainActivity extends AppCompatActivity {
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
        if (mHelloWorld2.getAlpha() != 0) {
            return;
        }
        final Runnable biggerTask = () -> {
            float alpha = mHelloWorld2.getAlpha() + 0.1f;
            Logger.i("bigger size -> " + alpha);
            updateAlpha(mHelloWorld2, alpha);
        };
        final Runnable smallerTask = () -> {
            float alpha = mHelloWorld2.getAlpha() - 0.1f;
            Logger.i("smaller size -> " + alpha);
            updateAlpha(mHelloWorld2, alpha);
        };
        updateAlpha(mHelloWorld2, 0f);
        mService.scheduleAtFixedRate(biggerTask, 20, 0, 50, TimeUnit.MILLISECONDS, () -> {
            showToast("reverse");
        });
        mService.scheduleAtFixedRate(smallerTask, 20, 2000, 50, TimeUnit.MILLISECONDS, () -> {
            showToast("completed");
            updateAlpha(mHelloWorld2, 0f);
        });
    }

    @OnClick(R.id.runner_animator)
    void animateAnimator() {
        if (mHelloWorld.getAlpha() != 0) {
            return;
        }
        PropertyValuesHolder biggerX = PropertyValuesHolder.ofFloat("scaleX", 0f, 1f);
        PropertyValuesHolder biggerY = PropertyValuesHolder.ofFloat("scaleY", 0f, 1f);
        ObjectAnimator biggerAnim = ObjectAnimator.ofPropertyValuesHolder(mHelloWorld, biggerX, biggerY);
        biggerAnim.setDuration(1000);

        PropertyValuesHolder smallerX = PropertyValuesHolder.ofFloat("scaleX", 10f, 1f);
        PropertyValuesHolder smallerY = PropertyValuesHolder.ofFloat("scaleY", 10f, 1f);
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

    private void updateAlpha(final View view, final float alpha) {
        mMainHandler.post(() -> {
            view.setAlpha(alpha);
        });
    }
}
