package ru.tpu.android.workprotection.Models;

import java.io.Serializable;
import java.util.Date;

//класс для хранения списка памяток и информации о них
public class BlitzInfo implements Serializable {
    //идентификаторы
    private String[] ids;
    //названия
    private String[] names;
    //даты создания памяток
    private Date[] dates;
    //файлы, прикрепленные к памяткам
    private String[] files;

    public BlitzInfo() {}

    public Date[] getDates() {
        return dates;
    }

    public String[] getFiles() {
        return files;
    }

    public String[] getIds() {
        return ids;
    }

    public String[] getNames() {
        return names;
    }

    public void setDates(Date[] dates) {
        this.dates = dates;
    }

    public void setIds(String[] ids) {
        this.ids = ids;
    }

    public void setFiles(String[] files) {
        this.files = files;
    }

    public void setNames(String[] names) {
        this.names = names;
    }
}
