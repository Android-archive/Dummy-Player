package com.amsavarthan.ztunes.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.amsavarthan.ztunes.R;

public class PaymentFragment extends Fragment {


    public PaymentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RelativeLayout item1=view.findViewById(R.id.item1);
        RelativeLayout item2=view.findViewById(R.id.item2);

        item1.setOnClickListener(v -> {
            item1.setBackground(view.getContext().getResources().getDrawable(R.drawable.rounded_box_selected));
            item2.setBackground(view.getContext().getResources().getDrawable(R.drawable.rounded_box));
        });

        item2.setOnClickListener(v -> {
            item2.setBackground(view.getContext().getResources().getDrawable(R.drawable.rounded_box_selected));
            item1.setBackground(view.getContext().getResources().getDrawable(R.drawable.rounded_box));
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_payment, container, false);
    }
}
