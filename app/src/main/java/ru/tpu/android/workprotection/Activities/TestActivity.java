package ru.tpu.android.workprotection.Activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.File;

import ru.tpu.android.workprotection.Auxiliary.MenuFiller;
import ru.tpu.android.workprotection.Auxiliary.Permissions;
import ru.tpu.android.workprotection.Auxiliary.TimeCounter;
import ru.tpu.android.workprotection.Auxiliary.Transition;
import ru.tpu.android.workprotection.Connection.TestInfoTask;
import ru.tpu.android.workprotection.Connection.TestListInfoTask;
import ru.tpu.android.workprotection.Models.DataStore;
import ru.tpu.android.workprotection.Models.TestInfo;
import ru.tpu.android.workprotection.Models.TestStats;
import ru.tpu.android.workprotection.R;

public class TestActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //информация о текущем тесте и его прохождении
    TestInfo testInfo;
    TestStats testStats;
    String test_id;

    //номер текущего вопроса
    int number;

    //прошедшее время
    int count;

    //объект для хранения данных о пользователе
    DataStore dataStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //создание элементов интерфейса
        setContentView(R.layout.activity_test);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        MenuFiller.setTitle(this, getString(R.string.title_activity_test));

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

                //получение номера текущего вопроса
                number = arguments.getInt("num");

                //получение информации о пройденных вопросах
                testStats = (TestStats) arguments.getSerializable(TestStats.class.getSimpleName());

                test_id = arguments.getString("test_id");

                this.setTitle(testInfo.getName());

                showQuestions();

                if (number == 0)
                    TimeCounter.startTimer(TestActivity.this);
                else
                    TimeCounter.continueTimer(TestActivity.this);
            }
            MenuFiller.fillMenu(this, dataStore.getUserInfo());
        } catch (Exception ex) {
            //в случае ошибки - возвращение назад
            Transition.returnOnError(this, MenuActivity.class, dataStore);
        }

        navigationView.setNavigationItemSelectedListener(this);
        Permissions.verifyStoragePermissions(this);
    }

    //отображение вопроса с вариантами ответов
    private void showQuestions () {
        //построение общей для всех вопросов части
        LayoutInflater ltInflater = getLayoutInflater();

        //получение общего куска активити, куда будет вставляться вопрос
        LinearLayout testPage = (LinearLayout) findViewById(R.id.testsLayout);

        //добавление текста вопроса
        View view = ltInflater.inflate(R.layout.test_text, testPage, false);

        try {
            TextView textOfQuestion = view.findViewById(R.id.textOfQuestion);
            textOfQuestion.setText(testInfo.getQuestions()[number].getText());

            testPage.addView(view);
        } catch (Exception ex) {
            ex.printStackTrace();
            Transition.returnOnError(TestActivity.this, TestsListActivity.class, dataStore);
        }

        //добавление изображения, если оно есть
        if ((!testInfo.getQuestions()[number].getFile().equals("_"))
                &&(!testInfo.getQuestions()[number].getFile().equals("Произошла ошибка"))) {
            try {
                view = ltInflater.inflate(R.layout.test_image, testPage, false);

                ImageView imageView = view.findViewById(R.id.imageTest);

                File file = new File(testInfo.getQuestions()[number].getFile());

                if (file.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    imageView.setImageBitmap(myBitmap);
                }

                testPage.addView(view);
            } catch (Exception ex) {
                ex.printStackTrace();
                TimeCounter.stopTimer();
                Transition.returnOnError(TestActivity.this, TestsListActivity.class, dataStore);
            }
        }

        //добавление ответов
        for (int i = 0; i<testInfo.getQuestions()[number].getAnswers().length;i++)
        {
            view = ltInflater.inflate(R.layout.test_answer, testPage, false);
            TextView textView = view.findViewById(R.id.textViewTest);
            textView.setText(testInfo.getQuestions()[number].getAnswers()[i]);

            CheckBox checkBox = view.findViewById(R.id.testCheckBox);
            checkBox.setId(number + i + 1099);

            testPage.addView(view);
        }
    }

    //открыть следующий вопрос
    private void openNextQuestion() {
        TimeCounter.pauseTimer();

        if (number != testInfo.getQuestions().length - 1) {
            //получаем следующий вопрос
            try {
                number = number + 1;

                Intent intent = new Intent(TestActivity.this, TestActivity.class);
                intent.putExtra(DataStore.class.getSimpleName(), dataStore);
                intent.putExtra("test_id", test_id);
                intent.putExtra("test_info", testInfo);
                intent.putExtra("num", number);
                intent.putExtra("time", count);
                intent.putExtra(TestStats.class.getSimpleName(), testStats);

                startActivity(intent);
            } catch (Exception ex)
            {
                ex.printStackTrace();
                TimeCounter.stopTimer();
                Transition.returnOnError(TestActivity.this, TestsListActivity.class, dataStore);
            }
        } else {
            //текущий вопрос - последний
            try {
                //вывод результата прохождения теста
                count = TimeCounter.stopTimer();

                Intent intent = new Intent(TestActivity.this, TestResultActivity.class);
                intent.putExtra(DataStore.class.getSimpleName(), dataStore);
                intent.putExtra("test_id", test_id);
                intent.putExtra("test_info", testInfo);
                intent.putExtra("time", count);
                intent.putExtra(TestStats.class.getSimpleName(), testStats);

                startActivity(intent);
            } catch (Exception ex) {
                ex.printStackTrace();
                TimeCounter.stopTimer();
                Transition.returnOnError(TestActivity.this, TestsListActivity.class, dataStore);
            }
        }
    }

    //проверка выбранного ответа
    private void checkAnswers(View view) {
        try {
            //проверка количества ответов в вопросе
            if (testInfo.getQuestions()[number].getAnswers().length == 1) {
                View answ = findViewById(R.id.answerField);
                EditText editText = (EditText)answ;
                String answer = editText.getText().toString();
                if(answer.equals(testInfo.getQuestions()[number].getAnswers()[0])) {
                    testStats.getCorrectPercents().add(100);
                }
                else testStats.getCorrectPercents().add(0);

                //добавление блока с правильным ответом
                LayoutInflater inflater = getLayoutInflater();
                LinearLayout testPage = (LinearLayout) findViewById(R.id.testsLayout);
                View correctAnswer = inflater.inflate(R.layout.test_correct_answer, testPage, false);
                TextView textView = correctAnswer.findViewById(R.id.answerOfQuestion);
                textView.setText("Правильный ответ: " + testInfo.getQuestions()[number].getAnswers()[0]);
                testPage.addView(correctAnswer);
            } else {
                //проверка правильности введенных ответов
                int summ = 0;

                //проверяет, не были ли выбраны неправильные ответы
                boolean flag = false;

                for (int i = 0; i<testInfo.getQuestions()[number].getAnswers().length; i++) {
                    CheckBox checkBox = findViewById(number + i + 1099);
                    if ((checkBox.isChecked())&&(testInfo.getQuestions()[number].getAnswers_correctness()[i]>0)) {
                        summ = summ + testInfo.getQuestions()[number].getAnswers_correctness()[i];
                    }
                }

                if (summ == 100) {
                    for (int i = 0; i<testInfo.getQuestions()[number].getAnswers().length; i++) {
                        CheckBox checkBox = findViewById(number + i + 1099);
                        if ((checkBox.isChecked())&&(testInfo.getQuestions()[number].getAnswers_correctness()[i]==0)) {
                            flag = true;
                        }
                    }
                }

                if (flag) {
                    summ = 0;
                }
                testStats.getCorrectPercents().add(summ);

                //добавление блока с правильным ответом
                LayoutInflater inflater = getLayoutInflater();
                LinearLayout testPage = (LinearLayout) findViewById(R.id.testsLayout);
                String corrAnswer = "Правильный ответ: ";
                for (int i = 0; i < testInfo.getQuestions()[number].getAnswers().length;i++) {
                    if (testInfo.getQuestions()[number].getAnswers_correctness()[i] > 0) {
                        corrAnswer = corrAnswer + testInfo.getQuestions()[number].getAnswers()[i] + "; ";
                    }
                }
                View correctAnswer = inflater.inflate(R.layout.test_correct_answer, testPage, false);
                TextView textView = correctAnswer.findViewById(R.id.answerOfQuestion);
                textView.setText(corrAnswer);
                testPage.addView(correctAnswer);
            }

            //перемотка скролла вниз, чтобы пользователь увидел правильный ответ
            final ScrollView scrollview = ((ScrollView) findViewById(R.id.testsScrollView));
            scrollview.post(new Runnable() {
                @Override
                public void run() {
                    scrollview.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });

            //отображение кнопки, отвечающей за переход далее
            Button button = (Button) findViewById(R.id.buttonTest);
            if (number != testInfo.getQuestions().length - 1) {
                button.setText("Следующий вопрос");
            }
            else {
                button.setText("Закончить тест");
            }

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openNextQuestion();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            TimeCounter.stopTimer();
            Transition.returnOnError(TestActivity.this, TestsListActivity.class, dataStore);
        }
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
            Transition.moveToActivity(TestActivity.this, BlitzListActivity.class, dataStore);
        } else if (id == R.id.nav_logout) {
            Transition.returnToAuthorization(this);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
