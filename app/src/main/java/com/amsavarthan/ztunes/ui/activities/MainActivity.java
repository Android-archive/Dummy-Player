package com.amsavarthan.ztunes.ui.activities;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.amsavarthan.ztunes.music.AudioStreamingManager;
import com.amsavarthan.ztunes.music.CurrentSessionCallback;
import com.amsavarthan.ztunes.music.MediaMetaData;
import com.amsavarthan.ztunes.utils.NetworkUtil;
import com.amsavarthan.ztunes.R;
import com.amsavarthan.ztunes.ui.fragments.FeedView;
import com.amsavarthan.ztunes.ui.fragments.HomeFragment;
import com.amsavarthan.ztunes.ui.fragments.NotificationsView;
import com.amsavarthan.ztunes.ui.fragments.ProfileView;
import com.amsavarthan.ztunes.ui.fragments.SearchView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.marcoscg.dialogsheet.DialogSheet;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import android.support.v4.media.session.PlaybackStateCompat;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.amsavarthan.ztunes.utils.NetworkUtil.NETWORK_STATUS_NOT_CONNECTED;


public class MainActivity extends AppCompatActivity implements CurrentSessionCallback {

    public static String mCurrentFragment;
    public static BottomNavigationView navigation;
    public SlidingUpPanelLayout slidingUpPanelLayout;
    private ImageView prev;
    private ImageView next;
    private ImageView close;
    private ImageView art,big_art,playPause;
    private ProgressBar buffer_progress;
    private FloatingActionButton playPauseFab;

