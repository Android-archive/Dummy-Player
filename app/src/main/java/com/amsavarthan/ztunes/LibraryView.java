package com.amsavarthan.ztunes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
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
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class LibraryView extends Fragment {

    private List<Songs> songsList=new ArrayList<>();
    private LibraryAdapter songsListsAdapter;


    TextView title;
    private int count;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.fragment_library_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        count=0;
        songsList.clear();

        title=view.findViewById(R.id.songs_count);
        RecentsViewModel viewModel= ViewModelProviders.of(this).get(RecentsViewModel.class);
        songsListsAdapter=new LibraryAdapter(songsList, view.getContext(),viewModel);

        RecyclerView mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager=new LinearLayoutManager(view.getContext());
        layoutManager.setSmoothScrollbarEnabled(true);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(songsListsAdapter);

        title.setText(R.string.please_wait);

        getItems();

    }

    private void getItems(){

        FirebaseFirestore.getInstance().collection("songs")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.getDocuments().isEmpty()){

                            Songs song=new Songs("Default", "", "","","","yes", "");
                            songsList.add(song);
                            songsListsAdapter.notifyDataSetChanged();

                        }else {

                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                if (documentChange.getType() == DocumentChange.Type.ADDED) {

                                    Animation fade_out = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out);

                                    view.findViewById(R.id.pbar).setVisibility(View.GONE);
                                    view.findViewById(R.id.pbar).startAnimation(fade_out);

                                    Songs song = documentChange.getDocument().toObject(Songs.class);
                                    songsList.add(song);
                                    songsListsAdapter.notifyDataSetChanged();
                                    count++;
                                }
                            }

                            Animation fade_in = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in);
                            title.setText(String.format( "Total songs : %s", count));
                            title.startAnimation(fade_in);

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
