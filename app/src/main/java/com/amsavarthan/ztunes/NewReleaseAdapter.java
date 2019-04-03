package com.amsavarthan.ztunes;

import android.content.AbstractThreadedSyncAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;


public class NewReleaseAdapter extends RecyclerView.Adapter<NewReleaseAdapter.ViewHolder> {

    private List<Songs> newReleaseList;
    private Context context;
    private String type;
    private RecentsViewModel viewModel;
    private View view;

    public NewReleaseAdapter(List<Songs> newReleaseList, Context context,String type,RecentsViewModel viewModel) {
        this.newReleaseList = newReleaseList;
        this.context = context;
        this.type=type;
        this.viewModel=viewModel;
    }

    @NonNull
    @Override
    public NewReleaseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(newReleaseList.get(0).getName().equals("Default")){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song_card_default, parent, false);
        }else{
            if (type.equals("full")) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song_card_full, parent, false);
            }else{
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song_card, parent, false);
            }
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewReleaseAdapter.ViewHolder holder, int position){

        final Songs release=newReleaseList.get(position);
        final Animation expandIn = AnimationUtils.loadAnimation(context, R.anim.expand_in);
        final Animation fade_in = AnimationUtils.loadAnimation(context, R.anim.fade_in);

        try {

            if (!release.getName().equals("Default")) {

                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle=new Bundle();
                        bundle.putString("song_name", release.getName());
                        bundle.putString("art", release.getArt());
                        bundle.putString("artist_name", release.getArtist());
                        bundle.putString("album_name", release.getAlbum());
                        bundle.putString("genre_name", release.getGenre());
                        bundle.putString("link", release.getLink());

                        Fragment fragment=new SongDetails();
                        fragment.setArguments(bundle);

                        ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                                .replace(R.id.container,fragment,"SongDetails")
                                .addToBackStack(null)
                                .commit();

                        MainActivity.mCurrentFragment="album";
                    }
                });

                if(type.equals("full")) {
                    holder.cardView.setVisibility(View.VISIBLE);

                    Glide.with(context)
                            .load(release.getArt())
                            .dontAnimate()
                            .into(holder.mSongArt);

                    holder.itemView.startAnimation(expandIn);
                    holder.mTextView.setText(release.getName());
                    holder.mTextView.startAnimation(fade_in);
                    holder.mPlayButton.startAnimation(expandIn);

                    holder.mPlayButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            RecentsEntity recentsEntity=new RecentsEntity();
                            recentsEntity.setName(release.getName());
                            recentsEntity.setLink(release.getLink());
                            recentsEntity.setGenre(release.getGenre());
                            recentsEntity.setAlbum(release.getAlbum());
                            recentsEntity.setArtist(release.getArtist());
                            recentsEntity.setArt(release.getArt());

                            String nameInDatabase=viewModel.findSongByName(release.getName());
                            if(!nameInDatabase.equals("null")){
                                viewModel.deletePost(viewModel.findSongEntityByName(release.getName()));
                                viewModel.savePost(recentsEntity);
                            }else{
                                viewModel.savePost(recentsEntity);
                            }

                            HomeFragment.HideDefaultCard(context);

                            MainActivity.startSong(context,recentsEntity);
                        }
                    });
                }else{

                    if (holder.getAdapterPosition() == 4) {
                        holder.cardView.setVisibility(View.GONE);
                        holder.more_layout.setVisibility(View.VISIBLE);
                        holder.more_layout.startAnimation(fade_in);
                        holder.more.startAnimation(expandIn);
                        holder.more.setOnClickListener(new View.OnClickListener() {
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
                    } else {
                        holder.cardView.setVisibility(View.VISIBLE);

                        Glide.with(context)
                                .load(release.getArt())
                                .dontAnimate()
                                .into(holder.mSongArt);

                        holder.itemView.startAnimation(expandIn);
                        holder.mTextView.setText(release.getName());
                        holder.mTextView.startAnimation(fade_in);
                        holder.mPlayButton.startAnimation(expandIn);

                        holder.mPlayButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                RecentsEntity recentsEntity=new RecentsEntity();
                                recentsEntity.setName(release.getName());
                                recentsEntity.setLink(release.getLink());
                                recentsEntity.setGenre(release.getGenre());
                                recentsEntity.setAlbum(release.getAlbum());
                                recentsEntity.setArtist(release.getArtist());
                                recentsEntity.setArt(release.getArt());

                                String nameInDatabase=viewModel.findSongByName(release.getName());
                                if(!nameInDatabase.equals("null")){
                                    viewModel.deletePost(viewModel.findSongEntityByName(release.getName()));
                                    viewModel.savePost(recentsEntity);
                                }else{
                                    viewModel.savePost(recentsEntity);
                                }

                                HomeFragment.HideDefaultCard(context);

                                MainActivity.startSong(context,recentsEntity);
                            }
                        });
                    }

                }
            } else {

                holder.itemView.startAnimation(fade_in);

            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return newReleaseList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextView;
        private FloatingActionButton mPlayButton,more;
        private LinearLayout more_layout;
        private CardView cardView;
        private ImageView mSongArt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            try {
                mSongArt=itemView.findViewById(R.id.song_cover);
                cardView=itemView.findViewById(R.id.card);
                more=itemView.findViewById(R.id.more);
                more_layout=itemView.findViewById(R.id.more_layout);
                mTextView = itemView.findViewById(R.id.title);
                mPlayButton = itemView.findViewById(R.id.fab);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
