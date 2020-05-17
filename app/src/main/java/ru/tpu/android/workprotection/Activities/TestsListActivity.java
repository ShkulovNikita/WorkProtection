package ru.tpu.android.workprotection.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
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
import ru.tpu.android.workprotection.Connection.Observer;
import ru.tpu.android.workprotection.Connection.Task;
import ru.tpu.android.workprotection.Connection.TestInfoTask;
import ru.tpu.android.workprotection.Connection.TestListInfoTask;
import ru.tpu.android.workprotection.Models.DataStore;
import ru.tpu.android.workprotection.Models.TestInfo;
import ru.tpu.android.workprotection.Models.TestListInfo;
import ru.tpu.android.workprotection.Models.TestStats;
import ru.tpu.android.workprotection.R;

public class TestsListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, TestsListActivity.class);
    }

    //объект для хранения и передачи данных между активити
    public static DataStore dataStore;

    //пул потоков
    private static Executor threadExecutor = Executors.newCachedThreadPool();

    //задача для выполнения поиска с помощью API
    private TestListInfoTask task;
    private TestInfoTask infoTask;

    //ID выбранного теста
    public static String test_id;

    //выбранный тест
    public static TestInfo testInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //создание элементов интерфейса
        setContentView(R.layout.activity_tests_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        MenuFiller.setTitle(this, getString(R.string.title_activity_tests_list));

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

            task = new TestListInfoTask(observer);
            infoTask = new TestInfoTask(testObserver);
            search();
        } catch (Exception ex) {
            //в случае ошибки - возвращение назад
            Transition.returnOnError(this, MenuActivity.class, dataStore);
        }
        navigationView.setNavigationItemSelectedListener(this);
        Permissions.verifyStoragePermissions(this);
    }

    ProgressBar progressBar;

    //обсервер получения списка тестов
    private Observer<TestListInfo> observer = new Observer<TestListInfo>() {
        @Override
        public void onLoading(@NonNull Task<TestListInfo> task) {
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSuccess(@NonNull Task<TestListInfo> task, @Nullable TestListInfo data) {
            if ((dataStore != null)&&(data != null)) {
                if (!data.getId()[0].equals("Произошла ошибка")) {
                    if (!data.getId()[0].equals("Ничего не найдено")) {
                        //данные получены корректно - происходит сохранение
                        dataStore.setTestListInfo(data);
                        showList(dataStore.getTestListInfo());
                    } else {
                        //ничего не найдено
                        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.documentsLayout);
                        NotFound.addNotFoundText(TestsListActivity.this, linearLayout);
                    }
                } else {
                    Transition.returnOnError(TestsListActivity.this, MenuActivity.class, dataStore);
                }
            } else {
                Transition.returnOnError(TestsListActivity.this, MenuActivity.class, dataStore);
            }
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onError(@NonNull Task<TestListInfo> task, @NonNull Exception e) {
            progressBar.setVisibility(View.GONE);
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.documentsLayout);
            NotFound.addNotFoundText(TestsListActivity.this, linearLayout);
        }
    };

    //обсервер получения информации о выбранном тесте
    private Observer<TestInfo> testObserver = new Observer<TestInfo>() {
        @Override
        public void onLoading(@NonNull Task<TestInfo> task) {
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSuccess(@NonNull Task<TestInfo> task, @Nullable TestInfo data) {
            if ((dataStore != null)&&(data != null)) {
                if (!data.getId().equals("Произошла ошибка")) {
                    if (!data.getId().equals("Ничего не найдено")) {
                        //данные получены корректно - сохранение
                        testInfo = data;

                        //переход к следующей активити
                        Intent intent = new Intent(TestsListActivity.this, TestActivity.class);
                        intent.putExtra(DataStore.class.getSimpleName(), dataStore);
                        intent.putExtra("test_id", test_id);
                        intent.putExtra("test_info", testInfo);

                        TestStats testStats = new TestStats();
                        intent.putExtra(TestStats.class.getSimpleName(), testStats);

                        //установление первоначального значения счетчика времени в 0
                        intent.putExtra("time", 0);
                        intent.putExtra("num", 0);
                        startActivity(intent);
                    } else {
                        Transition.showErrorToast(TestsListActivity.this);
                    }
                } else {
                    Transition.showErrorToast(TestsListActivity.this);
                }
            } else {
                Transition.showErrorToast(TestsListActivity.this);
            }
        }

        @Override
        public void onError(@NonNull Task<TestInfo> task, @NonNull Exception e) {
            progressBar.setVisibility(View.GONE);
        }
    };

    //отобразить список тестов
    public void showList (TestListInfo testListInfo) {
        Permissions.verifyStoragePermissions(this);

        final TestListInfo testsInformation;
        try {
            testsInformation = testListInfo;

            LayoutInflater ltInflater = getLayoutInflater();

            for (int i = 0; i<testsInformation.getName().length; i++)
            {
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.documentsLayout);

                View view = ltInflater.inflate(R.layout.document_list_item, linearLayout, false);

                TextView textOfItem = view.findViewById(R.id.textViewDocument);

                textOfItem.setText(testsInformation.getName()[i]);

                //навешивание обработчика нажатия, открывающего соответствующий тест
                final int numb = i;
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            test_id = testsInformation.getId()[numb];
                            findTest();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Transition.showErrorToast(TestsListActivity.this);
                        }
                    }
                });

                //обработка цвета

                //получение миллисекунд для даты окончания прохождения инструктажа
                long briefingEnd = testsInformation.getExpire_date()[i].getTime();

                //получение текущего времени
                Date currentTime = Calendar.getInstance().getTime();
                long currentMls = currentTime.getTime();

                //разница
                long difference = briefingEnd - currentMls;

                //получение цвета в зависимости от оставшегося времени
                if (testsInformation.getPassed()[i]) {
                    view.setBackgroundColor(Color.parseColor("#4CAF50"));
                } else  if (difference < 86400000) {
                    view.setBackgroundColor(Color.parseColor("#F44336"));
                } else if (difference < 604800000) {
                    view.setBackgroundColor(Color.parseColor("#FFEB3B"));
                } else {
                    view.setBackgroundColor(Color.parseColor("#00BCD4"));
                }

                //отметка о пройденности теста
                if (testsInformation.getPassed()[i]) {
                    CheckBox checkBox = (CheckBox) view.findViewById(R.id.documentCheckBox);
                    checkBox.setChecked(true);
                }

                linearLayout.addView(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Transition.returnOnError(TestsListActivity.this, MenuActivity.class, dataStore);
        }
    }

    //обращение к API для получения информации о тестах
    private void search() {
        threadExecutor.execute(task);
    }

    //обращение к API для получения информации о выбранном тесте
    private void findTest() { threadExecutor.execute(infoTask); }

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
            Transition.moveToActivity(TestsListActivity.this, BlitzListActivity.class, dataStore);
        } else if (id == R.id.nav_logout) {
            Transition.returnToAuthorization(this);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}