package com.amsavarthan.ztunes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.marcoscg.dialogsheet.DialogSheet;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.amsavarthan.ztunes.NetworkUtil.NETWORK_STATUS_NOT_CONNECTED;

public class ProfileView extends Fragment {

    private static final String TAG =ProfileView.class.getSimpleName() ;
    private static final int PICK_IMAGE = 100;
    private View view;
    private TextInputEditText name,email;
    private TextView name_txtview;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private MaterialButton manage;
    private ImageView logout;
    private CircleImageView profilePic;
    private String mode,name_pref;
    private RelativeLayout change_pass,change_type;
    private boolean user=true;
    private StorageReference storageReference;
    private Uri imageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.fragment_profile_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setLightStatusBar(getActivity());

        mFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();

        if(mAuth==null) {
            startActivity(new Intent(view.getContext(),LoginActivity.class));
            getActivity().finish();
            return;
        }

        storageReference= FirebaseStorage.getInstance().getReference().child("profile_pictures").child("users");

        name=view.findViewById(R.id.nameEditText);
        email=view.findViewById(R.id.emailEditText);
        name_txtview=view.findViewById(R.id.name);
        profilePic=view.findViewById(R.id.profilePic);
        manage=view.findViewById(R.id.manage_songs);
        logout=view.findViewById(R.id.logout);
        change_pass=view.findViewById(R.id.change_pass);
        change_type=view.findViewById(R.id.change_type);

        mode=view.getContext().getSharedPreferences("AccountPref",MODE_PRIVATE).getString("account_type","none");
        name_pref=view.getContext().getSharedPreferences("AccountPref",MODE_PRIVATE).getString("name","");

        name.setText(name_pref);
        name_txtview.setText(name_pref);

        String email_d=mAuth.getCurrentUser().getEmail();
        email.setText(email_d);

