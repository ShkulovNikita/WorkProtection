package ru.tpu.android.workprotection.Connection;

import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import ru.tpu.android.workprotection.Activities.AuthorizationActivity;
import ru.tpu.android.workprotection.Auxiliary.FilesDownloader;
import ru.tpu.android.workprotection.Models.UserInfo;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class UserInfoTask extends Task<UserInfo> {
    private static OkHttpClient httpClient;

    public static OkHttpClient getHttpClient() {
        if (httpClient == null) {
            synchronized (UserInfoTask.class) {
                if (httpClient == null) {
                    // Логирование запросов в logcat
                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor()
                            .setLevel(HttpLoggingInterceptor.Level.BASIC);
                    httpClient = new OkHttpClient.Builder()
                            .addInterceptor(loggingInterceptor)
                            .build();
                }
            }
        }
        return httpClient;
    }

    public UserInfoTask(@Nullable Observer<UserInfo> observer) {
        super(observer);
    }

    @Override
    @WorkerThread
    protected UserInfo executeInBackground() throws Exception {
        String response = search( AuthorizationActivity.CONNECTION_URL + "authorization/" + AuthorizationActivity.userID);
        UserInfo userInfo = parseSearch(response);
        if (userInfo != null) {
            if (!userInfo.getId().equals("Неверный табельный номер")) {
                String photoResponse = searchPhoto( AuthorizationActivity.CONNECTION_URL + "getphoto/" + AuthorizationActivity.userID);
                userInfo.setPhoto(photoResponse);
            }
        }
        return userInfo;
    }

    private String searchPhoto(String query) throws Exception {
        try {
            //установление соединения
            URL url = new URL(query);
            URLConnection conexion = url.openConnection();
            conexion.connect();

            InputStream input = new BufferedInputStream(url.openStream());

            OutputStream output;

            //задание имени файла фотографии
            String fileName = "userPhoto.png";
            //выбор места сохранения
            output = new FileOutputStream(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);

            //сохранение файла
            FilesDownloader.saveFileFromStream(input, output);

            //получение пути до фотографии
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName;

            //закрытие потоков
            output.flush();
            output.close();
            input.close();

            return filePath;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "Произошла ошибка";
        }
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
    private UserInfo parseSearch(String response) throws JSONException {
        //удалить лишние символы из ответа от API
        response = editResponse(response);

        UserInfo userInfo = new UserInfo();

        if (!response.equals("Пользователь не найден")) {
            try {
                //парсинг строки в JSON
                JsonParser parser = new JsonParser();
                JsonElement jsonTree = parser.parse(response);

                //преобразование дерева в объект для возможности получения значений полей
                JsonObject jsonObject = jsonTree.getAsJsonObject();

                //получение значений полей экземпляра класса UserInfo
                userInfo.setId(jsonObject.get("Id").getAsString());
                userInfo.setSurname(jsonObject.get("Surname").getAsString());
                userInfo.setName(jsonObject.get("Name").getAsString());
                userInfo.setPatronymic(jsonObject.get("Patronymic").getAsString());
                userInfo.setProfession(jsonObject.get("Profession").getAsString());
            } catch (Exception ex) {
                userInfo.setId("Неверный табельный номер");
            }
        } else {
            userInfo.setId("Неверный табельный номер");
        }
        return userInfo;
    }
}
