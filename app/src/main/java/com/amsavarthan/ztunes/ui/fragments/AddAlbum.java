package com.amsavarthan.ztunes.ui.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.amsavarthan.ztunes.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;

public class AddAlbum extends Fragment {

    private static final int PICK_IMAGE = 100;
    private static final String TAG = AddAlbum.class.getSimpleName();
    View view;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    MaterialButton upload,create;
    ImageView art;
    TextInputEditText name_et;
    private Uri imageUri;
    private StorageReference storageReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.fragment_add_album_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFirestore=FirebaseFirestore.getInstance();
        mAuth= FirebaseAuth.getInstance();

        storageReference= FirebaseStorage.getInstance().getReference().child("album_art");

        imageUri=null;

        create=view.findViewById(R.id.create);
        upload=view.findViewById(R.id.upload);
        name_et=view.findViewById(R.id.nameEditText);
        art=view.findViewById(R.id.art);

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select album cover"),PICK_IMAGE);
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(name_et.getText().toString())){
                    Toasty.error(view.getContext(),"Enter a valid album name",Toasty.LENGTH_SHORT,true).show();
                    return;
                }
                if(imageUri==null){
                    Toasty.error(view.getContext(),"Upload an cover for the album",Toasty.LENGTH_SHORT,true).show();
                    return;
                }

                addAlbum(name_et.getText().toString(),imageUri);

            }
        });

    }

    private void addAlbum(final String song_name,Uri imageUri) {

        final ProgressDialog progressDialog=new ProgressDialog(view.getContext());
        progressDialog.setMessage("Adding new album....");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.show();

        final StorageReference album_ref = storageReference.child(song_name + ".png");
        album_ref.putFile(imageUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                        if(!task.isSuccessful()){
                            progressDialog.dismiss();
                            Toasty.error(view.getContext(),"Some technical error occurred",Toasty.LENGTH_SHORT,true).show();
                            task.getException().printStackTrace();
                            Log.e(TAG,task.getException().getMessage());
                        }else{

                            album_ref.getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(final Uri uri) {

                                            HashMap<String,Object> albumMap=new HashMap<>();
                                            albumMap.put("name",song_name);
                                            albumMap.put("art",uri.toString());

                                            mFirestore.collection("Users")
                                                    .document(mAuth.getUid())
                                                    .collection("albums")
                                                    .add(albumMap)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            Toasty.success(view.getContext(),"Album has been created successfully",Toasty.LENGTH_SHORT,true).show();
                                                            progressDialog.dismiss();
                                                            ((AppCompatActivity)view.getContext()).onBackPressed();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            e.printStackTrace();
                                                            Log.e(TAG,e.getMessage());
                                                            progressDialog.dismiss();
                                                            Toasty.error(view.getContext(),"Error adding new album : "+e.getMessage(),Toasty.LENGTH_SHORT,true).show();
                                                        }
                                                    });

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toasty.error(view.getContext(),"Some technical error occurred",Toasty.LENGTH_SHORT,true).show();
                                            e.printStackTrace();
                                            Log.e(TAG,e.getMessage());
                                            progressDialog.dismiss();
                                        }
                                    });

                        }

                    }
                });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE){
            if(resultCode==RESULT_OK){
                imageUri=data.getData();

                CropImage.activity(imageUri)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setActivityTitle("Crop Album Cover")
                        .setAspectRatio(1,1)
                        .setAllowRotation(true)
                        .setOutputCompressFormat(Bitmap.CompressFormat.PNG)
                        .setOutputCompressQuality(80)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(view.getContext(),this);

            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                imageUri = result.getUri();
                art.setImageURI(imageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e(TAG,error.getMessage());
            }
        }

    }


}
