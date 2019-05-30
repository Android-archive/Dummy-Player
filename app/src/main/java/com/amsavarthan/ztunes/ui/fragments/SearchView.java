package com.amsavarthan.ztunes.ui.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import es.dmoral.toasty.Toasty;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amsavarthan.ztunes.R;
import com.amsavarthan.ztunes.room.RecentsViewModel;
import com.amsavarthan.ztunes.models.Songs;
import com.amsavarthan.ztunes.models.Users;
import com.amsavarthan.ztunes.adapters.AlbumsAdapter;
import com.amsavarthan.ztunes.adapters.ArtistAdapter;
import com.amsavarthan.ztunes.adapters.LibraryAdapter;
import com.amsavarthan.ztunes.adapters.UsersAdapter;
import com.amsavarthan.ztunes.models.Album;
import com.amsavarthan.ztunes.models.Artist;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchView extends Fragment {


    View view;
    SwipeRefreshLayout refreshLayout;
    TextInputEditText searchQueryText;
    String searchQuery,type,search_type;
    Chip c1,c2,c3,c4;
    FloatingActionButton searchFab;
    LinearLayout default_item;
    TextView default_title,default_text;
    RecentsViewModel viewModel;
    RecyclerView mRecyclerView;

    List<Songs> songsList=new ArrayList<>();
    LibraryAdapter songsListsAdapter;

    List<Album> albums=new ArrayList<>();
    AlbumsAdapter albumsAdapter;

    List<Artist> artistList=new ArrayList<>();
    ArtistAdapter artistAdapter;

    List<Users> usersList=new ArrayList<>();
    UsersAdapter usersAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.fragment_search_view,container,false);
    }

    private static void clearLightStatusBar(Activity activity){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            activity.getWindow().setStatusBarColor(Color.parseColor("#ffffff"));
        }
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        clearLightStatusBar(getActivity());

        searchQueryText=view.findViewById(R.id.searchQueryText);
        searchFab=view.findViewById(R.id.logo);

        c1=view.findViewById(R.id.c1);
        c2=view.findViewById(R.id.c2);
        c3=view.findViewById(R.id.c3);
        c4=view.findViewById(R.id.c4);

        refreshLayout=view.findViewById(R.id.refreshLayout);
        default_item=view.findViewById(R.id.default_item);
        default_title=view.findViewById(R.id.default_title);
        default_text=view.findViewById(R.id.default_text);

        viewModel= ViewModelProviders.of(this).get(RecentsViewModel.class);

        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);

        c1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.equals(type,"users")) {

                    searchQuery=searchQueryText.getText().toString();
                    type = "users";

                    if(TextUtils.isEmpty(searchQuery)){
                        loadItems();
                    }else{
                        loadItems(searchQuery);
                    }

                }
            }
        });

        c2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(!TextUtils.equals(type,"artists")) {

                    searchQuery=searchQueryText.getText().toString();
                    type = "artists";

                    if(TextUtils.isEmpty(searchQuery)){
                        loadItems();
                    }else{
                        loadItems(searchQuery);
                    }

                }
            }
        });

        c3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.equals(type,"songs")) {

                    searchQuery=searchQueryText.getText().toString();
                    type = "songs";

                    if(TextUtils.isEmpty(searchQuery)){
                        loadItems();
                    }else{
                        loadItems(searchQuery);
                    }

                }
            }
        });

        c4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.equals(type,"album")) {

                    searchQuery=searchQueryText.getText().toString();
                    type = "album";

                    if(TextUtils.isEmpty(searchQuery)){
                        loadItems();
                    }else{
                        loadItems(searchQuery);
                    }

                }
            }
        });

        type="users";
        search_type="search";

        searchFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TextUtils.equals(search_type,"search")) {

                    searchQuery = searchQueryText.getText().toString();
                    if (TextUtils.isEmpty(searchQuery)) {
                        return;
                    }

                    loadItems(searchQuery);

                }else{

                    searchQueryText.setText("");
                    searchFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_black_24dp));
                    loadItems();
                    search_type="search";

                }
            }
        });

        searchQueryText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(search_type.equals("clear")){
                    searchFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_black_24dp));
                    search_type="search";
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        loadItems();

    }

    private void loadItems(final String searchQuery){

        searchFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_clear_white_24dp));
        search_type="clear";
        default_item.setVisibility(View.GONE);

        switch (type){

            case "users":
                refreshLayout.setRefreshing(true);
                usersList.clear();
                getUsers(searchQuery);
                return;
            case "artists":
                refreshLayout.setRefreshing(true);
                artistList.clear();
                getArtists(searchQuery);
                return;
            case "songs":
                refreshLayout.setRefreshing(true);
                songsList.clear();
                getSongs(searchQuery);
                return;
            case "album":
                refreshLayout.setRefreshing(true);
                albums.clear();
                getAlbums(searchQuery);

        }

    }

    private void loadItems() {

        default_item.setVisibility(View.GONE);
        switch (type){

            case "users":
                refreshLayout.setRefreshing(true);
                usersList.clear();
                getUsers();
                return;
            case "artists":
                refreshLayout.setRefreshing(true);
                artistList.clear();
                getArtists();
                return;
            case "songs":
                refreshLayout.setRefreshing(true);
                songsList.clear();
                getSongs();
                return;
            case "album":
                refreshLayout.setRefreshing(true);
                albums.clear();
                getAlbums();

        }

    }

    private void getArtists() {

        artistAdapter=new ArtistAdapter(artistList,view.getContext(),true);
        setUpRefreshLayoutforArtists();

        LinearLayoutManager layoutManager=new LinearLayoutManager(view.getContext());
        layoutManager.setSmoothScrollbarEnabled(true);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(artistAdapter);

        FirebaseFirestore.getInstance()
                .collection("Verified Artists")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(queryDocumentSnapshots.isEmpty()) {

                            refreshLayout.setRefreshing(false);
                            default_item.setVisibility(View.VISIBLE);
                            default_title.setText("No artists found");
                            default_text.setText("Sorry we couldn't find any artists");

                        }else{

                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {

                                if(!documentChange.getDocument().getString("id").equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    refreshLayout.setRefreshing(false);
                                    view.findViewById(R.id.default_item).setVisibility(View.GONE);
                                    Artist artist = documentChange.getDocument().toObject(Artist.class).withId(documentChange.getDocument().getString("id"));
                                    artistList.add(artist);
                                    artistAdapter.notifyDataSetChanged();
                                }
                            }

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        refreshLayout.setRefreshing(false);
                        Toasty.error(view.getContext(),"Some technical error occured",Toasty.LENGTH_LONG,true).show();
                    }
                });

    }

    private void getArtists(final String query) {

        artistAdapter=new ArtistAdapter(artistList,view.getContext(),true);
        setUpRefreshLayoutforArtists(query);

        LinearLayoutManager layoutManager=new LinearLayoutManager(view.getContext());
        layoutManager.setSmoothScrollbarEnabled(true);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(artistAdapter);

        FirebaseFirestore.getInstance()
                .collection("Verified Artists")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(queryDocumentSnapshots.isEmpty()) {

                            refreshLayout.setRefreshing(false);
                            default_item.setVisibility(View.VISIBLE);
                            default_title.setText("No artists found");
                            default_text.setText("Sorry we couldn't find any artists");

                        }else{

                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {

                                String doc_name=documentChange.getDocument().getString("name").toLowerCase().replace(" ","");
                                if(doc_name.contains(query.toLowerCase().toLowerCase().replace(" ",""))) {

                                    refreshLayout.setRefreshing(false);
                                    view.findViewById(R.id.default_item).setVisibility(View.GONE);
                                    Artist artist = documentChange.getDocument().toObject(Artist.class).withId(documentChange.getDocument().getString("id"));
                                    artistList.add(artist);
                                    artistAdapter.notifyDataSetChanged();

                                }
                            }

                            if(artistList.isEmpty()){

                                refreshLayout.setRefreshing(false);
                                default_item.setVisibility(View.VISIBLE);
                                default_title.setText("No artists found");
                                default_text.setText("Sorry we couldn't find any artists for query : "+query);

                            }

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        refreshLayout.setRefreshing(false);
                        Toasty.error(view.getContext(),"Some technical error occured",Toasty.LENGTH_LONG,true).show();
                    }
                });

    }

    private void getUsers() {

        usersList.clear();
        usersAdapter=new UsersAdapter(usersList,view.getContext());
        usersAdapter.notifyDataSetChanged();
        setUpRefreshLayoutforUsers();

        LinearLayoutManager layoutManager=new LinearLayoutManager(view.getContext());
        layoutManager.setSmoothScrollbarEnabled(true);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(usersAdapter);

        FirebaseFirestore.getInstance()
                .collection("Users")
                .orderBy("name", Query.Direction.ASCENDING)
                .whereEqualTo("account_type","user")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(queryDocumentSnapshots.isEmpty()) {

                            refreshLayout.setRefreshing(false);
                            default_item.setVisibility(View.VISIBLE);
                            default_title.setText("No users found");
                            default_text.setText("Sorry we couldn't find any users");

                        }else{

                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                if(!documentChange.getDocument().getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    refreshLayout.setRefreshing(false);
                                    view.findViewById(R.id.default_item).setVisibility(View.GONE);
                                    Users user = documentChange.getDocument().toObject(Users.class).withId(documentChange.getDocument().getId());
                                    usersList.add(user);
                                    usersAdapter.notifyDataSetChanged();
                                }
                            }

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        refreshLayout.setRefreshing(false);
                        Toasty.error(view.getContext(),"Some technical error occured",Toasty.LENGTH_LONG,true).show();
                    }
                });

    }

    private void getUsers(final String query) {

        usersList.clear();
        usersAdapter=new UsersAdapter(usersList,view.getContext());
        usersAdapter.notifyDataSetChanged();
        setUpRefreshLayoutforUsers(query);

        LinearLayoutManager layoutManager=new LinearLayoutManager(view.getContext());
        layoutManager.setSmoothScrollbarEnabled(true);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(usersAdapter);

        FirebaseFirestore.getInstance()
                .collection("Users")
                .orderBy("name", Query.Direction.ASCENDING)
                .whereEqualTo("account_type","user")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(queryDocumentSnapshots.isEmpty()) {

                            refreshLayout.setRefreshing(false);
                            default_item.setVisibility(View.VISIBLE);
                            default_title.setText("No users found");
                            default_text.setText("Sorry we couldn't find any users");

                        }else{

                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {

                                String doc_name=documentChange.getDocument().getString("name").toLowerCase().replace(" ","");
                                if(doc_name.contains(query.toLowerCase().toLowerCase().replace(" ",""))) {

                                    refreshLayout.setRefreshing(false);
                                    view.findViewById(R.id.default_item).setVisibility(View.GONE);
                                    Users user = documentChange.getDocument().toObject(Users.class).withId(documentChange.getDocument().getId());;
                                    usersList.add(user);
                                    usersAdapter.notifyDataSetChanged();

                                }

                            }

                            if(usersList.isEmpty()){

                                refreshLayout.setRefreshing(false);
                                default_item.setVisibility(View.VISIBLE);
                                default_title.setText("No users found");
                                default_text.setText("Sorry we couldn't find any users for query : "+query);

                            }

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        refreshLayout.setRefreshing(false);
                        Toasty.error(view.getContext(),"Some technical error occured",Toasty.LENGTH_LONG,true).show();
                    }
                });

    }

    private void getAlbums(){

        albumsAdapter=new AlbumsAdapter(albums,view.getContext(),true,false,false);
        setUpRefreshLayoutforAlbums();

        LinearLayoutManager layoutManager=new GridLayoutManager(view.getContext(),2,RecyclerView.VERTICAL,false);
        layoutManager.setSmoothScrollbarEnabled(true);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(albumsAdapter);

        FirebaseFirestore.getInstance().collection("albums")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.getDocuments().isEmpty()){

                            refreshLayout.setRefreshing(false);
                            default_item.setVisibility(View.VISIBLE);
                            default_title.setText("No albums found");
                            default_text.setText("Sorry we couldn't find any albums");

                        }else {

                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                if (documentChange.getType() == DocumentChange.Type.ADDED) {

                                    refreshLayout.setRefreshing(false);
                                    Album song = documentChange.getDocument().toObject(Album.class);
                                    albums.add(song);
                                    albumsAdapter.notifyDataSetChanged();

                                }
                            }

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @SuppressLint("CheckResult")
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toasty.error(view.getContext(),"Some technical error occured",Toasty.LENGTH_LONG,true).show();
                        refreshLayout.setRefreshing(false);
                    }
                });


    }

    private void getAlbums(final String query){

        albumsAdapter=new AlbumsAdapter(albums,view.getContext(),true,false,false);
        setUpRefreshLayoutforAlbums(query);

        LinearLayoutManager layoutManager=new GridLayoutManager(view.getContext(),2,RecyclerView.VERTICAL,false);
        layoutManager.setSmoothScrollbarEnabled(true);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(albumsAdapter);

        FirebaseFirestore.getInstance().collection("albums")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.getDocuments().isEmpty()){

                            refreshLayout.setRefreshing(false);
                            default_item.setVisibility(View.VISIBLE);
                            default_title.setText("No albums found");
                            default_text.setText("Sorry we couldn't find any albums");

                        }else {

                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {

                                String doc_name=documentChange.getDocument().getString("name").toLowerCase().replace(" ","");
                                if(doc_name.contains(query.toLowerCase().toLowerCase().replace(" ",""))) {

                                    refreshLayout.setRefreshing(false);
                                    Album song = documentChange.getDocument().toObject(Album.class);
                                    albums.add(song);
                                    albumsAdapter.notifyDataSetChanged();

                                }

                            }

                            if(albums.isEmpty()){

                                refreshLayout.setRefreshing(false);
                                default_item.setVisibility(View.VISIBLE);
                                default_title.setText("No albums found");
                                default_text.setText("Sorry we couldn't find any albums for query : "+query);

                            }

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @SuppressLint("CheckResult")
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toasty.error(view.getContext(),"Some technical error occured",Toasty.LENGTH_LONG,true).show();
                        refreshLayout.setRefreshing(false);
                    }
                });


    }

    private void getSongs(){

        songsListsAdapter=new LibraryAdapter(getActivity(),songsList, view.getContext(),viewModel,false);
        setUpRefreshLayoutforSongs();

        LinearLayoutManager layoutManager=new LinearLayoutManager(view.getContext());
        layoutManager.setSmoothScrollbarEnabled(true);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(songsListsAdapter);

        FirebaseFirestore.getInstance().collection("songs")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.getDocuments().isEmpty()){

                            refreshLayout.setRefreshing(false);
                            default_item.setVisibility(View.VISIBLE);
                            default_title.setText("No songs found");
                            default_text.setText("Sorry we couldn't find any songs");

                        }else {

                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                if (documentChange.getType() == DocumentChange.Type.ADDED) {

                                    refreshLayout.setRefreshing(false);
                                    Songs song = documentChange.getDocument().toObject(Songs.class);
                                    songsList.add(song);
                                    songsListsAdapter.notifyDataSetChanged();
                                }
                            }

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @SuppressLint("CheckResult")
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        refreshLayout.setRefreshing(false);
                        e.printStackTrace();
                        Toasty.error(view.getContext(),"Some technical error occured",Toasty.LENGTH_LONG,true).show();
                    }
                });


    }

    private void getSongs(final String query){

        songsListsAdapter=new LibraryAdapter(getActivity(),songsList, view.getContext(),viewModel,false);
        setUpRefreshLayoutforSongs(query);

        LinearLayoutManager layoutManager=new LinearLayoutManager(view.getContext());
        layoutManager.setSmoothScrollbarEnabled(true);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(songsListsAdapter);

        FirebaseFirestore.getInstance().collection("songs")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.getDocuments().isEmpty()){

                            refreshLayout.setRefreshing(false);
                            default_item.setVisibility(View.VISIBLE);
                            default_title.setText("No songs found");
                            default_text.setText("Sorry we couldn't find any songs");

                        }else {

                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                String doc_name=documentChange.getDocument().getString("name").toLowerCase().replace(" ","");
                                if(doc_name.contains(query.toLowerCase().toLowerCase().replace(" ",""))) {

                                    refreshLayout.setRefreshing(false);
                                    Songs song = documentChange.getDocument().toObject(Songs.class);
                                    songsList.add(song);
                                    songsListsAdapter.notifyDataSetChanged();

                                }

                            }

                            if(songsList.isEmpty()){

                                refreshLayout.setRefreshing(false);
                                default_item.setVisibility(View.VISIBLE);
                                default_title.setText("No songs found");
                                default_text.setText("Sorry we couldn't find any songs for query : "+query);

                            }

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @SuppressLint("CheckResult")
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        refreshLayout.setRefreshing(false);
                        e.printStackTrace();
                        Toasty.error(view.getContext(),"Some technical error occured",Toasty.LENGTH_LONG,true).show();
                    }
                });


    }

    private void setUpRefreshLayoutforUsers() {

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                usersList.clear();
                usersAdapter.notifyDataSetChanged();
                getUsers();
            }
        });

    }

    private void setUpRefreshLayoutforSongs() {

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                songsList.clear();
                songsListsAdapter.notifyDataSetChanged();
                loadItems();
            }
        });

    }

    private void setUpRefreshLayoutforAlbums() {

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                albums.clear();
                albumsAdapter.notifyDataSetChanged();
                getAlbums();
            }
        });

    }

    private void setUpRefreshLayoutforArtists() {
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                artistList.clear();
                artistAdapter.notifyDataSetChanged();
                getArtists();
            }
        });

    }

    private void setUpRefreshLayoutforUsers(final String query) {

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                usersList.clear();
                usersAdapter.notifyDataSetChanged();
                getUsers(query);
            }
        });

    }

    private void setUpRefreshLayoutforSongs(final String query) {

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                songsList.clear();
                songsListsAdapter.notifyDataSetChanged();
                loadItems(query);
            }
        });

    }

    private void setUpRefreshLayoutforAlbums(final String query) {

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                albums.clear();
                albumsAdapter.notifyDataSetChanged();
                getAlbums(query);
            }
        });

    }

    private void setUpRefreshLayoutforArtists(final String query) {
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                artistList.clear();
                artistAdapter.notifyDataSetChanged();
                getArtists(query);
            }
        });

    }

}
