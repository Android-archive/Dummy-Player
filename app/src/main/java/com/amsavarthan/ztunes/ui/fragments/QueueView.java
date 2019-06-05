package com.amsavarthan.ztunes.ui.fragments;

import android.annotation.SuppressLint;
import android.media.MediaDataSource;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amsavarthan.ztunes.R;
import com.amsavarthan.ztunes.adapters.QueueAdapter;
import com.amsavarthan.ztunes.adapters.SongsAdapter;
import com.amsavarthan.ztunes.models.Songs;
import com.amsavarthan.ztunes.music.MediaMetaData;
import com.amsavarthan.ztunes.room.RecentsViewModel;
import com.amsavarthan.ztunes.ui.activities.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class QueueView extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_queue_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView=view.findViewById(R.id.recyclerView);
        RecentsViewModel viewModel=ViewModelProviders.of(this).get(RecentsViewModel.class);
        List<MediaMetaData> queue=getArguments().getParcelableArrayList("queue");

        QueueAdapter queueAdapter=new QueueAdapter(getContext(),queue,viewModel);

        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
        layoutManager.setSmoothScrollbarEnabled(true);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(queueAdapter);
        queueAdapter.setItemListener(media -> {
            ((MainActivity)getActivity()).playSong(media);
        });

    }

}
