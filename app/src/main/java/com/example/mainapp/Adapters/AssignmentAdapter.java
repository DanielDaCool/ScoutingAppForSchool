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

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder> {

    private ArrayList<Assignment> assignments;
    private OnAssignmentClickListener listener;

    public interface OnAssignmentClickListener {
        void onAssignmentClick(Assignment assignment);
    }

    public AssignmentAdapter(ArrayList<Assignment> assignments) {
        this.assignments = assignments;
    }

    public void setOnAssignmentClickListener(OnAssignmentClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assignment, parent, false);
        return new AssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        Assignment assignment = assignments.get(position);
        holder.bind(assignment);
    }

    @Override
    public int getItemCount() {
        return assignments.size();
    }

    public void updateData(ArrayList<Assignment> newAssignments) {
        this.assignments = newAssignments;
        notifyDataSetChanged();
    }

    // Removes an assignment from the list by key — called after form submission
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
        private TextView tvArrow;

        public AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGameNumber = itemView.findViewById(R.id.tvAssignmentGame);
            tvTeamNumber = itemView.findViewById(R.id.tvAssignmentTeam);
            tvArrow      = itemView.findViewById(R.id.tvAssignmentArrow);
        }

        public void bind(Assignment assignment) {
            tvGameNumber.setText("משחק " + assignment.getGameNumber());
            tvTeamNumber.setText("#" + assignment.getTeamNumber());

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onAssignmentClick(assignment);
            });
        }
    }
}