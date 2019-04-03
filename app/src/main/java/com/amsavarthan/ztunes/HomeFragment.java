package com.amsavarthan.ztunes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.marcoscg.dialogsheet.DialogSheet;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.amsavarthan.ztunes.NetworkUtil.NETWORK_STATUS_NOT_CONNECTED;

public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.getSimpleName() ;
    private int count;
    private static RecentsViewModel recentsViewModel;
    private static RelativeLayout default_layout;

    private RecyclerView mNewReleaseRecyclerView,mRecentsRecyclerView,mArtistsRecyclerView;
    private List<Songs> newReleaseList=new ArrayList<>();
    private List<RecentsEntity> recentsList=new ArrayList<>();
    private List<Artist> artistsList=new ArrayList<>();
    private NewReleaseAdapter mNewReleaseAdapter;
    private RecentsAdapter mRecentsAdapter;
    private ArtistAdapter mArtistsAdapter;
    private TextView view_all;
    private static TextView clear_all_recents;
    private FirebaseFirestore mFirestore;
    private ProgressBar pbar1;
    private View view;
    private Chip chip1,chip2,chip3,chip4,chip5,chip6,chip7;
    private ImageView logo;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.fragment_home,container,false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity.mCurrentFragment="home";
        mNewReleaseRecyclerView=view.findViewById(R.id.mNewReleasesRecyclerView);
        mRecentsRecyclerView=view.findViewById(R.id.mRecentsRecyclerView);
        mArtistsRecyclerView=view.findViewById(R.id.mArtistsRecyclerView);
        logo=view.findViewById(R.id.logo);

        newReleaseList.clear();
        recentsList.clear();
        artistsList.clear();

        view_all=view.findViewById(R.id.view_all);
        clear_all_recents=view.findViewById(R.id.clear_all_recents);

        chip1=view.findViewById(R.id.c1);
        chip2=view.findViewById(R.id.c2);
        chip3=view.findViewById(R.id.c3);
        chip4=view.findViewById(R.id.c4);
        chip5=view.findViewById(R.id.c5);
        chip6=view.findViewById(R.id.c6);
        chip7=view.findViewById(R.id.c7);

        pbar1=view.findViewById(R.id.pbar1);
        default_layout=view.findViewById(R.id.default_layout);
        recentsViewModel = ViewModelProviders.of(this).get(RecentsViewModel.class);

        if(recentsViewModel.getAllRecents().isEmpty()) {
            Animation fade_in = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in);
            default_layout.startAnimation(fade_in);
            default_layout.setVisibility(View.VISIBLE);
        }

        mFirestore=FirebaseFirestore.getInstance();
        mAuth= FirebaseAuth.getInstance();

        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logo.startAnimation(AnimationUtils.loadAnimation(view.getContext(),R.anim.expand_in));
            }
        });

        mNewReleaseAdapter=new NewReleaseAdapter(newReleaseList,view.getContext(),"normal",recentsViewModel);
        mNewReleaseRecyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager=new LinearLayoutManager(view.getContext());
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        layoutManager.setSmoothScrollbarEnabled(true);

        mNewReleaseRecyclerView.setLayoutManager(layoutManager);
        mNewReleaseRecyclerView.setHasFixedSize(true);
        mNewReleaseRecyclerView.setAdapter(mNewReleaseAdapter);

        mArtistsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mArtistsAdapter=new ArtistAdapter(artistsList,view.getContext());

        LinearLayoutManager layoutManager1=new LinearLayoutManager(view.getContext());
        layoutManager1.setOrientation(RecyclerView.HORIZONTAL);
        layoutManager1.setSmoothScrollbarEnabled(true);

        mArtistsRecyclerView.setLayoutManager(layoutManager1);
        mArtistsRecyclerView.setHasFixedSize(true);
        mArtistsRecyclerView.setAdapter(mArtistsAdapter);

        mRecentsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecentsAdapter=new RecentsAdapter(recentsList,view.getContext());

        LinearLayoutManager layoutManager2=new LinearLayoutManager(view.getContext());
        layoutManager2.setOrientation(RecyclerView.VERTICAL);
        layoutManager2.setSmoothScrollbarEnabled(true);

        mRecentsRecyclerView.setLayoutManager(layoutManager2);
        mRecentsRecyclerView.setHasFixedSize(true);
        mRecentsRecyclerView.setAdapter(mRecentsAdapter);

        clear_all_recents.setOnClickListener(new View.OnClickListener() {
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
                                RecentsViewModel viewModel= ViewModelProviders.of(HomeFragment.this).get(RecentsViewModel.class);
                                for(RecentsEntity recentsEntity:viewModel.getAllRecents()){
                                    viewModel.deletePost(recentsEntity);
                                }
                                ShowDefaultCard(view.getContext());
                                recentsList.clear();
                                mRecentsAdapter.setData(null);
                                mRecentsAdapter.notifyDataSetChanged();
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

        onGenreClick();
        onViewAllClicked();
        getNewReleases();
        addArtists();
        getRecents();

    }

    private void addArtists() {

        FirebaseFirestore.getInstance()
                .collection("Verified Artists")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for(DocumentChange documentChange:queryDocumentSnapshots.getDocumentChanges()){

                            if(documentChange.getType()== DocumentChange.Type.ADDED){

                                Artist artist=documentChange.getDocument().toObject(Artist.class);
                                artistsList.add(artist);
                                mArtistsAdapter.notifyDataSetChanged();

                            }

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,e.getMessage());
                        Toasty.error(view.getContext(),"Some technical error occured",Toasty.LENGTH_LONG,true).show();
                    }
                });

    }

    private void getNewReleases(){

        mFirestore.collection("songs")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        count=queryDocumentSnapshots.getDocuments().size();

                        for(DocumentChange documentChange:queryDocumentSnapshots.getDocumentChanges()){
                            if(documentChange.getType()== DocumentChange.Type.ADDED){

                                Animation fade_out = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out);
                                Animation fade_in = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in);

                                pbar1.setVisibility(View.GONE);
                                pbar1.startAnimation(fade_out);

                                if(documentChange.getDocument().getString("new_release").equals("yes")) {

                                    if(newReleaseList.size()<=3) {

                                        Songs songs = documentChange.getDocument().toObject(Songs.class);
                                        newReleaseList.add(songs);
                                        view_all.setVisibility(View.VISIBLE);
                                        view_all.startAnimation(fade_in);

                                    }else if(newReleaseList.size()==4){

                                        Songs songs = new Songs("more","","","","",String.valueOf(count), "");
                                        newReleaseList.add(songs);

                                    }

                                }else{
                                    Songs newRelease=new Songs("Default", "", "","","","yes", "");
                                    newReleaseList.add(newRelease);
                                    view_all.setVisibility(View.GONE);
                                    view_all.startAnimation(fade_out);
                                }
                                mNewReleaseAdapter.notifyDataSetChanged();

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

    public static void HideDefaultCard(Context context){

        if(default_layout.getVisibility()==View.VISIBLE){
            Animation fade_out = AnimationUtils.loadAnimation(context, R.anim.fade_out);
            default_layout.startAnimation(fade_out);
            default_layout.setVisibility(View.GONE);
        }

        if(recentsViewModel.getRecentsCount()>0){
            if(clear_all_recents.getVisibility()!=View.VISIBLE){
                clear_all_recents.setVisibility(View.VISIBLE);
            }
        }
    }

    public static void ShowDefaultCard(Context context){

        if(default_layout.getVisibility()!=View.VISIBLE){
            Animation fade_in = AnimationUtils.loadAnimation(context, R.anim.fade_in);
            default_layout.startAnimation(fade_in);
            default_layout.setVisibility(View.VISIBLE);
        }

        clear_all_recents.setVisibility(View.GONE);

    }

    private void getRecents() {

        List<RecentsEntity> recents=recentsViewModel.getAllRecents();

        if(recentsViewModel.getRecentsCount()>0){
            if(clear_all_recents.getVisibility()!=View.VISIBLE){
                clear_all_recents.setVisibility(View.VISIBLE);
            }

        }

        if(recentsViewModel.getRecentsCount()>0){

            HideDefaultCard(view.getContext());
            if(recentsViewModel.getRecentsCount() <=6){

                for(int i=0;i<=recentsViewModel.getRecentsCount();i++){

                    try {
                        RecentsEntity recentsEntity = new RecentsEntity(
                                recents.get(i).getId()
                                , recents.get(i).getName()
                                , recents.get(i).getAlbum()
                                , recents.get(i).getArtist()
                                , recents.get(i).getLink()
                                , recents.get(i).getGenre()
                                ,recents.get(i).getArt());

                        recentsList.add(recentsEntity);
                        mRecentsAdapter.notifyDataSetChanged();
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                }

            }else{

                for(int i=0;i<=6;i++){

                    try {
                        RecentsEntity recentsEntity = new RecentsEntity(
                                recents.get(i).getId()
                                , recents.get(i).getName()
                                , recents.get(i).getAlbum()
                                , recents.get(i).getArtist()
                                , recents.get(i).getLink()
                                , recents.get(i).getGenre()
                                , recents.get(i).getArt());

                        recentsList.add(recentsEntity);
                        mRecentsAdapter.notifyDataSetChanged();
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                }

            }

        }

        recentsViewModel.findAllRecents().observe(this, new Observer<List<RecentsEntity>>() {
            @Override
            public void onChanged(List<RecentsEntity> recentsList) {
                if(recentsViewModel.getRecentsCount()>0 && recentsViewModel.getRecentsCount()<=6){
                    if(clear_all_recents.getVisibility()!=View.VISIBLE){
                        clear_all_recents.setVisibility(View.VISIBLE);
                    }
                }
                if(recentsViewModel.getRecentsCount()<=6) {
                    mRecentsAdapter.setData(recentsList);
                }else if(recentsViewModel.getRecentsCount()==7){


                    RecentsEntity recentsEntity=new RecentsEntity(0,"more","","","","", "");
                    recentsList.add(recentsEntity);
                    mRecentsAdapter.notifyDataSetChanged();

                }
            }
        });

    }

    private void onViewAllClicked() {
        view.findViewById(R.id.view_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                        .replace(R.id.container,new NewRelease_FullView(),"NewRelease_FullView")
                        .addToBackStack(null)
                        .commit();

                MainActivity.mCurrentFragment="new_release";

            }
        });
    }

    private void onGenreClick() {

        MainActivity.mCurrentFragment="genre";

        chip1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle=new Bundle();
                bundle.putString("name",chip1.getText().toString());

                Fragment fragment=new GenreView();
                fragment.setArguments(bundle);

                ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                        .replace(R.id.container,fragment,"GenreView")
                        .addToBackStack(null)
                        .commit();


            }
        });

        chip2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle=new Bundle();
                bundle.putString("name",chip2.getText().toString());

                Fragment fragment=new GenreView();
                fragment.setArguments(bundle);

                ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                        .replace(R.id.container,fragment,"GenreView")
                        .addToBackStack(null)
                        .commit();

            }
        });

        chip3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle=new Bundle();
                bundle.putString("name",chip3.getText().toString());

                Fragment fragment=new GenreView();
                fragment.setArguments(bundle);

                ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                        .replace(R.id.container,fragment,"GenreView")
                        .addToBackStack(null)
                        .commit();

            }
        });

        chip4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle=new Bundle();
                bundle.putString("name",chip4.getText().toString());

                Fragment fragment=new GenreView();
                fragment.setArguments(bundle);

                ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                        .replace(R.id.container,fragment,"GenreView")
                        .addToBackStack(null)
                        .commit();

            }
        });

        chip5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle=new Bundle();
                bundle.putString("name",chip5.getText().toString());

                Fragment fragment=new GenreView();
                fragment.setArguments(bundle);

                ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                        .replace(R.id.container,fragment,"GenreView")
                        .addToBackStack(null)
                        .commit();

            }
        });

        chip6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle=new Bundle();
                bundle.putString("name",chip6.getText().toString());

                Fragment fragment=new GenreView();
                fragment.setArguments(bundle);

                ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                        .replace(R.id.container,fragment,"GenreView")
                        .addToBackStack(null)
                        .commit();

            }
        });

        chip7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle=new Bundle();
                bundle.putString("name",chip7.getText().toString());

                Fragment fragment=new GenreView();
                fragment.setArguments(bundle);

                ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                        .replace(R.id.container,fragment,"GenreView")
                        .commit();

            }
        });

    }


}
