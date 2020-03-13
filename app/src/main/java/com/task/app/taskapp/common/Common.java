package com.task.app.taskapp.common;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.task.app.taskapp.R;
import com.task.app.taskapp.databinding.DialogAlertBinding;

public class Common {


    public static void CloseKeyBoard(Context context, View view) {
        if (context != null && view != null) {
            InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

            if (manager != null) {
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);

            }

        }


    }

    public static void CreateDialogAlert(Context context,String msg) {
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .create();

        DialogAlertBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_alert, null, false);

        binding.tvMsg.setText(msg);
        binding.btnCancel.setOnClickListener(v -> dialog.dismiss()

        );
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
    }

    public static ProgressDialog createProgressDialog(Context context, String msg) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(msg);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        ProgressBar bar = new ProgressBar(context);
        Drawable drawable = bar.getIndeterminateDrawable().mutate();
        drawable.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        dialog.setIndeterminateDrawable(drawable);
        return dialog;

    }

}
