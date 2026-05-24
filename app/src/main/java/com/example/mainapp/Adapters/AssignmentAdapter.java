package com.example.mainapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mainapp.R;
import com.example.mainapp.Utils.DatabaseUtils.Assignment;

import java.util.ArrayList;

/**
 * AssignmentAdapter is a RecyclerView adapter used to display a list of scouting assignments.
 * Each assignment includes the game number and the team number to be scouted.
 */
public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder> {

    private ArrayList<Assignment> assignments;
    private OnAssignmentClickListener listener;

    /**
     * Interface definition for a callback to be invoked when an assignment is clicked.
     */
    public interface OnAssignmentClickListener {
        /**
         * Called when an assignment has been clicked.
         * @param assignment The assignment object that was clicked.
         */
        void onAssignmentClick(Assignment assignment);
    }

    public AssignmentAdapter(ArrayList<Assignment> assignments) {
        this.assignments = assignments;
    }

    /**
     * Sets the click listener for assignment items.
     * @param listener The listener to be notified of click events.
     */
    public void setOnAssignmentClickListener(OnAssignmentClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
/**
 * Creates and inflates a new RecyclerView item layout.
 * @param parent parameter required for this method.
 * @param viewType parameter required for this method.
 * @return the value produced by this method.
 */
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assignment, parent, false);
        return new AssignmentViewHolder(view);
    }

    @Override
/**
 * Binds data from the current item into the RecyclerView row.
 * @param holder parameter required for this method.
 * @param position parameter required for this method.
 */
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        Assignment assignment = assignments.get(position);
        holder.bind(assignment);
    }

    @Override
/**
 * Returns the number of items displayed by the adapter.
 * @return the value produced by this method.
 */
    public int getItemCount() {
        return assignments.size();
    }


    /**
     * Removes an assignment from the list based on its unique key.
     * @param key The unique key of the assignment to remove.
     */
    public void removeByKey(String key) {
        for (int i = 0; i < assignments.size(); i++) {
            if (assignments.get(i).getKey().equals(key)) {
                assignments.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }

    /**
     * ViewHolder class for assignment items.
     */
    public class AssignmentViewHolder extends RecyclerView.ViewHolder {

        private TextView tvGameNumber;
        private TextView tvTeamNumber;

        public AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGameNumber = itemView.findViewById(R.id.tvAssignmentGame);
            tvTeamNumber = itemView.findViewById(R.id.tvAssignmentTeam);
        }

        /**
         * Binds an assignment object to the view holder's UI elements.
         * @param assignment The assignment to bind.
         */
        public void bind(Assignment assignment) {
            tvGameNumber.setText("משחק " + assignment.getGameNumber());
            tvTeamNumber.setText("#" + assignment.getTeamNumber());

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onAssignmentClick(assignment);
            });
        }
    }
}