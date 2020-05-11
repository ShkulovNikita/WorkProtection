package ru.tpu.android.workprotection.Auxiliary;

import android.content.Context;
import android.content.Intent;

import ru.tpu.android.workprotection.Activities.AuthorizationActivity;
import ru.tpu.android.workprotection.Activities.MenuActivity;
import ru.tpu.android.workprotection.Models.DataStore;

//вспомогательный класс для выполнения общих переходов между активити
public class Transition {
    public Transition () {}

    //возвращение к главному меню
    static public void returnToHome(Context context, DataStore dataStore) {
        Intent intent = new Intent(context, MenuActivity.class);
        intent.putExtra(DataStore.class.getSimpleName(), dataStore);
        context.startActivity(intent);
    }

    //возвращение к экрану авторизации
    static public void returnToAuthorization(Context context) {
        Intent intent = new Intent(context, AuthorizationActivity.class);
        context.startActivity(intent);
    }

    //переход к другому экрану в результате ошибки
    static public void returnOnError(Context context, Class nextActivity) {
        Intent intent = new Intent(context, nextActivity);
        intent.putExtra("Error", "Произошла ошибка");
        context.startActivity(intent);
    }
}
