package com.task.app.taskapp.activities;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.task.app.taskapp.R;
import com.task.app.taskapp.databinding.ActivityFingerprintBinding;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerPrintActivity extends AppCompatActivity {
    private ActivityFingerprintBinding binding;
    private static final String KEY_NAME = "check_app_key";
    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fingerprint);
        initView();
    }
    // هذه الميثود تعمل على تعريف المتغيرات والربط بين الشكل الموجود فى ال xml
    //كما تعمل على معرفة نوع الجهاز والفرجن الخاص به لاستخدام البصمة

    private void initView() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);


            if (!fingerprintManager.isHardwareDetected()) {
                binding.imageScanner.setVisibility(View.GONE);
                binding.tvNoFingerprint.setVisibility(View.VISIBLE);
            }
            else if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.USE_FINGERPRINT}, 100);
            }

            else if (!fingerprintManager.hasEnrolledFingerprints()) {
                binding.imageScanner.setVisibility(View.GONE);
                binding.tvNoFingerprint.setVisibility(View.VISIBLE);
                binding.tvNoFingerprint.setText(R.string.no_finger);
            }

            else if (!keyguardManager.isKeyguardSecure()) {

                binding.imageScanner.setVisibility(View.GONE);
                binding.tvNoFingerprint.setVisibility(View.VISIBLE);
                binding.tvNoFingerprint.setText(R.string.enable_lockscreen);

            } else {

                binding.imageScanner.setVisibility(View.VISIBLE);

                generateKey();

                if (initCipher()) {
                    cryptoObject = new FingerprintManager.CryptoObject(cipher);

                    FingerprintHandler helper = new FingerprintHandler();
                    helper.startAuth(fingerprintManager, cryptoObject);
                }

            }

        }

    }

    // ميثود خاصة بانشاء الكى الخاص بالبصمة
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void generateKey() {

        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore");
        } catch (NoSuchAlgorithmException |
                NoSuchProviderException e) {
            throw new RuntimeException(
                    "Failed to get KeyGenerator instance", e);
        }

        try {
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean initCipher() {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }

    }

    // جميع الميثود اللى قبلها كلمة override مستدعاه اتوماتك من الاندرويد استوديو نقوم باستخدامها لل fingerprint
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        } else {
            Toast.makeText(this, "Fingerprint Permission denied", Toast.LENGTH_SHORT).show();
        }
    }


    private  class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

        private CancellationSignal cancellationSignal;

        public FingerprintHandler() {
        }

        public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {

            cancellationSignal = new CancellationSignal();
            if (ActivityCompat.checkSelfPermission(FingerPrintActivity.this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(FingerPrintActivity.this, new String[]{Manifest.permission.USE_FINGERPRINT}, 100);

            }
            manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
        }

        @Override

        public void onAuthenticationError(int errMsgId, CharSequence errString) {

            //I’m going to display the results of fingerprint authentication as a series of toasts.
            //Here, I’m creating the message that’ll be displayed if an error occurs//

            // Toast.makeText(FingerPrintActivity.this, "Authentication error\n" + errString, Toast.LENGTH_LONG).show();
        }

        @Override

        //onAuthenticationFailed is called when the fingerprint doesn’t match with any of the fingerprints registered on the device//

        public void onAuthenticationFailed() {

            Toast.makeText(FingerPrintActivity.this, "Authentication failed", Toast.LENGTH_LONG).show();
        }

        @Override

        //onAuthenticationHelp is called when a non-fatal error has occurred. This method provides additional information about the error,
        //so to provide the user with as much feedback as possible I’m incorporating this information into my toast//
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {

            // Toast.makeText(FingerPrintActivity.this, "Authentication help\n" + helpString, Toast.LENGTH_LONG).show();
        }

        @Override

        //onAuthenticationSucceeded is called when a fingerprint has been successfully matched to one of the fingerprints stored on the user’s device//
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {

            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.EFFECT_TICK));
            } else {
                //deprecated in API 26
                v.vibrate(50);
            }
            setResult(RESULT_OK);
            finish();
            Toast.makeText(FingerPrintActivity.this,getString(R.string.suc), Toast.LENGTH_LONG).show();
        }
    }

    // ميثود مستخدمه لعمل back
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
