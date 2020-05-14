package ru.tpu.android.workprotection.Auxiliary;

import android.app.Activity;

import java.util.Timer;
import java.util.TimerTask;

public class TimeCounter {
    static int count;
    static Timer T;

    public TimeCounter() { }

    //запустить таймер с нуля
    static public void startTimer (Activity activity) {
        count = 0;
        T = new Timer();
        T.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        count++;
                    }
                });
            }
        }, 0 , 1);
    }

    //продолжить с прошлой остановки
    static public void continueTimer (Activity activity) {
        T = new Timer();
        T.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        count++;
                    }
                });
            }
        }, 0 , 1);
    }

    //приостановить таймер
    static public void pauseTimer () {
        T.cancel();
    }

    //полная остановка таймера с возвращением прошедшего времени
    static public int stopTimer () {
        T.cancel();
        int result = count;
        count = 0;
        return result/1000;
    }
}
