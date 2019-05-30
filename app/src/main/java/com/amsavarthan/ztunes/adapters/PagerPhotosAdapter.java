package com.amsavarthan.ztunes.adapters;

import android.content.Context;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.amsavarthan.ztunes.models.Images;
import com.amsavarthan.ztunes.R;

import java.util.List;

public class PagerPhotosAdapter extends PagerAdapter {


    private List<Images> IMAGES;
    private Context context;
    private LayoutInflater inflater;
    public static View imageLayout;


    public PagerPhotosAdapter(Context context, List<Images> IMAGES) {
        this.context = context;
        this.IMAGES =IMAGES;
        inflater=LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return IMAGES.size();
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup view, final int position) {
        imageLayout = inflater.inflate(R.layout.item_viewpager_image, view, false);

        assert imageLayout !=null;
        ImageView imageView=imageLayout.findViewById(R.id.image);

        imageView.setImageURI(IMAGES.get(position).getUri());

        view.addView(imageLayout,0);

        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }


}
