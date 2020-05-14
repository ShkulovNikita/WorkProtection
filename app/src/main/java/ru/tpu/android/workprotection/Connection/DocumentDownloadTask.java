package ru.tpu.android.workprotection.Connection;

import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import ru.tpu.android.workprotection.Activities.AuthorizationActivity;
import ru.tpu.android.workprotection.Auxiliary.FilesDownloader;

public class DocumentDownloadTask extends Task<String> {
    private static OkHttpClient httpClient;

    public static OkHttpClient getHttpClient() {
        if (httpClient == null) {
            synchronized (String.class) {
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

    public DocumentDownloadTask(@Nullable Observer<String> observer) {
        super(observer);
    }

    @Override
    @WorkerThread
    protected String executeInBackground() throws Exception {
        String response = search( AuthorizationActivity.CONNECTION_URL + "authorization/" + AuthorizationActivity.userID);
        return response;
    }

    private String search(String fileName) throws Exception {
        try {
            //удаление пробелов и точек для корректного вызова API
            fileName = FilesDownloader.deleteSpacesAndDots(fileName);

            //установление соединения
            URL url = new URL(AuthorizationActivity.CONNECTION_URL + "getbriefingfile/" + fileName);
            URLConnection conexion = url.openConnection();
            conexion.connect();

            //потоки для получения и сохранения файла
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output;

            //возвращение имени файла к исходному виду
            fileName = FilesDownloader.returnSpacesAndDots(fileName);

            //место сохранения
            output = new FileOutputStream(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName + ".pdf");

            //передача файла из входного потока в выходной
            FilesDownloader.saveFileFromStream(input, output);

            //получение пути до сохраненного файла
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName + ".pdf";

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
}
