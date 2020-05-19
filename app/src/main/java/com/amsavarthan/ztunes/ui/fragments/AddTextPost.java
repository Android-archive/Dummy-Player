package com.amsavarthan.ztunes.ui.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.amsavarthan.ztunes.ui.activities.MainActivity;
import com.amsavarthan.ztunes.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.marcoscg.dialogsheet.DialogSheet;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import es.dmoral.toasty.Toasty;
import me.grantland.widget.AutofitTextView;

public class AddTextPost extends Fragment {

    View view;
    FrameLayout mImageholder;
    String color;
    MaterialButton upload;
    TextInputEditText caption;
    AutofitTextView caption_preview;
    FloatingActionButton fab1,fab2,fab3,fab4,fab5,fab6,fab7,fab8,fab9,fab10,fab11,fab12,fab13,fab14,fab15,fab16,fab17,fab18,fab19;
    private FirebaseFirestore mFireStore;
    private FirebaseAuth mAuth;
    private ProgressDialog progressdialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view = inflater.inflate(R.layout.fragment_add_text_post_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth=FirebaseAuth.getInstance();
        mFireStore=FirebaseFirestore.getInstance();

        mImageholder=view.findViewById(R.id.image_holder);
        upload=view.findViewById(R.id.upload);
        caption=view.findViewById(R.id.textEditText);
        caption_preview=view.findViewById(R.id.caption_preview);
        caption.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                caption_preview.setText(s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        progressdialog=new ProgressDialog(view.getContext());
        progressdialog.setCanceledOnTouchOutside(false);
        progressdialog.setCancelable(false);
        progressdialog.setIndeterminate(true);
        progressdialog.setMessage("Uploading now....");

        fab1=view.findViewById(R.id.fab1);
        fab2=view.findViewById(R.id.fab2);
        fab3=view.findViewById(R.id.fab3);
        fab4=view.findViewById(R.id.fab4);
        fab5=view.findViewById(R.id.fab5);
        fab6=view.findViewById(R.id.fab6);
        fab7=view.findViewById(R.id.fab7);
        fab8=view.findViewById(R.id.fab8);
        fab9=view.findViewById(R.id.fab9);
        fab10=view.findViewById(R.id.fab10);
        fab11=view.findViewById(R.id.fab11);
        fab12=view.findViewById(R.id.fab12);
        fab13=view.findViewById(R.id.fab13);
        fab14=view.findViewById(R.id.fab14);
        fab15=view.findViewById(R.id.fab15);
        fab16=view.findViewById(R.id.fab16);
        fab17=view.findViewById(R.id.fab17);
        fab18=view.findViewById(R.id.fab18);
        fab19=view.findViewById(R.id.fab19);

        color="4";
        setClickListeners();

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String entered_caption=caption.getText().toString();
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
                                progressdialog.show();
                                uploadPost(entered_caption);
                            }
                        })
                .show();

            }
        });

    }

    private void setClickListeners() {

        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_1));
                color = "1";
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_2));
                color = "2";
            }
        });
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_3));
                color = "3";
            }
        });
        fab4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_4));
                color = "4";
            }
        });

        fab5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_5));
                color = "5";
            }
        });
        fab6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_6));
                color = "6";
            }
        });
        fab7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_7));
                color = "7";
            }
        });
        fab8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_8));
                color = "8";
            }
        });
        fab9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_9));
                color = "9";
            }
        });
        fab10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_10));
                color = "10";
            }
        });
        fab11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_11));
                color = "11";
            }
        });
        fab12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_12));
                color = "12";
            }
        });
        fab13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_13));
                color = "13";
            }
        });
        fab14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_14));
                color = "14";
            }
        });
        fab15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_15));
                color = "15";
            }
        });
        fab16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_16));
                color = "16";
            }
        });
        fab17.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_17));
                color = "17";
            }
        });
        fab18.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_18));
                color = "18";
            }
        });
        fab19.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_19));
                color = "19";
            }
        });

    }

    private void uploadPost(final String caption){

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

                        Map<String, Object> postMap = new HashMap<>();
                        postMap.put("userId", mAuth.getCurrentUser().getUid());
                        postMap.put("username", documentSnapshot.getString("name"));
                        postMap.put("userimage", documentSnapshot.getString("picture"));
                        postMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
                        postMap.put("image_count",0);
                        postMap.put("likes", "0");
                        postMap.put("favourites", "0");
                        postMap.put("description", caption);
                        postMap.put("color", color);

                        mFireStore.collection("feed")
                                .add(postMap)
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

    void showFragment(Fragment fragment,String tag){

        ((AppCompatActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_up,R.anim.fade_out)
                .replace(R.id.container,fragment,tag)
                .commit();

        MainActivity.mCurrentFragment=tag;

    }

}