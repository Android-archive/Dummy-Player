package com.amsavarthan.ztunes.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amsavarthan.ztunes.models.MultipleImage;
import com.amsavarthan.ztunes.ui.activities.PromotePostActivity;
import com.amsavarthan.ztunes.utils.NetworkUtil;
import com.amsavarthan.ztunes.R;
import com.amsavarthan.ztunes.models.Feed;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.marcoscg.dialogsheet.DialogSheet;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;
import com.watermark.androidwm_light.WatermarkBuilder;
import com.watermark.androidwm_light.bean.WatermarkPosition;
import com.watermark.androidwm_light.bean.WatermarkText;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import me.grantland.widget.AutofitTextView;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

    private List<Feed> feedList;
    private Context context;
    private View view;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mCurrentUser;
    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private boolean isOwner;
    private Activity activity;

    public FeedAdapter(List<Feed> feedList, Context context,Activity activity) {
        this.feedList = feedList;
        this.context = context;
        this.activity=activity;
    }

    @NonNull
    @Override
    public FeedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FeedAdapter.ViewHolder holder, int position){

        Feed feed=feedList.get(position);

        mFirestore=FirebaseFirestore.getInstance();
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();

        holder.post_username.setText(feed.getUsername());
        holder.post_timestamp.setText(TimeAgo.using(Long.parseLong(feed.getTimestamp())));

        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_artist_art))
                .load(feed.getUserimage())
                .into(holder.post_user_image);


        if(NetworkUtil.isOnline(context)) {
            updateUserInformation(feedList,holder);
            enableDoubleTap(holder);
            holder.like_button.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
                @Override
                public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                    if (favorite) {
                        Map<String, Object> likeMap = new HashMap<>();
                        likeMap.put("liked", true);

                        FirebaseFirestore.getInstance()
                                .collection("feed")
                                .document(feedList.get(holder.getAdapterPosition()).postId)
                                .collection("liked_users")
                                .document(mCurrentUser.getUid())
                                .set(likeMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i("feed","liked:"+feedList.get(holder.getAdapterPosition()).postId);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("feed","error",e);
                                    }
                                });

                    } else {

                        mFirestore.collection("feed")
                                .document(feedList.get(holder.getAdapterPosition()).postId)
                                .collection("liked_users")
                                .document(mCurrentUser.getUid())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i("feed","disliked:"+feedList.get(holder.getAdapterPosition()).postId);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("feed","error",e);
                                    }
                                });


                    }
                }
            });
        }

        if (feedList.get(position).getImage_count()==0) {

            holder.pager_layout.setVisibility(View.GONE);
            holder.post_desc.setVisibility(View.GONE);
            setmImageHolderBg(feedList.get(position).getColor(), holder.image_holder);
            holder.post_text.setVisibility(View.VISIBLE);
            holder.post_text.setText(feedList.get(position).getDescription());

            holder.share_button.setOnFavoriteAnimationEndListener((buttonView, favorite) -> {

                Intent intent = new Intent(Intent.ACTION_SEND)
                        .setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, getBitmapUri(getBitmap(holder.image_holder), feedList.get(holder.getAdapterPosition()).getUsername()));
                try {
                    context.startActivity(Intent.createChooser(intent, "Share using..."));
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }

            });


        } else if(feedList.get(position).getImage_count()==1) {

            ArrayList<MultipleImage> multipleImages=new ArrayList<>();
            PostPhotosAdapter photosAdapter=new PostPhotosAdapter(context,activity,multipleImages,false,feedList.get(holder.getAdapterPosition()).postId,holder.like_button);
            setUrls(holder,multipleImages,photosAdapter);

            holder.pager.setAdapter(photosAdapter);
            holder.indicator_holder.setVisibility(View.GONE);
            photosAdapter.notifyDataSetChanged();

            holder.pager_layout.setVisibility(View.VISIBLE);
            holder.post_text.setVisibility(View.GONE);
            holder.post_desc.setVisibility(View.VISIBLE);
            String desc = "<b>" + feedList.get(position).getUsername() + "</b> : " + feedList.get(position).getDescription();
            holder.post_desc.setText(Html.fromHtml(desc));

            holder.share_button.setOnFavoriteAnimationEndListener((buttonView, favorite) ->
                    new DownloadTask(context,holder).execute(stringToURL(feedList.get(holder.getAdapterPosition()).getImage_url_0())));


        }else if(feedList.get(position).getImage_count()>0) {

            ArrayList<MultipleImage> multipleImages=new ArrayList<>();
            PostPhotosAdapter photosAdapter=new PostPhotosAdapter(context,activity,multipleImages,false,feedList.get(holder.getAdapterPosition()).postId,holder.like_button);
            setUrls(holder,multipleImages,photosAdapter);

            holder.pager.setAdapter(photosAdapter);
            photosAdapter.notifyDataSetChanged();
            holder.indicator.setDotsClickable(true);
            holder.indicator.setViewPager(holder.pager);

            //autoStartSlide(holder,multipleImages.size());

            holder.pager_layout.setVisibility(View.VISIBLE);
            holder.indicator_holder.setVisibility(View.VISIBLE);
            holder.post_text.setVisibility(View.GONE);
            holder.post_desc.setVisibility(View.VISIBLE);
            String desc = "<b>" + feedList.get(position).getUsername() + "</b> : " + feedList.get(position).getDescription();
            holder.post_desc.setText(Html.fromHtml(desc));

            holder.share_button.setOnFavoriteAnimationEndListener((buttonView, favorite) ->
                    new DownloadTask(context,holder).execute(stringToURL(multipleImages.get(holder.pager.getCurrentItem()).getUrl())));

        }


    }

    private void updateUserInformation(List<Feed> feeds,ViewHolder holder) {

        if(feeds.get(holder.getAdapterPosition()).getUserId().equals(mCurrentUser.getUid())){
            isOwner=true;

            holder.promote_text.setVisibility(View.VISIBLE);
            holder.promote_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, PromotePostActivity.class));
                }
            });

            holder.delete_button.setVisibility(View.VISIBLE);
            holder.delete_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DialogSheet(context)
                            .setTitle("Delete post")
                            .setMessage("Are you sure do you want to delete this post?")
                            .setPositiveButton("Yes", new DialogSheet.OnPositiveClickListener() {
                                @Override
                                public void onClick(View v) {

                                    final ProgressDialog pdialog=new ProgressDialog(context);
                                    pdialog.setMessage("Please wait...");
                                    pdialog.setIndeterminate(true);
                                    pdialog.setCancelable(false);
                                    pdialog.setCanceledOnTouchOutside(false);
                                    pdialog.show();

                                    FirebaseFirestore.getInstance().collection("feed")
                                            .document(feeds.get(holder.getAdapterPosition()).postId)
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    //Deleting all images then the post itself from database

                                                    if(!TextUtils.isEmpty(feeds.get(holder.getAdapterPosition()).getImage_url_0())) {
                                                        StorageReference img = FirebaseStorage.getInstance()
                                                                .getReferenceFromUrl(feeds.get(holder.getAdapterPosition()).getImage_url_0());
                                                        img.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                pdialog.dismiss();
                                                                Log.i("Post Image","deleted");
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.e("Post Image",e.getLocalizedMessage());
                                                            }
                                                        });
                                                    }

                                                    pdialog.show();
                                                    if(!TextUtils.isEmpty(feeds.get(holder.getAdapterPosition()).getImage_url_1())) {
                                                        StorageReference img = FirebaseStorage.getInstance()
                                                                .getReferenceFromUrl(feeds.get(holder.getAdapterPosition()).getImage_url_1());
                                                        img.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                pdialog.dismiss();
                                                                Log.i("Post Image","deleted");
                                                            }
                                                        })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.e("Post Image",e.getLocalizedMessage());
                                                                    }
                                                                });
                                                    }

                                                    pdialog.show();
                                                    if(!TextUtils.isEmpty(feeds.get(holder.getAdapterPosition()).getImage_url_2())) {
                                                        StorageReference img = FirebaseStorage.getInstance()
                                                                .getReferenceFromUrl(feeds.get(holder.getAdapterPosition()).getImage_url_2());
                                                        img.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                pdialog.dismiss();
                                                                Log.i("Post Image","deleted");
                                                            }
                                                        })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.e("Post Image",e.getLocalizedMessage());
                                                                    }
                                                                });
                                                    }

                                                    pdialog.show();
                                                    if(!TextUtils.isEmpty(feeds.get(holder.getAdapterPosition()).getImage_url_3())) {
                                                        StorageReference img = FirebaseStorage.getInstance()
                                                                .getReferenceFromUrl(feeds.get(holder.getAdapterPosition()).getImage_url_3());
                                                        img.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                pdialog.dismiss();
                                                                Log.i("Post Image","deleted");
                                                            }
                                                        })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.e("Post Image",e.getLocalizedMessage());
                                                                    }
                                                                });
                                                    }

                                                    pdialog.show();
                                                    if(!TextUtils.isEmpty(feeds.get(holder.getAdapterPosition()).getImage_url_4())) {
                                                        StorageReference img = FirebaseStorage.getInstance()
                                                                .getReferenceFromUrl(feeds.get(holder.getAdapterPosition()).getImage_url_4());
                                                        img.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                pdialog.dismiss();
                                                                Log.i("Post Image","deleted");
                                                            }
                                                        })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.e("Post Image",e.getLocalizedMessage());
                                                                    }
                                                                });
                                                    }

                                                    pdialog.show();
                                                    if(!TextUtils.isEmpty(feeds.get(holder.getAdapterPosition()).getImage_url_5())) {
                                                        StorageReference img = FirebaseStorage.getInstance()
                                                                .getReferenceFromUrl(feeds.get(holder.getAdapterPosition()).getImage_url_5());
                                                        img.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                pdialog.dismiss();
                                                                Log.i("Post Image","deleted");
                                                            }
                                                        })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.e("Post Image",e.getLocalizedMessage());
                                                                    }
                                                                });
                                                    }

                                                    pdialog.show();
                                                    if(!TextUtils.isEmpty(feeds.get(holder.getAdapterPosition()).getImage_url_6())) {
                                                        StorageReference img = FirebaseStorage.getInstance()
                                                                .getReferenceFromUrl(feeds.get(holder.getAdapterPosition()).getImage_url_6());
                                                        img.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                pdialog.dismiss();
                                                                Log.i("Post Image","deleted");
                                                            }
                                                        })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.e("Post Image",e.getLocalizedMessage());
                                                                    }
                                                                });
                                                    }

                                                    Toasty.success(context, "Post deleted", Toasty.LENGTH_SHORT,true).show();
                                                    feeds.remove(holder.getAdapterPosition());
                                                    notifyItemRemoved(holder.getAdapterPosition());
                                                    notifyDataSetChanged();

                                                    pdialog.dismiss();


                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    pdialog.dismiss();
                                                    Log.e("error",e.getLocalizedMessage());
                                                }
                                            });

                                }
                            })
                            .setNegativeButton("No", new DialogSheet.OnNegativeClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            })
                            .setRoundedCorners(true)
                            .setColoredNavigationBar(true)
                            .show();
                }
            });

        }else{
            isOwner=false;
            holder.promote_text.setVisibility(View.GONE);
            holder.delete_button.setVisibility(View.GONE);
        }

        mFirestore.collection("feed")
                .document(feeds.get(holder.getAdapterPosition()).postId)
                .collection("liked_users")
                .document(mCurrentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            holder.like_button.setFavorite(true);
                        }else{
                            holder.like_button.setFavorite(false);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("like","error",e);
                    }
                });

        //Updating user display information if any changes are there inside the user's collection
        try {

            mFirestore.collection("Users")
                    .document(feeds.get(holder.getAdapterPosition()).getUserId())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(final DocumentSnapshot documentSnapshot) {

                            try {
                                if (!documentSnapshot.getString("name").equals(feeds.get(holder.getAdapterPosition()).getUsername()) &&
                                        !documentSnapshot.getString("picture").equals(feeds.get(holder.getAdapterPosition()).getUserimage())) {

                                    Map<String, Object> postMap = new HashMap<>();
                                    postMap.put("username", documentSnapshot.getString("name"));
                                    postMap.put("userimage", documentSnapshot.getString("picture"));

                                    mFirestore.collection("Posts")
                                            .document(feeds.get(holder.getAdapterPosition()).postId)
                                            .update(postMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("post_update", "success");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.i("post_update", "failure");
                                                }
                                            });

                                    holder.post_username.setText(documentSnapshot.getString("name"));

                                    Glide.with(context)
                                            .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_artist_art))
                                            .load(documentSnapshot.getString("picture"))
                                            .into(holder.user_image);


                                } else if (!documentSnapshot.getString("name").equals(feeds.get(holder.getAdapterPosition()).getUsername())) {


                                    Map<String, Object> postMap = new HashMap<>();
                                    postMap.put("username", documentSnapshot.getString("name"));

                                    mFirestore.collection("Posts")
                                            .document(feeds.get(holder.getAdapterPosition()).postId)
                                            .update(postMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("post_update", "success");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.i("post_update", "failure");
                                                }
                                            });

                                    holder.post_username.setText(documentSnapshot.getString("name "));

                                } else if (!documentSnapshot.getString("picture").equals(feeds.get(holder.getAdapterPosition()).getUserimage())) {

                                    Map<String, Object> postMap = new HashMap<>();
                                    postMap.put("userimage", documentSnapshot.getString("picture"));

                                    mFirestore.collection("Posts")
                                            .document(feeds.get(holder.getAdapterPosition()).postId)
                                            .update(postMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("post_update", "success");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.i("post_update", "failure");
                                                }
                                            });

                                    Glide.with(context)
                                            .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_artist_art))
                                            .load(documentSnapshot.getString("picture"))
                                            .into(holder.user_image);

                                }


                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Error", e.getMessage());
                        }
                    });

        }catch (Exception ex){
            Log.w("error","fastscrolled",ex);
        }


    }

    private Uri getBitmapUri(Bitmap bitmap, String name) {

        try {

            OutputStream outputStream=null;
            File dir=new File(Environment.getExternalStorageDirectory().toString()+"/Pictures/VeloxYSoundZ/");

            if(!dir.exists()){
                dir.mkdirs();
            }

            File file=new File(dir,"POST_"+System.currentTimeMillis()+".png");
            if(file.exists()){
                file.delete();
            }else{
                file.createNewFile();
            }

            outputStream = new FileOutputStream(file);
            BufferedOutputStream outputStream1=new BufferedOutputStream(outputStream);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream1);
            outputStream1.flush();
            outputStream1.close();
            return Uri.parse(file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
            Toasty.error(context,"Sharing failed",Toasty.LENGTH_SHORT,true).show();
            return null;
        }
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

        WatermarkText watermarkText=new WatermarkText("VeloxYSoundZ")
                .setPosition(new WatermarkPosition(0.5,0.5,30))
                .setTextColor(Color.WHITE)
                .setTextFont(R.font.bold)
                .setTextAlpha(100)
                .setTextSize(20);

        Bitmap watermarked_bitmap= WatermarkBuilder.create(context,bitmap)
                .loadWatermarkText(watermarkText)
                .setTileMode(true)
                .getWatermark()
                .getOutputImage();

        return watermarked_bitmap;
    }

    private void setUrls(ViewHolder holder, ArrayList<MultipleImage> multipleImages, PostPhotosAdapter photosAdapter) {

        int pos=holder.getAdapterPosition();
        String url0,url1,url2,url3,url4,url5,url6;

        url0=feedList.get(pos).getImage_url_0();
        url1=feedList.get(pos).getImage_url_1();
        url2=feedList.get(pos).getImage_url_2();
        url3=feedList.get(pos).getImage_url_3();
        url4=feedList.get(pos).getImage_url_4();
        url5=feedList.get(pos).getImage_url_5();
        url6=feedList.get(pos).getImage_url_6();

        if(!TextUtils.isEmpty(url0)){
            MultipleImage image=new MultipleImage(url0);
            multipleImages.add(image);
            photosAdapter.notifyDataSetChanged();
            Log.i("url0",url0);
        }

        if(!TextUtils.isEmpty(url1)){
            MultipleImage image=new MultipleImage(url1);
            multipleImages.add(image);
            photosAdapter.notifyDataSetChanged();
            Log.i("url1",url1);
        }

        if(!TextUtils.isEmpty(url2)){
            MultipleImage image=new MultipleImage(url2);
            multipleImages.add(image);
            photosAdapter.notifyDataSetChanged();
            Log.i("url2",url2);
        }

        if(!TextUtils.isEmpty(url3)){
            MultipleImage image=new MultipleImage(url3);
            multipleImages.add(image);
            photosAdapter.notifyDataSetChanged();
            Log.i("url3",url3);
        }

        if(!TextUtils.isEmpty(url4)){
            MultipleImage image=new MultipleImage(url4);
            multipleImages.add(image);
            photosAdapter.notifyDataSetChanged();
            Log.i("url4",url4);
        }

        if(!TextUtils.isEmpty(url5)){
            MultipleImage image=new MultipleImage(url5);
            multipleImages.add(image);
            photosAdapter.notifyDataSetChanged();
            Log.i("url5",url5);
        }

        if(!TextUtils.isEmpty(url6)){
            MultipleImage image=new MultipleImage(url6);
            multipleImages.add(image);
            photosAdapter.notifyDataSetChanged();
            Log.i("ur6",url6);
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
        AutofitTextView post_text;
        ImageView ivLike;
        View vBgLike;
        ImageView delete_button;
        TextView post_timestamp,post_username,post_desc,like_count;
        CircleImageView post_user_image;
        RelativeLayout react_layout;
        MaterialFavoriteButton like_button,share_button,comment_button;
        FrameLayout image_holder;
        ViewPager pager;
        DotsIndicator indicator;
        FrameLayout pager_layout;
        private RelativeLayout indicator_holder;
        TextView promote_text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            user_image=itemView.findViewById(R.id.user_image);
            image_holder=itemView.findViewById(R.id.image_holder);
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
            pager=itemView.findViewById(R.id.pager);
            pager_layout=itemView.findViewById(R.id.pager_layout);
            indicator = itemView.findViewById(R.id.indicator);
            indicator_holder = itemView.findViewById(R.id.indicator_holder);

            promote_text=itemView.findViewById(R.id.post_promote);

        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadTask extends AsyncTask<URL,Integer,List<Bitmap>> {

        ProgressDialog mProgressDialog;
        ViewHolder holder;
        Context context;

        public DownloadTask(Context context,ViewHolder holder) {
            this.context = context;
            this.holder = holder;
        }

        protected void onPreExecute(){
            mProgressDialog=new ProgressDialog(context);
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setTitle("Please wait");
            mProgressDialog.setMessage("We are processing the image for sharing...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            mProgressDialog.setProgress(0);
        }

        protected List<Bitmap> doInBackground(URL...urls){
            int count = urls.length;
            HttpURLConnection connection = null;
            List<Bitmap> bitmaps = new ArrayList<>();

            for(int i=0;i<count;i++){
                URL currentURL = urls[i];
                try{
                    connection = (HttpURLConnection) currentURL.openConnection();
                    connection.connect();
                    InputStream inputStream = connection.getInputStream();
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                    Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);
                    bitmaps.add(bmp);
                    publishProgress((int) (((i+1) / (float) count) * 100));
                    if(isCancelled()){
                        break;
                    }

                }catch(IOException e){
                    e.printStackTrace();
                }finally{
                    connection.disconnect();
                }
            }
            return bitmaps;
        }

        protected void onProgressUpdate(Integer... progress){
            mProgressDialog.setProgress(progress[0]);
        }

        // On AsyncTask cancelled
        protected void onCancelled(){
        }

        protected void onPostExecute(List<Bitmap> result){
            mProgressDialog.dismiss();
            for(int i=0;i<result.size();i++){
                Bitmap bitmap = result.get(i);

                WatermarkText watermarkText=new WatermarkText("VeloxYSoundZ")
                        .setPosition(new WatermarkPosition(0.5,0.5,30))
                        .setTextColor(Color.WHITE)
                        .setTextFont(R.font.bold)
                        .setTextAlpha(150)
                        .setTextSize(20);

                Bitmap watermarked_bitmap= WatermarkBuilder.create(context,bitmap)
                        .loadWatermarkText(watermarkText)
                        .setTileMode(true)
                        .getWatermark()
                        .getOutputImage();

                Uri imageInternalUri = getBitmapUri(watermarked_bitmap,feedList.get(holder.getAdapterPosition()).getUsername());

                try {
                    Intent intent = new Intent(Intent.ACTION_SEND)
                            .setType("image/*");
                    intent.putExtra(Intent.EXTRA_STREAM, imageInternalUri);
                    context.startActivity(Intent.createChooser(intent, "Share using..."));
                } catch (Exception e) {
                    Toasty.error(context,"Sharing failed", Toasty.LENGTH_SHORT,true).show();
                    e.printStackTrace();
                }

            }
        }
    }

    private URL stringToURL(String urlString){
        try{
            URL url = new URL(urlString);
            return url;
        }catch(MalformedURLException e){
            e.printStackTrace();
        }
        return null;
    }

}
