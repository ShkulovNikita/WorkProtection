package ru.tpu.android.workprotection.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ru.tpu.android.workprotection.Auxiliary.MenuFiller;
import ru.tpu.android.workprotection.Auxiliary.NotFound;
import ru.tpu.android.workprotection.Auxiliary.Permissions;
import ru.tpu.android.workprotection.Auxiliary.Transition;
import ru.tpu.android.workprotection.Connection.BriefingsInfoTask;
import ru.tpu.android.workprotection.Connection.Observer;
import ru.tpu.android.workprotection.Connection.Task;
import ru.tpu.android.workprotection.Models.BriefingsInfo;
import ru.tpu.android.workprotection.Models.DataStore;
import ru.tpu.android.workprotection.R;

public class BriefingsListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, BriefingsListActivity.class);
    }

    //объект для хранения и передачи данных между активити
    public static DataStore dataStore;

    //пул потоков
    private static Executor threadExecutor = Executors.newCachedThreadPool();

    //задача для выполнения поиска с помощью API
    private BriefingsInfoTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //создание элементов интерфейса
        setContentView(R.layout.activity_briefings_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        MenuFiller.setTitle(this, getString(R.string.title_activity_briefings_list));

        //блокировка положения экрана для данной активити
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //получение информации о пользователе
        try {
            Bundle arguments = getIntent().getExtras();
            if (arguments!=null) {
                //обработка ошибки, возникшей в следующей активити
                Transition.checkError(this, arguments);

                dataStore = (DataStore) arguments.getSerializable(DataStore.class.getSimpleName());
            }
            MenuFiller.fillMenu(this, dataStore.getUserInfo());

            task = new BriefingsInfoTask(observer);
            search();
        } catch (Exception ex) {
            //в случае ошибки - возвращение назад
            Transition.returnOnError(this, MenuActivity.class, dataStore);
        }
        navigationView.setNavigationItemSelectedListener(this);
        Permissions.verifyStoragePermissions(this);
    }

    ProgressBar progressBar;

    private Observer<BriefingsInfo> observer = new Observer<BriefingsInfo>() {
        @Override
        public void onLoading(@NonNull Task<BriefingsInfo> task) {
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSuccess(@NonNull Task<BriefingsInfo> task, @Nullable BriefingsInfo data) {
            if ((dataStore != null)&&(data != null)) {
                if (!data.getId()[0].equals("Произошла ошибка")) {
                    if (!data.getId()[0].equals("Ничего не найдено")) {
                        //данные получены корректно - происходит сохранение
                        dataStore.setBriefingsInfo(data);
                        showList(dataStore.getBriefingsInfo());
                    } else {
                        //ничего не найдено
                        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.documentsLayout);
                        NotFound.addNotFoundText(BriefingsListActivity.this, linearLayout);
                    }
                } else {
                    Transition.returnOnError(BriefingsListActivity.this, MenuActivity.class, dataStore);
                }
            } else {
                Transition.returnOnError(BriefingsListActivity.this, MenuActivity.class, dataStore);
            }
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onError(@NonNull Task<BriefingsInfo> task, @NonNull Exception e) {
            progressBar.setVisibility(View.GONE);
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.documentsLayout);
            NotFound.addNotFoundText(BriefingsListActivity.this, linearLayout);
        }
    };

    //отображение списка инструктажей
    public void showList(BriefingsInfo briefingsInfo) {
        LayoutInflater inflater = getLayoutInflater();

        try {
            //добавление всех документов из списка
            for (int i = 0; i< briefingsInfo.getName().length; i++) {
                //список, в котороый добавляются элементы
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.documentsLayout);

                //добавляемый элемент
                View view = inflater.inflate(R.layout.document_list_item, linearLayout, false);

                //получение текста элемента
                TextView textOfItem = view.findViewById(R.id.textViewDocument);

                //его замена в соответствии с документом
                textOfItem.setText(briefingsInfo.getName()[i]);

                //навешивание обработчика нажатия, открывающего соответствующий документ
                final int numb = i;
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(BriefingsListActivity.this, ReadBriefingActivity.class);
                        intent.putExtra(DataStore.class.getSimpleName(), dataStore);
                        intent.putExtra("briefing_id", briefingsInfo.getId()[numb]);
                        intent.putExtra("document_name", briefingsInfo.getDocuments()[numb]);
                        startActivity(intent);
                    }
                });

                //обработка цвета

                //получение миллисекунд для даты окончания прохождения инструктажа
                long briefingEnd = briefingsInfo.getExpire_date()[i].getTime();

                //получение текущего времени
                Date currentTime = Calendar.getInstance().getTime();
                long currentMls = currentTime.getTime();

                //разница
                long difference = briefingEnd - currentMls;

                //получение цвета в зависимости от оставшегося времени
                if (briefingsInfo.getPassed()[i]) {
                    view.setBackgroundColor(Color.parseColor("#4CAF50"));
                } else  if (difference < 86400000) {
                    view.setBackgroundColor(Color.parseColor("#F44336"));
                } else if (difference < 604800000) {
                    view.setBackgroundColor(Color.parseColor("#FFEB3B"));
                } else {
                    view.setBackgroundColor(Color.parseColor("#00BCD4"));
                }

                //отметка о пройденности инструктажа
                if (briefingsInfo.getPassed()[i]) {
                    CheckBox checkBox = (CheckBox) view.findViewById(R.id.documentCheckBox);
                    checkBox.setChecked(true);
                }
                linearLayout.addView(view);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Transition.returnOnError(BriefingsListActivity.this, MenuActivity.class, dataStore);
        }
    }

    //обращение к API для получения информации об инструктажах
    private void search() {
        threadExecutor.execute(task);
    }

    //нажатие на кнопку "назад" смартфона
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //отписка обсервера
        task.unregisterObserver();
    }

    //вставляет элементы меню на верхней панели
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //обработка нажатий элементов меню
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_current) {
            //ничего не делать: выбран элемент текущего активити
        } else if (id == R.id.nav_return_to_home) {
            Transition.returnToHome(this, dataStore);
        } else if (id == R.id.nav_current_documents) {
            Transition.moveToActivity(BriefingsListActivity.this, BlitzListActivity.class, dataStore);
        } else if (id == R.id.nav_logout) {
            Transition.returnToAuthorization(this);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}