package com.amsavarthan.ztunes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.fragment_album_songs_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getArguments()!=null){
            album=getArguments().getString("name");
        }

        songsListbyGenre.clear();

        title=view.findViewById(R.id.album_name);
        RecentsViewModel viewModel= ViewModelProviders.of(this).get(RecentsViewModel.class);
        songsListbyGenreAdapter=new SongsAdapter(songsListbyGenre,view.getContext(),viewModel);

        mRecyclerView=view.findViewById(R.id.recyclerView);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager=new GridLayoutManager(view.getContext(),2,RecyclerView.VERTICAL,false);
        layoutManager.setSmoothScrollbarEnabled(true);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(songsListbyGenreAdapter);

        title.setText(album);

        getItems(album);

    }

    private void getItems(final String album){

        FirebaseFirestore.getInstance().collection("songs")
                .whereEqualTo("album",album)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.getDocuments().isEmpty()){

                            Songs song=new Songs("Default", "", "","","","yes", "");
                            songsListbyGenre.add(song);
                            songsListbyGenreAdapter.notifyDataSetChanged();

                        }else {

                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                if (documentChange.getType() == DocumentChange.Type.ADDED) {

                                    Animation fade_out = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out);

                                    view.findViewById(R.id.pbar).setVisibility(View.GONE);
                                    view.findViewById(R.id.pbar).startAnimation(fade_out);

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
                    }
                });


    }


}
