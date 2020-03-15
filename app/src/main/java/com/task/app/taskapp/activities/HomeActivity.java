package com.task.app.taskapp.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.auth.FirebaseAuth;
import com.task.app.taskapp.R;
import com.task.app.taskapp.databinding.ActivityHomeBinding;
import com.task.app.taskapp.models.UserModel;
import com.task.app.taskapp.preferences.Preferences;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private Preferences preferences;
    private UserModel userModel;
    private FirebaseAuth mAuth;


    // اول ميثود تستدعى عند فتح الابلكيشن
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        initView();
    }

    // بها ناخذ اسم اليوزر من الشيرد بريفرنس و زر تسجيل الخروج

    private void initView() {
        mAuth = FirebaseAuth.getInstance();
        preferences = Preferences.newInstance();
        userModel = preferences.getUserData(this);
        binding.setUserModel(userModel);

        binding.cardLogout.setOnClickListener(view -> {
            mAuth.signOut();
            preferences.clear(this);
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

    }
}
