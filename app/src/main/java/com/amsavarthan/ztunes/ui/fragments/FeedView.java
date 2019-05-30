package com.amsavarthan.ztunes.ui.fragments;

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

import com.amsavarthan.ztunes.utils.NetworkUtil;
import com.amsavarthan.ztunes.R;
import com.amsavarthan.ztunes.adapters.FeedAdapter;
import com.amsavarthan.ztunes.models.Feed;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

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
        feedAdapter=new FeedAdapter(feedList,view.getContext(),getActivity());
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

                                if(followingUsersId.contains(documentChange.getDocument().getString("userId"))) {
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

                                if(documentChange.getDocument().getString("userId").equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

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
