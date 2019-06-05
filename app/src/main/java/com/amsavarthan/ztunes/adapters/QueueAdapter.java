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

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.amsavarthan.ztunes.R;
import com.amsavarthan.ztunes.models.Songs;
import com.amsavarthan.ztunes.music.MediaMetaData;
import com.amsavarthan.ztunes.room.RecentsEntity;
import com.amsavarthan.ztunes.room.RecentsViewModel;
import com.amsavarthan.ztunes.ui.activities.MainActivity;
import com.amsavarthan.ztunes.ui.fragments.HomeFragment;
import com.amsavarthan.ztunes.ui.fragments.SongDetails;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.marcoscg.dialogsheet.DialogSheet;

import java.util.List;


public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> {

    private List<MediaMetaData> queue;
    private Context context;
    private View view;
    private RecentsViewModel viewModel;

    public QueueAdapter(Context context, List<MediaMetaData> queue,RecentsViewModel viewModel) {
        this.queue = queue;
        this.viewModel=viewModel;
        this.context = context;
    }

    @NonNull
    @Override
    public QueueAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_queue, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final QueueAdapter.ViewHolder holder, int position){

        MediaMetaData song=queue.get(position);

        holder.mTextView.setText(song.getMediaTitle());
        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_song_art))
                .load(song.getMediaAlbum())
                .into(holder.mSongArt);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(listItemListener!=null){
                    HomeFragment.HideDefaultCard(context);
                    listItemListener.onItemClickListener(song);
                }

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
        return queue.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextView;
        private ImageView mSongArt;
        private CardView mPlayButtonCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mSongArt=itemView.findViewById(R.id.song_cover);
            mTextView = itemView.findViewById(R.id.title);
            mPlayButtonCard = itemView.findViewById(R.id.fab);

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
