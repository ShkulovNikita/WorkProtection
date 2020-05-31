package ru.tpu.android.workprotection.Auxiliary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import ru.tpu.android.workprotection.Activities.AuthorizationActivity;
import ru.tpu.android.workprotection.Activities.MenuActivity;

//вспомогательный класс для выполнения общих переходов между активити
public class Transition {
    public Transition () {}

    //переход к другому активити
    static public void moveToActivity(Context context, Class nextActivity, DataStore dataStore) {
        Intent intent = new Intent(context, nextActivity);
        intent.putExtra(DataStore.class.getSimpleName(), dataStore);
        context.startActivity(intent);
    }

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
    static public void returnOnError(Context context, Class nextActivity, DataStore dataStore) {
        Intent intent = new Intent(context, nextActivity);
        intent.putExtra(DataStore.class.getSimpleName(), dataStore);
        intent.putExtra("Error", "Произошла ошибка");
        context.startActivity(intent);
    }

    //показ сообщения об ошибке
    static public void showErrorToast(Context context) {
        Toast toast = Toast.makeText(context, "Произошла ошибка", Toast.LENGTH_SHORT);
        toast.show();
    }

    //проверка возникновения ошибки
    static public void checkError(Context context, Bundle arguments) {
        if (arguments!=null) {
            String error = arguments.getString("Error");
            if (error != null) {
                if (error.equals("Произошла ошибка")) {
                    Transition.showErrorToast(context);
                }
            }
        }
    }
}
