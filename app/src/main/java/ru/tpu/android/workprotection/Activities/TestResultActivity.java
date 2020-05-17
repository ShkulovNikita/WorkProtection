package ru.tpu.android.workprotection.Activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import ru.tpu.android.workprotection.Auxiliary.MenuFiller;
import ru.tpu.android.workprotection.Auxiliary.Permissions;
import ru.tpu.android.workprotection.Auxiliary.TimeCounter;
import ru.tpu.android.workprotection.Auxiliary.Transition;
import ru.tpu.android.workprotection.Models.DataStore;
import ru.tpu.android.workprotection.Models.TestInfo;
import ru.tpu.android.workprotection.Models.TestStats;
import ru.tpu.android.workprotection.R;

public class TestResultActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

    //объект для хранения информации о текущем пользователе
    DataStore dataStore;

    //ID текущего теста
    String test_id;

    //результаты прохождения теста
    TestStats testStats;

    //информация о текущем тесте
    TestInfo testInfo;

    //прошедшее время
    int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //создание элементов интерфейса
        setContentView(R.layout.activity_test_result);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        MenuFiller.setTitle(this, getString(R.string.title_activity_test_result));

        //блокировка положения экрана для данной активити
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //получение информации о пользователе
        try {
            Bundle arguments = getIntent().getExtras();
            if (arguments!=null) {
                //обработка ошибки, возникшей в следующей активити
                Transition.checkError(this, arguments);

                dataStore = (DataStore) arguments.getSerializable(DataStore.class.getSimpleName());
                testInfo = (TestInfo) arguments.getSerializable("test_info");

                //получение информации о пройденных вопросах
                testStats = (TestStats) arguments.getSerializable(TestStats.class.getSimpleName());

                //получение идентификатора текущего теста
                test_id = arguments.getString("test_id");

                //получение времени прохождения теста
                count = arguments.getInt("time");

                this.setTitle(getString(R.string.title_activity_test_result));
            }
            MenuFiller.fillMenu(this, dataStore.getUserInfo());

            //запись результатов в текстовое поле
            TextView textView = findViewById(R.id.resultsTextView);

            String resultText = "Правильность ответов:\n";

            for (int i = 0; i < testStats.getCorrectPercents().size();i++)
            {
                resultText = resultText + "№" + (i+1) + ": ";

                if (testStats.getCorrectPercents().get(i) > 95)
                {
                    resultText = resultText + "верно (100%);\n";
                }
                else if (testStats.getCorrectPercents().get(i) > 0)
                {
                    resultText = resultText + "частично верно (" + testStats.getCorrectPercents().get(i) + "%);\n";
                }
                else
                {
                    resultText = resultText + "неверно (0%);\n";
                }
            }

            resultText = resultText + "Прошло времени: " + count + "\n";

            textView.setText(resultText);

            navigationView.setNavigationItemSelectedListener(this);
            Permissions.verifyStoragePermissions(this);
        } catch (Exception ex) {
            //в случае ошибки - возвращение назад
            Transition.returnOnError(this, MenuActivity.class, dataStore);
        }
    }

    public void returnToHome (View view) {
        Transition.moveToActivity(TestResultActivity.this, TestsListActivity.class, dataStore);
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
        //task.unregisterObserver();
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
            Transition.moveToActivity(TestResultActivity.this, BlitzListActivity.class, dataStore);
        } else if (id == R.id.nav_logout) {
            Transition.returnToAuthorization(this);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
