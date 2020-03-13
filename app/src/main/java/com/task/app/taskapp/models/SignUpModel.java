package com.task.app.taskapp.models;

import android.content.Context;
import android.util.Patterns;
import android.widget.Toast;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;
import androidx.databinding.library.baseAdapters.BR;

import com.task.app.taskapp.R;

import java.io.Serializable;

public class SignUpModel extends BaseObservable implements Serializable {

    private String name;
    private String email;
    private String password;
    private String re_password;
    private boolean isFingerprint;

    public ObservableField<String> error_name = new ObservableField<>();
    public ObservableField<String> error_email = new ObservableField<>();

    public ObservableField<String> error_password = new ObservableField<>();
    public ObservableField<String> error_re_password = new ObservableField<>();

    public boolean isDataValid(Context context) {





        if (!name.trim().isEmpty() &&
                !email.trim().isEmpty() &&
                Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() &&
                password.length() >= 6 &&
                re_password.equals(password) &&
                isFingerprint

        ) {
            error_name.set(null);
            error_email.set(null);
            error_password.set(null);
            error_re_password.set(null);


            return true;
        } else {

            if (name.trim().isEmpty()) {
                error_name.set(context.getString(R.string.field_req));
            } else {
                error_name.set(null);

            }

            if (email.trim().isEmpty()) {
                error_email.set(context.getString(R.string.field_req));
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                error_email.set(context.getString(R.string.inv_email));

            } else {
                error_email.set(null);

            }


            if (password.trim().isEmpty()) {
                error_password.set(context.getString(R.string.field_req));
            } else if (password.trim().length() < 6) {
                error_password.set(context.getString(R.string.pass_short));

            }
            else {
                error_password.set(null);

            }

            if (re_password.isEmpty()) {
                error_re_password.set(context.getString(R.string.field_req));

            } else if (!password.equals(re_password)) {
                error_re_password.set(context.getString(R.string.pas_not_match));

            } else {

                error_re_password.set(null);
            }

            if (!isFingerprint)
            {
                Toast.makeText(context, R.string.ch_finger, Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    }

    public SignUpModel() {
        setName("");
        setEmail("");
        setFingerprint(false);
        setPassword("");
        setRe_password("");

    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    @Bindable
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        notifyPropertyChanged(BR.email);
    }




    @Bindable
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        notifyPropertyChanged(BR.password);

    }

    @Bindable
    public String getRe_password() {
        return re_password;
    }

    public void setRe_password(String re_password) {
        this.re_password = re_password;
        notifyPropertyChanged(BR.re_password);

    }

    public boolean isFingerprint() {
        return isFingerprint;
    }

    public void setFingerprint(boolean fingerprint) {
        isFingerprint = fingerprint;
    }

}
