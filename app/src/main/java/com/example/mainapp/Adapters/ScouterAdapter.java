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

/**
 * Represents the ScouterAdapter component in the application.
 *
 * This class is responsible for handling the logic, data flow,
 * and interactions related to its specific feature inside the Android app.
 */
public class ScouterAdapter extends RecyclerView.Adapter<ScouterAdapter.ScouterViewHolder> {

    private ArrayList<User> scouters;
    private OnScouterClickListener listener;
    private HashMap<String, Integer> pendingCounts = new HashMap<>();

    public interface OnScouterClickListener {
        void onScouterClick(User scouter);
    }

    public ScouterAdapter(ArrayList<User> scouters) { this.scouters = scouters; }

/**
 * Executes the logic associated with the setOnScouterClickListener operation.
 * @param l parameter required for this method.
 */
    public void setOnScouterClickListener(OnScouterClickListener l) { this.listener = l; }

/**
 * Executes the logic associated with the updateData operation.
 * @param newList parameter required for this method.
 */
    public void updateData(ArrayList<User> newList) {
        this.scouters = newList;
        notifyDataSetChanged();
    }

/**
 * Executes the logic associated with the setPendingCount operation.
 * @param userId parameter required for this method.
 * @param count parameter required for this method.
 */
    public void setPendingCount(String userId, int count) {
        pendingCounts.put(userId, count);
        notifyDataSetChanged();
    }

    @NonNull @Override
/**
 * Creates and inflates a new RecyclerView item layout.
 * @param parent parameter required for this method.
 * @param viewType parameter required for this method.
 * @return the value produced by this method.
 */
    public ScouterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scouter, parent, false);
        return new ScouterViewHolder(v);
    }

    @Override
/**
 * Binds data from the current item into the RecyclerView row.
 * @param holder parameter required for this method.
 * @param position parameter required for this method.
 */
    public void onBindViewHolder(@NonNull ScouterViewHolder holder, int position) {
        User scouter = scouters.get(position);
        holder.bind(scouter);

        // Set count AFTER bind() so it is not overwritten
        Integer count = pendingCounts.get(scouter.getUserId());
        holder.tvPendingCount.setText(count != null ? count + " משימות פתוחות" : "טוען...");
    }

    @Override public int getItemCount() { return scouters.size(); }

    public class ScouterViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvEmail;
        public  TextView tvPendingCount;

        public ScouterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName         = itemView.findViewById(R.id.tvScouterName);
            tvEmail        = itemView.findViewById(R.id.tvScouterEmail);
            tvPendingCount = itemView.findViewById(R.id.tvPendingCount);
        }

/**
 * Executes the logic associated with the bind operation.
 * @param scouter parameter required for this method.
 */
        public void bind(User scouter) {
            tvName.setText(scouter.getFullName());
            tvEmail.setText(scouter.getEmail());
            // tvPendingCount is set in onBindViewHolder — NOT here
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onScouterClick(scouter);
            });
        }
    }
}