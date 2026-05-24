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
 * Represents the AssignmentAdapter component in the application.
 *
 * This class is responsible for handling the logic, data flow,
 * and interactions related to its specific feature inside the Android app.
 */
public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder> {

    private ArrayList<Assignment> assignments;
    private OnAssignmentClickListener listener;

    public interface OnAssignmentClickListener {
        void onAssignmentClick(Assignment assignment);
    }

    public AssignmentAdapter(ArrayList<Assignment> assignments) {
        this.assignments = assignments;
    }

/**
 * Executes the logic associated with the setOnAssignmentClickListener operation.
 * @param listener parameter required for this method.
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
 * Executes the logic associated with the removeByKey operation.
 * @param key parameter required for this method.
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

    public class AssignmentViewHolder extends RecyclerView.ViewHolder {

        private TextView tvGameNumber;
        private TextView tvTeamNumber;

        public AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGameNumber = itemView.findViewById(R.id.tvAssignmentGame);
            tvTeamNumber = itemView.findViewById(R.id.tvAssignmentTeam);
        }

/**
 * Executes the logic associated with the bind operation.
 * @param assignment parameter required for this method.
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