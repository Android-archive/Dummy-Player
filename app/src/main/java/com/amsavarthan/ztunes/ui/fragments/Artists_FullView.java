package com.amsavarthan.ztunes.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amsavarthan.ztunes.R;
import com.amsavarthan.ztunes.adapters.ArtistAdapter;
import com.amsavarthan.ztunes.models.Artist;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
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

public class Artists_FullView extends Fragment {

    private static final String TAG = Artists_FullView.class.getSimpleName();
    private List<Artist> artistList=new ArrayList<>();
    private ArtistAdapter artistAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout refreshLayout;
    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.fragment_artists_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        artistList.clear();
        artistAdapter=new ArtistAdapter(artistList,view.getContext(),true);

        mRecyclerView=view.findViewById(R.id.recyclerView);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager=new LinearLayoutManager(view.getContext());
        layoutManager.setSmoothScrollbarEnabled(true);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(artistAdapter);

        setUpRefreshLayout();
        refreshLayout.setRefreshing(true);
        getItems();

    }

    private void setUpRefreshLayout() {

        refreshLayout=view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                artistList.clear();
                artistAdapter.notifyDataSetChanged();
                getItems();
            }
        });

    }

    private void getItems() {

        view.findViewById(R.id.default_item).setVisibility(View.GONE);
        FirebaseFirestore.getInstance()
                .collection("Verified Artists")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                refreshLayout.setRefreshing(false);
                                view.findViewById(R.id.default_item).setVisibility(View.GONE);
                                Artist artist = documentChange.getDocument().toObject(Artist.class);
                                artistList.add(artist);
                                artistAdapter.notifyDataSetChanged();
                            }
                        }else{
                            refreshLayout.setRefreshing(false);
                            view.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        refreshLayout.setRefreshing(false);
                        Log.e(TAG,e.getMessage());
                        Toasty.error(view.getContext(),"Some technical error occured",Toasty.LENGTH_LONG,true).show();
                    }
                });

    }

}
