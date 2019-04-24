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

public class NewRelease_FullView extends Fragment {

    private RecyclerView mNewReleaseRecyclerView;
    private List<Songs> newReleaseList=new ArrayList<>();
    private NewReleaseAdapter mNewReleaseAdapter;
    private TextView title;
    private int count;
    private View view;
    private SwipeRefreshLayout refreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.fragment_new_full_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        newReleaseList.clear();
        count=0;

        title=view.findViewById(R.id.total);

        RecentsViewModel recentsViewModel = ViewModelProviders.of(this).get(RecentsViewModel.class);

        mNewReleaseRecyclerView=view.findViewById(R.id.recyclerView);
        mNewReleaseAdapter=new NewReleaseAdapter(newReleaseList,view.getContext(),"full",recentsViewModel);
        mNewReleaseRecyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager=new GridLayoutManager(view.getContext(),2,RecyclerView.VERTICAL,false);
        layoutManager.setSmoothScrollbarEnabled(true);

        mNewReleaseRecyclerView.setLayoutManager(layoutManager);
        mNewReleaseRecyclerView.setHasFixedSize(true);
        mNewReleaseRecyclerView.setAdapter(mNewReleaseAdapter);

        title.setText(R.string.please_wait);
        setUpRefreshLayout();
        refreshLayout.setRefreshing(true);
        addItems();

    }

    private void addItems() {

        FirebaseFirestore.getInstance().collection("songs")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentChange documentChange:queryDocumentSnapshots.getDocumentChanges()){
                            if(documentChange.getType()== DocumentChange.Type.ADDED){

                                Animation fade_in = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in);

                                refreshLayout.setRefreshing(false);

                                if(documentChange.getDocument().getString("new_release").equals("yes")) {
                                    Songs songs=documentChange.getDocument().toObject(Songs.class);
                                    newReleaseList.add(songs);
                                    count++;
                                }else{
                                    Songs newRelease=new Songs("Default", "", "","","","yes", "", "");
                                    newReleaseList.add(newRelease);
                                }

                                title.setText(String.format( "Total songs : %s", count));
                                title.startAnimation(fade_in);

                                mNewReleaseAdapter.notifyDataSetChanged();

                            }
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        refreshLayout.setRefreshing(false);
                        e.printStackTrace();
                    }
                });


    }

    private void setUpRefreshLayout() {

        refreshLayout=view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                count=0;
                title.setText(R.string.please_wait);
                refreshLayout.setRefreshing(true);
                newReleaseList.clear();
                mNewReleaseAdapter.notifyDataSetChanged();
                addItems();
            }
        });

    }

}
