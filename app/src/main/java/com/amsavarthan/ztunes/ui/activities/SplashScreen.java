package com.amsavarthan.ztunes.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDex;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.amsavarthan.ztunes.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private boolean openMain;
    private boolean openLogin;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
        MultiDex.install(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/GoogleSans_Regular.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

        setContentView(R.layout.activity_splash_screen);

        mAuth=FirebaseAuth.getInstance();
        sharedPreferences=getSharedPreferences("AccountPref",MODE_PRIVATE);

        if(mAuth.getCurrentUser()==null){

            mAuth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    sharedPreferences.edit().putBoolean("anonymous",true).apply();
                    startActivity(new Intent(SplashScreen.this, MainActivity.class));
                    finish();
                }
            });

        }else{
            startActivity(new Intent(SplashScreen.this, MainActivity.class));
            finish();
        }

        /*if(mAuth.getCurrentUser()!=null){
            openLogin=false;

            if(mAuth.getCurrentUser().isEmailVerified()) {
                if (!sharedPreferences.getString("account_type", "none").equals("none")) {
                   openMain=true;
                } else {
                    openMain=false;

                }
            }else{
                openLogin=true;
            }

        }else{
          openLogin=true;
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(!openLogin) {
                            if (openMain) {
                                startActivity(new Intent(SplashScreen.this, MainActivity.class));
                                finish();
                            } else {
                                startActivity(new Intent(SplashScreen.this, AccountTypeSelection.class));
                                finish();
                            }
                        }else{
                            LoginActivity.startActivity(SplashScreen.this);
                            finish();
                        }

                    }
                });

            }
        },1200);*/


    }
}
