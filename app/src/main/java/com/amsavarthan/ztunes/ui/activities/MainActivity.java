package com.amsavarthan.ztunes.ui.activities;


import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.amsavarthan.ztunes.adapters.NewReleaseAdapter;
import com.amsavarthan.ztunes.adapters.QueueAdapter;
import com.amsavarthan.ztunes.adapters.SongsAdapter;
import com.amsavarthan.ztunes.music.AudioStreamingManager;
import com.amsavarthan.ztunes.music.CurrentSessionCallback;
import com.amsavarthan.ztunes.music.MediaMetaData;
import com.amsavarthan.ztunes.room.RecentsViewModel;
import com.amsavarthan.ztunes.ui.customviews.LineProgress;
import com.amsavarthan.ztunes.ui.customviews.Slider;
import com.amsavarthan.ztunes.ui.fragments.QueueView;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.marcoscg.dialogsheet.DialogSheet;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import android.os.Parcelable;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static com.amsavarthan.ztunes.utils.NetworkUtil.NETWORK_STATUS_NOT_CONNECTED;


public class MainActivity extends AppCompatActivity implements CurrentSessionCallback, Slider.OnValueChangedListener {

    public static String mCurrentFragment;
    public static BottomNavigationView navigation;
    public SlidingUpPanelLayout slidingUpPanelLayout;
    private ImageView art,close,next,prev,big_art,playPause,repeat,shuffle,queue;
    private ProgressBar buffer_progress;
    private FloatingActionButton playPauseFab;
    private Slider audioPg;
    private ScrollView scrollview;
    private AudioStreamingManager streamingManager;
    private MediaMetaData currentSong;
    private List<MediaMetaData> songsQueueList=new ArrayList<>();
    private QueueAdapter queueAdapter;
    private RecentsViewModel viewModel;
    public static MainActivity activity;

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
                    showFragment(new FeedView(), "feed");
                    setStatusLightTheme(getWindow().getDecorView());
                    if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                    mCurrentFragment = "feed";
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
                case R.id.search:
                case R.id.notification:
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
    private TextView music_name,author_name,startTime,duration;
    private FirebaseAuth mAuth;

    public void startSong(MediaMetaData mediaMetaData){

        int status= NetworkUtil.getConnectivityStatus(this);

        if(status==NETWORK_STATUS_NOT_CONNECTED){
            Toasty.error(this,"Error connecting to the server, Please try again later...",Toasty.LENGTH_SHORT,true).show();
        }else{

            songsQueueList.add(mediaMetaData);
            queueAdapter.notifyDataSetChanged();
            streamingManager.setMediaList(songsQueueList);
            checkAlreadyPlaying(mediaMetaData);

        }
    }

