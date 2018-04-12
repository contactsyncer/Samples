package com.apps.meet.userauthentication;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

/**
 * Created by dharamvir on 25/07/2017.
 */

public class Utils {


    public static final String SIGN_UP_REQUEST_URL = "http://participateme.com/contactsyncer/signin_app.php";
    public static final String SECRET_KEY = "xPbuGDGxPuLC";
    public static final String VERIFY_PHONE_NUMBERS_REQUEST_URL = "http://participateme.com/contactsyncer/verifyphonenumbers.php";

    public static void showNetworkDialog(final Activity context) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

            alertDialogBuilder.setTitle("Network Error");
            alertDialogBuilder.setMessage("Please check your network connection and try again later.");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //DisplayContactsActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + DisplayContactsActivity.this.getPackageName())));
                    dialog.cancel();
                    context.finish();
                }
            });

        alertDialogBuilder.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                //context.finish();
                dialog.cancel();
                context.finish();

                Intent in = new Intent(context, context.getClass());
                context.startActivity(in);
            }
        });

            alertDialogBuilder.show();
    }

}
