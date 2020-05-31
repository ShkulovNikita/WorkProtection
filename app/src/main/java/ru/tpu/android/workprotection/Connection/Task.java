package ru.tpu.android.workprotection.Connection;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    //редактирование ответа от API
    public String editResponse(String response) {
        String result = response;

        //удаление лишних кавычек
        result = result.substring(1);
        result = result.substring(0, result.length() - 1);

        //удаление лишних слэшей
        result = result.replaceAll("\\\\", "");

        return result;
    }

    //конвертация строк в даты
    public Date[] stringsToDates(String[] rawDates) {
        Date[] dates = new Date[rawDates.length];

        for (int i = 0; i<rawDates.length;i++)
        {
            //удаление лишних чисел (времени в минутах, часах и секундах)
            rawDates[i] = rawDates[i].substring(0, rawDates[i].length() - 8);
            rawDates[i] = rawDates[i].replaceAll("\\.", "/");
            try {
                dates[i] = new SimpleDateFormat("dd/MM/yyyy").parse(rawDates[i]);
            } catch (ParseException ex) {
                Date dateObj = new Date();
                dates[i] = dateObj;
            }
        }
        return dates;
    }
}
