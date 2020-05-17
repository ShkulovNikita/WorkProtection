package ru.tpu.android.workprotection.Models;

import java.io.Serializable;
import java.util.ArrayList;

public class TestStats implements Serializable {

    ArrayList<Integer> correctPercents = new ArrayList<Integer>();

    public TestStats () { }

    public ArrayList<Integer> getCorrectPercents () {
        return correctPercents;
    }

    public void setCorrectPercents (ArrayList<Integer> correctPercents) {
        this.correctPercents = correctPercents;
    }
}
