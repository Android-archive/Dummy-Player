package com.amsavarthan.ztunes.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import android.view.View;

import com.amsavarthan.ztunes.R;
import com.amsavarthan.ztunes.adapters.PromoteViewPagerAdapter;
import com.amsavarthan.ztunes.ui.fragments.AudienceSelectFragment;
import com.amsavarthan.ztunes.ui.fragments.BudgetFragment;
import com.amsavarthan.ztunes.ui.fragments.PaymentFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shuhart.stepview.StepView;

import java.util.ArrayList;

public class PromotePostActivity extends AppCompatActivity {

    private StepView stepView;
    private ViewPager viewPager;
    private FloatingActionButton nextFab;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promote_post);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager= findViewById(R.id.viewpager);
        stepView= findViewById(R.id.stepView);
        nextFab=findViewById(R.id.next);

        stepView.getState().steps(new ArrayList<String>(){
            {
                add("Audience");
                add("Duration & Budget");
                add("Checkout");
            }
        }).commit();

        PromoteViewPagerAdapter adapter=new PromoteViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFragment(new AudienceSelectFragment());
        adapter.addFragment(new BudgetFragment());
        adapter.addFragment(new PaymentFragment());
        viewPager.setAdapter(adapter);

        stepView.setOnStepClickListener(step -> {
            viewPager.setCurrentItem(step, true);
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


            }

            @Override
            public void onPageSelected(int position) {

                if(position!=2){
                    nextFab.setImageResource(R.drawable.ef_ic_arrow_forward);
                }else{
                    nextFab.setImageResource(R.drawable.ef_ic_done_white);
                }
                stepView.go(position,true);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


    public void goTonextPage(View view) {

        if(stepView.getCurrentStep()!=2){
            viewPager.setCurrentItem(stepView.getCurrentStep()+1,true);
        }

    }
}

