package com.amsavarthan.ztunes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Album_SongsView extends Fragment {

    private List<Songs> songsListbyGenre=new ArrayList<>();
    private SongsAdapter songsListbyGenreAdapter;
    private RecyclerView mRecyclerView;


    TextView title;
    String album;
    View view;
    private String type;
    private SwipeRefreshLayout refreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.fragment_album_songs_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getArguments()!=null){

            try {
                type = getArguments().getString("type");
            }catch (Exception e){
                type="null";
            }

            album=getArguments().getString("name");

        }

        songsListbyGenre.clear();

        title=view.findViewById(R.id.album_name);
        RecentsViewModel viewModel= ViewModelProviders.of(this).get(RecentsViewModel.class);

        songsListbyGenreAdapter=new SongsAdapter(songsListbyGenre,view.getContext(),viewModel,type);

        mRecyclerView=view.findViewById(R.id.recyclerView);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());


        LinearLayoutManager layoutManager=new GridLayoutManager(view.getContext(),2,RecyclerView.VERTICAL,false);
        layoutManager.setSmoothScrollbarEnabled(true);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(songsListbyGenreAdapter);

        title.setText(album);

        setUpRefreshLayout(album);
        refreshLayout.setRefreshing(true);
        getItems(album);

    }

    private void getItems(final String album){

        FirebaseFirestore.getInstance().collection("songs")
                .whereEqualTo("album",album)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Animation fade_out = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out);
                        Animation fade_in = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in);
                        if(queryDocumentSnapshots.getDocuments().isEmpty()){

                            view.findViewById(R.id.default_layout).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.default_layout).startAnimation(fade_in);
                            refreshLayout.setRefreshing(false);

                        }else {

                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                if (documentChange.getType() == DocumentChange.Type.ADDED) {

                                    refreshLayout.setRefreshing(false);
                                    Songs song = documentChange.getDocument().toObject(Songs.class);
                                    songsListbyGenre.add(song);
                                    songsListbyGenreAdapter.notifyDataSetChanged();

                                }
                            }

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @SuppressLint("CheckResult")
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Animation fade_out = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out);
                        refreshLayout.setRefreshing(false);
                        e.printStackTrace();
                    }
                });


    }

    private void setUpRefreshLayout(final String album) {

        refreshLayout=view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                songsListbyGenre.clear();
                songsListbyGenreAdapter.notifyDataSetChanged();
                getItems(album);
            }
        });

    }


}
