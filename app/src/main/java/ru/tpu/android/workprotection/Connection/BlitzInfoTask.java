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
import ru.tpu.android.workprotection.Models.BlitzInfo;

public class BlitzInfoTask extends Task<BlitzInfo> {
    private static OkHttpClient httpClient;

    public static OkHttpClient getHttpClient() {
        if (httpClient == null) {
            synchronized (BlitzInfoTask.class) {
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

    public BlitzInfoTask(@Nullable Observer<BlitzInfo> observer) {
        super(observer);
    }

    @Override
    @WorkerThread
    protected BlitzInfo executeInBackground() throws Exception {
        String response = search( AuthorizationActivity.CONNECTION_URL + "instructionblitz");
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
    private BlitzInfo parseSearch(String response) throws JSONException {
        //удалить лишние символы из ответа от API
        response = editResponse(response);

        BlitzInfo blitzInfo = new BlitzInfo();

        if ((response.equals("Ничего не найдено")) || (response.equals("Произошла ошибка"))) {
            String[] resp = new String[1];
            resp[0] = response;
            blitzInfo.setIds(resp);
        } else {
            try {
                //парсинг строки в JSON
                JsonParser parser = new JsonParser();
                JsonElement jsonTree = parser.parse(response);

                //преобразование дерева в объект для возможности получения значений полей
                JsonObject jsonObject = jsonTree.getAsJsonObject();

                Gson gson = new Gson();

                //передача значений полей объекту
                blitzInfo.setIds(gson.fromJson(jsonObject.getAsJsonArray("Id"), String[].class));
                blitzInfo.setNames(gson.fromJson(jsonObject.getAsJsonArray("Names") , String[].class));
                blitzInfo.setFiles(gson.fromJson(jsonObject.getAsJsonArray("Files") , String[].class));
                blitzInfo.setDates(stringsToDates(gson.fromJson(jsonObject.getAsJsonArray("Dates"), String[].class)));
            } catch (Exception ex) {
                String[] resp = new String[1];
                resp[0] = "Произошла ошибка";
                blitzInfo.setIds(resp);
            }
        }
        return blitzInfo;
    }
}
