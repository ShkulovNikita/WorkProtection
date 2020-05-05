package ru.tpu.android.workprotection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface Observer<T> {
    //начало выполнения задачи
    void onLoading(@NonNull Task<T> task);

    //успешное выполнение задачи
    void onSuccess(@NonNull Task<T> task, @Nullable T data);

    //выполнение с ошибкой
    void onError(@NonNull Task<T> task, @NonNull Exception e);
}
