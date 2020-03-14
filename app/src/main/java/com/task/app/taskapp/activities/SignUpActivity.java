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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.task.app.taskapp.R;
import com.task.app.taskapp.common.Common;
import com.task.app.taskapp.common.Tags;
import com.task.app.taskapp.databinding.ActivitySignUpBinding;
import com.task.app.taskapp.models.AESUtils;
import com.task.app.taskapp.models.SignUpModel;
import com.task.app.taskapp.models.UserModel;
import com.task.app.taskapp.preferences.Preferences;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private String lang = "en";
    private FirebaseAuth mAuth;
    private DatabaseReference dRef;
    private SignUpModel signUpModel;
    private Preferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);
        initView();
    }

    private void initView() {
        preferences = Preferences.newInstance();
        signUpModel = new SignUpModel();
        mAuth = FirebaseAuth.getInstance();
        dRef = FirebaseDatabase.getInstance().getReference(Tags.DATABASE_NAME);
        binding.setModel(signUpModel);
        binding.checkboxFingerprint.setOnClickListener(view -> {
            if (binding.checkboxFingerprint.isChecked()) {
                navigateToFingerPrintActivity();

            } else {
                signUpModel.setFingerprint(false);
                binding.setModel(signUpModel);
            }
        });
        binding.btnBack.setOnClickListener(view -> back());
        binding.btnSignUp.setOnClickListener(view -> checkDataSignUp());
    }


    public void checkDataSignUp() {
        if (signUpModel.isDataValid(this)) {
            Common.CloseKeyBoard(this, binding.edtName);
            createAccount();
        }
    }

    private void createAccount() {

        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.show();
        mAuth.createUserWithEmailAndPassword(signUpModel.getEmail(), signUpModel.getPassword())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = task.getResult().getUser().getUid();

                        addUserToDatabase(dialog, userId);
                        mAuth.getCurrentUser().sendEmailVerification()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("sent", "Email sent.");

                                            Toast.makeText(SignUpActivity.this, "Register success , Please check your email ", Toast.LENGTH_SHORT).show();
                                        

                                        } else {

                                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                });



                    }
                }).addOnFailureListener(e -> {
            if (dialog != null) {
                dialog.dismiss();
            }
            if (e.getMessage() != null) {
                Common.CreateDialogAlert(SignUpActivity.this, e.getMessage());
            }
        });
    }

    private void addUserToDatabase(ProgressDialog dialog, String userId) {

        String encyreptedPassword = "";
        try {
            encyreptedPassword = AESUtils.encrypt(signUpModel.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }

        UserModel userModel = new UserModel(userId, signUpModel.getName(), signUpModel.getEmail(), encyreptedPassword);


        dRef.child(Tags.TABLE_USERS).child(userId)
                .setValue(userModel)
                .addOnSuccessListener(aVoid -> {

                    dialog.dismiss();
                    preferences.create_update_userData(SignUpActivity.this, userModel);

                    navigateToLoginActivity();
                    dialog.dismiss();
              

                }).addOnFailureListener(e -> {
            if (e.getMessage() != null) {
                Common.CreateDialogAlert(SignUpActivity.this, e.getMessage());
            } else {
                Toast.makeText(this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void navigateToLoginActivity() {
        mAuth.signOut();
        preferences.clear(this);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();

    }


    private void navigateToFingerPrintActivity() {
        Intent intent = new Intent(this, FingerPrintActivity.class);
        startActivityForResult(intent, 100);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                binding.checkboxFingerprint.setChecked(true);
                signUpModel.setFingerprint(true);
                binding.setModel(signUpModel);
            } else {
                signUpModel.setFingerprint(false);
                binding.setModel(signUpModel);
                binding.checkboxFingerprint.setChecked(false);

            }
        }

    }


    public void back() {

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        back();
    }

}
