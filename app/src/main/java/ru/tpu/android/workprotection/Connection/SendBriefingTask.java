package ru.tpu.android.workprotection.Connection;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import ru.tpu.android.workprotection.Activities.AuthorizationActivity;
import ru.tpu.android.workprotection.Activities.ReadBriefingActivity;

public class SendBriefingTask extends Task<Void> {
    private static OkHttpClient httpClient;

    public static OkHttpClient getHttpClient() {
        if (httpClient == null) {
            synchronized (SendBriefingTask.class) {
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

    public SendBriefingTask(@Nullable Observer<Void> observer) {
        super(observer);
    }

    @Override
    @WorkerThread
    protected Void executeInBackground() throws Exception {
        try {
            executeRequest(AuthorizationActivity.CONNECTION_URL + "getbriefingresult/briefing", ReadBriefingActivity.REQUEST);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Void executeRequest(String address, String request) {
        HttpURLConnection urlConnection;
        String requestBody = request;

        try {
            //отправка на сервер
            URL url = new URL(address);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            OutputStream outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "utf-8"));
            writer.write(requestBody);
            writer.flush();
            writer.close();
            outputStream.close();

            //получение ответа от сервера после выполнения POST-запроса
            JSONObject jsonObject1 = new JSONObject();
            InputStream inputStream;
            //получение потока
            if (urlConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                inputStream = urlConnection.getInputStream();
            } else {
                inputStream = urlConnection.getErrorStream();
            }
            //парсинг потока
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String temp, response = "";
            while ((temp = bufferedReader.readLine()) != null) {
                response += temp;
            }
            //сохранение результата в JSON
            jsonObject1.put("Content", response);
            jsonObject1.put("Message", urlConnection.getResponseMessage());
            jsonObject1.put("Length", urlConnection.getContentLength());
            jsonObject1.put("Type", urlConnection.getContentType());

            String res = jsonObject1.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
