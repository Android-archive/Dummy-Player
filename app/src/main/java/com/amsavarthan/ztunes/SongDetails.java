package com.amsavarthan.ztunes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import es.dmoral.toasty.Toasty;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SongDetails extends Fragment {

    private ImageView art;
    private String song_name,artist_name,album_name,genre_name,link,art_link;
    private TextView song_name_txt,artist_name_txt,album_name_txt,genre_name_txt;
    private LinearLayout artist_view,album_view,genre_view;
    private View view;
    private FloatingActionButton play;
    private RecentsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.fragment_song_details,container,false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getArguments()!=null){

            song_name=getArguments().getString("song_name");
            artist_name=getArguments().getString("artist_name");
            album_name=getArguments().getString("album_name");
            genre_name=getArguments().getString("genre_name");
            art_link=getArguments().getString("art");
            link=getArguments().getString("link");

        }

        art=view.findViewById(R.id.art);
        play=view.findViewById(R.id.fab);
        song_name_txt=view.findViewById(R.id.song_name);
        artist_name_txt=view.findViewById(R.id.artist_name);
        album_name_txt=view.findViewById(R.id.album_name);
        genre_name_txt=view.findViewById(R.id.genre_name);
        artist_view=view.findViewById(R.id.artist_view);
        album_view=view.findViewById(R.id.album_view);
        genre_view=view.findViewById(R.id.genre_view);

        song_name_txt.setText(song_name);
        artist_name_txt.setText(artist_name.replace(";",","));
        album_name_txt.setText(album_name);
        genre_name_txt.setText(genre_name);

        Glide.with(this)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_song_art))
                .load(art_link)
                .into(art);

        artist_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle=new Bundle();
                bundle.putString("name",artist_name);

                Fragment fragment=new Artist_SongsView();
                fragment.setArguments(bundle);

                ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                        .replace(R.id.container,fragment,"Artist_SongsView")
                        .addToBackStack(null)
                        .commit();

                MainActivity.mCurrentFragment="album";            }
        });

        genre_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle=new Bundle();
                bundle.putString("name",genre_name);

                Fragment fragment=new GenreView();
                fragment.setArguments(bundle);

                ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                        .replace(R.id.container,fragment,"GenreView")
                        .addToBackStack(null)
                        .commit();

                MainActivity.mCurrentFragment="genre";

            }
        });

        album_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle=new Bundle();
                bundle.putString("name",album_name);

                Fragment fragment=new Album_SongsView();
                fragment.setArguments(bundle);

                ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.activity_expand_in,R.anim.fade_out)
                        .replace(R.id.container,fragment,"Album_SongsView")
                        .addToBackStack(null)
                        .commit();

                MainActivity.mCurrentFragment="album";
            }
        });

        viewModel= ViewModelProviders.of(this).get(RecentsViewModel.class);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RecentsEntity recentsEntity=new RecentsEntity();
                recentsEntity.setName(song_name);
                recentsEntity.setLink(link);
                recentsEntity.setAlbum(album_name);
                recentsEntity.setArt(art_link);
                recentsEntity.setArtist(artist_name);
                recentsEntity.setGenre(genre_name);

                String nameInDatabase=viewModel.findSongByName(song_name);
                if(!nameInDatabase.equals("null")){
                    viewModel.deletePost(viewModel.findSongEntityByName(song_name));
                    viewModel.savePost(recentsEntity);
                }else{
                    viewModel.savePost(recentsEntity);
                }

                HomeFragment.HideDefaultCard(view.getContext());

                MainActivity.startSong(view.getContext(),recentsEntity);
            }
        });


    }

}
