package ru.tpu.android.workprotection.Models;

import java.io.Serializable;
import java.util.Date;

//класс для хранения списка инструктажей и информации о них
public class BriefingsInfo implements Serializable {
    //идентификаторы инструктажей
    private String[] id;
    //названия
    private String[] name;
    //даты, до которых их нужно пройти
    private Date[] expire_date;
    //названия файлов с инструктажами
    private String[] documents;
    //отметка о предыдущем прохождении
    private boolean[] passed;

    public BriefingsInfo() { }

    public String[] getId() {
        return id;
    }

    public void setId (String[] id) {
        this.id = id;
    }

    public String[] getName () {
        return name;
    }

    public void setName (String[] name) {
        this.name = name;
    }

    public String[] getDocuments () {
        return documents;
    }

    public void setDocuments(String[] documents) {
        this.documents = documents;
    }

    public Date[] getExpire_date() {
        return expire_date;
    }

    public void setExpire_date(Date[] expire_date) {
        this.expire_date = expire_date;
    }

    public boolean[] getPassed () {
        return passed;
    }

    public void setPassed(boolean[] passed){
        this.passed = passed;
    }
}
