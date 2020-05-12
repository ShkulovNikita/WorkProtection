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

    //замена пробелов и точек в названии файла, чтобы оно могло корректно обрабатываться API
    static public String deleteSpacesAndDots (String nameOfFile) {
        String result = nameOfFile.replace(" ", "prob");
        result = result.replace(".", "pnt");
        return result;
    }

    //возвращение замененных символов
    static public String returnSpacesAndDots (String nameOfFile) {
        String result = nameOfFile.replace("pnt", ".");
        result = result.replace("prob", " ");
        return result;
    }
}