        if(TextUtils.equals(mode,"user")){

            logout.setVisibility(View.GONE);
            manage.setText("Logout");
            manage.setIcon(getResources().getDrawable(R.drawable.ic_exit_to_app_24dp));
            manage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logout();
                }
            });

        }else if(TextUtils.equals(mode,"artist")){

            logout.setVisibility(View.VISIBLE);
            manage.setText("Manage Songs");
            manage.setIcon(getResources().getDrawable(R.mipmap.music));
            manage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toasty.info(view.getContext(),"Account type and action vaild",Toasty.LENGTH_SHORT,true).show();
                }
            });

        }else{
            startActivity(new Intent(view.getContext(),AccountTypeSelection.class));
            getActivity().finish();
            return;
        }


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        change_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testChange();
            }
        });

        mFirestore.collection("Users")
                .document(mAuth.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        String name_d=documentSnapshot.getString("name");
                        String profile_d=documentSnapshot.getString("picture");
                        String type_d=documentSnapshot.getString("account_type");
                        view.getContext().getSharedPreferences("AccountPref", MODE_PRIVATE).edit().putString("name", name_d).putString("account_type", type_d).apply();

                        name.setText(name_d);
                        name_txtview.setText(name_d);

                        if(!TextUtils.equals(profile_d,"default")){
                            Glide.with(view.getContext())
                                    .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_artist_art))
                                    .load(profile_d)
                                    .into(profilePic);
                        }


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toasty.error(view.getContext(),"Some technical error occurred",Toasty.LENGTH_SHORT,true).show();
                        e.printStackTrace();
                        Log.e(TAG,e.getMessage());
                    }
                });

        change_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toasty.info(view.getContext(),"Change password clicked",Toasty.LENGTH_SHORT,true).show();
            }
        });

        profilePic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Intent intent=new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select profile picture"),PICK_IMAGE);

                return true;
            }
        });

    }

    private void logout() {

        new DialogSheet(view.getContext())
                .setTitle("Log out")
                .setMessage("Are you sure do you want to log out from this account?")
                .setRoundedCorners(true)
                .setColoredNavigationBar(true)
                .setPositiveButton("Yes", new DialogSheet.OnPositiveClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(NetworkUtil.getConnectivityStatus(view.getContext())!=NETWORK_STATUS_NOT_CONNECTED) {
                            mAuth.signOut();
                            view.getContext().getSharedPreferences("AccountPref", MODE_PRIVATE).edit().putString("name", "").putString("account_type", "none").apply();
                            Toasty.success(view.getContext(), "Account logged out", Toasty.LENGTH_SHORT, true).show();
                            startActivity(new Intent(getActivity(),LoginActivity.class));
                            getActivity().finish();
                        }else{
                            Toasty.error(view.getContext(), "No connection", Toasty.LENGTH_SHORT, true).show();
                        }
                    }
                })
                .setNegativeButton("No", new DialogSheet.OnNegativeClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                })
                .show();

    }

    private static void setLightStatusBar(Activity activity){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            activity.getWindow().setStatusBarColor(Color.parseColor("#5F6BDF"));
        }
    }

    private static void clearLightStatusBar(Activity activity){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            activity.getWindow().setStatusBarColor(Color.parseColor("#ffffff"));
        }
    }

    void testChange(){

        if(user){
            logout.setVisibility(View.VISIBLE);
            manage.setText("Manage Songs");
            manage.setIcon(getResources().getDrawable(R.mipmap.music));
            manage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toasty.info(view.getContext(),"Account type and action vaild",Toasty.LENGTH_SHORT,true).show();
                }
            });
            user=false;
        }else{
            logout.setVisibility(View.GONE);
            manage.setText("Logout");
            manage.setIcon(getResources().getDrawable(R.drawable.ic_exit_to_app_24dp));
            manage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logout();
                }
            });
            user=true;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE){
            if(resultCode==RESULT_OK){
                imageUri=data.getData();

                UCrop.Options options = new UCrop.Options();
                options.setCompressionFormat(Bitmap.CompressFormat.PNG);
                options.setCompressionQuality(100);
                options.setShowCropGrid(true);

                UCrop.of(imageUri, Uri.fromFile(new File(view.getContext().getCacheDir(), "ztunes_user_profile_picture.png")))
                        .withAspectRatio(1, 1)
                        .withOptions(options)
                        .start(getActivity());

            }
        }
        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                imageUri = UCrop.getOutput(data);
                try {
                    File compressedFile= new Compressor(view.getContext()).setCompressFormat(Bitmap.CompressFormat.PNG).setQuality(80).setMaxHeight(96).setMaxWidth(96).compressToFile(new File(imageUri.getPath()));
                    profilePic.setImageURI(Uri.fromFile(compressedFile));
                } catch (IOException e) {
                    e.printStackTrace();
                    profilePic.setImageURI(imageUri);
                }
                uploadToPictureServer();
            } else if (resultCode == UCrop.RESULT_ERROR) {
                Log.e("Error", "Crop error:" + UCrop.getError(data).getMessage());
            }
        }
    }

    private void uploadToPictureServer() {

        final StorageReference user_profile = storageReference.child(mAuth.getUid() + ".png");
        user_profile.putFile(imageUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                        if(!task.isSuccessful()){
                            Toasty.error(view.getContext(),"Some technical error occurred",Toasty.LENGTH_SHORT,true).show();
                            task.getException().printStackTrace();
                            Log.e(TAG,task.getException().getMessage());
                        }else{

                            user_profile.getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {

                                            HashMap<String,Object> userMap=new HashMap<>();
                                            userMap.put("picture",uri.toString());

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toasty.error(view.getContext(),"Some technical error occurred",Toasty.LENGTH_SHORT,true).show();
                                            e.printStackTrace();
                                            Log.e(TAG,e.getMessage());
                                        }
                                    });

                        }

                    }
                });

    }

    @Override
    public void onDestroyView() {
        clearLightStatusBar(getActivity());
        super.onDestroyView();
    }
}
