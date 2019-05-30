package com.amsavarthan.ztunes.adapters;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amsavarthan.ztunes.ui.fragments.Artists_FullView;
import com.amsavarthan.ztunes.ui.fragments.FriendProfileView;
import com.amsavarthan.ztunes.ui.activities.MainActivity;
import com.amsavarthan.ztunes.ui.fragments.ProfileView;
import com.amsavarthan.ztunes.R;
import com.amsavarthan.ztunes.models.Artist;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import static com.amsavarthan.ztunes.ui.activities.MainActivity.navigation;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> {

    private List<Artist> artistsList;
    private Context context;
    private View view;
    private boolean fullView;


    public ArtistAdapter(List<Artist> artistsList, Context context,boolean fullView) {
        this.artistsList = artistsList;
        this.context = context;
        this.fullView=fullView;
    }

    @NonNull
    @Override
    public ArtistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(fullView) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artist_list, parent, false);
        }else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artist_card, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistAdapter.ViewHolder holder, int position){

        final Artist artist=artistsList.get(position);
        Animation expandIn = AnimationUtils.loadAnimation(context, R.anim.expand_in);
        Animation fade_in = AnimationUtils.loadAnimation(context, R.anim.fade_in);

        if(artist.getName().equals("default")){

            holder.mArtistImage.setVisibility(View.GONE);
            holder.mMoreLayout.setVisibility(View.VISIBLE);

            holder.fab.startAnimation(expandIn);
            holder.fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Fragment fragment=new Artists_FullView();

                    ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                            .replace(R.id.container,fragment,"Artists_FullView")
                            .addToBackStack(null)
                            .commit();

                    MainActivity.mCurrentFragment="artists_fullview";

                }
            });

        }else{

            holder.mArtistImage.setVisibility(View.VISIBLE);
            try {
                holder.mMoreLayout.setVisibility(View.GONE);
            }catch (Exception e){
                e.printStackTrace();
            }

            holder.mArtistName.startAnimation(fade_in);
            holder.mArtistName.setText(artist.getName());
            holder.itemView.startAnimation(fade_in);

            holder.mArtistImage.startAnimation(expandIn);
            Glide.with(context)
                    .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_artist_art))
                    .load(artist.getPhoto_link())
                    .into(holder.mArtistImage);


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(TextUtils.equals(artist.userId,FirebaseAuth.getInstance().getCurrentUser().getUid())){

                        MainActivity.navigation.setSelectedItemId(R.id.profile);
                        showFragment(new ProfileView(),"profile");
                        MainActivity.mCurrentFragment="profile";
                        return;

                    }

                    Bundle bundle=new Bundle();
                    bundle.putString("name",artist.getName());
                    bundle.putString("picture",artist.getPhoto_link());
                    bundle.putString("type","artist");
                    bundle.putString("id",artist.userId);

                    Fragment fragment=new FriendProfileView();
                    fragment.setArguments(bundle);

                    ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                            .replace(R.id.container,fragment,"Friend_Profile")
                            .addToBackStack(null)
                            .commit();

                    MainActivity.mCurrentFragment="friend_profile";

                }
            });

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
        return artistsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mArtistName;
        private ImageView mArtistImage;
        private LinearLayout mMoreLayout;
        private FloatingActionButton fab;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mArtistName=itemView.findViewById(R.id.artist_name);
            mArtistImage=itemView.findViewById(R.id.artist_image);
            try {
                mMoreLayout = itemView.findViewById(R.id.more_layout);
                fab = itemView.findViewById(R.id.more);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    private void showFragment(Fragment fragment, String tag){

        ((AppCompatActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                .replace(R.id.container,fragment,tag)
                .commit();

    }

}
