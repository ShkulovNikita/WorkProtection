package ru.tpu.android.workprotection.Models;

import java.io.Serializable;

/*класс, хранящий основную информацию об авторизуемом пользователе*/
public class UserInfo implements Serializable {

    private String id;
    private String surname;
    private String name;
    private String patronymic;
    private String profession;

    public UserInfo () {
    }

    public UserInfo (String id, String surname, String name, String patronymic, String profession) {
        this.id = id;
        this.surname = surname;
        this.name = name;
        this.patronymic = patronymic;
        this.profession = profession;
    }

    public String getId(){
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }
}
