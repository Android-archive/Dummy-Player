package com.amsavarthan.ztunes;

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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumsView extends Fragment {

    private List<Album> albums=new ArrayList<>();
    private AlbumsAdapter albumsAdapter;
    private RecyclerView mRecyclerView;
    int count;

    TextView title;
    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.fragment_albums_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        count=0;
        albums.clear();

        title=view.findViewById(R.id.count);
        albumsAdapter=new AlbumsAdapter(albums,view.getContext());

        mRecyclerView=view.findViewById(R.id.recyclerView);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager=new GridLayoutManager(view.getContext(),2,RecyclerView.VERTICAL,false);
        layoutManager.setSmoothScrollbarEnabled(true);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(albumsAdapter);

        title.setText(getString(R.string.please_wait));



        getItems();

    }

    private void getItems(){

        FirebaseFirestore.getInstance().collection("albums")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Animation fade_out = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out);
                        if(queryDocumentSnapshots.getDocuments().isEmpty()){

                            Album album=new Album("Default", "");
                            albums.add(album);
                            albumsAdapter.notifyDataSetChanged();
                            view.findViewById(R.id.pbar).setVisibility(View.GONE);
                            view.findViewById(R.id.pbar).startAnimation(fade_out);

                        }else {

                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                if (documentChange.getType() == DocumentChange.Type.ADDED) {


                                    view.findViewById(R.id.pbar).setVisibility(View.GONE);
                                    view.findViewById(R.id.pbar).startAnimation(fade_out);

                                    Album song = documentChange.getDocument().toObject(Album.class);
                                    albums.add(song);
                                    count++;
                                    albumsAdapter.notifyDataSetChanged();

                                }
                            }

                        }
                        title.setText(String.format("Total albums : %d", count));

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @SuppressLint("CheckResult")
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });


    }


}
