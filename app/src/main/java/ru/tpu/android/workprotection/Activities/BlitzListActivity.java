package ru.tpu.android.workprotection.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ru.tpu.android.workprotection.Auxiliary.MenuFiller;
import ru.tpu.android.workprotection.Auxiliary.NotFound;
import ru.tpu.android.workprotection.Auxiliary.Permissions;
import ru.tpu.android.workprotection.Auxiliary.Transition;
import ru.tpu.android.workprotection.Connection.BlitzInfoTask;
import ru.tpu.android.workprotection.Connection.Observer;
import ru.tpu.android.workprotection.Connection.Task;
import ru.tpu.android.workprotection.Models.BlitzInfo;
import ru.tpu.android.workprotection.Auxiliary.DataStore;
import ru.tpu.android.workprotection.R;

public class BlitzListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, BlitzListActivity.class);
    }

    //объект для хранения и передачи данных между активити
    public static DataStore dataStore;

    //пул потоков
    private static Executor threadExecutor = Executors.newCachedThreadPool();

    //задача для выполнения поиска с помощью API
    private BlitzInfoTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //создание элементов интерфейса
        setContentView(R.layout.activity_blitz_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        MenuFiller.setTitle(this, getString(R.string.title_activity_current_documents));

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

            task = new BlitzInfoTask(observer);
            search();
        } catch (Exception ex) {
            //в случае ошибки - возвращение назад
            Transition.returnOnError(this, MenuActivity.class, dataStore);
        }
        navigationView.setNavigationItemSelectedListener(this);
        Permissions.verifyStoragePermissions(this);
    }

    ProgressBar progressBar;

    private Observer<BlitzInfo> observer = new Observer<BlitzInfo>() {
        @Override
        public void onLoading(@NonNull Task<BlitzInfo> task) {
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSuccess(@NonNull Task<BlitzInfo> task, @Nullable BlitzInfo data) {
            if ((dataStore != null)&&(data != null)) {
                if (!data.getIds()[0].equals("Произошла ошибка")) {
                    if (!data.getIds()[0].equals("Ничего не найдено")) {
                        //данные получены корректно - происходит сохранение
                        dataStore.setBlitzInfo(data);
                        showList(dataStore.getBlitzInfo());
                    } else {
                        //ничего не найдено
                        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.documentsLayout);
                        NotFound.addNotFoundText(BlitzListActivity.this, linearLayout);
                    }
                } else {
                    Transition.returnOnError(BlitzListActivity.this, MenuActivity.class, dataStore);
                }
            } else {
                Transition.returnOnError(BlitzListActivity.this, MenuActivity.class, dataStore);
            }
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onError(@NonNull Task<BlitzInfo> task, @NonNull Exception e) {
            progressBar.setVisibility(View.GONE);
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.documentsLayout);
            NotFound.addNotFoundText(BlitzListActivity.this, linearLayout);
        }
    };

    //отобразить список памяток
    public void showList(BlitzInfo blitzInfo) {
        Permissions.verifyStoragePermissions(this);

        final BlitzInfo instructionBlitzInfo;
        try
        {
            instructionBlitzInfo = blitzInfo;

            LayoutInflater ltInflater = getLayoutInflater();

            //добавление всех заметок из списка
            for (int i = 0; i<instructionBlitzInfo.getIds().length; i++)
            {
                //список, в который добавляются элементы
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.documentsLayout);

                //добавляемый элемент
                View view = ltInflater.inflate(R.layout.blitz_list_item, linearLayout, false);

                TextView textOfItem = view.findViewById(R.id.textViewBlitz);

                DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                String formattedDate = df.format(instructionBlitzInfo.getDates()[i]);

                String newText = instructionBlitzInfo.getNames()[i] + "\n(" + formattedDate + ")";
                textOfItem.setText(newText);

                //навешивание обработчика нажатия, открывающего выбранную заметку
                final int numb = i;
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(BlitzListActivity.this, ReadBlitzActivity.class);
                        intent.putExtra(DataStore.class.getSimpleName(), dataStore);
                        intent.putExtra("document_name", instructionBlitzInfo.getFiles()[numb]);
                        intent.putExtra("blitz_name", instructionBlitzInfo.getNames()[numb]);
                        startActivity(intent);
                    }
                });
                linearLayout.addView(view);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
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
            //текущий экран
        } else if (id == R.id.nav_logout) {
            Transition.returnToAuthorization(this);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
