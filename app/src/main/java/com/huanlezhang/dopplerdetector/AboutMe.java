package com.huanlezhang.dopplerdetector;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by huanle on 2016/6/13.
 */
public class AboutMe {
    static public void showDialog(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String message = "Hello. My name is Huanle Zhang." +
                " For more info. about this app, please go to my website: huanlezhang.com/project/doppler-collector";
        builder.setMessage(message).setTitle("About");
        builder.setPositiveButton("Return", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
