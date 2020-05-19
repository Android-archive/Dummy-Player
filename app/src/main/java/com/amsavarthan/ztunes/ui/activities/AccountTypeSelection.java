package com.amsavarthan.ztunes.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.amsavarthan.ztunes.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.marcoscg.dialogsheet.DialogSheet;

import java.util.HashMap;
import java.util.Map;

public class AccountTypeSelection extends AppCompatActivity {

    LinearLayout user,artist;
    String account_type;
    SharedPreferences sharedPreferences;
    FloatingActionButton doneFab;
    private ProgressDialog progressDialog;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
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


        setContentView(R.layout.activity_account_type_selection);

        user=findViewById(R.id.user);
        artist=findViewById(R.id.artist);
        doneFab=findViewById(R.id.done_fab);
        sharedPreferences=getSharedPreferences("AccountPref",MODE_PRIVATE);
        sharedPreferences.edit().putString("account_type","none").apply();

        doneFab.hide();

        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                account_type="user";
                doneFab.show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(getResources().getColor(R.color.purple_light));
                }
                user.setBackgroundColor(getResources().getColor(R.color.purple_light));
                artist.setBackgroundColor(getResources().getColor(R.color.white));
            }
        });

        artist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                account_type="artist";
                doneFab.show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                }
                user.setBackgroundColor(getResources().getColor(R.color.white));
                artist.setBackgroundColor(getResources().getColor(R.color.purple_light));
            }
        });

        progressDialog=new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        doneFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogSheet(AccountTypeSelection.this)
                        .setRoundedCorners(true)
                        .setColoredNavigationBar(true)
                        .setTitle("Conformation")
                        .setMessage("Are you sure with your choice? You cannot change it later.")
                        .setPositiveButton("Proceed", new DialogSheet.OnPositiveClickListener() {
                            @Override
                            public void onClick(View v) {

                                if(!TextUtils.isEmpty(account_type)){

                                    progressDialog.show();
                                    Map<String,Object> typeMap=new HashMap<>();
                                    typeMap.put("account_type",account_type);

                                    FirebaseFirestore.getInstance().collection("Users")
                                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .update(typeMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    sharedPreferences.edit().putString("account_type",account_type).apply();
                                                    if(MainActivity.activity!=null){
                                                        MainActivity.activity.finish();
                                                    }
                                                    startActivity(new Intent(AccountTypeSelection.this, MainActivity.class));
                                                    finish();
                                                    Toasty.success(AccountTypeSelection.this,"Account type set",Toasty.LENGTH_SHORT,true).show();
                                                    progressDialog.dismiss();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toasty.error(AccountTypeSelection.this,"Some error occured : "+e.getLocalizedMessage(),Toasty.LENGTH_SHORT,true).show();
                                                    Log.e("Error",e.getMessage());
                                                }
                                            });

                                }

                            }
                        })
                        .setNegativeButton("No", new DialogSheet.OnNegativeClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
            }
        });

    }


}
