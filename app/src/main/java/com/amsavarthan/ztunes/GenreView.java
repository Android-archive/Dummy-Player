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

public class GenreView extends Fragment {

    private List<Songs> songsListbyGenre=new ArrayList<>();
    private SongsAdapter songsListbyGenreAdapter;
    private RecyclerView mRecyclerView;

    TextView title;
    String genre;
    View view;
    boolean empty;
    private SwipeRefreshLayout refreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.fragment_genre_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        songsListbyGenre.clear();

        if(getArguments()!=null){
            genre=getArguments().getString("name");
        }

        title=view.findViewById(R.id.genre_name);
        RecentsViewModel viewModel= ViewModelProviders.of(this).get(RecentsViewModel.class);
        songsListbyGenreAdapter=new SongsAdapter(songsListbyGenre,view.getContext(),viewModel,"null");

        mRecyclerView=view.findViewById(R.id.recyclerView);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager=new GridLayoutManager(view.getContext(),2,RecyclerView.VERTICAL,false);
        layoutManager.setSmoothScrollbarEnabled(true);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(songsListbyGenreAdapter);

        title.setText(genre);

        setUpRefreshLayout(genre);
        refreshLayout.setRefreshing(true);
        getItems(genre);

        if(empty){
            LinearLayoutManager layoutManager1=new GridLayoutManager(view.getContext(),1,RecyclerView.VERTICAL,false);
            layoutManager1.setSmoothScrollbarEnabled(true);

            mRecyclerView.setLayoutManager(layoutManager1);
        }

    }

    private void setUpRefreshLayout(final String genre) {

        refreshLayout=view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                songsListbyGenre.clear();
                songsListbyGenreAdapter.notifyDataSetChanged();
                getItems(genre);
            }
        });

    }

    private void getItems(final String genre){

        FirebaseFirestore.getInstance().collection("songs")
                .whereEqualTo("genre",genre)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.getDocuments().isEmpty()){

                            empty=true;
                            title.setText("No songs found");
                            refreshLayout.setRefreshing(false);

                            Animation fade_in = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in);
                            view.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.default_item).startAnimation(fade_in);

                            Animation fade_out = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out);


                        }else {

                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                if (documentChange.getType() == DocumentChange.Type.ADDED) {

                                    Animation fade_out = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out);

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
                        e.printStackTrace();
                        refreshLayout.setRefreshing(false);
                    }
                });


    }

}
