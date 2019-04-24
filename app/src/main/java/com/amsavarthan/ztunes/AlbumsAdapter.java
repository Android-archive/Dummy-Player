package com.amsavarthan.ztunes;

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
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.marcoscg.dialogsheet.DialogSheet;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;


public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> {

    private final boolean full,canManage,showAsList;
    private List<Album> albumsList;
    private Context context;
    private View view;


    public AlbumsAdapter(List<Album> albumsList, Context context,boolean full,boolean canManage,boolean showAsList) {
        this.albumsList = albumsList;
        this.context = context;
        this.full=full;
        this.canManage=canManage;
        this.showAsList=showAsList;
    }

    @NonNull
    @Override
    public AlbumsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(full){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album_card_full, parent, false);
        }else{
            if(showAsList) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song_list, parent, false);
            }else{
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album_card, parent, false);
            }
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumsAdapter.ViewHolder holder, int position){

        final Album release=albumsList.get(position);
        final Animation fade_in = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        final Animation expand_in = AnimationUtils.loadAnimation(context, R.anim.expand_in);

        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_song_art))
                .load(release.getArt())
                .into(holder.mSongArt);

        holder.itemView.startAnimation(expand_in);

        if(showAsList) {

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AddSong.album_name=release.getName();
                    AddSong.album_art=release.getArt();
                    ((AppCompatActivity)context).onBackPressed();

                }
            });
        }

        holder.mTextView.startAnimation(fade_in);

        holder.mTextView.setText(release.getName());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(showAsList) {

                    AddSong.album_name=release.getName();
                    AddSong.album_art=release.getArt();
                    ((AppCompatActivity)context).onBackPressed();

                }else{

                    if (!canManage) {
                        Bundle bundle = new Bundle();
                        bundle.putString("name", release.getName());

                        Fragment fragment = new Album_SongsView();
                        fragment.setArguments(bundle);

                        ((FragmentActivity) view.getContext()).getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.activity_expand_in, R.anim.fade_out)
                                .replace(R.id.container, fragment, "Album_SongsView")
                                .addToBackStack(null)
                                .commit();

                        MainActivity.mCurrentFragment = "album";
                    } else {

                        new DialogSheet(context)
                                .setColoredNavigationBar(true)
                                .setTitle(release.getName())
                                .setMessage("What you want to do? Note: Only your approved songs will be shown")
                                .setRoundedCorners(true)
                                .setPositiveButton("Edit", new DialogSheet.OnPositiveClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                })
                                .setNegativeButton("View Songs", new DialogSheet.OnNegativeClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Bundle bundle = new Bundle();
                                        bundle.putString("name", release.getName());
                                        bundle.putString("type", "manage_view_all");

                                        Fragment fragment = new Album_SongsView();
                                        fragment.setArguments(bundle);

                                        ((FragmentActivity) view.getContext()).getSupportFragmentManager().beginTransaction()
                                                .setCustomAnimations(R.anim.activity_expand_in, R.anim.fade_out)
                                                .replace(R.id.container, fragment, "Album_SongsView")
                                                .addToBackStack(null)
                                                .commit();

                                        MainActivity.mCurrentFragment = "album";
                                    }
                                })
                                .show();

                    }
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
        return albumsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextView;
        private CardView cardView;
        private ImageView mSongArt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mSongArt=itemView.findViewById(R.id.song_cover);
            try {
                cardView = itemView.findViewById(R.id.card);
            }catch (NullPointerException e){
                cardView = itemView.findViewById(R.id.fab);
            }
            mTextView = itemView.findViewById(R.id.title);

        }
    }
}
