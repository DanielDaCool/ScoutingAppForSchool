package com.example.mainapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mainapp.R;
import com.example.mainapp.Utils.DatabaseUtils.User;

import java.util.ArrayList;
import java.util.HashMap;

public class ScouterAdapter extends RecyclerView.Adapter<ScouterAdapter.ScouterViewHolder> {

    private ArrayList<User> scouters;
    private OnScouterClickListener listener;
    private HashMap<String, Integer> pendingCounts = new HashMap<>();

    public interface OnScouterClickListener {
        void onScouterClick(User scouter);
    }

    public ScouterAdapter(ArrayList<User> scouters) {
        this.scouters = scouters;
    }

    public void setOnScouterClickListener(OnScouterClickListener listener) {
        this.listener = listener;
    }

    public void updateData(ArrayList<User> newScouters) {
        this.scouters = newScouters;
        notifyDataSetChanged();
    }

    public void setPendingCount(String userId, int count) {
        pendingCounts.put(userId, count);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ScouterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scouter, parent, false);
        return new ScouterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScouterViewHolder holder, int position) {
        User scouter = scouters.get(position);
        holder.bind(scouter);

        // Set count AFTER bind() so it doesn't get overwritten
        Integer count = pendingCounts.get(scouter.getUserId());
        if (count != null) {
            holder.tvPendingCount.setText(count + " משימות פתוחות");
        } else {
            holder.tvPendingCount.setText("טוען...");
        }
    }

    @Override
    public int getItemCount() {
        return scouters.size();
    }

    public class ScouterViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvEmail;
        public TextView tvPendingCount;

        public ScouterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName         = itemView.findViewById(R.id.tvScouterName);
            tvEmail        = itemView.findViewById(R.id.tvScouterEmail);
            tvPendingCount = itemView.findViewById(R.id.tvPendingCount);
        }

        public void bind(User scouter) {
            tvName.setText(scouter.getFullName());
            tvEmail.setText(scouter.getEmail());
            // tvPendingCount is set in onBindViewHolder — not here

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onScouterClick(scouter);
            });
        }
    }
}