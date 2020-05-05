package ru.tpu.android.workprotection;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

public abstract class Task<T> implements Runnable {
    private Observer<T> observer;

    //объект Handler для добавления сообщений в очередь Looper
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public Task(@Nullable Observer<T> observer) {
        this.observer = observer;
    }

    @Override
    public final void run() {
        //установление низкого приоритета потоку, чтобы не нагружать цпу
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        //посредством метода POST сообщаются через обсервер статусы выполнения задачи
        //статус: начало выполнения задачи
        mainHandler.post(() -> {
            if (observer != null) {
                observer.onLoading(Task.this);
            }
        });
        try {
            final T data = executeInBackground();
            //статус: успешное выполнение задачи
            mainHandler.post(() -> {
                if (observer != null) {
                    observer.onSuccess(Task.this, data);
                }
            });
        } catch (final Exception ex) {
            //статус: получение ошибки при выполнении задачи
            mainHandler.post(() -> {
                if (observer != null) {
                    observer.onError(Task.this, ex);
                }
            });
        }
    }

    @Nullable
    @WorkerThread
    protected abstract T executeInBackground() throws Exception;

    //зануление обсервера
    public final void unregisterObserver() {
        observer = null;
    }
}
