package com.task.app.taskapp.common;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;


public class UI_General_Method {

    @BindingAdapter("error")
    public static void setErrorUi(View view, String error)
    {
        if (view instanceof EditText)
        {
            EditText editText = (EditText) view;
            editText.setError(error);

        }else if (view instanceof TextView)
        {
            TextView textView = (TextView) view;
            textView.setError(error);

        }
    }









}
