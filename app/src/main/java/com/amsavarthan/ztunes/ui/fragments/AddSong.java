package com.amsavarthan.ztunes.ui.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amsavarthan.ztunes.ui.activities.MainActivity;
import com.amsavarthan.ztunes.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.marcoscg.dialogsheet.DialogSheet;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;

public class AddSong extends Fragment {

    private static final int PICK_AUDIO = 101;
    View view;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    Button upload,create,select_song;
    private static final int PICK_IMAGE = 100;
    private static final String TAG = AddSong.class.getSimpleName();
    private Uri imageUri=null;
    private ImageView art;
    private Uri audioUri=null;
    public static String album_name,album_art;
    TextView album_name_view;
    private RelativeLayout select_album;
    private TextInputEditText nameEditText,genreEditText,addArtistsEditText;
    private StorageReference songsstorageReference;
    private StorageReference songArtRef;
    private ProgressDialog progressDialog;
    private ProgressDialog progressDialog1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.fragment_add_song_view,container,false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!TextUtils.isEmpty(album_name)) {
            album_name_view.setText(album_name);
        }else{
            album_name_view.setText("Choose from your albums");
        }    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(!TextUtils.isEmpty(album_name)) {
            album_name_view.setText(album_name);
        }else{
            album_name_view.setText("Choose from your albums");
        }
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mFirestore=FirebaseFirestore.getInstance();
        mAuth= FirebaseAuth.getInstance();
        songsstorageReference= FirebaseStorage.getInstance().getReference().child("songs");
        album_name_view=view.findViewById(R.id.album_name);
        upload=view.findViewById(R.id.upload);
        select_song=view.findViewById(R.id.select_song);
        create=view.findViewById(R.id.create);
        art=view.findViewById(R.id.art);
        select_album=view.findViewById(R.id.select_album);

        progressDialog=new ProgressDialog(view.getContext());
        progressDialog.setMessage("Please wait....");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setIndeterminate(true);

        progressDialog1=new ProgressDialog(view.getContext());
        progressDialog1.setMessage("Uploading song....");
        progressDialog1.setCancelable(false);
        progressDialog1.setCanceledOnTouchOutside(false);
        progressDialog1.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        if(!TextUtils.isEmpty(album_name)) {
            album_name_view.setText(album_name);
        }else{
            album_name_view.setText("Choose from your albums");
        }

        nameEditText=view.findViewById(R.id.nameEditText);
        genreEditText=view.findViewById(R.id.genreEditText);
        addArtistsEditText=view.findViewById(R.id.artistEditText);

        album_name_view.setText(album_name);

        if(audioUri==null) {
            select_song.setVisibility(View.VISIBLE);
            create.setVisibility(View.GONE);
        }else{
            select_song.setVisibility(View.GONE);
            create.setVisibility(View.VISIBLE);
        }

        select_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                        .replace(R.id.container,new SelectAlbum(),"Select_Album")
                        .addToBackStack(null)
                        .commit();

                MainActivity.mCurrentFragment="select_album";

            }
        });

        select_song.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setType("audio/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select song"),PICK_AUDIO);
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select song cover"),PICK_IMAGE);
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(audioUri==null){
                    Toasty.error(view.getContext(),"No song has been selected",Toasty.LENGTH_SHORT,true).show();
                    return;
                }

                if(TextUtils.isEmpty(album_name)){
                    Toasty.info(view.getContext(),"Add this song to a Album",Toasty.LENGTH_SHORT,true).show();
                    return;
                }

                if(imageUri==null && TextUtils.isEmpty(album_art)) {
                    Toasty.error(view.getContext(),"Upload a cover for the song",Toasty.LENGTH_SHORT,true).show();
                    return;
                }

                if(TextUtils.isEmpty(nameEditText.getText().toString())){
                    Toasty.error(view.getContext(),"Invalid song name",Toasty.LENGTH_SHORT,true).show();
                    return;
                }

                if(TextUtils.isEmpty(genreEditText.getText().toString())){
                    Toasty.error(view.getContext(),"Invalid genre",Toasty.LENGTH_SHORT,true).show();
                    return;
                }

                if(imageUri==null && !TextUtils.isEmpty(album_art)){

                    new DialogSheet(view.getContext())
                            .setRoundedCorners(true)
                            .setColoredNavigationBar(true)
                            .setTitle("No song cover")
                            .setMessage("You haven't added a cover for the song")
                            .setPositiveButton("Select", new DialogSheet.OnPositiveClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent=new Intent();
                                    intent.setType("image/*");
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(Intent.createChooser(intent,"Select song cover"),PICK_IMAGE);
                                }
                            })
                            .setNegativeButton("Take from album", new DialogSheet.OnNegativeClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Glide.with(view.getContext())
                                            .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_song_art))
                                            .load(album_art)
                                            .into(art);

                                    if(TextUtils.isEmpty(addArtistsEditText.getText().toString())){

                                        new DialogSheet(view.getContext())
                                                .setRoundedCorners(true)
                                                .setColoredNavigationBar(true)
                                                .setTitle("Confirmation")
                                                .setMessage("Are you the only artist who contributed for this song?")
                                                .setPositiveButton("Yes", new DialogSheet.OnPositiveClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        uploadSongWithAlbumArt("",nameEditText.getText().toString(),genreEditText.getText().toString());
                                                    }
                                                })
                                                .setNegativeButton("No", new DialogSheet.OnNegativeClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        Toasty.info(view.getContext(), "Please mention their names in Addition Artists section", Toasty.LENGTH_SHORT, true).show();
                                                    }
                                                })
                                                .show();

                                    }else{
                                        uploadSongWithAlbumArt(addArtistsEditText.getText().toString(),nameEditText.getText().toString(),genreEditText.getText().toString());
                                    }

                                }
                            })
                            .show();
                    return;
                }

                if(TextUtils.isEmpty(addArtistsEditText.getText().toString())){

                    new DialogSheet(view.getContext())
                            .setRoundedCorners(true)
                            .setColoredNavigationBar(true)
                            .setTitle("Confirmation")
                            .setMessage("Are you the only artist who contributed for this song?")
                            .setPositiveButton("Yes", new DialogSheet.OnPositiveClickListener() {
                                @Override
                                public void onClick(View v) {
                                    uploadSongWithCustomArt("",nameEditText.getText().toString(),genreEditText.getText().toString());
                                }
                            })
                            .setNegativeButton("No", new DialogSheet.OnNegativeClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toasty.info(view.getContext(), "Please mention their names in Addition Artists section", Toasty.LENGTH_SHORT, true).show();
                                }
                            })
                            .show();

                }else{
                    uploadSongWithCustomArt(addArtistsEditText.getText().toString(),nameEditText.getText().toString(),genreEditText.getText().toString());
                }

            }
        });

    }

    private void uploadForApproval(String uid,String song_doc_id,final ProgressDialog progressDialog){

        Map<String,Object> map=new HashMap<>();
        map.put("artist_id",uid);
        map.put("song_doc_id",song_doc_id);
        map.put("timestamp",String.valueOf(System.currentTimeMillis()));

        mFirestore.collection("songs_approval_list")
                .add(map)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        Toasty.success(view.getContext(), "Song added successfully", Toasty.LENGTH_SHORT, true).show();
                        progressDialog.dismiss();
                        ((AppCompatActivity) view.getContext()).onBackPressed();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, e.getMessage());
                        progressDialog.dismiss();
                        Toasty.error(view.getContext(), "Error adding new song : " + e.getMessage(), Toasty.LENGTH_SHORT, true).show();
                    }
                });

    }

    private void uploadSongWithAlbumArt(final String artists, final String name, final String genre) {

        progressDialog.show();

        mFirestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        final String artist,owner_name;
                        owner_name=documentSnapshot.getString("name");
                        if(!TextUtils.isEmpty(artists)) {
                            artist = documentSnapshot.getString("name") + ", " + artists;
                        }else{
                            artist = documentSnapshot.getString("name");
                        }

                        final StorageReference songRef=songsstorageReference.child(name+".mp3");
                        songRef.putFile(audioUri)
                                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                        double progress=(100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                                        progressDialog.dismiss();
                                        progressDialog1.show();
                                        progressDialog1.setProgress((int)progress);
                                        Toasty.success(view.getContext(),"Uploaded : "+progress+"%",Toasty.LENGTH_SHORT,true).show();
                                    }
                                })
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        progressDialog1.dismiss();
                                        progressDialog.show();

                                        Map<String,Object> songMap=new HashMap<>();
                                        songMap.put("name",name);
                                        songMap.put("owner_name",owner_name);
                                        songMap.put("artist",artist);
                                        songMap.put("genre",genre);
                                        songMap.put("album",album_name);
                                        songMap.put("new_release","no");
                                        songMap.put("art",album_art);
                                        songMap.put("approved","false");
                                        songMap.put("link","");
                                        songMap.put("timestamp",String.valueOf(System.currentTimeMillis()));

                                        try {
                                            mFirestore.collection("Users")
                                                    .document(mAuth.getCurrentUser().getUid())
                                                    .collection("songs")
                                                    .add(songMap)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(final DocumentReference documentReference) {

                                                            songRef.getDownloadUrl()
                                                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                        @Override
                                                                        public void onSuccess(Uri uri) {

                                                                            Map<String,Object> songLinkMap=new HashMap<>();
                                                                            songLinkMap.put("link",uri.toString());

                                                                            MediaMetadataRetriever metadataRetriever=new MediaMetadataRetriever();
                                                                            metadataRetriever.setDataSource(getContext(),audioUri);
                                                                            long duration= Long.parseLong(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                                                                            metadataRetriever.release();

                                                                            songLinkMap.put("duration",duration);

                                                                            mFirestore.collection("Users")
                                                                                    .document(mAuth.getCurrentUser().getUid())
                                                                                    .collection("songs")
                                                                                    .document(documentReference.getId())
                                                                                    .update(songLinkMap)
                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {

                                                                                            uploadForApproval(mAuth.getUid(),documentReference.getId(),progressDialog);

                                                                                        }
                                                                                    })
                                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                                        @Override
                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                            e.printStackTrace();
                                                                                            Log.e(TAG, e.getMessage());
                                                                                            progressDialog.dismiss();
                                                                                            Toasty.error(view.getContext(), "Error adding new song : " + e.getMessage(), Toasty.LENGTH_SHORT, true).show();
                                                                                        }
                                                                                    });

                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    e.printStackTrace();
                                                                    Log.e(TAG, e.getMessage());
                                                                    progressDialog.dismiss();
                                                                    Toasty.error(view.getContext(), "Error adding new song : " + e.getMessage(), Toasty.LENGTH_SHORT, true).show();
                                                                }
                                                            });

                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            e.printStackTrace();
                                                            Log.e(TAG, e.getMessage());
                                                            progressDialog.dismiss();
                                                            Toasty.error(view.getContext(), "Error adding new song : " + e.getMessage(), Toasty.LENGTH_SHORT, true).show();
                                                        }
                                                    });

                                        }catch (Exception e){
                                            Toast.makeText(view.getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                            Log.e(TAG,e.getMessage());
                                        }

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        e.printStackTrace();
                                        Log.e(TAG,e.getMessage());
                                        progressDialog.dismiss();
                                        Toasty.error(view.getContext(),"Error adding new song : "+e.getMessage(),Toasty.LENGTH_SHORT,true).show();
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Log.e(TAG,e.getMessage());
                        progressDialog.dismiss();
                        Toasty.error(view.getContext(),"Error adding new song : "+e.getMessage(),Toasty.LENGTH_SHORT,true).show();
                    }
                });

    }


    private void uploadSongWithCustomArt(final String artists, final String name, final String genre) {

        progressDialog.show();

        mFirestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        final String artist,owner_name;
                        owner_name=documentSnapshot.getString("name");
                        if(!TextUtils.isEmpty(artists)) {
                            artist = documentSnapshot.getString("name") + ", " + artists;
                        }else{
                            artist = documentSnapshot.getString("name");
                        }

                        final StorageReference songRef=songsstorageReference.child(name+".mp3");
                        songRef.putFile(audioUri)
                                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                        double progress=(100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                                        progressDialog.dismiss();
                                        progressDialog1.show();
                                        progressDialog1.setProgress((int)progress);
                                        Toasty.success(view.getContext(),"Uploaded : "+progress+"%",Toasty.LENGTH_SHORT,true).show();
                                    }
                                })
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        progressDialog1.dismiss();
                                        progressDialog.show();
                                        songArtRef=FirebaseStorage.getInstance().getReference().child("songs_art")
                                                .child(name+".png");

                                        songArtRef.putFile(imageUri)
                                                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                                        if(!task.isSuccessful()){
                                                            progressDialog.dismiss();
                                                            Toasty.error(view.getContext(),"Some technical error occurred",Toasty.LENGTH_SHORT,true).show();
                                                            task.getException().printStackTrace();
                                                            Log.e(TAG,task.getException().getMessage());
                                                            return;
                                                        }

                                                        songArtRef.getDownloadUrl()
                                                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                    @Override
                                                                    public void onSuccess(Uri song_art_uri) {

                                                                        Map<String,Object> songMap=new HashMap<>();
                                                                        songMap.put("name",name);
                                                                        songMap.put("artist",artist);
                                                                        songMap.put("owner_name",owner_name);
                                                                        songMap.put("genre",genre);
                                                                        songMap.put("link","");
                                                                        songMap.put("approved","false");
                                                                        songMap.put("album",album_name);
                                                                        songMap.put("new_release","no");
                                                                        songMap.put("art",song_art_uri.toString());
                                                                        songMap.put("timestamp",String.valueOf(System.currentTimeMillis()));

                                                                        try {
                                                                            mFirestore.collection("Users")
                                                                                    .document(mAuth.getCurrentUser().getUid())
                                                                                    .collection("songs")
                                                                                    .add(songMap)
                                                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                                        @Override
                                                                                        public void onSuccess(final DocumentReference documentReference) {

                                                                                            songRef.getDownloadUrl()
                                                                                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                                                        @Override
                                                                                                        public void onSuccess(Uri uri) {

                                                                                                            Map<String,Object> songLinkMap=new HashMap<>();
                                                                                                            songLinkMap.put("link",uri.toString());

                                                                                                            MediaMetadataRetriever metadataRetriever=new MediaMetadataRetriever();
                                                                                                            metadataRetriever.setDataSource(getContext(),audioUri);
                                                                                                            long duration= Long.parseLong(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                                                                                                            metadataRetriever.release();

                                                                                                            songLinkMap.put("duration",duration);

                                                                                                            mFirestore.collection("Users")
                                                                                                                    .document(mAuth.getCurrentUser().getUid())
                                                                                                                    .collection("songs")
                                                                                                                    .document(documentReference.getId())
                                                                                                                    .update(songLinkMap)
                                                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                        @Override
                                                                                                                        public void onSuccess(Void aVoid) {
                                                                                                                            uploadForApproval(mAuth.getUid(),documentReference.getId(),progressDialog);
                                                                                                                        }
                                                                                                                    })
                                                                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                                                                        @Override
                                                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                                                            e.printStackTrace();
                                                                                                                            Log.e(TAG, e.getMessage());
                                                                                                                            progressDialog.dismiss();
                                                                                                                            Toasty.error(view.getContext(), "Error adding new song : " + e.getMessage(), Toasty.LENGTH_SHORT, true).show();
                                                                                                                        }
                                                                                                                    });

                                                                                                        }
                                                                                                    })
                                                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                                                        @Override
                                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                                            e.printStackTrace();
                                                                                                            Log.e(TAG, e.getMessage());
                                                                                                            progressDialog.dismiss();
                                                                                                            Toasty.error(view.getContext(), "Error adding new song : " + e.getMessage(), Toasty.LENGTH_SHORT, true).show();
                                                                                                        }
                                                                                                    });


                                                                                        }
                                                                                    })
                                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                                        @Override
                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                            e.printStackTrace();
                                                                                            Log.e(TAG, e.getMessage());
                                                                                            progressDialog.dismiss();
                                                                                            Toasty.error(view.getContext(), "Error adding new song : " + e.getMessage(), Toasty.LENGTH_SHORT, true).show();
                                                                                        }
                                                                                    });

                                                                        }catch (Exception e){
                                                                            Toast.makeText(view.getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                                            progressDialog.dismiss();
                                                                            Log.e(TAG,e.getMessage());
                                                                        }

                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        progressDialog.dismiss();
                                                                        Toasty.error(view.getContext(),"Some technical error occurred",Toasty.LENGTH_SHORT,true).show();
                                                                        e.printStackTrace();
                                                                        Log.e(TAG,e.getMessage());
                                                                        return;
                                                                    }
                                                                });

                                                    }
                                                });




                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        e.printStackTrace();
                                        Log.e(TAG,e.getMessage());
                                        progressDialog.dismiss();
                                        Toasty.error(view.getContext(),"Error adding new song : "+e.getMessage(),Toasty.LENGTH_SHORT,true).show();
                                    }
                                });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Log.e(TAG,e.getMessage());
                        progressDialog.dismiss();
                        Toasty.error(view.getContext(),"Error adding new song : "+e.getMessage(),Toasty.LENGTH_SHORT,true).show();
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
                        .setAllowRotation(true)
                        .setAspectRatio(1,1)
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

        if(requestCode==PICK_AUDIO){

            if(resultCode==RESULT_OK){
                audioUri=data.getData();
                select_song.setVisibility(View.GONE);
                create.setVisibility(View.VISIBLE);
            }

        }
    }



}
