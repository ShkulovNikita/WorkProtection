package ru.tpu.android.workprotection.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ru.tpu.android.workprotection.Connection.Observer;
import ru.tpu.android.workprotection.Connection.Task;
import ru.tpu.android.workprotection.Connection.UserInfoTask;
import ru.tpu.android.workprotection.Models.UserInfo;
import ru.tpu.android.workprotection.R;

public class AuthorizationActivity extends AppCompatActivity {

    static final String CONNECTION_URL = "http://192.168.1.28:45455/api/";
    
    static public String userID = "";

    //пул потоков
    private static Executor threadExecutor = Executors.newCachedThreadPool();

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, AuthorizationActivity.class);
    }

    //задача для выполнения поиска с помощью API
    private UserInfoTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        task = new UserInfoTask(observer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //отписка обсервера
        task.unregisterObserver();
    }

    ProgressBar progressBar;

    private Observer<UserInfo> observer = new Observer<UserInfo>() {
        @Override
        public void onLoading(@NonNull Task<UserInfo> task) {
            /*ConstraintLayout layout = findViewById(R.id.auth_layout);
            progressBar = new ProgressBar(AuthorizationActivity.this, null, android.R.attr.progressBarStyleLarge);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            layout.addView(progressBar, params);*/
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSuccess(@NonNull Task<UserInfo> task, @Nullable UserInfo data) {
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onError(@NonNull Task<UserInfo> task, @NonNull Exception e) {
            progressBar.setVisibility(View.GONE);
        }
    };

    //обращение к API для получения информации о пользователе
    private void search() {
        threadExecutor.execute(task);
    }

    //нажатие на кнопку авторизации
    public void onClickAuth(View view) {
        userID = getTextField().getText().toString();
        search();
    }

    //функция получения текста из поля ввода
    public EditText getTextField () {
        View view = findViewById(R.id.tabelNumText);
        return (EditText) view;
    }

    //проверяет, было ли выведено в поле "Неверный табельный номер"
    public boolean checkEmptyness () {
        EditText editText = getTextField();
        String textOfField = editText.getText().toString();
        if ((textOfField.equals("Неверный табельный номер"))||(textOfField.equals("Произошла ошибка"))) return true;
        else return false;
    }

    //очищает поле ввода
    public void clearTabelNumber () {
        EditText editText = getTextField();
        String textOfField = editText.getText().toString();
        textOfField = "";
        editText.setText(textOfField);
    }

    //добавление одной цифры в соответствии с нажатой кнопкой
    public void addNum (String num) {
        EditText editText = getTextField();
        String textOfField = editText.getText().toString();
        textOfField = textOfField + num;
        editText.setText(textOfField);
    }

    //удаление последнего символа
    public void deleteLastSymbol () {
        EditText editText = getTextField();
        String textOfField = editText.getText().toString();

        if(textOfField.length()>0)
        {
            if ((textOfField.equals("Произошла ошибка"))||(textOfField.equals("Неверный табельный номер")))
            {
                textOfField = "";
            }
            else
            {
                textOfField = textOfField.substring(0, textOfField.length() - 1);
            }
        }
        editText.setText(textOfField);
    }

    //нажатие на кнопку очистки поля
    public void onClickClearButton (View view) {
        if (checkEmptyness()) {
            clearTabelNumber();
        }
        deleteLastSymbol();
    }

    //нажатия на кнопки с цифрами
    public void onClickNum1 (View view) {
        if (checkEmptyness()) {
            clearTabelNumber();
        }
        addNum("1");
    }

    public void onClickNum2 (View view) {
        if (checkEmptyness()) {
            clearTabelNumber();
        }
        addNum("2");
    }

    public void onClickNum3 (View view) {
        if (checkEmptyness()) {
            clearTabelNumber();
        }
        addNum("3");
    }

    public void onClickNum4 (View view) {
        if (checkEmptyness()) {
            clearTabelNumber();
        }
        addNum("4");
    }

    public void onClickNum5 (View view) {
        if (checkEmptyness()) {
            clearTabelNumber();
        }
        addNum("5");
    }

    public void onClickNum6 (View view) {
        if (checkEmptyness()) {
            clearTabelNumber();
        }
        addNum("6");
    }

    public void onClickNum7 (View view) {
        if (checkEmptyness()) {
            clearTabelNumber();
        }
        addNum("7");
    }

    public void onClickNum8 (View view) {
        if (checkEmptyness()) {
            clearTabelNumber();
        }
        addNum("8");
    }

    public void onClickNum9 (View view) {
        if (checkEmptyness()) {
            clearTabelNumber();
        }
        addNum("9");
    }

    public void onClickNum0 (View view) {
        if (checkEmptyness()) {
            clearTabelNumber();
        }
        addNum("0");
    }
}
