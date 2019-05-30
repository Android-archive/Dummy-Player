package com.amsavarthan.ztunes.ui.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.amsavarthan.ztunes.ui.activities.MainActivity;
import com.amsavarthan.ztunes.R;
import com.amsavarthan.ztunes.adapters.AlbumsAdapter;
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

public class SelectAlbum extends Fragment {

    private List<Album> albums=new ArrayList<>();
    private AlbumsAdapter albumsAdapter;
    private RecyclerView mRecyclerView;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.fragment_select_album_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        albumsAdapter=new AlbumsAdapter(albums,view.getContext(),true,false,true);

        mRecyclerView=view.findViewById(R.id.recyclerView);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager=new LinearLayoutManager(view.getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.setSmoothScrollbarEnabled(true);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(albumsAdapter);

        view.findViewById(R.id.add_album).setOnClickListener(new View.OnClickListener() {
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


        getAlbums();


    }

    private void getAlbums() {

        albums.clear();

        FirebaseFirestore.getInstance().collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("albums")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Animation fade_out = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out);
                        Animation fade_in = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in);
                        if(queryDocumentSnapshots.getDocuments().isEmpty()){

                            view.findViewById(R.id.pbar).setVisibility(View.
                                    GONE);
                            view.findViewById(R.id.pbar).startAnimation(fade_out);
                            view.findViewById(R.id.a_default_layout).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.a_default_layout).startAnimation(fade_in);

                        }else {

                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                if (documentChange.getType() == DocumentChange.Type.ADDED) {


                                    view.findViewById(R.id.pbar).setVisibility(View.GONE);
                                    view.findViewById(R.id.pbar).startAnimation(fade_out);

                                    Album song = documentChange.getDocument().toObject(Album.class);
                                    albums.add(song);
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
