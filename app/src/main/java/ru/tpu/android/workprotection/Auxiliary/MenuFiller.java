package ru.tpu.android.workprotection.Auxiliary;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import java.io.File;

import ru.tpu.android.workprotection.Models.UserInfo;
import ru.tpu.android.workprotection.R;

public class MenuFiller {
    static public void FillMenu (Activity activity, UserInfo userInfo) {
        Permissions.verifyStoragePermissions(activity);

        //получение боковой панели активити
        NavigationView navigationView = activity.findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);

        //запись ФИО работника
        TextView userText = (TextView) header.findViewById(R.id.nav_name);
        userText.setText(userInfo.getSurname() + " " + userInfo.getName() + " " + userInfo.getPatronymic());

        //запись должности работника
        TextView userProfession = (TextView) header.findViewById(R.id.nav_profession);
        userProfession.setText(userInfo.getProfession());

        //установление фотографии
        File file = new File(userInfo.getPhoto());
        ImageView imageView = (ImageView) header.findViewById(R.id.imageView);

        if(file.exists()){
            try {
                Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                imageView.setImageBitmap(myBitmap);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
