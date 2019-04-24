package com.amsavarthan.ztunes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;


import com.marcoscg.dialogsheet.DialogSheet;

import java.util.ArrayList;
import java.util.List;

public class Recents_FullView extends Fragment {

    private List<RecentsEntity> recentsList=new ArrayList<>();
    View view;
    private RelativeLayout default_layout;
    private Recents_FullAdapter mRecentsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.fragment_recents_full_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recentsList.clear();

        RecentsViewModel recentsViewModel = ViewModelProviders.of(this).get(RecentsViewModel.class);
        recentsList = recentsViewModel.getAllRecents();

        default_layout=view.findViewById(R.id.default_layout);

        if(recentsViewModel.getRecentsCount()==0){
            Animation fade_in = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in);
            default_layout.startAnimation(fade_in);
            default_layout.setVisibility(View.VISIBLE);
        }

        RecyclerView mRecentsRecyclerview = view.findViewById(R.id.recyclerView);
        mRecentsAdapter = new Recents_FullAdapter(recentsList, view.getContext());
        mRecentsRecyclerview.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager=new GridLayoutManager(view.getContext(),2,RecyclerView.VERTICAL,false);
        layoutManager.setSmoothScrollbarEnabled(true);

        mRecentsRecyclerview.setLayoutManager(layoutManager);
        mRecentsRecyclerview.setHasFixedSize(true);
        mRecentsRecyclerview.setAdapter(mRecentsAdapter);

        onClearFabClicked();
    }

    public void onClearFabClicked() {

        view.findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new DialogSheet(view.getContext())
                        .setTitle("Clear Recents")
                        .setMessage("Are you sure do you want to clear all the recents?")
                        .setColoredNavigationBar(true)
                        .setRoundedCorners(true)
                        .setPositiveButton("Yes", new DialogSheet.OnPositiveClickListener() {
                            @Override
                            public void onClick(View v) {
                                RecentsViewModel viewModel= ViewModelProviders.of(Recents_FullView.this).get(RecentsViewModel.class);
                                for(RecentsEntity recentsEntity:viewModel.getAllRecents()){
                                    viewModel.deletePost(recentsEntity);
                                }
                                recentsList.clear();
                                mRecentsAdapter.notifyDataSetChanged();
                                HomeFragment.ShowDefaultCard(view.getContext());
                                mRecentsAdapter.notifyDataSetChanged();
                                getActivity().finish();
                                Toasty.success(view.getContext(),"Recents cleared",Toasty.LENGTH_SHORT,true).show();
                            }
                        })
                        .setNegativeButton("No", new DialogSheet.OnNegativeClickListener() {
                            @Override
                            public void onClick(View v) {
                            }
                        })
                        .show();


            }
        });
    }

}
