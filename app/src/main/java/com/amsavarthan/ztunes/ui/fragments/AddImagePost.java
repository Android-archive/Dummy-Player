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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.amsavarthan.ztunes.models.Images;
import com.amsavarthan.ztunes.ui.activities.MainActivity;
import com.amsavarthan.ztunes.adapters.PagerPhotosAdapter;
import com.amsavarthan.ztunes.R;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.marcoscg.dialogsheet.DialogSheet;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;

public class AddImagePost extends Fragment {

    private static final int PICK_IMAGE = 100;
    private static final String TAG = AddImagePost.class.getSimpleName();
    View view;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private EditText mEditText;
    private Map<String, Object> postMap;
    private ProgressDialog mDialog;
    List<Images> imagesList=new ArrayList<>();
    private Compressor compressor;
    boolean canUpload=false;
    boolean isFirst=true;
    ViewPager pager;
    PagerPhotosAdapter adapter;
    private StorageReference mStorage;
    private DotsIndicator indicator;
    private RelativeLayout indicator_holder;
    private int selectedIndex;
    private RelativeLayout edit_layout;
    ImageView add,delete,crop;
    MaterialButton upload;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        startPickImage(7);
        return view=inflater.inflate(R.layout.fragment_add_image_post_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth= FirebaseAuth.getInstance();
        mFirestore= FirebaseFirestore.getInstance();
        mStorage= FirebaseStorage.getInstance().getReference().child("feed");
        mCurrentUser = mAuth.getCurrentUser();

        postMap = new HashMap<>();

        pager=view.findViewById(R.id.pager);
        upload=view.findViewById(R.id.upload);
        indicator=view.findViewById(R.id.indicator);
        indicator_holder=view.findViewById(R.id.indicator_holder);
        mEditText = view.findViewById(R.id.post_desc);
        add=view.findViewById(R.id.edit_add);
        delete=view.findViewById(R.id.edit_delete);
        crop=view.findViewById(R.id.edit_crop);
        edit_layout=view.findViewById(R.id.edit_layout);
        edit_layout.setVisibility(GONE);
        mDialog=new ProgressDialog(view.getContext());

        compressor=new Compressor(view.getContext())
                .setQuality(75)
                .setCompressFormat(Bitmap.CompressFormat.PNG)
                .setMaxHeight(350);

        mDialog = new ProgressDialog(view.getContext());
        indicator.setDotsClickable(true);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(imagesList.size()==7){
                    Toasty.info(view.getContext(),"Max limit reached",Toasty.LENGTH_SHORT,true).show();
                }else if(imagesList.size()<7){
                    isFirst=false;
                    startPickImage(7-imagesList.size());
                }

            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem();
            }
        });

        crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCropItem();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String entered_caption=mEditText.getText().toString();
                if(TextUtils.isEmpty(entered_caption)){

                    Toasty.error(view.getContext(),"Caption cannot be empty, please enter some text to continue",Toasty.LENGTH_SHORT,true).show();
                    return;

                }

                if(entered_caption.length()<=4){

                    Toasty.error(view.getContext(),"Caption cannot be too short",Toasty.LENGTH_SHORT,true).show();
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
                                uploadImages(0);
                            }
                        })
                        .show();

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Toast.makeText(view.getContext(), "Cropped", Toast.LENGTH_SHORT).show();
                long old_id=imagesList.get(selectedIndex).getId();
                String old_name=imagesList.get(selectedIndex).getName();
                String old_path=imagesList.get(selectedIndex).getOg_path();

                imagesList.remove(selectedIndex);
                imagesList.add(selectedIndex,new Images(old_name,old_path,result.getUri(),old_id));
                adapter=new PagerPhotosAdapter(view.getContext(),imagesList);
                pager.setAdapter(adapter);
                indicator.setViewPager(pager);
                adapter.notifyDataSetChanged();
                pager.setCurrentItem(selectedIndex,true);
                Toast.makeText(view.getContext(), "Cropped2", Toast.LENGTH_SHORT).show();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toasty.error(view.getContext(),"Error cropping image",Toasty.LENGTH_SHORT,true).show();
                Log.e(TAG,error.getMessage());
            }


        }

        if (requestCode == PICK_IMAGE ) {
            if (resultCode == RESULT_OK && data != null) {
                if(isFirst){
                    imagesList.clear();
                    edit_layout.setVisibility(View.VISIBLE);
                }

                List<Image> pickedImages=ImagePicker.getImages(data);

                for(Image image:pickedImages){
                    imagesList.add(new Images(image.getName(),image.getPath(),Uri.fromFile(new File(image.getPath())),image.getId()));
                }
                adapter=new PagerPhotosAdapter(view.getContext(),imagesList);
                pager.setAdapter(adapter);

                if(imagesList.size()>1){
                    indicator_holder.setVisibility(View.VISIBLE);
                    indicator.setViewPager(pager);
                }else{
                    indicator_holder.setVisibility(GONE);
                }

                adapter.notifyDataSetChanged();

            }else if(resultCode==RESULT_CANCELED){
                showFragment(new FeedView(),"feed");
            }
        }

    }

    void showFragment(Fragment fragment, String tag){

        ((AppCompatActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                .replace(R.id.container,fragment,tag)
                .commit();

        MainActivity.mCurrentFragment=tag;

    }


    private void startPickImage(int limit) {

        ImagePicker.create(this)
                .folderMode(true)
                .toolbarFolderTitle("Folders")
                .toolbarImageTitle("Tap to select")
                .includeVideo(false)
                .multi()
                .limit(limit)
                .showCamera(true)
                .enableLog(true)
                .imageDirectory("VeloxYSoundZ")
                .start(PICK_IMAGE);

    }

    public void deleteItem() {

        new DialogSheet(view.getContext())
                .setColoredNavigationBar(true)
                .setRoundedCorners(true)
                .setTitle("Remove")
                .setMessage("Are you sure do you want to remove this image?")
                .setPositiveButton("Yes", new DialogSheet.OnPositiveClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(imagesList.size()==1) {
                            showFragment(new FeedView(),"feed");
                            return;
                        }

                        imagesList.remove(pager.getCurrentItem());

                        adapter=new PagerPhotosAdapter(view.getContext(),imagesList);
                        pager.setAdapter(adapter);
                        indicator.setViewPager(pager);

                        if(imagesList.size()>1){
                            indicator_holder.setVisibility(View.VISIBLE);
                            indicator.setViewPager(pager);
                        }else{
                            indicator_holder.setVisibility(GONE);
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

    public void openCropItem() {

        selectedIndex=pager.getCurrentItem();

        Uri imageUri=Uri.fromFile(new File(imagesList.get(selectedIndex).getOg_path()));
        CropImage.activity(imageUri)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setActivityTitle("Crop Image")
                .setAllowRotation(true)
                .setAllowFlipping(true)
                .setAllowCounterRotation(true)
                .setAspectRatio(1,1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(view.getContext(),this);

    }

    List<String> uploadedImagesUrl=new ArrayList<>();
    private void uploadImages(final int index) {

        int img_count=index+1;
        mDialog.dismiss();
        mDialog.setMessage("Uploading "+img_count+"/"+imagesList.size()+" images...");
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        if(!mDialog.isShowing()){
            Toasty.info(view.getContext(), "Uploading " + img_count + "/" + imagesList.size() + " images...", Toasty.LENGTH_SHORT, true).show();
            mDialog.show();
        }

        final StorageReference fileToUpload=mStorage.child("images").child("POST_"+System.currentTimeMillis()+"_"+imagesList.get(index).getName());
        fileToUpload.putFile(imagesList.get(index).getUri())
                .addOnSuccessListener(taskSnapshot -> fileToUpload.getDownloadUrl()
                        .addOnSuccessListener(uri -> {

                            uploadedImagesUrl.add(uri.toString());
                            int next_index=index+1;
                            try {
                                if (!TextUtils.isEmpty(imagesList.get(index + 1).getOg_path())) {
                                    uploadImages(next_index);
                                } else {
                                    canUpload = true;
                                    mDialog.dismiss();
                                    uploadPost();
                                }
                            }catch (Exception e){
                                canUpload = true;
                                mDialog.dismiss();
                                uploadPost();
                            }

                        })
                        .addOnFailureListener(Throwable::printStackTrace))
                .addOnFailureListener(Throwable::printStackTrace);

    }

    private void uploadPost() {

        mDialog.setMessage("Posting...");
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);

        if(canUpload) {
            if (!uploadedImagesUrl.isEmpty()) {

                mDialog.show();

                mFirestore.collection("Users").document(mCurrentUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {

                    postMap.put("userId", mCurrentUser.getUid());
                    postMap.put("username", documentSnapshot.getString("name"));
                    postMap.put("userimage", documentSnapshot.getString("picture"));
                    postMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
                    postMap.put("image_count", uploadedImagesUrl.size());
                    try {
                        postMap.put("image_url_0", uploadedImagesUrl.get(0));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        postMap.put("image_url_1", uploadedImagesUrl.get(1));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        postMap.put("image_url_2", uploadedImagesUrl.get(2));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        postMap.put("image_url_3", uploadedImagesUrl.get(3));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        postMap.put("image_url_4", uploadedImagesUrl.get(4));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        postMap.put("image_url_5", uploadedImagesUrl.get(5));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        postMap.put("image_url_6", uploadedImagesUrl.get(6));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    postMap.put("likes", "0");
                    postMap.put("favourites", "0");
                    postMap.put("description", mEditText.getText().toString());
                    postMap.put("color", "0");

                    mFirestore.collection("feed")
                            .add(postMap)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    mDialog.dismiss();
                                    Toasty.success(view.getContext(), "Post uploaded", Toasty.LENGTH_SHORT,true).show();
                                    showFragment(new FeedView(),"feed");
                                }
                            })
                            .addOnFailureListener(e -> {
                                mDialog.dismiss();
                                Log.e("Error sending post", e.getMessage());
                            });


                }).addOnFailureListener(e -> {
                    mDialog.dismiss();
                    Log.e("Error getting user", e.getMessage());
                });

            } else {
                mDialog.dismiss();
                Toasty.info(view.getContext(), "No image has been uploaded, Please wait or try again", Toasty.LENGTH_SHORT,true).show();
            }
        }else{
            Toasty.info(view.getContext(), "Please wait, images are uploading...", Toasty.LENGTH_SHORT,true).show();
        }

    }

}
