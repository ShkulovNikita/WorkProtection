package ru.tpu.android.workprotection.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ru.tpu.android.workprotection.Auxiliary.MenuFiller;
import ru.tpu.android.workprotection.Auxiliary.Permissions;
import ru.tpu.android.workprotection.Auxiliary.Transition;
import ru.tpu.android.workprotection.Connection.BlitzDownloadTask;
import ru.tpu.android.workprotection.Connection.Observer;
import ru.tpu.android.workprotection.Connection.Task;
import ru.tpu.android.workprotection.Models.DataStore;
import ru.tpu.android.workprotection.R;

public class ReadBlitzActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, ReadBriefingActivity.class);
    }

    //объект для хранения и передачи данных между активити
    public static DataStore dataStore;

    //имя файла памятки
    public static String document_name;

    //название памятки
    public String blitz_name;

    //пул потоков
    private static Executor threadExecutor = Executors.newCachedThreadPool();

    //задача для выполнения поиска с помощью API
    private BlitzDownloadTask task;

    //JSON, отправляемый API при завершении инструктажа
    public static String REQUEST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //создание элементов интерфейса
        setContentView(R.layout.activity_read_blitz);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        MenuFiller.setTitle(this, getString(R.string.title_activity_read_blitz));

        //блокировка положения экрана для данной активити
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //получение информации о пользователе
        try {
            Bundle arguments = getIntent().getExtras();
            if (arguments!=null) {
                //обработка ошибки, возникшей в следующей активити
                Transition.checkError(this, arguments);

                dataStore = (DataStore) arguments.getSerializable(DataStore.class.getSimpleName());
                document_name = arguments.getString("document_name");
                blitz_name = arguments.getString("blitz_name");
                this.setTitle(blitz_name);
            }
            MenuFiller.fillMenu(this, dataStore.getUserInfo());

            task = new BlitzDownloadTask(observer);
            search();
        } catch (Exception ex) {
            //в случае ошибки - возвращение назад
            Transition.returnOnError(this, BlitzListActivity.class, dataStore);
        }
        navigationView.setNavigationItemSelectedListener(this);
        Permissions.verifyStoragePermissions(this);
    }

    ProgressBar progressBar;

    private Observer<String> observer = new Observer<String>() {
        @Override
        public void onLoading(@NonNull Task<String> task) {
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSuccess(@NonNull Task<String> task, @Nullable String data) {
            if ((dataStore != null)&&(data != null)) {
                if (!data.equals("Произошла ошибка")) {
                    try {
                        //отображение pdf-файла
                        File file = new File(data);
                        PDFView pdfView = (PDFView) findViewById(R.id.pdfView);
                        pdfView.fromFile(file).load();
                        //установление скроллбара всегда видимым
                        pdfView.setScrollBarFadeDuration(0);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Transition.returnOnError(ReadBlitzActivity.this, BlitzListActivity.class, dataStore);
                    }
                } else {
                    Transition.returnOnError(ReadBlitzActivity.this, BlitzListActivity.class, dataStore);
                }
            } else {
                Transition.returnOnError(ReadBlitzActivity.this, BlitzListActivity.class, dataStore);
            }
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onError(@NonNull Task<String> task, @NonNull Exception e) {
            progressBar.setVisibility(View.GONE);
            Transition.returnOnError(ReadBlitzActivity.this, BlitzListActivity.class, dataStore);
        }
    };

    //обращение к API для получения документа
    private void search() {
        threadExecutor.execute(task);
    }

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
            Transition.moveToActivity(ReadBlitzActivity.this, BlitzListActivity.class, dataStore);
        } else if (id == R.id.nav_logout) {
            Transition.returnToAuthorization(this);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onClick (View view) {
        Transition.moveToActivity(ReadBlitzActivity.this, BlitzListActivity.class, dataStore);
    }
}
