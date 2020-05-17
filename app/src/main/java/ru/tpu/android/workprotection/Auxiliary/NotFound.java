package ru.tpu.android.workprotection.Auxiliary;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NotFound {
    static public void addNotFoundText (Context context, LinearLayout linearLayout) {
        TextView textView = new TextView(context);
        textView.setText("Ничего не найдено");
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.CENTER);
        linearLayout.addView(textView);
    }
}
