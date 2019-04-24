package com.amsavarthan.ztunes;

import android.content.Context;
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
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import static com.amsavarthan.ztunes.MainActivity.navigation;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private List<Users> usersList;
    private Context context;
    private View view;


    public UsersAdapter(List<Users> usersList, Context context) {
        this.usersList = usersList;
        this.context = context;
    }

    @NonNull
    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artist_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.ViewHolder holder, int position){

        final Users users=usersList.get(holder.getAdapterPosition());
        Animation expandIn = AnimationUtils.loadAnimation(context, R.anim.expand_in);
        Animation fade_in = AnimationUtils.loadAnimation(context, R.anim.fade_in);

        holder.mArtistImage.setVisibility(View.VISIBLE);
        try {
            holder.mMoreLayout.setVisibility(View.GONE);
        }catch (Exception e){
            e.printStackTrace();
        }

        holder.mArtistName.startAnimation(fade_in);
        holder.mArtistName.setText(users.getName());
        holder.itemView.startAnimation(fade_in);

        holder.mArtistImage.startAnimation(expandIn);
        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_artist_art))
                .load(users.getPicture())
                .into(holder.mArtistImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(users.userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                    navigation.setSelectedItemId(R.id.profile);
                    showFragment(new ProfileView(),"profile");
                    MainActivity.mCurrentFragment="profile";
                    return;

                }

                Bundle bundle=new Bundle();
                bundle.putString("name",users.getName());
                bundle.putString("picture",users.getPicture());
                bundle.putString("type",users.getAccount_type());
                bundle.putString("id",users.userId);
                //bundle.putString("online_status",users.getOnline());

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
        return usersList.size();
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
