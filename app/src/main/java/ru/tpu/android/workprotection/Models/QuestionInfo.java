package ru.tpu.android.workprotection.Models;

import java.io.Serializable;

//класс для хранения информации о вопросе из теста
public class QuestionInfo implements Serializable {
    //текст вопроса
    private String text;
    //варианты ответа
    private String[] answers;
    //правильность ответов
    private int[] answers_correctness;
    //файл к вопросу
    private String file;
    //тип файла
    private String type;

    public QuestionInfo() {
    }

    public String getText () {
        return text;
    }

    public void setText (String text) {
        this.text = text;
    }

    public String[] getAnswers () {
        return answers;
    }

    public void setAnswers (String[] answers) {
        this.answers = answers;
    }

    public int[] getAnswers_correctness () {
        return answers_correctness;
    }

    public void setAnswers_correctness (int[] answers_correctness) {
        this.answers_correctness = answers_correctness;
    }

    public String getFile () {
        return file;
    }

    public void setFile (String file) {
        this.file = file;
    }

    public String getType () {
        return type;
    }

    public void setType (String type) {
        this.type = type;
    }
}
