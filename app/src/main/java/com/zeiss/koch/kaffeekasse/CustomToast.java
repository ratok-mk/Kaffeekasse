package com.zeiss.koch.kaffeekasse;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast {
    public static void showText(android.content.Context context, java.lang.CharSequence text, int duration) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast, null);

        // set a message
        TextView toastText = (TextView) layout.findViewById(R.id.text);
        toastText.setText(text);

        Toast toast = new Toast(context.getApplicationContext());
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 10);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.show();
    }
}
