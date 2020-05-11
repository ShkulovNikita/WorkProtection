package ru.tpu.android.workprotection.Models;

import java.io.Serializable;

/*класс, объекты которого используются для передачи данных между активити*/
public class DataStore implements Serializable {
    private UserInfo userInfo;
    private BriefingsInfo briefingsInfo;

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
}
