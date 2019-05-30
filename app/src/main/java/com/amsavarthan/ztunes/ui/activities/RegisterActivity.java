package com.amsavarthan.ztunes.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDex;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.amsavarthan.ztunes.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private TextInputLayout nameLayout,emailLayout,passwordLayout;
    private TextInputEditText nameInput,emailInput,passwordInput;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private static final int PICK_IMAGE=1;
    private static final int PIC_CROP=2;
    private Uri imageUri;
    private StorageReference storageReference;
    private CircleImageView profile_image;

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


        setContentView(R.layout.activity_register);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.grey));
        }

        mAuth= FirebaseAuth.getInstance();
        mFirestore= FirebaseFirestore.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference().child("profile_pictures").child("users");

        nameLayout=findViewById(R.id.name_layout);
        emailLayout=findViewById(R.id.email_layout);
        passwordLayout=findViewById(R.id.password_layout);

        nameInput=findViewById(R.id.nameEditText);
        emailInput=findViewById(R.id.emailEditText);
        passwordInput=findViewById(R.id.passwordEditText);

        profile_image=findViewById(R.id.profilePic);

    }

    private void checkInput() {

        final String name=nameInput.getText().toString();
        String email=emailInput.getText().toString();
        String password=passwordInput.getText().toString();

        if(TextUtils.isEmpty(name)){
            Toasty.error(this,"Please enter your name",Toasty.LENGTH_SHORT,true).show();
        } else if(TextUtils.isEmpty(email)){
            Toasty.error(this,"Please enter a valid email address",Toasty.LENGTH_SHORT,true).show();
        }else if(TextUtils.isEmpty(password)){
            Toasty.error(this,"Invalid password",Toasty.LENGTH_SHORT,true).show();
        }else{

            final ProgressDialog progressDialog=new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Registering now...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(final AuthResult authResult) {

                            final String userUid = authResult.getUser().getUid();

                            if (imageUri == null) {

                                authResult.getUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                HashMap<String, Object> userMap = new HashMap<>();
                                                userMap.put("name", name);
                                                userMap.put("date_of_creation", String.valueOf(System.currentTimeMillis()));
                                                userMap.put("picture", "default");
                                                userMap.put("account_type", "none");
                                                userMap.put("approved", "false");
                                                userMap.put("last_login", "");

                                                mFirestore.collection("Users")
                                                        .document(userUid)
                                                        .set(userMap)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                progressDialog.dismiss();
                                                                finish();
                                                                Toasty.success(RegisterActivity.this, "Verification mail has been sent", Toasty.LENGTH_SHORT, true).show();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                progressDialog.dismiss();
                                                                Toasty.error(RegisterActivity.this, "Error registering your account : " + e.getLocalizedMessage(), Toasty.LENGTH_SHORT, true).show();
                                                            }
                                                        });

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toasty.error(RegisterActivity.this,"Some technical error occurred",Toasty.LENGTH_SHORT,true).show();
                                                e.printStackTrace();
                                                Log.e(TAG,e.getMessage());
                                                progressDialog.dismiss();
                                            }
                                        });

                            }else{

                                authResult.getUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        final StorageReference user_profile = storageReference.child(userUid + ".png");
                                        user_profile.putFile(imageUri)
                                                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                                        if(task.isSuccessful()){
                                                            user_profile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                @Override
                                                                public void onSuccess(Uri uri) {

                                                                    HashMap<String,Object> userMap=new HashMap<>();
                                                                    userMap.put("name",name);
                                                                    userMap.put("date_of_creation",String.valueOf(System.currentTimeMillis()));
                                                                    userMap.put("picture",uri.toString());
                                                                    userMap.put("last_login","");
                                                                    userMap.put("approved", "false");
                                                                    userMap.put("account_type","none");

                                                                    mFirestore.collection("Users")
                                                                            .document(userUid)
                                                                            .set(userMap)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    progressDialog.dismiss();
                                                                                    finish();
                                                                                    Toasty.success(RegisterActivity.this,"Verification mail has been sent",Toasty.LENGTH_SHORT,true).show();
                                                                                }
                                                                            })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    progressDialog.dismiss();
                                                                                    Toasty.error(RegisterActivity.this,"Error registering your account : "+e.getLocalizedMessage(),Toasty.LENGTH_SHORT,true).show();
                                                                                }
                                                                            });

                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    progressDialog.dismiss();
                                                                    Toasty.error(RegisterActivity.this,"Error registering your account : "+e.getLocalizedMessage(),Toasty.LENGTH_SHORT,true).show();
                                                                }
                                                            });
                                                        }

                                                    }
                                                });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toasty.error(RegisterActivity.this,"Error sending verification mail : "+e.getLocalizedMessage(),Toasty.LENGTH_SHORT,true).show();
                                    }
                                });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toasty.error(RegisterActivity.this,"Error registering your account : "+e.getLocalizedMessage(),Toasty.LENGTH_SHORT,true).show();
                }
            });

        }

    }

    public void onBackClicked(View view) {
        finish();
    }

    public void onRegisterClick(View view) {
        checkInput();
    }

    public void onProfileSelectClicked(View view) {

        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select profile picture"),PICK_IMAGE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PICK_IMAGE){
            if(resultCode==RESULT_OK){
                imageUri=data.getData();

                CropImage.activity(imageUri)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .setActivityTitle("Crop Profile Picture")
                        .setAllowRotation(true)
                        .setAspectRatio(1,1)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);

            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                profile_image.setImageURI(imageUri);
                /*try {
                    File compressedFile= new Compressor(this).setCompressFormat(Bitmap.CompressFormat.PNG).setQuality(50).setMaxHeight(96).setMaxWidth(96).compressToFile(new File(imageUri.getPath()));
                    profile_image.setImageURI(Uri.fromFile(compressedFile));
                } catch (IOException e) {
                    e.printStackTrace();
                    Toasty.error(this, "Unable to compress: "+e.getLocalizedMessage(), Toasty.LENGTH_SHORT,true).show();
                    profile_image.setImageURI(imageUri);
                }*/

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e(TAG,error.getMessage());
            }
        }


    }
}
