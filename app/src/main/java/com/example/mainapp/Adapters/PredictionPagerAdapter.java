package com.example.mainapp.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mainapp.Screens.Predictions.GamePredictionFragment;
import com.example.mainapp.Screens.Predictions.ManualPredictionFragment;

public class PredictionPagerAdapter extends FragmentStateAdapter {

    public PredictionPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new GamePredictionFragment();
        } else {
            return new ManualPredictionFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}