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

import com.amsavarthan.ztunes.ui.fragments.HomeFragment;
import com.amsavarthan.ztunes.ui.activities.MainActivity;
import com.amsavarthan.ztunes.R;
import com.amsavarthan.ztunes.room.RecentsEntity;
import com.amsavarthan.ztunes.room.RecentsViewModel;
import com.amsavarthan.ztunes.ui.fragments.SongDetails;
import com.amsavarthan.ztunes.models.Songs;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.marcoscg.dialogsheet.DialogSheet;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.amsavarthan.ztunes.music.MediaMetaData;


public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {

    private List<Songs> newReleaseList;
    private Context context;
    private RecentsViewModel viewModel;
    private View view;
    private boolean canManage;
    private Activity activity;

    public LibraryAdapter(Activity activity, List<Songs> newReleaseList, Context context, RecentsViewModel viewModel, boolean canManage) {
        this.newReleaseList = newReleaseList;
        this.viewModel=viewModel;
        this.context = context;
        this.activity=activity;
        this.canManage=canManage;
    }

    @NonNull
    @Override
    public LibraryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_library_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final LibraryAdapter.ViewHolder holder, int position){

        final Songs release=newReleaseList.get(position);
        final Animation expandIn = AnimationUtils.loadAnimation(context, R.anim.expand_in);
        final Animation fade_in = AnimationUtils.loadAnimation(context, R.anim.fade_in);

        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_song_art))
                .load(release.getArt())
                .dontAnimate()
                .into(holder.mSongArt);
        holder.mPlayButtonCard.startAnimation(expandIn);

        holder.itemView.startAnimation(fade_in);
        holder.mTextView.setText(release.getName());

        if(canManage){

            holder.mApprovalTextView.setVisibility(View.VISIBLE);

            if(release.getApproved().equals("false")) {

                holder.mApprovalTextView.setTextColor(context.getResources().getColor(R.color.yellow_dark));
                holder.mApprovalTextView.setText(R.string.pending);

            }else if(release.getApproved().equals("removed")){

                holder.mApprovalTextView.setTextColor(context.getResources().getColor(R.color.red_dark));
                holder.mApprovalTextView.setText(R.string.removed);

            }else{

                holder.mApprovalTextView.setTextColor(context.getResources().getColor(R.color.green_dark));
                holder.mApprovalTextView.setText(R.string.approved);

            }

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!canManage){

                   if(listItemListener!=null){
                       RecentsEntity recentsEntity = new RecentsEntity();
                       recentsEntity.setName(release.getName());
                       recentsEntity.setLink(release.getLink());
                       recentsEntity.setGenre(release.getGenre());
                       recentsEntity.setAlbum(release.getAlbum());
                       recentsEntity.setArtist(release.getArtist());
                       recentsEntity.setArt(release.getArt());

                       String nameInDatabase = (String) viewModel.findSongByName(release.getName());
                       if (!nameInDatabase.equals("null")) {
                           viewModel.deletePost(viewModel.findSongEntityByName(release.getName()));
                           viewModel.savePost(recentsEntity);
                       } else {
                           viewModel.savePost(recentsEntity);
                       }

                       MediaMetaData metaData = new MediaMetaData();
                       metaData.setMediaId(release.getName());
                       metaData.setMediaTitle(release.getName());
                       metaData.setMediaUrl(release.getLink());
                       metaData.setMediaAlbum(release.getAlbum());
                       metaData.setMediaArtist(release.getArtist());
                       metaData.setMediaArt(release.getArt());

                       HomeFragment.HideDefaultCard(context);
                       listItemListener.onItemClickListener(metaData);
                       //MainActivity.startSong(activity,context,recentsEntity);
                   }

                }else{

                    if(!release.getApproved().equals("removed")) {
                        new DialogSheet(context)
                                .setColoredNavigationBar(true)
                                .setRoundedCorners(true)
                                .setTitle("Action")
                                .setMessage("What action you need to perform?")
                                .setPositiveButton("Edit", new DialogSheet.OnPositiveClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                })
                                .setNegativeButton("Play", new DialogSheet.OnNegativeClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                      if(listItemListener!=null){

                                          RecentsEntity recentsEntity = new RecentsEntity();
                                          recentsEntity.setName(release.getName());
                                          recentsEntity.setLink(release.getLink());
                                          recentsEntity.setGenre(release.getGenre());
                                          recentsEntity.setAlbum(release.getAlbum());
                                          recentsEntity.setArtist(release.getArtist());
                                          recentsEntity.setArt(release.getArt());

                                          MediaMetaData metaData = new MediaMetaData();
                                          metaData.setMediaId(release.getName());
                                          metaData.setMediaTitle(release.getName());
                                          metaData.setMediaUrl(release.getLink());
                                          metaData.setMediaAlbum(release.getAlbum());
                                          metaData.setMediaArtist(release.getArtist());
                                          metaData.setMediaArt(release.getArt());

                                          HomeFragment.HideDefaultCard(context);
                                          listItemListener.onItemClickListener(metaData);
                                          //MainActivity.startSong(activity,context, recentsEntity);

                                      }
                                    }
                                })
                                .show();
                    }
                }
            }
        });

        holder.mPlayButtonCard.setOnClickListener(new View.OnClickListener() {
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

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if(!canManage) {

                    Bundle bundle = new Bundle();
                    bundle.putString("song_name", release.getName());
                    bundle.putString("art", release.getArt());
                    bundle.putString("artist_name", release.getArtist());
                    bundle.putString("album_name", release.getAlbum());
                    bundle.putString("genre_name", release.getGenre());
                    bundle.putString("link", release.getLink());

                    Fragment fragment = new SongDetails();
                    fragment.setArguments(bundle);

                    ((FragmentActivity) view.getContext()).getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.activity_expand_in, R.anim.fade_out)
                            .replace(R.id.container, fragment, "SongDetails")
                            .addToBackStack(null)
                            .commit();

                    MainActivity.mCurrentFragment = "album";

                }else{

                    new DialogSheet(context)
                            .setColoredNavigationBar(true)
                            .setTitle(release.getName())
                            .setMessage("What you want to do?")
                            .setRoundedCorners(true)
                            .setPositiveButton("Edit", new DialogSheet.OnPositiveClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            })
                            .setNegativeButton("Play", new DialogSheet.OnNegativeClickListener() {
                                @Override
                                public void onClick(View v) {
                                   if(listItemListener!=null){

                                       RecentsEntity recentsEntity = new RecentsEntity();
                                       recentsEntity.setName(release.getName());
                                       recentsEntity.setLink(release.getLink());
                                       recentsEntity.setGenre(release.getGenre());
                                       recentsEntity.setAlbum(release.getAlbum());
                                       recentsEntity.setArtist(release.getArtist());
                                       recentsEntity.setArt(release.getArt());

                                       MediaMetaData metaData = new MediaMetaData();
                                       metaData.setMediaTitle(release.getName());
                                       metaData.setMediaUrl(release.getLink());
                                       metaData.setMediaAlbum(release.getAlbum());
                                       metaData.setMediaArtist(release.getArtist());
                                       metaData.setMediaArt(release.getArt());


                                       HomeFragment.HideDefaultCard(context);
                                       listItemListener.onItemClickListener(metaData);
                                       //MainActivity.startSong(activity,context,recentsEntity);

                                   }
                                }
                            })
                            .show();


                }

                return true;
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
        return newReleaseList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextView,mApprovalTextView;
        private ImageView mSongArt;
        private CardView mPlayButtonCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mApprovalTextView=itemView.findViewById(R.id.approved_status);
            mSongArt=itemView.findViewById(R.id.song_cover);
                mTextView = itemView.findViewById(R.id.title);
                mPlayButtonCard = itemView.findViewById(R.id.fab);


        }
    }

    public void setItemListener(SongsAdapter.ListItemListener listItemListener) {
        this.listItemListener = listItemListener;
    }

    public SongsAdapter.ListItemListener listItemListener;

    public interface ListItemListener {
        void onItemClickListener(MediaMetaData media);
    }

}
