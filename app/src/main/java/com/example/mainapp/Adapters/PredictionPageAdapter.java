package com.example.mainapp.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mainapp.Screens.Predictions.GamePredictionFragment;
import com.example.mainapp.Screens.Predictions.ManualPredictionFragment;

/**
 * Represents the PredictionPageAdapter component in the application.
 *
 * This class is responsible for handling the logic, data flow,
 * and interactions related to its specific feature inside the Android app.
 */
public class PredictionPageAdapter extends FragmentStateAdapter {

    public PredictionPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
/**
 * Executes the logic associated with the createFragment operation.
 * @param position parameter required for this method.
 * @return the value produced by this method.
 */
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new GamePredictionFragment();
        } else {
            return new ManualPredictionFragment();
        }
    }

    @Override
/**
 * Returns the number of items displayed by the adapter.
 * @return the value produced by this method.
 */
    public int getItemCount() {
        return 2;
    }
}