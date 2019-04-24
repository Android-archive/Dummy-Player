package com.amsavarthan.ztunes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDex;
import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import pl.droidsonroids.gif.GifImageView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.marcoscg.dialogsheet.DialogSheet;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends AppCompatActivity {

    TextInputLayout emailLayout,passwordLayout;
    TextInputEditText emailInput,passwordInput;
    FirebaseAuth mAuth;
    FirebaseFirestore mFirestore;
    SharedPreferences sharedPreferences;
    LinearLayout top,bottom;
    GifImageView gifLogo;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    public static void startActivity(Context context){

        context.startActivity(new Intent(context,LoginActivity.class));

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


        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.grey));
        }

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        sharedPreferences = getSharedPreferences("AccountPref", MODE_PRIVATE);

        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);

        emailInput = findViewById(R.id.emailEditText);
        passwordInput = findViewById(R.id.passwordEditText);

    }

    private void checkInput() {

        String email=emailInput.getText().toString();
        String password=passwordInput.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toasty.error(this,"Please enter a vaild email address",Toasty.LENGTH_SHORT,true).show();
        }else if(TextUtils.isEmpty(password)){
            Toasty.error(this,"Invalid password",Toasty.LENGTH_SHORT,true).show();
        }else{

            final ProgressDialog progressDialog=new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Logging in...");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(final AuthResult authResult) {
                            if(authResult.getUser().isEmailVerified()) {

                               mFirestore.collection("Users")
                                       .document(authResult.getUser().getUid())
                                       .get()
                                       .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                           @Override
                                           public void onSuccess(DocumentSnapshot documentSnapshot) {

                                               Map<String,Object> map=new HashMap<>();
                                               map.put("last_login",String.valueOf(System.currentTimeMillis()));

                                               final String name=documentSnapshot.getString("name");
                                               final String profile=documentSnapshot.getString("picture");
                                               final String account_type=documentSnapshot.getString("account_type");

                                               sharedPreferences.edit()
                                                       .putString("name", name)
                                                       .putString("picture", profile)
                                                       .putString("account_type", account_type)
                                                       .apply();

                                               mFirestore.collection("Users")
                                                       .document(documentSnapshot.getId())
                                                       .update(map)
                                                       .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                           @Override
                                                           public void onSuccess(Void aVoid) {

                                                               Toasty.success(LoginActivity.this, "Successfully logged in", Toasty.LENGTH_SHORT, true).show();
                                                               if(account_type.equals("none")){
                                                                   startActivity(new Intent(LoginActivity.this,AccountTypeSelection.class));
                                                                   finish();
                                                                   progressDialog.dismiss();
                                                               }else{
                                                                   startActivity(new Intent(LoginActivity.this,MainActivity.class));
                                                                   finish();
                                                                   progressDialog.dismiss();
                                                               }
                                                           }
                                                       })
                                                       .addOnFailureListener(new OnFailureListener() {
                                                           @Override
                                                           public void onFailure(@NonNull Exception e) {
                                                               progressDialog.dismiss();
                                                               Log.e("Login error",e.getLocalizedMessage());
                                                           }
                                                       });

                                           }
                                       })
                                       .addOnFailureListener(new OnFailureListener() {
                                           @Override
                                           public void onFailure(@NonNull Exception e) {
                                               Toasty.error(LoginActivity.this,"Error logging in : "+e.getLocalizedMessage(),Toasty.LENGTH_SHORT,true).show();
                                               progressDialog.dismiss();
                                               Log.e("Error",e.getLocalizedMessage());
                                           }
                                       });

                            }else{

                                progressDialog.dismiss();
                                final ProgressDialog pDialog=new ProgressDialog(LoginActivity.this);
                                pDialog.setIndeterminate(true);
                                pDialog.setMessage("Sending...");
                                progressDialog.setCancelable(false);
                                pDialog.setCanceledOnTouchOutside(false);

                                new DialogSheet(LoginActivity.this)
                                        .setRoundedCorners(true)
                                        .setColoredNavigationBar(true)
                                        .setMessage("It seems that you haven't verified your E-mail address yet, Please verify to continue")
                                        .setTitle("Not Verified")
                                        .setPositiveButton("Re-send Mail", new DialogSheet.OnPositiveClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                pDialog.show();
                                                authResult.getUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        pDialog.dismiss();
                                                        Toasty.success(LoginActivity.this,"Verification email sent",Toasty.LENGTH_SHORT,true).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        pDialog.dismiss();
                                                        Toasty.error(LoginActivity.this,"Error sending mail : "+e.getLocalizedMessage(),Toasty.LENGTH_SHORT,true).show();
                                                        Log.e("Error",e.getLocalizedMessage());
                                                    }
                                                });
                                            }
                                        })
                                        .setNegativeButton("OK", new DialogSheet.OnNegativeClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                            }
                                        }).show();

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Log.e("Error",e.getLocalizedMessage());
                            Toasty.error(LoginActivity.this,"Error logging in : "+e.getLocalizedMessage(),Toasty.LENGTH_SHORT,true).show();
                        }
                    });

        }


    }

    public void onCreateAccountButtonCLick(View view) {

        startActivity(new Intent(this,RegisterActivity.class));

    }

    public void onBackClicked(View view) {
        finish();
    }

    public void onLoginClick(View view) {

        checkInput();

    }
}
