package ru.tpu.android.workprotection.Connection;

import android.os.Environment;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

public class FilesDownloader {
    static public String getUserPhoto (String response) {
        try {
            InputStream input = new ByteArrayInputStream(response.getBytes());;
            OutputStream output;

            String fileName = "userPhoto.png";

            //инициализация потока с местом сохранения файла
            output = new FileOutputStream(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);

            //чтение полученного файла
            byte data[] = new byte[1024];
            int count;
            while (( count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            //получение расположения файла
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName;

            //закрытие потоков Stream
            output.flush();
            output.close();
            input.close();

            return  filePath;
        } catch (MalformedURLException mue) {
            return "Произошла ошибка";
        } catch (IOException ioe) {
            return "Произошла ошибка";
        } catch (SecurityException se) {
            return "Произошла ошибка";
        }
    }
}
