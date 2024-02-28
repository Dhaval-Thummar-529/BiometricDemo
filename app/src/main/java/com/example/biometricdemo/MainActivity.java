package com.example.biometricdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private CancellationSignal cancellationSignal = null;

    private BiometricPrompt.AuthenticationCallback authenticationCallback;

    private Button authenticate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            authenticationCallback = new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    notifyUser("Authentication Error : " + errString);

                }

                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    notifyUser("Authentication Succeeded");
                }
            };
        }

        if(checkBiometricsSupport()){
            showFingerPrintPrompt();
        }

        authenticate = findViewById(R.id.start_authentication);
        authenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFingerPrintPrompt();
            }
        });
    }

    private void showFingerPrintPrompt() {
        BiometricPrompt prompt = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                prompt = new BiometricPrompt
                        .Builder(getApplicationContext())
                        .setTitle("Authentication")
                        .setNegativeButton("Cancel", getMainExecutor(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                notifyUser("Authentication Cancelled");
                            }
                        })
                        .build();
            }

            prompt.authenticate(getCancellationSignal(), getMainExecutor(), authenticationCallback);
        }
    }
    private CancellationSignal getCancellationSignal() {
        cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                notifyUser("Authentication was Cancelled by the user");
            }
        });
        return cancellationSignal;
    }

    private boolean checkBiometricsSupport() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if(!keyguardManager.isDeviceSecure()){
            notifyUser("Authentication has not been enabled in settings");
            return false;
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
            notifyUser("Authentication Permission is not enabled");
            return false;
        }
        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_FACE) || getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)){
            return true;
        } else
            return true;
    }

    private void notifyUser(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}