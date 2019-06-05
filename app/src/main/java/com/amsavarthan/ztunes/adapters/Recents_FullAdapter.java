package com.amsavarthan.ztunes.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.amsavarthan.ztunes.ui.activities.MainActivity;
import com.amsavarthan.ztunes.R;
import com.amsavarthan.ztunes.room.RecentsEntity;
import com.amsavarthan.ztunes.ui.fragments.SongDetails;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.amsavarthan.ztunes.music.MediaMetaData;



public class Recents_FullAdapter extends RecyclerView.Adapter<Recents_FullAdapter.ViewHolder> {

    private List<RecentsEntity> recentsList;
    private Context context;
    private View view;

    public Recents_FullAdapter(List<RecentsEntity> recentsList, Context context) {
        this.recentsList = recentsList;
        this.context = context;
    }
    @NonNull
    @Override
    public Recents_FullAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song_card_full, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull Recents_FullAdapter.ViewHolder holder, int position){

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
                bundle.putLong("duration", recent.getDuration());

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

        Animation expandIn = AnimationUtils.loadAnimation(context, R.anim.expand_in);
        Animation fade_in = AnimationUtils.loadAnimation(context, R.anim.fade_in);

        Glide.with(context)
                .load(recent.getArt())
                .dontAnimate()
                .into(holder.mSongArt);

        holder.itemView.startAnimation(expandIn);
        holder.mTextView.startAnimation(fade_in);

        holder.cardView.setVisibility(View.VISIBLE);
        holder.mTextView.setText(recent.getName());
        holder.mTextView.startAnimation(fade_in);
        holder.mPlayButton.startAnimation(expandIn);

        holder.mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(listItemListener!=null){

                    MediaMetaData metaData = new MediaMetaData();
                    metaData.setMediaId(recent.getName());
                    metaData.setMediaTitle(recent.getName());
                    metaData.setMediaUrl(recent.getLink());
                    metaData.setMediaAlbum(recent.getAlbum());
                    metaData.setMediaArtist(recent.getArtist());
                    metaData.setMediaArt(recent.getArt());
                    metaData.setMediaDuration(recent.getDuration());

                    listItemListener.onItemClickListener(metaData);

                }
                //MainActivity.startSong(activity,context,recent);

            }
        });


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

        private TextView mTextView;
        private FloatingActionButton mPlayButton;
        private CardView cardView;
        private ImageView mSongArt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mSongArt=itemView.findViewById(R.id.song_cover);
            cardView=itemView.findViewById(R.id.card);
                mTextView = itemView.findViewById(R.id.title);
                mPlayButton = itemView.findViewById(R.id.fab);
        }
    }

    public void setItemListener(ListItemListener listItemListener) {
        this.listItemListener = listItemListener;
    }

    public ListItemListener listItemListener;

    public interface ListItemListener {
        void onItemClickListener(MediaMetaData media);
    }
}
