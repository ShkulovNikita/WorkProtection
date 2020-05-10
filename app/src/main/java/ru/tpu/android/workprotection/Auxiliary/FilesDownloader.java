package ru.tpu.android.workprotection.Auxiliary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//вспомогательный класс для реализации загрузки и сохранения файлов
public class FilesDownloader {
    //запись файла из входного потока в выходной
    static public void saveFileFromStream (InputStream inputStream, OutputStream outputStream) {
        try {
            byte data[] = new byte[1024];

            int count;
            while (( count = inputStream.read(data)) != -1) {
                outputStream.write(data, 0, count);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
