package com.amsavarthan.ztunes.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.amsavarthan.ztunes.ui.activities.MainActivity;
import com.amsavarthan.ztunes.R;

public class PostTypeSelectView extends Fragment {

    View view;
    LinearLayout image,text;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.fragment_post_type_selection_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        image=view.findViewById(R.id.image);
        text=view.findViewById(R.id.text);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new AddImagePost(),"add_img_post");
                MainActivity.mCurrentFragment="add_img_post";

            }
        });

        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new AddTextPost(),"add_text_post");
                MainActivity.mCurrentFragment="add_text_post";

            }
        });

    }

    void showFragment(Fragment fragment,String tag){

        ((AppCompatActivity)view.getContext()).getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_up,R.anim.fade_out)
                .addToBackStack(null)
                .replace(R.id.container,fragment,tag)
                .commit();

    }

}
