package ru.tpu.android.workprotection.Connection;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import ru.tpu.android.workprotection.Activities.AuthorizationActivity;
import ru.tpu.android.workprotection.Activities.BriefingsListActivity;
import ru.tpu.android.workprotection.Models.BriefingsInfo;

public class BriefingsInfoTask extends Task<BriefingsInfo> {
    private static OkHttpClient httpClient;

    public static OkHttpClient getHttpClient() {
        if (httpClient == null) {
            synchronized (BriefingsInfoTask.class) {
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

    public BriefingsInfoTask(@Nullable Observer<BriefingsInfo> observer) {
        super(observer);
    }

    @Override
    @WorkerThread
    protected BriefingsInfo executeInBackground() throws Exception {
        String response = search( AuthorizationActivity.CONNECTION_URL + "briefings/" + BriefingsListActivity.dataStore.getUserInfo().getId());
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
    private BriefingsInfo parseSearch(String response) throws JSONException {
        //удалить лишние символы из ответа от API
        response = editResponse(response);

        BriefingsInfo briefingsInfo = new BriefingsInfo();

        if ((response.equals("Ничего не найдено")) || (response.equals("Произошла ошибка"))) {
            String[] resp = new String[1];
            resp[0] = response;
            briefingsInfo.setId(resp);
        } else {
            try {
                //парсинг строки в JSON
                JsonParser parser = new JsonParser();
                JsonElement jsonTree = parser.parse(response);

                //преобразование дерева в объект для возможности получения значений полей
                JsonObject jsonObject = jsonTree.getAsJsonObject();

                Gson gson = new Gson();

                //получение значений полей
                briefingsInfo.setId(gson.fromJson(jsonObject.getAsJsonArray("Id"), String[].class));
                briefingsInfo.setName(gson.fromJson(jsonObject.getAsJsonArray("Names"), String[].class));
                briefingsInfo.setExpire_date(stringsToDates(gson.fromJson(jsonObject.getAsJsonArray("ExpireDates"), String[].class)));
                briefingsInfo.setDocuments(gson.fromJson(jsonObject.getAsJsonArray("Files"), String[].class));
                briefingsInfo.setPassed(gson.fromJson(jsonObject.getAsJsonArray("Passed"), boolean[].class));
            } catch (Exception ex) {
                String[] resp = new String[1];
                resp[0] = "Произошла ошибка";
                briefingsInfo.setId(resp);
            }
        }
        return briefingsInfo;
    }
}
