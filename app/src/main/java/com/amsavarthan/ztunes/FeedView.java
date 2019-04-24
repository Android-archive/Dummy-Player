package com.amsavarthan.ztunes;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import es.dmoral.toasty.Toasty;

public class FeedView extends Fragment {


    View view;
    SwipeRefreshLayout refreshLayout;
    RecyclerView recyclerView;
    LinearLayout default_item;
    List<Feed> feedList=new ArrayList<>();
    FeedAdapter feedAdapter;
    FirebaseFirestore mFirestore;
    Animation fade_in,fade_out;
    MaterialButton add_post;
    List<String> followingUsersId=new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.fragment_feed_view,container,false);
    }

    private static void clearLightStatusBar(Activity activity){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            activity.getWindow().setStatusBarColor(Color.parseColor("#ffffff"));
        }
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        clearLightStatusBar(getActivity());
        mFirestore=FirebaseFirestore.getInstance();

        recyclerView=view.findViewById(R.id.recyclerView);
        default_item=view.findViewById(R.id.default_item);
        add_post=view.findViewById(R.id.add);
        fade_out = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out);
        fade_in = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in);

        add_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new PostTypeSelectView(),"postTypeSelect");
            }
        });

        refreshLayout=view.findViewById(R.id.refreshLayout);
        feedAdapter=new FeedAdapter(feedList,view.getContext());
        refreshLayout.setRefreshing(true);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(NetworkUtil.isOnline(view.getContext())){
                    feedList.clear();
                    feedAdapter.notifyDataSetChanged();
                    default_item.startAnimation(fade_out);
                    default_item.setVisibility(View.GONE);
                    getFollowingUsers();
                    //addAPost();
                }else{
                    refreshLayout.setRefreshing(false);
                    Toasty.error(view.getContext(),"No internet connection",Toasty.LENGTH_SHORT,true).show();
                }
            }
        });

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager=new LinearLayoutManager(view.getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.setSmoothScrollbarEnabled(true);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(feedAdapter);

        if(NetworkUtil.isOnline(view.getContext())){
            //addSampleFeed();
            getFollowingUsers();
        }else{
            default_item.startAnimation(fade_in);
            default_item.setVisibility(View.VISIBLE);
            Toasty.error(view.getContext(),"No internet connection",Toasty.LENGTH_SHORT,true).show();
        }

    }

    void showFragment(Fragment fragment,String tag){

        ((AppCompatActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                .replace(R.id.container,fragment,tag)
                .addToBackStack(null)
                .commit();

    }


    private void addAPost(){

        Random random=new Random();
        int n=random.nextInt(4)+1;

        Feed feed;
        switch (n){

            case 1:
                feed=new Feed("me6dnN9blZhl7ywj7ppakj3oNNu2"
                        ,"Amsavarthan"
                        ,"https://firebasestorage.googleapis.com/v0/b/project-ztunes.appspot.com/o/profile_pictures%2Fusers%2FYZ528EutCQfgppWt4k52J7ZSwD53.png?alt=media&token=cdaa76d6-dc9a-4def-b59d-a202f663a4c8"
                        ,"1554645153703"
                        ,""
                        ,"https://firebasestorage.googleapis.com/v0/b/project-ztunes.appspot.com/o/album_art%2FGitHub.png?alt=media&token=a71c85cc-30be-4a2c-ab31-6f348ad13012"
                        ,"The Open Source Guy"
                        ,"200"
                        ,"6000"
                        ,"2"
                        ,"image"
                );
                feedList.add(0,feed);
                refreshLayout.setRefreshing(false);
                feedAdapter.notifyDataSetChanged();
                return;

            case 2:
                feed=new Feed("me6dnN9blZhl7ywj7ppakj3oNNu2"
                        ,"Zenem"
                        ,"default"
                        ,"1553846969990"
                        ,""
                        ,""
                        ,"Prototype soon !"
                        ,"100"
                        ,"200"
                        , "3"
                        , "text"
                );
                feedList.add(0,feed);
                refreshLayout.setRefreshing(false);
                feedAdapter.notifyDataSetChanged();
                return;

            case 3:
                feed=new Feed("me6dnN9blZhl7ywj7ppakj3oNNu2"
                        ,"Zenem"
                        ,"default"
                        ,"1553685090449"
                        ,""
                        ,"https://firebasestorage.googleapis.com/v0/b/project-ztunes.appspot.com/o/album_art%2FAvengers.png?alt=media&token=a4eee71b-fe66-4188-b801-dbacf0676678"
                        ,"What ever it takes"
                        ,"200"
                        ,"5000"
                        , "2"
                        , "image"
                );
                feedList.add(0,feed);
                refreshLayout.setRefreshing(false);
                feedAdapter.notifyDataSetChanged();
                return;

            case 4:
                feed=new Feed("me6dnN9blZhl7ywj7ppakj3oNNu2"
                        ,"Amsavarthan"
                        ,"https://firebasestorage.googleapis.com/v0/b/project-ztunes.appspot.com/o/profile_pictures%2Fusers%2FYZ528EutCQfgppWt4k52J7ZSwD53.png?alt=media&token=cdaa76d6-dc9a-4def-b59d-a202f663a4c8"
                        ,"1554645153703"
                        ,""
                        ,"https://firebasestorage.googleapis.com/v0/b/project-ztunes.appspot.com/o/album_art%2FGitHub.png?alt=media&token=a71c85cc-30be-4a2c-ab31-6f348ad13012"
                        ,"The Open Source Guy"
                        ,"200"
                        ,"6000"
                        , "2"
                        , "image"
                );
                feedList.add(0,feed);
                refreshLayout.setRefreshing(false);
                feedAdapter.notifyDataSetChanged();
                return;

            case 5:

                Random random1=new Random();
                int n1=random1.nextInt(18)+1;
                feed=new Feed("me6dnN9blZhl7ywj7ppakj3oNNu2"
                        ,"Amsavarthan"
                        ,"https://firebasestorage.googleapis.com/v0/b/project-ztunes.appspot.com/o/profile_pictures%2Fusers%2FYZ528EutCQfgppWt4k52J7ZSwD53.png?alt=media&token=cdaa76d6-dc9a-4def-b59d-a202f663a4c8"
                        ,"1554645153703"
                        ,""
                        ,""
                        ,"New post in here"
                        ,"200"
                        ,"6000"
                        ,String.valueOf(n1)
                        ,"text"
                );
                feedList.add(0,feed);
                refreshLayout.setRefreshing(false);
                feedAdapter.notifyDataSetChanged();
                return;


        }

    }

    private void addSampleFeed(){

        Feed feed=new Feed("me6dnN9blZhl7ywj7ppakj3oNNu2"
                ,"Amsavarthan"
                ,"https://firebasestorage.googleapis.com/v0/b/project-ztunes.appspot.com/o/profile_pictures%2Fusers%2FYZ528EutCQfgppWt4k52J7ZSwD53.png?alt=media&token=cdaa76d6-dc9a-4def-b59d-a202f663a4c8"
                ,"1554645153703"
                ,""
                ,"https://firebasestorage.googleapis.com/v0/b/project-ztunes.appspot.com/o/album_art%2FGitHub.png?alt=media&token=a71c85cc-30be-4a2c-ab31-6f348ad13012"
                ,"The Open Source Guy"
                ,"200"
                ,"6000"
                , "2"
                , "image"
        );
        feedList.add(feed);

        feed=new Feed("me6dnN9blZhl7ywj7ppakj3oNNu2"
                ,"Zenem"
                ,"default"
                ,"1553685090449"
                ,""
                ,"https://firebasestorage.googleapis.com/v0/b/project-ztunes.appspot.com/o/album_art%2FAvengers.png?alt=media&token=a4eee71b-fe66-4188-b801-dbacf0676678"
                ,"What ever it takes"
                ,"200"
                ,"5000"
                , "2"
                , "image"
        );
        feedList.add(feed);

        feed=new Feed("me6dnN9blZhl7ywj7ppakj3oNNu2"
                ,"Amsavarthan"
                ,"https://firebasestorage.googleapis.com/v0/b/project-ztunes.appspot.com/o/profile_pictures%2Fusers%2FYZ528EutCQfgppWt4k52J7ZSwD53.png?alt=media&token=cdaa76d6-dc9a-4def-b59d-a202f663a4c8"
                ,"1554645153703"
                ,""
                ,""
                ,"The Open Source Guy"
                ,"200"
                ,"6000"
                , "4"
                , "text"
        );
        feedList.add(feed);


        feed=new Feed("me6dnN9blZhl7ywj7ppakj3oNNu2"
                ,"Zenem"
                ,"default"
                ,"1553846969990"
                ,""
                ,""
                ,"Prototype soon !"
                ,"100"
                ,"200"
                , "3"
                , "text"
        );
        feedList.add(feed);

        refreshLayout.setRefreshing(false);
        feedAdapter.notifyDataSetChanged();

    }

    private void getPosts() {

        mFirestore.collection("feed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(queryDocumentSnapshots.isEmpty()){

                            default_item.startAnimation(fade_in);
                            default_item.setVisibility(View.VISIBLE);
                            refreshLayout.setRefreshing(false);

                        }else{

                            for(DocumentChange documentChange:queryDocumentSnapshots.getDocumentChanges()){

                                if(followingUsersId.contains(documentChange.getDocument().getString("user_id"))) {
                                    default_item.startAnimation(fade_out);
                                    default_item.setVisibility(View.GONE);
                                    Feed feed = documentChange.getDocument().toObject(Feed.class).withId(documentChange.getDocument().getId());
                                    feedList.add(feed);
                                    feedAdapter.notifyDataSetChanged();
                                    refreshLayout.setRefreshing(false);
                                }

                            }

                            if(feedList.isEmpty()){

                                getCurrentUserPosts();

                            }

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        default_item.startAnimation(fade_out);
                        default_item.setVisibility(View.GONE);
                        refreshLayout.setRefreshing(false);
                        Toasty.error(view.getContext(),"Error getting posts: "+e.getMessage(),Toasty.LENGTH_SHORT,true).show();
                    }
                });


    }

    private void getFollowingUsers(){

        followingUsersId.clear();
        mFirestore.collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("following")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(queryDocumentSnapshots.isEmpty()) {
                            getCurrentUserPosts();
                            return;
                        }

                        for(DocumentChange documentChange:queryDocumentSnapshots.getDocumentChanges()){
                            followingUsersId.add(documentChange.getDocument().getString("user_id"));
                            if(followingUsersId.size()==queryDocumentSnapshots.size()){
                                Toasty.info(view.getContext(),"Getting following users posts...",Toasty.LENGTH_SHORT,true).show();
                                getPosts();
                            }
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toasty.error(view.getContext(),"Sorry some technical error occurred",Toasty.LENGTH_SHORT,true).show();

                    }
                });

    }

    private void getCurrentUserPosts() {

        Toasty.info(view.getContext(),"Getting current user's posts...",Toasty.LENGTH_SHORT,true).show();
        mFirestore.collection("feed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(queryDocumentSnapshots.isEmpty()){

                            default_item.startAnimation(fade_in);
                            default_item.setVisibility(View.VISIBLE);
                            refreshLayout.setRefreshing(false);

                        }else{

                            for(DocumentChange documentChange:queryDocumentSnapshots.getDocumentChanges()){

                                if(documentChange.getDocument().getString("user_id").equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                                    default_item.startAnimation(fade_out);
                                    default_item.setVisibility(View.GONE);
                                    Feed feed = documentChange.getDocument().toObject(Feed.class).withId(documentChange.getDocument().getId());
                                    feedList.add(feed);
                                    feedAdapter.notifyDataSetChanged();
                                    refreshLayout.setRefreshing(false);

                                }

                            }

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        default_item.startAnimation(fade_out);
                        default_item.setVisibility(View.GONE);
                        refreshLayout.setRefreshing(false);
                        Toasty.error(view.getContext(),"Error getting posts: "+e.getMessage(),Toasty.LENGTH_SHORT,true).show();
                    }
                });

    }

}
