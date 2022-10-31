package ae.adpolice.gov.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

public class UIUtils {

    public static void displayAlert(final Context context, final String title, final String message, final String okText, final DialogInterface.OnClickListener okListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNeutralButton(okText,okListener);
        builder.create().show();
    }

    public static void displayAlertWithAction(final Context context, final String title, final String message,
                                              final String yes, final String no, final DialogInterface.OnClickListener yesListener, final DialogInterface.OnClickListener noListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(yes, yesListener);
        builder.setNegativeButton(no, noListener);
        builder.create().show();
    }

    public static Dialog displayProgress(Context context, String message) {
        return ProgressDialog.show(context, null, message, true, false, null);
    }

    public static void hideProgress(Dialog dialog) {
        if (dialog != null)
            dialog.dismiss();
    }
}
