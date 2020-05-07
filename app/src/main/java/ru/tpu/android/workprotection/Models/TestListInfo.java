package ru.tpu.android.workprotection.Models;

import java.io.Serializable;
import java.util.Date;

//класс для хранения списка и информации о тестах
public class TestListInfo implements Serializable {
    //идентификаторы
    private String[] id;
    //названия
    private String[] name;
    //даты, до которых их нужно пройти
    private Date[] expire_date;
    //отметка о предыдущем прохождении
    private boolean[] passed;

    public TestListInfo() { }

    public void setName(String[] name) {
        this.name = name;
    }

    public String[] getName() {
        return name;
    }

    public void setId(String[] id) {
        this.id = id;
    }

    public String[] getId() {
        return id;
    }

    public Date[] getExpire_date() {
        return expire_date;
    }

    public void setExpire_date(Date[] expire_date) {
        this.expire_date = expire_date;
    }

    public boolean[] getPassed() {
        return passed;
    }

    public void setPassed(boolean[] passed) {
        this.passed = passed;
    }
}
