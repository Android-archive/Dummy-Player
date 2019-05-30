package com.amsavarthan.ztunes.ui.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amsavarthan.ztunes.ui.activities.MainActivity;
import com.amsavarthan.ztunes.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class FriendProfileView extends Fragment {

    View view;
    CircleImageView imageView;
    TextView name,following,followers;
    String account_type,user_name,user_pic,user_uid;
    RelativeLayout songs_view,posts_view;
    MaterialButton follow;
    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;
    ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.fragment_friends_profile_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        clearLightStatusBar(getActivity());
        setLightStatusBar(getActivity());

        if(getArguments()==null){
            return;
        }

        progressDialog=new ProgressDialog(view.getContext());
        progressDialog.setMessage("Please wait...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        mFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();

        name=view.findViewById(R.id.name);
        imageView=view.findViewById(R.id.profilePic);
        posts_view=view.findViewById(R.id.posts_view);
        songs_view=view.findViewById(R.id.songs_view);
        follow=view.findViewById(R.id.action_button);
        following=view.findViewById(R.id.following_count);
        followers=view.findViewById(R.id.followers_count);

        account_type=getArguments().getString("type");
        user_uid=getArguments().getString("id");
        user_name=getArguments().getString("name");
        user_pic=getArguments().getString("picture");

        getFollowingAndFollowers();

        mFirestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("following")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        Animation animation=AnimationUtils.loadAnimation(view.getContext(),R.anim.expand_in);
                        animation.setDuration(200);

                        if(queryDocumentSnapshots.isEmpty()){
                            follow.setVisibility(View.VISIBLE);
                            follow.startAnimation(animation);
                            follow.setText("follow");
                            return;
                        }

                        for(DocumentChange documentChange:queryDocumentSnapshots.getDocumentChanges()){

                            follow.setVisibility(View.VISIBLE);
                            follow.startAnimation(animation);

                            if(documentChange.getDocument().getString("user_id").equals(user_uid)){

                                follow.setText("unfollow");

                            }else{

                                follow.setText("follow");

                            }

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toasty.error(view.getContext(),"Some technical error occurred",Toasty.LENGTH_SHORT,true).show();
                    }
                });

        if(TextUtils.equals(account_type,"artist")){

            songs_view.setVisibility(View.VISIBLE);

            songs_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Bundle bundle=new Bundle();
                    bundle.putString("name",user_name);
                    bundle.putString("photo",user_pic);

                    Fragment fragment=new Artist_SongsView();
                    fragment.setArguments(bundle);

                    ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                            .replace(R.id.container,fragment,"Artist_SongsView")
                            .addToBackStack(null)
                            .commit();

                    MainActivity.mCurrentFragment="artist_songs";

                }
            });

        }else{
            songs_view.setVisibility(View.GONE);
        }

        name.setText(user_name);
        Glide.with(view.getContext())
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_artist_art))
                .load(user_pic)
                .into(imageView);

        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String button_string=follow.getText().toString();

                if(TextUtils.equals(button_string,"follow")) {

                    progressDialog.show();
                    Map<String, Object> map = new HashMap<>();
                    map.put("user_id", user_uid);

                    mFirestore.collection("Users")
                            .document(mAuth.getCurrentUser().getUid())
                            .collection("following")
                            .add(map)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {

                                    Map<String, Object> map = new HashMap<>();
                                    map.put("user_id", mAuth.getCurrentUser().getUid());

                                    mFirestore.collection("Users")
                                            .document(user_uid)
                                            .collection("followers")
                                            .add(map)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    followers.setText(String.valueOf(Integer.valueOf(followers.getText().toString())+1));
                                                    follow.setText("unfollow");
                                                    progressDialog.dismiss();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    e.printStackTrace();
                                                    Toasty.error(view.getContext(),"Some technical error occurred",Toasty.LENGTH_SHORT,true).show();
                                                }
                                            });


                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    e.printStackTrace();
                                    Toasty.error(view.getContext(),"Some technical error occurred",Toasty.LENGTH_SHORT,true).show();
                                }
                            });

                }else{

                    progressDialog.show();
                    mFirestore.collection("Users")
                            .document(mAuth.getCurrentUser().getUid())
                            .collection("following")
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    for(DocumentChange documentChange:queryDocumentSnapshots.getDocumentChanges()){

                                        if(documentChange.getDocument().getString("user_id").equals(user_uid)){

                                            mFirestore.collection("Users")
                                                    .document(mAuth.getCurrentUser().getUid())
                                                    .collection("following")
                                                    .document(documentChange.getDocument().getId())
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            mFirestore.collection("Users")
                                                                    .document(user_uid)
                                                                    .collection("followers")
                                                                    .get()
                                                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                                            for(DocumentChange documentChange:queryDocumentSnapshots.getDocumentChanges()) {

                                                                                if (documentChange.getDocument().getString("user_id").equals(mAuth.getCurrentUser().getUid())) {

                                                                                    mFirestore.collection("Users")
                                                                                            .document(user_uid)
                                                                                            .collection("followers")
                                                                                            .document(documentChange.getDocument().getId())
                                                                                            .delete()
                                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                @Override
                                                                                                public void onSuccess(Void aVoid) {

                                                                                                    followers.setText(String.valueOf(Integer.valueOf(followers.getText().toString())-1));
                                                                                                    follow.setText("follow");
                                                                                                    progressDialog.dismiss();

                                                                                                }
                                                                                            })
                                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                                @Override
                                                                                                public void onFailure(@NonNull Exception e) {
                                                                                                    progressDialog.dismiss();
                                                                                                    e.printStackTrace();
                                                                                                    Toasty.error(view.getContext(),"Some technical error occurred",Toasty.LENGTH_SHORT,true).show();
                                                                                                }
                                                                                            });

                                                                                }
                                                                            }

                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            progressDialog.dismiss();
                                                                            e.printStackTrace();
                                                                            Toasty.error(view.getContext(),"Some technical error occurred",Toasty.LENGTH_SHORT,true).show();
                                                                        }
                                                                    });

                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            progressDialog.dismiss();
                                                            e.printStackTrace();
                                                            Toasty.error(view.getContext(),"Some technical error occurred",Toasty.LENGTH_SHORT,true).show();
                                                        }
                                                    });

                                        }

                                    }

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    e.printStackTrace();
                                    Toasty.error(view.getContext(),"Some technical error occurred",Toasty.LENGTH_SHORT,true).show();
                                }
                            });

                }
            }
        });

        posts_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle=new Bundle();
                bundle.putString("user_id",user_uid);

                Fragment fragment=new FriendPostsView();
                fragment.setArguments(bundle);

                ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                        .replace(R.id.container,fragment,"Friend_Posts")
                        .addToBackStack(null)
                        .commit();

                MainActivity.mCurrentFragment="friend_posts";

            }
        });

    }

    private void getFollowingAndFollowers() {

        mFirestore.collection("Users")
                .document(user_uid)
                .collection("following")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        Animation animation=AnimationUtils.loadAnimation(view.getContext(),R.anim.fade_in);
                        animation.setDuration(300);
                        following.setText(String.valueOf(queryDocumentSnapshots.size()));
                        following.startAnimation(animation);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toasty.error(view.getContext(),"Some technical error occurred",Toasty.LENGTH_SHORT,true).show();
                    }
                });


        mFirestore.collection("Users")
                .document(user_uid)
                .collection("followers")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        Animation animation=AnimationUtils.loadAnimation(view.getContext(),R.anim.fade_in);
                        animation.setDuration(300);
                        followers.setText(String.valueOf(queryDocumentSnapshots.size()));
                        followers.startAnimation(animation);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toasty.error(view.getContext(),"Some technical error occurred",Toasty.LENGTH_SHORT,true).show();
                    }
                });

    }

    @Override
    public void onDestroyView() {
        clearLightStatusBar(getActivity());
        super.onDestroyView();
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

}
