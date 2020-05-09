package ru.tpu.android.workprotection.Models;

import java.io.Serializable;

//класс для хранения информации о конкретном тесте
public class TestInfo implements Serializable {
    //идентификатор
    private String id;
    //название
    private String name;
    //время на прохождение
    private int time;
    //список вопросов
    private QuestionInfo[] questions;

    public TestInfo () {

    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public int getTime () {
        return time;
    }

    public void setTime (int time) {
        this.time = time;
    }

    public QuestionInfo[] getQuestions () {
        return questions;
    }

    public void setQuestions (QuestionInfo[] questions) {
        this.questions = questions;
    }
}
