package com.amsavarthan.ztunes;

import android.content.Context;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> {

    private List<Artist> artistsList;
    private Context context;
    private View view;


    public ArtistAdapter(List<Artist> artistsList, Context context) {
        this.artistsList = artistsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ArtistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artist_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistAdapter.ViewHolder holder, int position){

        final Artist artist=artistsList.get(position);
        Animation expandIn = AnimationUtils.loadAnimation(context, R.anim.expand_in);
        Animation fade_in = AnimationUtils.loadAnimation(context, R.anim.fade_in);

        holder.mArtistName.startAnimation(fade_in);
        holder.mArtistName.setText(artist.getName());
        holder.itemView.startAnimation(expandIn);

        holder.mArtistImage.startAnimation(expandIn);
        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_artist_art))
                .load(artist.getPhoto_link())
                .into(holder.mArtistImage);

        holder.mArtistImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle=new Bundle();
                bundle.putString("name",artist.getName());
                bundle.putString("photo",artist.getPhoto_link());

                Fragment fragment=new Artist_SongsView();
                fragment.setArguments(bundle);

                ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                        .replace(R.id.container,fragment,"Artist_SongsView")
                        .addToBackStack(null)
                        .commit();

                MainActivity.mCurrentFragment="artist_songs";


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
        return artistsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mArtistName;
        private ImageView mArtistImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mArtistName=itemView.findViewById(R.id.artist_name);
            mArtistImage=itemView.findViewById(R.id.artist_image);

        }
    }
}