    //For  Implementation
    private AudioStreamingManager streamingManager;
    private MediaMetaData currentSong;
    public List<MediaMetaData> listOfSongs = new ArrayList<MediaMetaData>();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.home:
                    showFragment(new HomeFragment(),"home");
                    setStatusLightTheme(getWindow().getDecorView());
                    if(slidingUpPanelLayout.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED) {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                    mCurrentFragment="home";
                    return true;
                case R.id.search:
                    showFragment(new SearchView(),"search");
                    setStatusLightTheme(getWindow().getDecorView());
                    if(slidingUpPanelLayout.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED) {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                    mCurrentFragment="search";
                    return true;
                case R.id.notification:
                    showFragment(new NotificationsView(),"notification");
                    setStatusLightTheme(getWindow().getDecorView());
                    if(slidingUpPanelLayout.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED) {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                    mCurrentFragment="notification";
                    return true;
                case R.id.news_feed:
                    showFragment(new FeedView(),"feed");
                    setStatusLightTheme(getWindow().getDecorView());
                    if(slidingUpPanelLayout.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED) {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                    mCurrentFragment="feed";
                    return true;
                case R.id.profile:
                    showFragment(new ProfileView(),"profile");
                    removeStatusLightTheme(getWindow().getDecorView());
                    if(slidingUpPanelLayout.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED) {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                    mCurrentFragment="profile";
                    return true;
            }
            return false;
        }
    };
    private BottomNavigationView.OnNavigationItemReselectedListener mOnNavigationItemReSelectedListener = new BottomNavigationView.OnNavigationItemReselectedListener() {

        @Override
        public void onNavigationItemReselected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.home:
                    setStatusLightTheme(getWindow().getDecorView());
                    if(slidingUpPanelLayout.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED) {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                    break;
                case R.id.search:
                    setStatusLightTheme(getWindow().getDecorView());
                    if(slidingUpPanelLayout.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED) {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                    break;
                case R.id.notification:
                    setStatusLightTheme(getWindow().getDecorView());
                    if(slidingUpPanelLayout.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED) {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                    break;
                case R.id.news_feed:
                    setStatusLightTheme(getWindow().getDecorView());
                    if(slidingUpPanelLayout.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED) {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                    break;
                case R.id.profile:
                    removeStatusLightTheme(getWindow().getDecorView());
                    if(slidingUpPanelLayout.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED) {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                    break;
            }
        }
    };
    private int dominant_color;
    private boolean isExpanded=false;
    private SeekBar seekbar;
    private TextView music_name,author_name,startTime,duration;
    public void startSong(MediaMetaData mediaMetaData){

        int status= NetworkUtil.getConnectivityStatus(this);

        if(status==NETWORK_STATUS_NOT_CONNECTED){
            Toasty.error(this,"Error connecting to the server, Please try again later...",Toasty.LENGTH_SHORT,true).show();
        }else{

            listOfSongs.add(mediaMetaData);
            configAudioStreamer();
            checkAlreadyPlaying(mediaMetaData);

        }
    }
    public void setStatusLightTheme(View view){

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){

            int flags=view.getSystemUiVisibility();
            flags|=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);

        }

    }
    public void removeStatusLightTheme(View view){

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){

            int flags=view.getSystemUiVisibility();
            flags&=~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);

        }
    }
    public void removeNavLightTheme(View view){

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

            int flags=view.getSystemUiVisibility();
            flags&=~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            view.setSystemUiVisibility(flags);

        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }
    @Override
    public void onBackPressed() {
        if(slidingUpPanelLayout.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED){
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/GoogleSans_Regular.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

        setContentView(R.layout.activity_main);

        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            LoginActivity.startActivity(MainActivity.this);
            finish();
        }else if(getSharedPreferences("AccountPref",MODE_PRIVATE).getString("account_type", "none").equals("none")){
            startActivity(new Intent(MainActivity.this, AccountTypeSelection.class));
            finish();
        }

        close=findViewById(R.id.close);
        prev=findViewById(R.id.prev);
        next=findViewById(R.id.next);
        art=findViewById(R.id.music_art);
        big_art=findViewById(R.id.bigArt);
        slidingUpPanelLayout=findViewById(R.id.slidingPanel);
        navigation=findViewById(R.id.navigation);
        buffer_progress=findViewById(R.id.buffer_progress);
        playPause=findViewById(R.id.playPause);
        playPauseFab=findViewById(R.id.playPauseFab);
        seekbar=findViewById(R.id.seekbar);
        music_name=findViewById(R.id.music_name);
        author_name=findViewById(R.id.music_author_name);
        startTime=findViewById(R.id.startTime);
        duration=findViewById(R.id.duration);

        configAudioStreamer();

        removeNavLightTheme(getWindow().getDecorView());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorAccent));
        }
        playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_24dp));
        playPauseFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_24dp));
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

                //navigation.setAlpha(1-slideOffset);

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

                if(previousState== SlidingUpPanelLayout.PanelState.DRAGGING && newState== SlidingUpPanelLayout.PanelState.EXPANDED) {
                    if(!isExpanded) {
                        Animation slide_down = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_down);
                        Animation expand_in = AnimationUtils.loadAnimation(MainActivity.this, R.anim.expand_in);
                        Animation fade_out = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);
                        fade_out.setDuration(200);
                        if(buffer_progress.getVisibility()!=View.VISIBLE) {
                            playPause.startAnimation(fade_out);
                            playPause.setVisibility(View.INVISIBLE);
                        }
                        //navigation.startAnimation(slide_down);
                        //navigation.setVisibility(View.INVISIBLE);
                        removeStatusLightTheme(getWindow().getDecorView());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            getWindow().setStatusBarColor(dominant_color);
                            getWindow().setNavigationBarColor(dominant_color);
                        }
                        isExpanded=true;
                    }
                }else if(previousState== SlidingUpPanelLayout.PanelState.DRAGGING && newState== SlidingUpPanelLayout.PanelState.COLLAPSED){
                    if(isExpanded){

                        Animation slide_up = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up);
                        Animation expand_in = AnimationUtils.loadAnimation(MainActivity.this, R.anim.expand_in);
                        Animation fade_out = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);
                        fade_out.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                if(buffer_progress.getVisibility()!=View.VISIBLE) {
                                    playPause.startAnimation(expand_in);
                                    playPause.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        fade_out.setDuration(200);
                        slide_up.setDuration(200);
                        expand_in.setDuration(200);
                        if(mCurrentFragment.equals("profile") || mCurrentFragment.equals("friend_profile")) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                getWindow().setStatusBarColor(Color.parseColor("#5F6BDF"));
                            }

                        }else {
                            setStatusLightTheme(getWindow().getDecorView());

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                            }

                        }
                        isExpanded=false;
                    }
                }

            }
        });

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setOnNavigationItemReselectedListener(mOnNavigationItemReSelectedListener);

        showFragment(new HomeFragment(),"home");

        if(NetworkUtil.getConnectivityStatus(this)==NETWORK_STATUS_NOT_CONNECTED){
            Toasty.error(this,"No connection",Toasty.LENGTH_LONG,true).show();
        }
        Animation expand_in = AnimationUtils.loadAnimation(MainActivity.this, R.anim.expand_in);
        close.startAnimation(expand_in);

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPauseEvent();
            }
        });

        playPauseFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPauseEvent();
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                streamingManager.onSkipToPrevious();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                streamingManager.onSkipToNext();
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogSheet(MainActivity.this)
                        .setTitle("Clear Queue")
                        .setMessage("Clearing queue would stop current playing song")
                        .setPositiveButton("Yes", new DialogSheet.OnPositiveClickListener() {
                            @Override
                            public void onClick(View v) {



                            }
                        })
                        .setNegativeButton("No", new DialogSheet.OnNegativeClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        })
                        .setColoredNavigationBar(true)
                        .setRoundedCorners(true)
                        .show();
            }
        });

    }

    private void playPauseEvent() {

        if (currentSong != null) {

            if (streamingManager.isPlaying()) {
                playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_24dp));
                playPauseFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_24dp));
                streamingManager.onPause();
            } else {
                playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_24dp));
                playPauseFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_24dp));
                streamingManager.onPlay(currentSong);
            }

        }

    }

    void showFragment(Fragment fragment,String tag){

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                .replace(R.id.container,fragment,tag)
                .commit();

    }

    public void configAudioStreamer() {
        streamingManager = AudioStreamingManager.getInstance(this);
        streamingManager.setPlayMultiple(true);
        streamingManager.setMediaList(listOfSongs);
        streamingManager.setShowPlayerNotification(true);
        streamingManager.setPendingIntentAct(getNotificationPendingIntent());
    }

    public void checkAlreadyPlaying(MediaMetaData mediaMetaData) {
        if (streamingManager.isPlaying()) {
            Toasty.success(this,"Added to queue: "+mediaMetaData.getMediaTitle(),Toasty.LENGTH_SHORT,true).show();
            currentSong = streamingManager.getCurrentAudio();
            if (currentSong != null) {
                currentSong.setPlayState(streamingManager.mLastPlaybackState);
                showMediaInfo(currentSong);
            }
        }else {
            playSong(mediaMetaData);
        }
    }

    public void playSong(MediaMetaData media) {
        if (streamingManager != null) {
            streamingManager.onPlay(media);
            showMediaInfo(media);
        }
    }

    private void showMediaInfo(MediaMetaData song) {


        currentSong=song;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            seekbar.setProgress(0,true);
        }else{
            seekbar.setProgress(0);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seekbar.setMin(0);
        }

        if(slidingUpPanelLayout.getPanelState()== SlidingUpPanelLayout.PanelState.HIDDEN){
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }

        music_name.setText(song.getMediaTitle());
        author_name.setText(song.getMediaArtist());

        Glide.with(this)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_song_art))
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .load(song.getMediaArt())
                .into(art);

        Glide.with(this)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_song_art))
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .load(song.getMediaArt())
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if(resource!=null){
                            Palette p=Palette.from(resource).generate();
                            dominant_color=p.getDarkMutedColor(getResources().getColor(R.color.colorAccent));

                            if(isExpanded){
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        getWindow().setStatusBarColor(dominant_color);
                                }
                                removeLightTheme(getWindow().getDecorView());
                            }

                            removeNavLightTheme(getWindow().getDecorView());
                            slidingUpPanelLayout.setBackgroundColor(dominant_color);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                getWindow().setNavigationBarColor(dominant_color);
                            }

                            navigation.setBackgroundColor(dominant_color);

                        }
                        return false;
                    }
                })
                .into(big_art);

    }

    public PendingIntent getNotificationPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction("openplayer");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }
    @Override
    public void onStart() {
        super.onStart();
        try {
            if (streamingManager != null) {
                streamingManager.subscribesCallBack(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onStop() {
        try {
            if (streamingManager != null) {
                streamingManager.unSubscribeCallBack();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        try {
            if (streamingManager != null) {
                streamingManager.unSubscribeCallBack();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public void removeLightTheme(View view){

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && Build.VERSION.SDK_INT<Build.VERSION_CODES.O){

            int flags=view.getSystemUiVisibility();
            flags&=~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);

        }else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

            int flags=view.getSystemUiVisibility();
            flags&=~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            flags&=~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            view.setSystemUiVisibility(flags);

        }

    }

    @Override
    public void updatePlaybackState(int state) {

        switch (state) {
            case PlaybackStateCompat.STATE_PLAYING:
                buffer_progress.setVisibility(View.INVISIBLE);

                playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_24dp));
                playPauseFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_24dp));
                if(isExpanded) {
                    playPause.setVisibility(View.VISIBLE);
                }else{
                    playPause.setVisibility(View.GONE);
                }

                if (currentSong != null) {
                    currentSong.setPlayState(PlaybackStateCompat.STATE_PLAYING);
                }
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                buffer_progress.setVisibility(View.INVISIBLE);

                playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_24dp));
                playPauseFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_24dp));
                if(isExpanded) {
                    playPause.setVisibility(View.VISIBLE);
                }else{
                    playPause.setVisibility(View.GONE);
                }

                if (currentSong != null) {
                    currentSong.setPlayState(PlaybackStateCompat.STATE_PAUSED);
                }
                break;
            case PlaybackStateCompat.STATE_NONE:
                currentSong.setPlayState(PlaybackStateCompat.STATE_NONE);
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                buffer_progress.setVisibility(View.INVISIBLE);
                seekbar.setProgress(0);
                if (currentSong != null) {
                    currentSong.setPlayState(PlaybackStateCompat.STATE_NONE);
                }
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                playPause.setVisibility(View.INVISIBLE);
                buffer_progress.setVisibility(View.VISIBLE);
                if (currentSong != null) {
                    currentSong.setPlayState(PlaybackStateCompat.STATE_NONE);
                }
                break;
        }

    }

    @Override
    public void playSongComplete() {

    }

    @Override
    public void currentSeekBarPosition(int progress) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            seekbar.setProgress(progress,true);
        }else{
            seekbar.setProgress(progress);
        }
    }

    @Override
    public void playCurrent(int indexP, MediaMetaData currentAudio) {
        showMediaInfo(currentAudio);
    }

    @Override
    public void playNext(int indexP, MediaMetaData currentAudio) {
        showMediaInfo(currentAudio);
    }

    @Override
    public void playPrevious(int indexP, MediaMetaData currentAudio) {
        showMediaInfo(currentAudio);
    }


}
