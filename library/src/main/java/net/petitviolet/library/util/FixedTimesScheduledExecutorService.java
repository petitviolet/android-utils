package net.petitviolet.library.util;

import android.util.Log;
import android.util.SparseArray;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 指定した回数、指定した感覚でタスクを実行する
 * シングルスレッドで逐次実行
 * 終了時のコールバックも設定可能
 */
public class FixedTimesScheduledExecutorService {
    private static final String TAG = FixedTimesScheduledExecutorService.class.getSimpleName();
    private final ScheduledExecutorService executorService;
    private final SparseArray<Integer> mTaskRunCounts;
    private final SparseArray<Integer> mTaskExecutedCounts;
    private final SparseArray<ScheduledFuture> mFutures;

    public FixedTimesScheduledExecutorService() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        mFutures = new SparseArray<>();
        mTaskRunCounts = new SparseArray<>();
        mTaskExecutedCounts = new SparseArray<>();
    }

    public void shutDown() {
        executorService.shutdown();
    }

    public void shutDownNow() {
        executorService.shutdownNow();
    }

    private void stopTask(Callable command) {
        stopTask(command, true);
    }

    private void stopTask(Callable command, boolean interrupt) {
        Log.d(TAG, "stop: " + command.hashCode());
        ScheduledFuture future = mFutures.get(command.hashCode());
        if (future != null) {
            future.cancel(interrupt);
        }
    }

    private void stopTask(Runnable command) {
        stopTask(command, true);
    }

    private void stopTask(Runnable command, boolean interrupt) {
        Log.d(TAG, "stop: " + command.hashCode());
        ScheduledFuture future = mFutures.get(command.hashCode());
        if (future != null) {
            future.cancel(interrupt);
        }
    }

    public void clearTask() {
        mTaskExecutedCounts.clear();
        mFutures.clear();
    }

    public void release() {
        clearTask();
        shutDownNow();
    }

    private <T> Callable<T> countUpCallable(final Callable<T> command, final FixedTimesTaskListener listener) {
        return new Callable<T>() {
            @Override
            public T call() throws Exception {
                final int key = this.hashCode();
                if (mTaskExecutedCounts.get(key, 0) >= mTaskRunCounts.get(key)) {
                    stopTask(this);
                    if (listener != null) {
                        listener.onComplete();
                    }
                    return null;
                }
                mTaskExecutedCounts.put(key, mTaskExecutedCounts.get(key, 0) + 1);
                Log.d(TAG, "current count: " + mTaskExecutedCounts.get(key));
                return command.call();
            }
        };
    }

    private Runnable countUpRunnable(final Runnable command, final FixedTimesTaskListener listener) {
        return new Runnable() {
            @Override
            public void run() {
                final int key = this.hashCode();
                if (mTaskExecutedCounts.get(key, 0) >= mTaskRunCounts.get(key)) {
                    stopTask(this);
                    if (listener != null) {
                        listener.onComplete();
                    }
                    return;
                }
                mTaskExecutedCounts.put(key, mTaskExecutedCounts.get(key, 0) + 1);
                Log.d(TAG, "current count: " + mTaskExecutedCounts.get(key));
                command.run();
            }
        };
    }

    private void saveTaskInfo(ScheduledFuture future, Object countUpTask, int executeTime) {
        final int key = countUpTask.hashCode();
        mTaskRunCounts.put(key, executeTime);
        mFutures.put(key, future);
        Log.d(TAG, "key : " + key);
    }

    public ScheduledFuture<?> schedule(Runnable command, int executeTime, long delay, TimeUnit unit) {
        return schedule(command, executeTime, delay, unit, null);
    }

    public ScheduledFuture<?> schedule(Runnable command, int executeTime, long delay, TimeUnit unit, FixedTimesTaskListener listener) {
        Runnable countUpRunnable = countUpRunnable(command, listener);
        ScheduledFuture future = executorService.schedule(countUpRunnable, delay, unit);
        saveTaskInfo(future, countUpRunnable, executeTime);
        return future;
    }


    public <V> ScheduledFuture<V> schedule(Callable<V> callable, int executeTime, long delay, TimeUnit unit) {
        return schedule(callable, executeTime, delay, unit, null);
    }

    public <V> ScheduledFuture<V> schedule(Callable<V> callable, int executeTime, long delay, TimeUnit unit, FixedTimesTaskListener listener) {
        Callable<V> countUpCallable = countUpCallable(callable, listener);
        ScheduledFuture<V> future = executorService.schedule(countUpCallable, delay, unit);
        saveTaskInfo(future, countUpCallable, executeTime);
        return future;
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, int executeTime, long initialDelay, long period, TimeUnit unit) {
        return scheduleAtFixedRate(command, executeTime, initialDelay, period, unit, null);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, int executeTime, long initialDelay, long period, TimeUnit unit, FixedTimesTaskListener listener) {
        Runnable countUpRunnable = countUpRunnable(command, listener);
        ScheduledFuture future = executorService.scheduleAtFixedRate(countUpRunnable, initialDelay, period, unit);
        saveTaskInfo(future, countUpRunnable, executeTime);
        return future;
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, int executeTime, long initialDelay, long delay, TimeUnit unit) {
        return scheduleWithFixedDelay(command, executeTime, initialDelay, delay, unit, null);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, int executeTime, long initialDelay, long delay, TimeUnit unit, FixedTimesTaskListener listener) {
        Runnable countUpRunnable = countUpRunnable(command, listener);
        ScheduledFuture future = executorService.scheduleAtFixedRate(countUpRunnable, initialDelay, delay, unit);
        saveTaskInfo(future, countUpRunnable, executeTime);
        return future;
    }

    public interface FixedTimesTaskListener {
        void onComplete();
    }
}