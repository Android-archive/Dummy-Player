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

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;


public class RecentsAdapter extends RecyclerView.Adapter<RecentsAdapter.ViewHolder> {

    private List<RecentsEntity> recentsList;
    private Context context;
    private View view;

    public RecentsAdapter(List<RecentsEntity> recentsList, Context context) {
        this.recentsList = recentsList;
        this.context = context;
    }
    @NonNull
    @Override
    public RecentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song_list, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull RecentsAdapter.ViewHolder holder, int position){

        final RecentsEntity recent=recentsList.get(position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle=new Bundle();
                bundle.putString("song_name", recent.getName());
                bundle.putString("art", recent.getArt());
                bundle.putString("artist_name", recent.getArtist());
                bundle.putString("album_name", recent.getAlbum());
                bundle.putString("genre_name", recent.getGenre());
                bundle.putString("link", recent.getLink());

                Fragment fragment=new SongDetails();
                fragment.setArguments(bundle);

                ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                        .addToBackStack(null)
                        .replace(R.id.container,fragment,"SongDetails")
                        .commit();

                MainActivity.mCurrentFragment="album";
            }
        });

        Animation expandIn = AnimationUtils.loadAnimation(context, R.anim.expand_in);
        Animation fade_in = AnimationUtils.loadAnimation(context, R.anim.fade_in);

        Glide.with(context)
                .load(recent.getArt())
                .dontAnimate()
                .into(holder.mSongArt);


        holder.itemView.startAnimation(expandIn);
        holder.mTextView.startAnimation(fade_in);
        holder.mTextView.setText(recent.getName());
        holder.mTextView.startAnimation(fade_in);
        holder.mPlayButton.startAnimation(expandIn);

        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_song_art))
                .load(recent.getArt())
                .into(holder.mSongArt);

        holder.mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle=new Bundle();
                bundle.putString("song_name", recent.getName());
                bundle.putString("art", recent.getArt());
                bundle.putString("artist_name", recent.getArtist());
                bundle.putString("album_name", recent.getAlbum());
                bundle.putString("genre_name", recent.getGenre());
                bundle.putString("link", recent.getLink());

                Fragment fragment=new SongDetails();
                fragment.setArguments(bundle);

                ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                        .addToBackStack(null)
                        .replace(R.id.container,fragment,"SongDetails")
                        .commit();

                MainActivity.mCurrentFragment="album";

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.startSong(context,recent);
                //playSong(recent.getLink());
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Bundle bundle=new Bundle();
                bundle.putString("song_name", recent.getName());
                bundle.putString("art", recent.getArt());
                bundle.putString("artist_name", recent.getArtist());
                bundle.putString("album_name", recent.getAlbum());
                bundle.putString("genre_name", recent.getGenre());
                bundle.putString("link", recent.getLink());

                Fragment fragment=new SongDetails();
                fragment.setArguments(bundle);

                ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                        .addToBackStack(null)
                        .replace(R.id.container,fragment,"SongDetails")
                        .commit();

                MainActivity.mCurrentFragment="album";

                return true;
            }
        });

    }

    public void setData(List<RecentsEntity> newData) {
        if(newData!=null) {
            this.recentsList = newData;
            notifyDataSetChanged();
        }else{
            this.recentsList.clear();
            notifyDataSetChanged();
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
        return recentsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView mSongArt;
        private TextView mTextView;
        private CardView mPlayButton;

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
