package com.amsavarthan.ztunes;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import me.grantland.widget.AutofitTextView;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

    private List<Feed> feedList;
    private Context context;
    private View view;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mCurrentUser;
    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();

    public FeedAdapter(List<Feed> feedList, Context context) {
        this.feedList = feedList;
        this.context = context;
    }

    @NonNull
    @Override
    public FeedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.KITKAT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed_post_v19, parent, false);
        }else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed_post, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FeedAdapter.ViewHolder holder, int position){

        final Feed feed=feedList.get(position);

        mFirestore=FirebaseFirestore.getInstance();
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();

        holder.post_username.setText(feed.getUser_name());
        holder.post_timestamp.setText(TimeAgo.using(Long.parseLong(feed.getTimestamp())));

        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_artist_art))
                .load(feed.getUser_photo())
                .into(holder.post_user_image);

        if(feed.getType().equals("text")) {

            setmImageHolderBg(feed.getColor(),holder.image_holder);
            holder.post_image.setVisibility(View.GONE);
            holder.post_text.setVisibility(View.VISIBLE);
            holder.post_desc.setVisibility(View.GONE);
            holder.post_text.setText(feed.getCaption());

            holder.share_button.setOnFavoriteAnimationEndListener(new MaterialFavoriteButton.OnFavoriteAnimationEndListener() {
                @Override
                public void onAnimationEnd(MaterialFavoriteButton buttonView, boolean favorite) {
                    Intent intent = new Intent(Intent.ACTION_SEND)
                            .setType("image/*");
                    intent.putExtra(Intent.EXTRA_STREAM, getBitmapUri(getBitmap(holder.image_holder), holder, "post_user_" + feedList.get(holder.getAdapterPosition()).getUser_id()));
                    try {
                        context.startActivity(Intent.createChooser(intent, "Share using..."));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            });

        }else {

            holder.post_image.setVisibility(View.VISIBLE);
            holder.post_text.setVisibility(View.GONE);
            holder.post_desc.setVisibility(View.VISIBLE);
            holder.post_desc.setText(feed.getCaption());

            Glide.with(context)
                    .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_post))
                    .load(feed.getImage())
                    .into(holder.post_image);

        }

        if(NetworkUtil.isOnline(context)) {

                enableDoubleTap(holder);
                holder.like_button.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
                    @Override
                    public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                        if (favorite) {
                            Map<String, Object> likeMap = new HashMap<>();
                            likeMap.put("liked", true);

                            try {

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {
                            Map<String, Object> likeMap = new HashMap<>();
                            likeMap.put("liked", false);

                            try {

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });
            }

    }

    private void setmImageHolderBg(String color, FrameLayout mImageholder) {

        switch (Integer.parseInt(color)) {
            case 1:
                mImageholder.setBackgroundResource(R.drawable.gradient_1);
                break;
            case 2:
                mImageholder.setBackgroundResource(R.drawable.gradient_2);
                break;
            case 3:
                mImageholder.setBackgroundResource(R.drawable.gradient_3);
                break;
            case 4:
                mImageholder.setBackgroundResource(R.drawable.gradient_4);
                break;
            case 5:
                mImageholder.setBackgroundResource(R.drawable.gradient_5);
                break;
            case 6:
                mImageholder.setBackgroundResource(R.drawable.gradient_6);
                break;
            case 7:
                mImageholder.setBackgroundResource(R.drawable.gradient_7);
                break;
            case 8:
                mImageholder.setBackgroundResource(R.drawable.gradient_8);
                break;
            case 9:
                mImageholder.setBackgroundResource(R.drawable.gradient_9);
                break;
            case 10:
                mImageholder.setBackgroundResource(R.drawable.gradient_10);
                break;
            case 11:
                mImageholder.setBackgroundResource(R.drawable.gradient_11);
                break;
            case 12:
                mImageholder.setBackgroundResource(R.drawable.gradient_12);
                break;
            case 13:
                mImageholder.setBackgroundResource(R.drawable.gradient_13);
                break;
            case 14:
                mImageholder.setBackgroundResource(R.drawable.gradient_14);
                break;
            case 15:
                mImageholder.setBackgroundResource(R.drawable.gradient_15);
                break;
            case 16:
                mImageholder.setBackgroundResource(R.drawable.gradient_16);
                break;
            case 17:
                mImageholder.setBackgroundResource(R.drawable.gradient_17);
                break;
            case 18:
                mImageholder.setBackgroundResource(R.drawable.gradient_18);
                break;
            case 19:
                mImageholder.setBackgroundResource(R.drawable.gradient_19);
                break;
        }


    }

    private Uri getBitmapUri(Bitmap bitmap, ViewHolder holder, String name) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);

        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, feedList.get(holder.getAdapterPosition()).postId, "Post by " + name);
        return Uri.parse(path);
    }

    private Bitmap getBitmap(FrameLayout view) {

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.parseColor("#212121"));
        }
        view.draw(canvas);

        return bitmap;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void enableDoubleTap(final ViewHolder holder) {

        //Double Tap for Photo is set on PostPhotosAdapter

        final GestureDetector detector=new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                animatePhotoLike(holder);
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
            }
        }
        );

        holder.image_holder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return detector.onTouchEvent(event);
            }
        });

    }

    private void animatePhotoLike(final ViewHolder holder) {
        holder.vBgLike.setVisibility(View.VISIBLE);
        holder.ivLike.setVisibility(View.VISIBLE);

        holder.vBgLike.setScaleY(0.1f);
        holder.vBgLike.setScaleX(0.1f);
        holder.vBgLike.setAlpha(1f);
        holder.ivLike.setScaleY(0.1f);
        holder.ivLike.setScaleX(0.1f);

        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator bgScaleYAnim = ObjectAnimator.ofFloat(holder.vBgLike, "scaleY", 0.1f, 1f);
        bgScaleYAnim.setDuration(200);
        bgScaleYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        ObjectAnimator bgScaleXAnim = ObjectAnimator.ofFloat(holder.vBgLike, "scaleX", 0.1f, 1f);
        bgScaleXAnim.setDuration(200);
        bgScaleXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        ObjectAnimator bgAlphaAnim = ObjectAnimator.ofFloat(holder.vBgLike, "alpha", 1f, 0f);
        bgAlphaAnim.setDuration(200);
        bgAlphaAnim.setStartDelay(150);
        bgAlphaAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

        ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleY", 0.1f, 1f);
        imgScaleUpYAnim.setDuration(300);
        imgScaleUpYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleX", 0.1f, 1f);
        imgScaleUpXAnim.setDuration(300);
        imgScaleUpXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

        ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleY", 1f, 0f);
        imgScaleDownYAnim.setDuration(300);
        imgScaleDownYAnim.setInterpolator(ACCELERATE_INTERPOLATOR);
        ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleX", 1f, 0f);
        imgScaleDownXAnim.setDuration(300);
        imgScaleDownXAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

        animatorSet.playTogether(bgScaleYAnim, bgScaleXAnim, bgAlphaAnim, imgScaleUpYAnim, imgScaleUpXAnim);
        animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                resetLikeAnimationState(holder);
                holder.like_button.setFavorite(true,true);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                Map<String, Object> likeMap = new HashMap<>();
                likeMap.put("liked", true);

                try {

                    //Add like to post

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
        animatorSet.start();

    }

    private void resetLikeAnimationState(ViewHolder holder) {
        holder.vBgLike.setVisibility(View.INVISIBLE);
        holder.ivLike.setVisibility(View.INVISIBLE);
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
        return feedList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView user_image;
        HifyImageView post_image;
        AutofitTextView post_text;
        ImageView ivLike;
        View vBgLike;
        ImageView delete_button;
        TextView post_timestamp,post_username,post_desc,like_count;
        CircleImageView post_user_image;
        RelativeLayout react_layout;
        MaterialFavoriteButton like_button,share_button,comment_button;
        FrameLayout image_holder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            user_image=itemView.findViewById(R.id.user_image);
            image_holder=itemView.findViewById(R.id.image_holder);
            post_image=itemView.findViewById(R.id.post_image);
            post_text=itemView.findViewById(R.id.post_text);
            ivLike=itemView.findViewById(R.id.ivLike);
            vBgLike=itemView.findViewById(R.id.vBgLike);
            delete_button=itemView.findViewById(R.id.delete_button);
            post_timestamp=itemView.findViewById(R.id.post_timestamp);
            post_username=itemView.findViewById(R.id.post_username);
            post_desc=itemView.findViewById(R.id.post_desc);
            like_count=itemView.findViewById(R.id.like_count);
            post_user_image=itemView.findViewById(R.id.post_user_image);
            react_layout=itemView.findViewById(R.id.react_layout);
            like_button=itemView.findViewById(R.id.like_button);
            share_button=itemView.findViewById(R.id.share_button);
            comment_button=itemView.findViewById(R.id.comment_button);

        }
    }
}
