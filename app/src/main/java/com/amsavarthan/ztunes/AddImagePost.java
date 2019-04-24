package com.amsavarthan.ztunes;

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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.marcoscg.dialogsheet.DialogSheet;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;

public class AddImagePost extends Fragment {

    private static final int PICK_IMAGE = 100;
    private static final String TAG = AddImagePost.class.getSimpleName();
    View view;
    private Compressor compressor;
    private FrameLayout add_image;
    private ImageView post_image;
    private Uri imageUri=null;
    private MaterialButton upload;
    private TextInputEditText caption;
    private ProgressDialog progressdialog;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFireStore;
    private StorageReference storageReference;
    private ProgressDialog progressDialog1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.fragment_add_image_post_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth= FirebaseAuth.getInstance();
        mFireStore= FirebaseFirestore.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference().child("feed");

        add_image=view.findViewById(R.id.add_image);
        post_image=view.findViewById(R.id.imageview);
        upload=view.findViewById(R.id.upload);
        caption=view.findViewById(R.id.textEditText);

        compressor=new Compressor(view.getContext())
                .setQuality(75)
                .setCompressFormat(Bitmap.CompressFormat.PNG)
                .setMaxHeight(350);

        progressdialog=new ProgressDialog(view.getContext());
        progressdialog.setCanceledOnTouchOutside(false);
        progressdialog.setCancelable(false);
        progressdialog.setIndeterminate(true);
        progressdialog.setMessage("Uploading now....");

        progressDialog1=new ProgressDialog(view.getContext());
        progressDialog1.setMessage("Please wait....");
        progressDialog1.setCancelable(false);
        progressDialog1.setCanceledOnTouchOutside(false);
        progressDialog1.setIndeterminate(true);

        add_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select picture"),PICK_IMAGE);
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String entered_caption=caption.getText().toString();
                if(TextUtils.isEmpty(entered_caption)){

                    Toasty.error(view.getContext(),"Caption cannot be empty, please enter some text to continue",Toasty.LENGTH_SHORT,true).show();
                    return;

                }

                if(imageUri==null){

                    Toasty.error(view.getContext(),"Image cannot be empty, please select some image to continue",Toasty.LENGTH_SHORT,true).show();
                    return;

                }

                new DialogSheet(view.getContext())
                        .setRoundedCorners(true)
                        .setColoredNavigationBar(true)
                        .setTitle("Upload")
                        .setMessage("Are you sure is this the content you needed to post?")
                        .setNegativeButton("No", new DialogSheet.OnNegativeClickListener() {
                            @Override
                            public void onClick(View v) {
                                //dismiss
                            }
                        })
                        .setPositiveButton("Yes", new DialogSheet.OnPositiveClickListener() {
                            @Override
                            public void onClick(View v) {
                                progressDialog1.show();
                                uploadPost(entered_caption);
                            }
                        })
                        .show();

            }
        });

    }

    private void uploadPost(final String caption) {

        final StorageReference ref=storageReference.child(mAuth.getCurrentUser().getUid()+"_"+System.currentTimeMillis()+"_"+random()+".png");
        ref.putFile(imageUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(!task.isSuccessful()){
                            progressDialog1.dismiss();
                            Toasty.error(view.getContext(),"Error uploading image to server",Toasty.LENGTH_SHORT,true).show();
                            return;
                        }

                        ref.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(final Uri uri) {

                                        progressDialog1.dismiss();
                                        progressdialog.show();
                                        mFireStore.collection("Users")
                                                .document(mAuth.getCurrentUser().getUid())
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                        if(!documentSnapshot.exists()){
                                                            progressdialog.dismiss();
                                                            Toasty.error(view.getContext(),"Error getting current user info",Toasty.LENGTH_SHORT,true).show();
                                                            return;
                                                        }

                                                        Map<String, Object> map=new HashMap<>();
                                                        map.put("user_id",mAuth.getCurrentUser().getUid());
                                                        map.put("user_name",documentSnapshot.getString("name"));
                                                        map.put("user_photo",documentSnapshot.getString("picture"));
                                                        map.put("timestamp",String.valueOf(System.currentTimeMillis()));
                                                        map.put("caption",caption);
                                                        map.put("image",String.valueOf(uri));
                                                        map.put("likes","0");
                                                        map.put("shares","0");
                                                        map.put("color","4");
                                                        map.put("type","image");

                                                        mFireStore.collection("feed")
                                                                .add(map)
                                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentReference documentReference) {
                                                                        progressdialog.dismiss();
                                                                        Toasty.success(view.getContext(),"Post uploaded successfully",Toasty.LENGTH_SHORT,true).show();
                                                                        showFragment(new FeedView(),"feed");
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        progressdialog.dismiss();
                                                                        e.printStackTrace();
                                                                        Toasty.error(view.getContext(),"Error uploading post: "+e.getMessage(),Toasty.LENGTH_SHORT,true).show();
                                                                    }
                                                                });

                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressdialog.dismiss();
                                                        e.printStackTrace();
                                                        Toasty.error(view.getContext(),"Error uploading post: "+e.getMessage(),Toasty.LENGTH_SHORT,true).show();
                                                    }
                                                });


                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        e.printStackTrace();
                                        Toasty.error(view.getContext(),"Error uploading image: "+e.getMessage(),Toasty.LENGTH_SHORT,true).show();
                                    }
                                });

                    }
                });

    }

    void showFragment(Fragment fragment,String tag){

        ((AppCompatActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                .replace(R.id.container,fragment,tag)
                .commit();

        MainActivity.mCurrentFragment=tag;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE){
            if(resultCode==RESULT_OK){
                imageUri=data.getData();
                CropImage.activity(imageUri)
                        .setActivityTitle("Crop Picture")
                        .setAllowRotation(true)
                        .setAllowFlipping(true)
                        .setAutoZoomEnabled(true)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(view.getContext(),this);

            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                imageUri = result.getUri();
                try {
                    post_image.setImageURI(Uri.fromFile(compressor.compressToFile(new File(imageUri.getPath()))));
                } catch (IOException e) {
                    e.printStackTrace();
                    Toasty.error(view.getContext(), "Unable to compress: "+e.getLocalizedMessage(), Toasty.LENGTH_SHORT,true).show();
                    post_image.setImageURI(imageUri);
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e(TAG,error.getMessage());
            }
        }

    }

    @NonNull
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

}