    public static void setStatusLightTheme(View view){

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){

            int flags=view.getSystemUiVisibility();
            flags|=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);

        }

    }

    public static void removeStatusLightTheme(View view){

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


        //[changed for pitch]

        activity=this;
        mAuth=FirebaseAuth.getInstance();

        /*if(FirebaseAuth.getInstance().getCurrentUser()==null){
            LoginActivity.startActivity(MainActivity.this);
            finish();
        }else if(getSharedPreferences("AccountPref",MODE_PRIVATE).getString("account_type", "none").equals("none")){
            startActivity(new Intent(MainActivity.this, AccountTypeSelection.class));
            finish();
        }*/

        if(getSharedPreferences("AccountPref",MODE_PRIVATE).getString("account_type", "null").equals("none")){
            startActivity(new Intent(MainActivity.this, AccountTypeSelection.class));
        }

        //[changed for pitch]

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
        music_name=findViewById(R.id.music_name);
        author_name=findViewById(R.id.music_author_name);
        startTime=findViewById(R.id.startTime);
        duration=findViewById(R.id.duration);
        shuffle=findViewById(R.id.shuffle);
        repeat=findViewById(R.id.repeat);
        audioPg=findViewById(R.id.slider);
        scrollview=findViewById(R.id.scrollview);
        queue=findViewById(R.id.queue);

        viewModel = ViewModelProviders.of(this).get(RecentsViewModel.class);

        queueAdapter=new QueueAdapter(this,songsQueueList,viewModel);
        queueAdapter.setItemListener(media -> playSong(media));

        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.setSmoothScrollbarEnabled(true);

        configAudioStreamer();
        String timeString = "00:00";
        startTime.setText(timeString);
        duration.setText(timeString);
        audioPg.setValue(0);
        audioPg.setOnValueChangedListener(this);

        removeNavLightTheme(getWindow().getDecorView());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorAccent));
        }
        queue.setVisibility(GONE);
        playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_24dp));
        playPauseFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_24dp));
        mCurrentFragment="home";
        checkAlreadyPlaying(null);
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

                //navigation.setAlpha(1-slideOffset);

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

                if(previousState== SlidingUpPanelLayout.PanelState.DRAGGING && newState== SlidingUpPanelLayout.PanelState.EXPANDED) {
                    if(!isExpanded) {
                        Animation expand_in = AnimationUtils.loadAnimation(MainActivity.this, R.anim.expand_in);
                        Animation fade_out = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);
                        fade_out.setDuration(200);
                        if(buffer_progress.getVisibility()!=View.VISIBLE) {
                            playPause.startAnimation(fade_out);
                            playPause.setVisibility(View.INVISIBLE);
                            queue.startAnimation(expand_in);
                            queue.setVisibility(View.VISIBLE);
                        }
                        removeStatusLightTheme(getWindow().getDecorView());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            getWindow().setStatusBarColor(dominant_color);
                            getWindow().setNavigationBarColor(dominant_color);
                        }
                        isExpanded=true;
                    }
                }else if(previousState== SlidingUpPanelLayout.PanelState.DRAGGING && newState== SlidingUpPanelLayout.PanelState.COLLAPSED){
                    if(isExpanded){

                        scrollview.scrollTo(0,0);
                        Animation fade_out = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);
                        Animation expand_in = AnimationUtils.loadAnimation(MainActivity.this, R.anim.expand_in);
                        expand_in.setDuration(200);

                        if(buffer_progress.getVisibility()!=View.VISIBLE) {
                            queue.startAnimation(fade_out);
                            queue.setVisibility(GONE);
                            playPause.startAnimation(expand_in);
                            playPause.setVisibility(View.VISIBLE);
                        }

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

        playPause.setOnClickListener(v -> playPauseEvent());

        queue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mCurrentFragment!="queue"){
                    Bundle args=new Bundle();
                    args.putParcelableArrayList("queue", (ArrayList<? extends Parcelable>) songsQueueList);

                    Fragment fragment=new QueueView();
                    fragment.setArguments(args);


                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.slide_up,R.anim.fade_out)
                            .addToBackStack(null)
                            .replace(R.id.container,fragment,"queue")
                            .commit();

                    slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    mCurrentFragment="queue";
                }

            }
        });

        playPauseFab.setOnClickListener(v -> playPauseEvent());

        prev.setOnClickListener(v -> {
            streamingManager.onSkipToPrevious();
        });

        next.setOnClickListener(v -> {
            streamingManager.onSkipToNext();
        });

        repeat.setOnClickListener(v -> {

            if(currentSong!=null) {
                if (streamingManager.getIsRepeatSong()) {
                    Toasty.success(getApplicationContext(),"Repeat off",Toasty.LENGTH_SHORT,true).show();
                    streamingManager.setRepeatSong(null);
                    repeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_one_trans_white_24dp));
                } else {
                    Toasty.success(getApplicationContext(),"Song will repeated once again",Toasty.LENGTH_SHORT,true).show();
                    streamingManager.setRepeatSong(currentSong);
                    repeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_one_white_24dp));
                }
            }

        });

        close.setOnClickListener(v -> new DialogSheet(MainActivity.this)
                .setTitle("Clear Queue")
                .setMessage("Clearing queue would stop current playing song")
                .setPositiveButton("Yes", v1 -> {

                   stopSong();

                })
                .setNegativeButton("No", v12 -> {

                })
                .setColoredNavigationBar(true)
                .setRoundedCorners(true)
                .show());

    }

    public void stopSong() {

        streamingManager.cleanupPlayer(MainActivity.this,true,true);
        streamingManager.clearList();
        streamingManager.setRepeatSong(null);
        songsQueueList.clear();
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

        navigation.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorAccent));
        }
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        buffer_progress.setVisibility(View.INVISIBLE);

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
                .setCustomAnimations(R.anim.slide_up,R.anim.fade_out)
                .replace(R.id.container,fragment,tag)
                .commit();

    }

    public void configAudioStreamer() {
        streamingManager = AudioStreamingManager.getInstance(this);
        streamingManager.setPlayMultiple(true);
        songsQueueList.addAll(streamingManager.getMediaList());
        if(!streamingManager.getIsRepeatSong()) {
            streamingManager.setRepeatSong(null);
        }
        streamingManager.setShowPlayerNotification(true);
        streamingManager.setPendingIntentAct(getNotificationPendingIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAlreadyPlaying(null);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkAlreadyPlaying(null);
    }

    public void checkAlreadyPlaying(MediaMetaData mediaMetaData) {
        if (streamingManager.isPlaying()) {
            currentSong = streamingManager.getCurrentAudio();

            if(mediaMetaData!=null)
                Toasty.success(this,"Added to queue: "+mediaMetaData.getMediaTitle(),Toasty.LENGTH_SHORT,true).show();

            if (currentSong != null) {
                currentSong.setPlayState(streamingManager.mLastPlaybackState);
                showMediaInfo(currentSong,false);
            }
        }else {
            if(mediaMetaData!=null)
                playSong(mediaMetaData);
            else {
                if(!songsQueueList.isEmpty()){
                    showMediaInfo(songsQueueList.get(0),false);
                }else{

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

                    navigation.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorAccent));
                    }
                    slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                    buffer_progress.setVisibility(View.INVISIBLE);

                }
            }
        }
    }

    public void playSong(MediaMetaData media) {
        if (streamingManager != null) {
            queueAdapter.notifyDataSetChanged();
            streamingManager.onPlay(media);
            showMediaInfo(media,false);
        }
    }

    private void showMediaInfo(MediaMetaData song,boolean checkOnStart) {

        currentSong=song;

        audioPg.setValue(0);
        audioPg.setMin(0);
        audioPg.setMax((int)currentSong.getMediaDuration());

        long song_duration=currentSong.getMediaDuration()/1000;
        long minute=song_duration/60;
        long second=song_duration-(minute*60);
        duration.setText(minute + ":" + second);

        if(checkOnStart) {
            isExpanded=true;
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        }else{
            if(slidingUpPanelLayout.getPanelState()==SlidingUpPanelLayout.PanelState.HIDDEN) {
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        }

        if(streamingManager.getIsRepeatSong()){

            if(streamingManager.getRepeatSong().getMediaId().equals(currentSong.getMediaId())){
                repeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_one_white_24dp));
            }else{
                repeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_one_trans_white_24dp));
            }

        }else{
            repeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_one_trans_white_24dp));
        }

        music_name.setText(song.getMediaTitle());
        author_name.setText(song.getMediaArtist());
        dominant_color=getResources().getColor(R.color.colorAccent);

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

                if(isExpanded){
                   queue.setVisibility(View.VISIBLE);
                   playPause.setVisibility(View.GONE);
                }else{
                    queue.setVisibility(View.GONE);
                    playPause.setVisibility(View.VISIBLE);
                }

                playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_24dp));
                playPauseFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_24dp));

               if (currentSong != null) {
                    currentSong.setPlayState(PlaybackStateCompat.STATE_PLAYING);
                    showMediaInfo(currentSong,false);
                }
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                buffer_progress.setVisibility(View.INVISIBLE);

                playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_24dp));
                playPauseFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_24dp));

                if(isExpanded){
                    queue.setVisibility(View.VISIBLE);
                    playPause.setVisibility(View.GONE);
                }else{
                    queue.setVisibility(View.GONE);
                    playPause.setVisibility(View.VISIBLE);
                }

                if (currentSong != null) {
                    currentSong.setPlayState(PlaybackStateCompat.STATE_PAUSED);
                    showMediaInfo(currentSong,false);
                }
                break;
            case PlaybackStateCompat.STATE_NONE:
                currentSong.setPlayState(PlaybackStateCompat.STATE_NONE);
                break;
            case PlaybackStateCompat.STATE_STOPPED:

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

                navigation.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setNavigationBarColor(getResources().getColor(R.color.colorAccent));
                }
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                buffer_progress.setVisibility(View.INVISIBLE);
                if (currentSong != null) {
                    currentSong.setPlayState(PlaybackStateCompat.STATE_NONE);
                }
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                queue.setVisibility(View.INVISIBLE);
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
        String timeString = "00.00";
        startTime.setText(timeString);
        duration.setText(timeString);
        audioPg.setValue(0);
    }

    @Override
    public void currentSeekBarPosition(int progress) {
        audioPg.setValue(progress);
        setPGTime(progress);
    }

    @Override
    public void playCurrent(int indexP, MediaMetaData currentAudio) {
        showMediaInfo(currentAudio,false);
    }

    @Override
    public void playNext(int indexP, MediaMetaData currentAudio) {
        showMediaInfo(currentAudio,false);
    }

    @Override
    public void playPrevious(int indexP, MediaMetaData currentAudio) {
        showMediaInfo(currentAudio,false);
    }

    @Override
    public void onValueChanged(int value) {
        streamingManager.onSeekTo(value);
        streamingManager.scheduleSeekBarUpdate();
    }

    private void setPGTime(int progress) {
        try {
            String timeString = "00.00";
            int linePG = 0;
            currentSong = streamingManager.getCurrentAudio();
            if (currentSong != null && progress != currentSong.getMediaDuration()) {
                timeString = DateUtils.formatElapsedTime(progress / 1000);
                Long audioDuration = currentSong.getMediaDuration();
                linePG = (int) (((progress / 1000) * 100) / audioDuration);
            }
            startTime.setText(timeString);
            //lineProgress.setLineProgress(linePG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
