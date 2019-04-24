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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;


public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {

    private List<Songs> newReleaseList;
    private Context context;
    private RecentsViewModel viewModel;
    private View view;
    private String type;

    public SongsAdapter(List<Songs> newReleaseList, Context context, RecentsViewModel viewModel,String type) {
        this.newReleaseList = newReleaseList;
        this.viewModel=viewModel;
        this.type=type;
        this.context = context;
    }

    @NonNull
    @Override
    public SongsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       if(newReleaseList.get(0).getName().equals("Default")){
           view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_genre_default, parent, false);
       }else{
           view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song_card_full, parent, false);
       }
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull SongsAdapter.ViewHolder holder, int position){

        final Songs release=newReleaseList.get(position);
        final Animation expandIn = AnimationUtils.loadAnimation(context, R.anim.expand_in);
        final Animation fade_in = AnimationUtils.loadAnimation(context, R.anim.fade_in);

        if (!release.getName().equals("Default")) {

            Glide.with(context)
                    .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_song_art))
                    .load(release.getArt())
                    .dontAnimate()
                    .into(holder.mSongArt);

            holder.itemView.startAnimation(fade_in);
            holder.mTextView.setText(release.getName());
            holder.mPlayButton.startAnimation(expandIn);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
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

            holder.mPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    RecentsEntity recentsEntity = new RecentsEntity();
                    recentsEntity.setName(release.getName());
                    recentsEntity.setLink(release.getLink());
                    recentsEntity.setGenre(release.getGenre());
                    recentsEntity.setAlbum(release.getAlbum());
                    recentsEntity.setArtist(release.getArtist());
                    recentsEntity.setArt(release.getArt());

                    if(!type.equals("manage_view_all")) {

                        String nameInDatabase = viewModel.findSongByName(release.getName());
                        if (!nameInDatabase.equals("null")) {
                            viewModel.deletePost(viewModel.findSongEntityByName(release.getName()));
                            viewModel.savePost(recentsEntity);
                        } else {
                            viewModel.savePost(recentsEntity);
                        }

                        HomeFragment.HideDefaultCard(context);
                        MainActivity.startSong(context, recentsEntity);
                    }else{

                        HomeFragment.HideDefaultCard(context);
                        MainActivity.startSong(context, recentsEntity);

                    }

                }
            });


        } else {

            holder.itemView.startAnimation(fade_in);

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
        private ImageView mSongArt;
        private FloatingActionButton mPlayButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            try {
                mSongArt=itemView.findViewById(R.id.song_cover);
                mTextView = itemView.findViewById(R.id.title);
                mPlayButton = itemView.findViewById(R.id.fab);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
