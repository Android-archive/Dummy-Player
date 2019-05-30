package com.amsavarthan.ztunes.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.amsavarthan.ztunes.ui.activities.MainActivity;
import com.amsavarthan.ztunes.R;
import com.amsavarthan.ztunes.room.RecentsViewModel;
import com.amsavarthan.ztunes.models.Songs;
import com.amsavarthan.ztunes.adapters.AlbumsAdapter;
import com.amsavarthan.ztunes.adapters.LibraryAdapter;
import com.amsavarthan.ztunes.models.Album;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ManageSongs extends Fragment {

    View view;
    RecyclerView mAlbumsRecyclerView,mSongsRecyclerView;
    List<Album> albums=new ArrayList<>();
    List<Songs> songs=new ArrayList<>();
    LibraryAdapter songsAdapter;
    AlbumsAdapter albumsAdapter;
    ImageView add_s,add_a;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private int count_a=0,count_s=0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.fragment_manage_songs_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAlbumsRecyclerView=view.findViewById(R.id.mAlbumsRecyclerView);
        mSongsRecyclerView=view.findViewById(R.id.mSongsRecyclerView);
        add_a=view.findViewById(R.id.add_album);
        add_s=view.findViewById(R.id.add_song);

        mFirestore=FirebaseFirestore.getInstance();
        mAuth= FirebaseAuth.getInstance();

        RecentsViewModel viewModel= ViewModelProviders.of(this).get(RecentsViewModel.class);
        songsAdapter=new LibraryAdapter(getActivity(),songs,view.getContext(),viewModel,true);
        albumsAdapter=new AlbumsAdapter(albums,view.getContext(),false,true,false);

        mAlbumsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager=new LinearLayoutManager(view.getContext());
        layoutManager.setSmoothScrollbarEnabled(true);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);

        mAlbumsRecyclerView.setLayoutManager(layoutManager);
        mAlbumsRecyclerView.setHasFixedSize(true);
        mAlbumsRecyclerView.setAdapter(albumsAdapter);

        mSongsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager1=new LinearLayoutManager(view.getContext());
        layoutManager1.setSmoothScrollbarEnabled(true);
        layoutManager1.setOrientation(RecyclerView.VERTICAL);

        mSongsRecyclerView.setLayoutManager(layoutManager1);
        mSongsRecyclerView.setHasFixedSize(true);
        mSongsRecyclerView.setAdapter(songsAdapter);

        getAlbums();
        getSongs();

        add_a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                        .replace(R.id.container,new AddAlbum(),"Add_Album")
                        .addToBackStack(null)
                        .commit();

                MainActivity.mCurrentFragment="add_album";
            }
        });

        add_s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                        .replace(R.id.container,new AddSong(),"Add_Song")
                        .addToBackStack(null)
                        .commit();

                MainActivity.mCurrentFragment="add_song";
            }
        });

    }

    private void getSongs(){

        songs.clear();

        mFirestore
                .collection("Users")
                .document(mAuth.getUid())
                .collection("songs")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Animation fade_out = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out);
                        Animation fade_in = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in);
                        if(queryDocumentSnapshots.getDocuments().isEmpty()){

                            view.findViewById(R.id.pbar2).setVisibility(View.GONE);
                            view.findViewById(R.id.pbar2).startAnimation(fade_out);

                            view.findViewById(R.id.s_default_layout).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.s_default_layout).startAnimation(fade_in);

                        }else {

                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                if (documentChange.getType() == DocumentChange.Type.ADDED) {

                                    view.findViewById(R.id.pbar2).setVisibility(View.GONE);
                                    view.findViewById(R.id.pbar2).startAnimation(fade_out);

                                    Songs song = documentChange.getDocument().toObject(Songs.class);
                                    songs.add(song);
                                    songsAdapter.notifyDataSetChanged();
                                    count_s++;
                                }
                            }

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @SuppressLint("CheckResult")
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });


    }

    private void getAlbums() {

        albums.clear();

        mFirestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("albums")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Animation fade_out = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out);
                        Animation fade_in = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in);
                        if(queryDocumentSnapshots.getDocuments().isEmpty()){

                            view.findViewById(R.id.pbar1).setVisibility(View.
                                    GONE);
                            view.findViewById(R.id.pbar1).startAnimation(fade_out);
                            view.findViewById(R.id.a_default_layout).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.a_default_layout).startAnimation(fade_in);

                        }else {

                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                if (documentChange.getType() == DocumentChange.Type.ADDED) {


                                    view.findViewById(R.id.pbar1).setVisibility(View.GONE);
                                    view.findViewById(R.id.pbar1).startAnimation(fade_out);

                                    Album song = documentChange.getDocument().toObject(Album.class);
                                    albums.add(song);
                                    count_a++;
                                    albumsAdapter.notifyDataSetChanged();

                                }
                            }

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @SuppressLint("CheckResult")
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });


    }


}
