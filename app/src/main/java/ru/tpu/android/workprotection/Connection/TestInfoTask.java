package ru.tpu.android.workprotection.Connection;

import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import ru.tpu.android.workprotection.Activities.AuthorizationActivity;
import ru.tpu.android.workprotection.Activities.TestsListActivity;
import ru.tpu.android.workprotection.Auxiliary.FilesDownloader;
import ru.tpu.android.workprotection.Models.QuestionInfo;
import ru.tpu.android.workprotection.Models.TestInfo;

public class TestInfoTask extends Task<TestInfo> {
    private static OkHttpClient httpClient;

    public static OkHttpClient getHttpClient() {
        if (httpClient == null) {
            synchronized (BriefingsInfoTask.class) {
                if (httpClient == null) {
                    // Логирование запросов в logcat
                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor()
                            .setLevel(HttpLoggingInterceptor.Level.BASIC);
                    httpClient = new OkHttpClient.Builder()
                            .connectTimeout(5, TimeUnit.MINUTES)
                            .writeTimeout(5, TimeUnit.MINUTES)
                            .readTimeout(5, TimeUnit.MINUTES)
                            .addInterceptor(loggingInterceptor)
                            .build();
                }
            }
        }
        return httpClient;
    }

    public TestInfoTask(@Nullable Observer<TestInfo> observer) {
        super(observer);
    }

    @Override
    @WorkerThread
    protected TestInfo executeInBackground() throws Exception {
        String response = search( AuthorizationActivity.CONNECTION_URL + "chosentest/" + TestsListActivity.test_id);
        return parseSearch(response);
    }

    //запрос к API с возвращением тела ответа
    private String search(String query) throws Exception {
        Request request = new Request.Builder()
                .url(query)
                .build();
        Response response = null;
        try {
            response = getHttpClient().newCall(request).execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (response.code() != 200) {
            throw new Exception("api returned unexpected http code: " + response.code());
        }
        return response.body().string();
    }

    //парсинг ответа
    private TestInfo parseSearch(String response) throws Exception {
        //удалить лишние символы из ответа от API
        response = editResponse(response);

        TestInfo testInfo = new TestInfo();

        if ((response.equals("Ничего не найдено")) || (response.equals("Произошла ошибка"))) {
            testInfo.setId(response);
        } else {
            try {
                //парсинг строки в JSON
                JsonParser parser = new JsonParser();
                JsonElement jsonTree = parser.parse(response);

                //преобразование дерева в объект для возможности получения значений полей
                JsonObject jsonObject = jsonTree.getAsJsonObject();

                Gson gson = new Gson();

                testInfo.setId(jsonObject.get("Id").getAsString());
                testInfo.setName(jsonObject.get("Name").getAsString());
                testInfo.setTime(jsonObject.get("Time").getAsInt());

                //получение текстов вопросов
                String[] textsOfQuestions = gson.fromJson(jsonObject.getAsJsonArray("Questions"), String[].class);

                //получение типов вопросов
                String[] typesOfQuestions = gson.fromJson(jsonObject.getAsJsonArray("TypesOfQuestions"), String[].class);

                //названия файлов вопросов
                String[] namesOfFiles = gson.fromJson(jsonObject.getAsJsonArray("NamesOfFiles"), String[].class);

                //получение количеств ответов
                int[] numbersOfAnswers = gson.fromJson(jsonObject.getAsJsonArray("NumberOfAnswers"), int[].class);

                //получение вариантов ответов
                String[] answers = gson.fromJson(jsonObject.getAsJsonArray("Answers"), String[].class);

                //получение процентов правильности ответов
                int[] answersCorrectness = gson.fromJson(jsonObject.getAsJsonArray("AnswersCorrectness"), int[].class);

                //получение числа вопросов
                int numberOfQuestions = textsOfQuestions.length;

                //массив с информацией о вопросах выбранного теста и их ответах
                QuestionInfo[] questionInfos = new QuestionInfo[numberOfQuestions];

                for (int i = 0; i<numberOfQuestions; i++)
                    questionInfos[i] = new QuestionInfo();

                int counter = 0;

                //заполнение массива
                for (int i = 0; i<numberOfQuestions; i++)
                {
                    questionInfos[i].setText(textsOfQuestions[i]);
                    questionInfos[i].setType(typesOfQuestions[i]);
                    questionInfos[i].setFile(namesOfFiles[i]);

                    int numberOfAnswers = numbersOfAnswers[i];

                    String[] answersOfQuestion = new String[numberOfAnswers];
                    int[] correctnessOfAnswers = new int[numberOfAnswers];

                    int it = 0;
                    int initialCounter = counter;
                    for (int j = initialCounter; j<initialCounter + numberOfAnswers; j++)
                    {
                        answersOfQuestion[it] = answers[j];
                        correctnessOfAnswers[it] = answersCorrectness[j];
                        counter++;
                        it++;
                    }

                    questionInfos[i].setAnswers(answersOfQuestion);
                    questionInfos[i].setAnswers_correctness(correctnessOfAnswers);
                }

                //передача списка вопросов заданному тесту
                testInfo.setQuestions(questionInfos);

                //проход по вопросам, чтобы получить их файлы
                for (int i = 0; i < testInfo.getQuestions().length; i++) {
                    String filePath = getFile(testInfo, i);
                    testInfo.getQuestions()[i].setFile(filePath);
                }
            } catch (Exception ex) {
                testInfo.setId("Произошла ошибка");
            }
        }
        return testInfo;
    }

    //получение файла для вопроса
    private String getFile(TestInfo testInfo, int numb) {
        try {
            if (testInfo.getQuestions()[numb].getFile().equals("_")) {
                return "_";
            } else {
                //получение и обработка имени файла
                String fileName = testInfo.getQuestions()[numb].getFile();
                fileName = FilesDownloader.deleteSpacesAndDots(fileName);

                //установление соединения
                URL url = new URL(AuthorizationActivity.CONNECTION_URL + "gettestfile/" + fileName);
                URLConnection conexion = url.openConnection();
                conexion.setConnectTimeout(7000);
                conexion.connect();

                //потоки для получения и сохранения файла
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output;

                //возвращение имени файла к исходному виду
                fileName = FilesDownloader.returnSpacesAndDots(fileName);

                //место сохранения
                output = new FileOutputStream(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);

                //передача файла из входного потока в выходной
                FilesDownloader.saveFileFromStream(input, output);

                //получение пути до сохраненного файла
                String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName;

                //закрытие потоков
                output.flush();
                output.close();
                input.close();

                return filePath;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "Произошла ошибка";
        }
    }
}
