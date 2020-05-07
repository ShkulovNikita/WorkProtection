package ru.tpu.android.workprotection.Models;

import java.io.Serializable;

/*класс, объекты которого используются для передачи данных между активити*/
public class DataStore implements Serializable {
    private UserInfo userInfo;
    private String userPhoto;

    public DataStore() {
    }

    public DataStore(UserInfo userInfo, String userPhoto) {
        this.userInfo = userInfo;
        this.userPhoto = userPhoto;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }
}
