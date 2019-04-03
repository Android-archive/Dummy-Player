package com.amsavarthan.ztunes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import androidx.multidex.MultiDex;
import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import static com.amsavarthan.ztunes.NetworkUtil.NETWORK_STATUS_MOBILE;
import static com.amsavarthan.ztunes.NetworkUtil.NETWORK_STATUS_NOT_CONNECTED;


public class MainActivity extends AppCompatActivity {

    public static String mCurrentFragment;
    private BottomNavigationView navigation;
    public static SlidingUpPanelLayout slidingUpPanelLayout;
    @SuppressLint("StaticFieldLeak")
    public static ImageView art,bigArt;
    @SuppressLint("StaticFieldLeak")
    public static  TextView song_name,song_artist;
    @SuppressLint("StaticFieldLeak")
    public static FloatingActionButton playPauseFab;
    @SuppressLint("StaticFieldLeak")
    private static ImageView playPause;
    public static boolean playing;
    private static ProgressDialog progressDialog;
    private static boolean initialStage=true;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.home:
                    showFragment(new HomeFragment(),"home");
                    mCurrentFragment="home";
                    return true;
                case R.id.library:
                    showFragment(new LibraryView(),"library");
                    mCurrentFragment="library";
                    return true;
                case R.id.recents:
                    showFragment(new Recents_FullView(),"recents");
                    mCurrentFragment="recents";
                    return true;
                case R.id.album:
                    showFragment(new AlbumsView(),"album");
                    mCurrentFragment="album";
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
                case R.id.library:
                case R.id.recents:
                case R.id.album:
                case R.id.profile:
            }
        }
    };
    private static MediaPlayer mediaPlayer;

    public static void startSong(final Context context, final RecentsEntity recentsEntity){

        int status=NetworkUtil.getConnectivityStatus(context);

        if(status==NETWORK_STATUS_NOT_CONNECTED){
            Toasty.error(context,"Error connecting to the server, Please try again later...",Toasty.LENGTH_SHORT,true).show();
        }else if(status==NETWORK_STATUS_MOBILE){

            new DialogSheet(context)
                    .setTitle("Network type : Mobile data")
                    .setMessage("Are you sure do you want to play this song, Data charges may apply.")
                    .setColoredNavigationBar(true)
                    .setRoundedCorners(true)
                    .setPositiveButton("Continue", new DialogSheet.OnPositiveClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mediaPlayer != null && slidingUpPanelLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN) {

                                mediaPlayer.reset();
                                initialStage = true;
                                playing = false;

                                Glide.with(context)
                                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_song_art))
                                        .load(recentsEntity.getArt())
                                        .into(art);

                                Glide.with(context)
                                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_song_art))
                                        .load(recentsEntity.getArt())
                                        .into(bigArt);

                                song_name.setText(recentsEntity.getName());
                                song_artist.setText(recentsEntity.getArtist().replace(";", ","));
                                playSong(context, recentsEntity.getLink(), recentsEntity.getName());
                                playPause.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        playSong(context, recentsEntity.getLink(), recentsEntity.getName());
                                    }
                                });
                                playPauseFab.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        playSong(context, recentsEntity.getLink(), recentsEntity.getName());
                                    }
                                });

                            } else {

                                if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
                                    slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                }

                                Glide.with(context)
                                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_song_art))
                                        .load(recentsEntity.getArt())
                                        .into(art);

                                Glide.with(context)
                                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_song_art))
                                        .load(recentsEntity.getArt())
                                        .into(bigArt);

                                song_name.setText(recentsEntity.getName());
                                song_artist.setText(recentsEntity.getArtist().replace(";", ","));
                                playSong(context, recentsEntity.getLink(), recentsEntity.getName());
                                playing = true;
                                playPause.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        playSong(context, recentsEntity.getLink(), recentsEntity.getName());
                                    }
                                });
                                playPauseFab.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        playSong(context, recentsEntity.getLink(), recentsEntity.getName());
                                    }
                                });

                            }
                        }
                    })
                    .setNegativeButton("Set up WI-FI", new DialogSheet.OnNegativeClickListener() {
                        @Override
                        public void onClick(View v) {
                            context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    })
                    .show();


        }else{

            if (mediaPlayer != null && slidingUpPanelLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN) {

                mediaPlayer.reset();
                initialStage = true;
                playing = false;

                Glide.with(context)
                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_song_art))
                        .load(recentsEntity.getArt())
                        .into(art);

                Glide.with(context)
                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_song_art))
                        .load(recentsEntity.getArt())
                        .into(bigArt);

                song_name.setText(recentsEntity.getName());
                song_artist.setText(recentsEntity.getArtist().replace(";", ","));
                playSong(context, recentsEntity.getLink(), recentsEntity.getName());
                playPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playSong(context, recentsEntity.getLink(), recentsEntity.getName());
                    }
                });
                playPauseFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playSong(context, recentsEntity.getLink(), recentsEntity.getName());
                    }
                });

            } else {

                if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
                    slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }

                Glide.with(context)
                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_song_art))
                        .load(recentsEntity.getArt())
                        .into(art);

                Glide.with(context)
                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_song_art))
                        .load(recentsEntity.getArt())
                        .into(bigArt);

                song_name.setText(recentsEntity.getName());
                song_artist.setText(recentsEntity.getArtist().replace(";", ","));
                playSong(context, recentsEntity.getLink(), recentsEntity.getName());
                playing = true;
                playPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playSong(context, recentsEntity.getLink(), recentsEntity.getName());
                    }
                });
                playPauseFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playSong(context, recentsEntity.getLink(), recentsEntity.getName());
                    }
                });

            }

        }
    }

    public static void playSong(Context context,String song_link,String song_name){

        if(playing) {
            playPause.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow_24dp));
            playPauseFab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow_24dp));

            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
            }

            playing=false;
            Toasty.info(context,"Paused song : "+song_name,Toasty.LENGTH_SHORT,true).show();

        }else{

            if(initialStage){
                new Player(context,song_name).execute(song_link);
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
            startActivity(new Intent(this,LoginActivity.class));
            finish();
            return;
        }else if(getSharedPreferences("AccountPref",MODE_PRIVATE).getString("account_type", "none").equals("none")){
            startActivity(new Intent(this,AccountTypeSelection.class));
            finish();
            return;
        }

        art=findViewById(R.id.music_art);
        bigArt=findViewById(R.id.bigArt);
        song_name=findViewById(R.id.music_name);
        song_artist=findViewById(R.id.music_author_name);
        playPause=findViewById(R.id.playPause);
        playPauseFab=findViewById(R.id.playPauseFab);


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
                    playPause.startAnimation(expand_in);
                    playPause.setVisibility(View.VISIBLE);
                    navigation.startAnimation(slide_up);
                    navigation.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
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

    }

    void showFragment(Fragment fragment,String tag){

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                .replace(R.id.container,fragment,tag)
                .commit();

    }

    @Override
    protected void onPause() {
        super.onPause();

        /*if(mediaPlayer!=null){
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer=null;
        }*/

    }

    static class Player extends AsyncTask<String,Void,Boolean>{

        @SuppressLint("StaticFieldLeak")
        Context context;
        String song_name;

        Player(Context context, String song_name) {
            this.context=context;
            this.song_name=song_name;
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            boolean prepared;

            try{
                mediaPlayer.setDataSource(strings[0]);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        initialStage=true;
                        playing=false;
                        playPause.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow_24dp));
                        playPauseFab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow_24dp));
                        mediaPlayer.stop();
                        mediaPlayer.reset();
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
            Toasty.info(context,"Playing song : "+song_name,Toasty.LENGTH_SHORT,true).show();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setMessage("Buffering...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();


        }
    }

}
