package com.task.app.taskapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.task.app.taskapp.common.Common;
import com.task.app.taskapp.models.LoginModel;
import com.task.app.taskapp.R;
import com.task.app.taskapp.common.Tags;
import com.task.app.taskapp.models.UserModel;
import com.task.app.taskapp.databinding.ActivityLoginBinding;
import com.task.app.taskapp.preferences.Preferences;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private Preferences preferences;
    private LoginModel loginModel;
    private FirebaseAuth mAuth;
    private DatabaseReference dRef;
    private UserModel userModel = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        initView();
    }

    // ميثود للتحقق من البيانات صحيحه كما يمكننى من خلالها الذهاب لصفحة تسجيل مستخدم جديد
    private void initView() {
        preferences = Preferences.newInstance();
        mAuth = FirebaseAuth.getInstance();
        dRef = FirebaseDatabase.getInstance().getReference(Tags.DATABASE_NAME);
        loginModel = new LoginModel();
        binding.setModel(loginModel);

        binding.btnLogin.setOnClickListener(view -> checkDataLogin());
        binding.btnSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });

        String session = preferences.getSession(this);
        if (session.equals(Tags.session_login))
        {
            navigateToHomeActivity();
        }
    }

    // ميثود خاصة للتحقق من البيانات موجوده وصالحة للاستخدام

    public void checkDataLogin() {

        if (loginModel.isDataValid(this)) {
            Common.CloseKeyBoard(this, binding.edtEmail);
            login();
        }
    }

    // ميثود خاصة لارسال البيانات الى الفاير بيز لعمل تسجيل دخول كما بها يمكننى التحقق اذا كان الايميل نشيط وصالح الاستخدام او لا
    private void login() {
        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.show();
        mAuth.signInWithEmailAndPassword(loginModel.getEmail(), loginModel.getPassword())
                .addOnSuccessListener(authResult -> {

                    if (authResult.getUser() != null&&mAuth.getCurrentUser().isEmailVerified()) {
                        String user_id = authResult.getUser().getUid();
                        Log.e("user_id",user_id);
                        getUserDataById(dialog, user_id);
                    }else{
                        dialog.dismiss();

                        Toast.makeText(this, "You should verfy Your email address", Toast.LENGTH_SHORT).show();

                    }

                }).addOnFailureListener(e -> {
            dialog.dismiss();
            if (e.getMessage() != null) {
                Common.CreateDialogAlert(this, e.getMessage());
            } else {
                Toast.makeText(this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
            }
        });

    }

    // ميثود تستخدم للحصول على تفاصيل المستخدم باستخدام ال id
    private void getUserDataById(ProgressDialog dialog, String user_id) {

        dRef.child(Tags.TABLE_USERS).child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                dialog.dismiss();
                if (dataSnapshot.getValue() != null) {
                    UserModel model = dataSnapshot.getValue(UserModel.class);

                    if (model != null) {

                        LoginActivity.this.userModel = model;
                        navigateToFingerprintActivity();

                    }

                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dialog.dismiss();
            }
        });

    }

    // تستدعى لفتح صفحة البصمة
    private void navigateToFingerprintActivity() {

        Intent intent = new Intent(this, FingerPrintActivity.class);
        startActivityForResult(intent, 100);
    }

    // ميثود تستخدم لفتح الصفحة الرئيسية

    private void navigateToHomeActivity() {

        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }


    // ميثود تستدعى لقراءه النتائج
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            if (userModel != null) {
                preferences.create_update_userData(this,userModel);
                navigateToHomeActivity();
            }
        }
    }


}
