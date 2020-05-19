package com.amsavarthan.ztunes.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.amsavarthan.ztunes.ui.activities.MainActivity;
import com.amsavarthan.ztunes.utils.NetworkUtil;
import com.amsavarthan.ztunes.R;
import com.amsavarthan.ztunes.adapters.FeedAdapter;
import com.amsavarthan.ztunes.models.Feed;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import es.dmoral.toasty.Toasty;

public class FriendPostsView extends Fragment {

    View view;
    SwipeRefreshLayout refreshLayout;
    RecyclerView recyclerView;
    LinearLayout default_item;
    List<Feed> feedList=new ArrayList<>();
    FeedAdapter feedAdapter;
    FirebaseFirestore mFirestore;
    Animation fade_in,fade_out;
    private String user_id;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.fragment_friend_posts_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity.setStatusLightTheme(view);
        user_id=getArguments().getString("user_id");

        mFirestore= FirebaseFirestore.getInstance();
        recyclerView=view.findViewById(R.id.recyclerView);
        default_item=view.findViewById(R.id.default_item);
        refreshLayout=view.findViewById(R.id.refreshLayout);

        fade_out = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out);
        fade_in = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in);

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
                    getPosts();
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
            getPosts();
        }else{
            default_item.startAnimation(fade_in);
            default_item.setVisibility(View.VISIBLE);
            Toasty.error(view.getContext(),"No internet connection",Toasty.LENGTH_SHORT,true).show();
        }

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

                                if(documentChange.getDocument().getString("userId").equals(user_id)) {
                                    default_item.startAnimation(fade_out);
                                    default_item.setVisibility(View.GONE);
                                    Feed feed = documentChange.getDocument().toObject(Feed.class).withId(documentChange.getDocument().getId());
                                    feedList.add(feed);
                                    feedAdapter.notifyDataSetChanged();
                                    refreshLayout.setRefreshing(false);
                                }

                            }

                            if(feedList.isEmpty()){
                                default_item.startAnimation(fade_in);
                                default_item.setVisibility(View.VISIBLE);
                                refreshLayout.setRefreshing(false);
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
