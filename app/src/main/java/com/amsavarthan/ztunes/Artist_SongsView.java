package com.amsavarthan.ztunes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import android.annotation.SuppressLint;
import android.content.Context;
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

public class Artist_SongsView extends Fragment {

    private List<Songs> songsListbyArtist=new ArrayList<>();
    private SongsAdapter songsListbyArtistAdapter;

    TextView title;
    String artist;
    String[] artists;
    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.frag_artist_songs_view,container,false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        songsListbyArtist.clear();

        if(getArguments()!=null){
            artist=getArguments().getString("name");
        }

        title=view.findViewById(R.id.artist_name);
        RecentsViewModel viewModel= ViewModelProviders.of(this).get(RecentsViewModel.class);
        songsListbyArtistAdapter=new SongsAdapter(songsListbyArtist,view.getContext(),viewModel);

        RecyclerView mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager=new GridLayoutManager(view.getContext(),2,RecyclerView.VERTICAL,false);
        layoutManager.setSmoothScrollbarEnabled(true);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(songsListbyArtistAdapter);

        title.setText(artist);

        try {
            artists = artist.split(",");
        }catch (Exception e){
            e.printStackTrace();
        }


        getItems(artist);


    }

    private void getItems(final String artist){

        FirebaseFirestore.getInstance().collection("songs")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                            if (documentChange.getType() == DocumentChange.Type.ADDED) {

                                if(documentChange.getDocument().getString("artist").contains(artist)) {
                                    Animation fade_out = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out);

                                    view.findViewById(R.id.pbar).setVisibility(View.GONE);
                                    view.findViewById(R.id.pbar).startAnimation(fade_out);

                                    Songs song = documentChange.getDocument().toObject(Songs.class);
                                    songsListbyArtist.add(song);
                                    songsListbyArtistAdapter.notifyDataSetChanged();
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
