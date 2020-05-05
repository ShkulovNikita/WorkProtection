package ru.tpu.android.workprotection.Connection;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import ru.tpu.android.workprotection.Activities.AuthorizationActivity;
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
        String response = search("http://192.168.1.28:45455/api/authorization/" + AuthorizationActivity.userID);
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
    private UserInfo parseSearch(String response) throws JSONException {
        //удалить лишние символы из ответа от API
        response = editResponse(response);

        UserInfo userInfo = new UserInfo();

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
            ex.printStackTrace();
        }

        return userInfo;
    }
}
