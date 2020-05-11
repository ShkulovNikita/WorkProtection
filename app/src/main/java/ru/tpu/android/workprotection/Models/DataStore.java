package ru.tpu.android.workprotection.Models;

import java.io.Serializable;

/*класс, объекты которого используются для передачи данных между активити*/
public class DataStore implements Serializable {
    private UserInfo userInfo;
    private BriefingsInfo briefingsInfo;
    private TestListInfo testListInfo;
    private BlitzInfo blitzInfo;

    public DataStore() { }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public BriefingsInfo getBriefingsInfo() {
        return briefingsInfo;
    }

    public void setBriefingsInfo(BriefingsInfo briefingsInfo) {
        this.briefingsInfo = briefingsInfo;
    }

    public BlitzInfo getBlitzInfo() {
        return blitzInfo;
    }

    public void setBlitzInfo(BlitzInfo blitzInfo) {
        this.blitzInfo = blitzInfo;
    }

    public TestListInfo getTestListInfo() {
        return testListInfo;
    }

    public void setTestListInfo(TestListInfo testListInfo) {
        this.testListInfo = testListInfo;
    }
}