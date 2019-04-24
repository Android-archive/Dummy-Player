package com.amsavarthan.ztunes;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.marcoscg.dialogsheet.DialogSheet;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.amsavarthan.ztunes.NetworkUtil.NETWORK_STATUS_MOBILE;
import static com.amsavarthan.ztunes.NetworkUtil.NETWORK_STATUS_NOT_CONNECTED;


public class MainActivity extends AppCompatActivity{

    public static String mCurrentFragment;
    public static BottomNavigationView navigation;
    public static SlidingUpPanelLayout slidingUpPanelLayout;
    @SuppressLint("StaticFieldLeak")
    public static ImageView art,bigArt;
    @SuppressLint("StaticFieldLeak")
    public static  TextView song_name,song_artist;
    @SuppressLint("StaticFieldLeak")
    public static FloatingActionButton playPauseFab;
    @SuppressLint("StaticFieldLeak")
    private static ImageView playPause;
    private static ImageView prev;
    private static ImageView next;
    public static boolean playing;
    private ImageView close;
    private static ProgressDialog progressDialog;
    public static boolean initialStage=true;
    private static List<RecentsEntity> playList=new ArrayList<>();
    private static int song_index;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.home:
                    showFragment(new HomeFragment(),"home");
                    mCurrentFragment="home";
                    return true;
                case R.id.search:
                    showFragment(new SearchView(),"search");
                    mCurrentFragment="search";
                    return true;
                case R.id.notification:
                    showFragment(new NotificationsView(),"notification");
                    mCurrentFragment="notification";
                    return true;
                case R.id.news_feed:
                    showFragment(new FeedView(),"feed");
                    mCurrentFragment="feed";
                    return true;
                case R.id.profile:
                    showFragment(new ProfileView(),"profile");
                    mCurrentFragment="profile";
                    return true;
            }
            return false;
        }
    };

    private BottomNavigationView.OnNavigationItemReselectedListener mOnNavigationItemReSelectedListener
            = new BottomNavigationView.OnNavigationItemReselectedListener() {

        @Override
        public void onNavigationItemReselected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.home:
                case R.id.search:
                case R.id.notification:
                case R.id.news_feed:
                case R.id.profile:
            }
        }
    };
    public static MediaPlayer mediaPlayer;

    public static void startSong(final Context context, final RecentsEntity recentsEntity){

        int status=NetworkUtil.getConnectivityStatus(context);

        if(status==NETWORK_STATUS_NOT_CONNECTED){
            Toasty.error(context,"Error connecting to the server, Please try again later...",Toasty.LENGTH_SHORT,true).show();
        }else{

            if (mediaPlayer != null && slidingUpPanelLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN) {

                playList.add(recentsEntity);
                if(mediaPlayer.isPlaying()) {
                    Toasty.success(context, recentsEntity.getName() + " added to current queue", Toasty.LENGTH_SHORT, true).show();
                }else{
                    ++song_index;
                    new Player(context
                            ,recentsEntity.getName()
                            ,recentsEntity.getLink()
                            ,recentsEntity.getArt()
                            ,recentsEntity.getAlbum()
                            ,recentsEntity.getArtist()).execute();
                }

            } else {

                song_index=0;
                playList.add(recentsEntity);
                playSong(context, recentsEntity);

                playing = true;
                playPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playSong(context, recentsEntity);
                    }
                });
                playPauseFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playSong(context, recentsEntity);
                    }
                });


            }

        }
    }

    public static void stopSong(Context context){

        if(playing) {
            playPause.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow_24dp));
            playPauseFab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow_24dp));

            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
            }

            playing=false;

        }

    }

    public static void playSong(Context context,RecentsEntity recentsEntity){

        if(playing) {
            playPause.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow_24dp));
            playPauseFab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow_24dp));

            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
            }

            playing=false;
            Toasty.info(context,"Paused song : "+recentsEntity.getName(),Toasty.LENGTH_SHORT,false).show();

        }else{

            if(initialStage){
                new Player(context
                        ,recentsEntity.getName()
                        ,recentsEntity.getLink()
                        ,recentsEntity.getArt()
                        ,recentsEntity.getAlbum()
                        ,recentsEntity.getArtist()).execute();
            }else{
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                }
            }

            playPause.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pause_24dp));
            playPauseFab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pause_24dp));
            playing=true;
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
        }else {

            /*switch (mCurrentFragment){

                case "home":
                    navigation.setSelectedItemId(R.id.home);
                    break;
                case "search":
                    navigation.setSelectedItemId(R.id.search);
                    break;
                case "notification":
                    navigation.setSelectedItemId(R.id.notification);
                    break;
                case "feed":
                    navigation.setSelectedItemId(R.id.news_feed);
                    break;
                case "profile":
                    navigation.setSelectedItemId(R.id.profile);
                    break;
                case "friend_profile":
                    navigation.setSelectedItemId(R.id.search);
                    break;
                case "manage_songs":
                    navigation.setSelectedItemId(R.id.profile);
                    break;
                case "add_album":
                    navigation.setSelectedItemId(R.id.profile);
                    break;
                case "add_song":
                    navigation.setSelectedItemId(R.id.profile);
                    break;
                case "select_album":
                    navigation.setSelectedItemId(R.id.profile);
                    break;
            }*/

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
            startActivity(new Intent(MainActivity.this,AccountTypeSelection.class));
            finish();
        }

        art=findViewById(R.id.music_art);
        bigArt=findViewById(R.id.bigArt);
        song_name=findViewById(R.id.music_name);
        song_artist=findViewById(R.id.music_author_name);
        playPause=findViewById(R.id.playPause);
        playPauseFab=findViewById(R.id.playPauseFab);
        close=findViewById(R.id.close);
        prev=findViewById(R.id.prev);
        next=findViewById(R.id.next);

        navigation = findViewById(R.id.navigation);

        slidingUpPanelLayout=findViewById(R.id.slidingPanel);

        mediaPlayer=new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        playing=false;
        progressDialog=new ProgressDialog(this);

        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

                if(newState== SlidingUpPanelLayout.PanelState.EXPANDED) {
                    Animation slide_down = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_down);
                    Animation fade_out = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);
                    fade_out.setDuration(200);
                    playPause.startAnimation(fade_out);
                    playPause.setVisibility(View.INVISIBLE);
                    navigation.startAnimation(slide_down);
                    navigation.setVisibility(View.INVISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setStatusBarColor(getResources().getColor(R.color.grey));
                    }
                }

                if(newState== SlidingUpPanelLayout.PanelState.COLLAPSED){
                    Animation slide_up = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up);
                    Animation expand_in = AnimationUtils.loadAnimation(MainActivity.this, R.anim.expand_in);
                    expand_in.setDuration(200);
                    playPause.startAnimation(expand_in);
                    playPause.setVisibility(View.VISIBLE);
                    navigation.startAnimation(slide_up);
                    navigation.setVisibility(View.VISIBLE);
                    if(mCurrentFragment.equals("profile") || mCurrentFragment.equals("friend_profile")) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            getWindow().setStatusBarColor(Color.parseColor("#5F6BDF"));
                        }

                    }else{

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                        }

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

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    try {

                        --song_index;
                        new Player(MainActivity.this
                                , playList.get(song_index).getName()
                                , playList.get(song_index).getLink()
                                , playList.get(song_index).getArt()
                                , playList.get(song_index).getAlbum()
                                , playList.get(song_index).getArtist()).execute();
                    } catch (IndexOutOfBoundsException e) {
                        ++song_index;
                        e.printStackTrace();
                    }

               }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    try {

                        ++song_index;
                        new Player(MainActivity.this
                                , playList.get(song_index).getName()
                                , playList.get(song_index).getLink()
                                , playList.get(song_index).getArt()
                                , playList.get(song_index).getAlbum()
                                , playList.get(song_index).getArtist()).execute();
                    } catch (Exception e) {
                        --song_index;
                        e.printStackTrace();
                    }

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
                                Animation slide_up = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up);
                                navigation.startAnimation(slide_up);
                                navigation.setVisibility(View.VISIBLE);

                                initialStage=true;
                                playing=false;
                                playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_24dp));
                                playPauseFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_24dp));
                                mediaPlayer.stop();
                                mediaPlayer.reset();

                                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                                if(mCurrentFragment.equals("profile") || mCurrentFragment.equals("friend_profile")) {

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        getWindow().setStatusBarColor(Color.parseColor("#5F6BDF"));
                                    }

                                }else{

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                                    }

                                }
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

    void showFragment(Fragment fragment,String tag){

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                .replace(R.id.container,fragment,tag)
                .commit();

    }

    static class Player extends AsyncTask<String,Void,Boolean>{

        @SuppressLint("StaticFieldLeak")
        Context context;
        String songName;
        String song_link;
        String song_album;
        String song_art;
        String songArtist;

        Player(Context context, String songName, String song_link,String song_art,String song_album,String songArtist) {
            this.context=context;
            this.songName=songName;
            this.song_link=song_link;
            this.song_album=song_album;
            this.song_art=song_art;
            this.songArtist=songArtist;
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            boolean prepared;

            try{
                mediaPlayer.setDataSource(song_link);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {

                        try {

                            ++song_index;
                            new Player(context
                                    , playList.get(song_index).getName()
                                    , playList.get(song_index).getLink()
                                    , playList.get(song_index).getArt()
                                    , playList.get(song_index).getAlbum()
                                    , playList.get(song_index).getArtist()).execute();
                        } catch (IndexOutOfBoundsException e) {
                            --song_index;
                            initialStage = true;
                            playing = false;
                            playPause.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow_24dp));
                            playPauseFab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow_24dp));
                            mediaPlayer.stop();
                            mediaPlayer.reset();
                        }

                    }
                });

                mediaPlayer.prepare();
                prepared=true;

            } catch (IOException e) {
                e.printStackTrace();
                prepared=false;
            }

            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if(progressDialog.isShowing()){
                progressDialog.cancel();
            }

            mediaPlayer.start();
            initialStage=false;
            playing = true;
            playPause.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pause_24dp));
            playPauseFab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pause_24dp));
            try {
                Toasty.info(context, "Playing song : " + songName, Toasty.LENGTH_SHORT, true).show();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            initialStage = true;
            playing = false;
            mediaPlayer.stop();
            mediaPlayer.reset();

            try {
                progressDialog.setMessage("Buffering...");
                progressDialog.setIndeterminate(true);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
                    slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }

                Glide.with(context)
                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_song_art))
                        .load(song_art)
                        .into(art);

                Glide.with(context)
                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_song_art))
                        .load(song_art)
                        .into(bigArt);

                song_name.setText(songName);
                song_artist.setText(songArtist.replace(";", ","));

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

}
