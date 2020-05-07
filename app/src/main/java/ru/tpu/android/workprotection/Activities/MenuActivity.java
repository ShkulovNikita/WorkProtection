package ru.tpu.android.workprotection.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import ru.tpu.android.workprotection.Connection.UserInfoTask;
import ru.tpu.android.workprotection.Models.DataStore;
import ru.tpu.android.workprotection.R;

public class MenuActivity extends AppCompatActivity {

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, MenuActivity.class);
    }

    //объект для хранения и передачи информации о пользователе
    static DataStore dataStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        //блокировка положения экрана для данной активити
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //получение информации о пользователе
        try {
            Bundle arguments = getIntent().getExtras();
            if (arguments!=null) {
                dataStore = (DataStore) arguments.getSerializable(DataStore.class.getSimpleName());
            }
        } catch (Exception ex) {
            //в случае ошибки - возвращение назад к экрану авторизации
            Intent intent = new Intent(MenuActivity.this, AuthorizationActivity.class);
            intent.putExtra("Error", "Произошла ошибка");
            startActivity(intent);
        }
    }

    //просмотр тестов
    public void onClickTests(View view) {

    }

    //просмотр инструктажей
    public void onClickBriefings(View view) {

    }

    //просмотр памяток
    public void onClickBlitz(View view) {

    }
}
