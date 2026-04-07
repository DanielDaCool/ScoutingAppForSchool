package com.example.mainapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mainapp.R;
import com.example.mainapp.Utils.DatabaseUtils.CLIMB;
import com.example.mainapp.Utils.GamePiece;
import com.example.mainapp.Utils.TeamUtils.TeamAtGame;

import java.util.ArrayList;

public class GameHistoryAdapter extends RecyclerView.Adapter<GameHistoryAdapter.GameViewHolder> {

    private final ArrayList<TeamAtGame> games;

    public GameHistoryAdapter(ArrayList<TeamAtGame> games) {
        this.games = games;
    }

    @NonNull @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game_history, parent, false);
        return new GameViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        holder.bind(games.get(position));
    }

    @Override public int getItemCount() { return games.size(); }

    static class GameViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvGameNumber;
        private final TextView tvPoints;
        private final TextView tvAutoPoints;
        private final TextView tvTelePoints;
        private final TextView tvBestHeight;
        private final TextView tvClimb;
        private final TextView tvL1, tvL2, tvL3, tvL4, tvNet, tvProc;

        GameViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGameNumber = itemView.findViewById(R.id.tvGameNumber);
            tvPoints     = itemView.findViewById(R.id.tvPoints);
            tvAutoPoints = itemView.findViewById(R.id.tvAutoPoints);
            tvTelePoints = itemView.findViewById(R.id.tvTelePoints);
            tvBestHeight = itemView.findViewById(R.id.tvBestHeight);
            tvClimb      = itemView.findViewById(R.id.tvClimb);
            tvL1         = itemView.findViewById(R.id.tvL1Count);
            tvL2         = itemView.findViewById(R.id.tvL2Count);
            tvL3         = itemView.findViewById(R.id.tvL3Count);
            tvL4         = itemView.findViewById(R.id.tvL4Count);
            tvNet        = itemView.findViewById(R.id.tvNetCount);
            tvProc       = itemView.findViewById(R.id.tvProcCount);
        }

        void bind(TeamAtGame game) {
            tvGameNumber.setText("משחק " + game.getGameID());
            tvPoints.setText(String.valueOf(game.calculatePoints()));

            // Auto vs Teleop breakdown
            int autoPoints = 0, telePoints = 0;
            int l1 = 0, l2 = 0, l3 = 0, l4 = 0, net = 0, proc = 0;
            String bestHeight = "—";

            if (game.getGamePiecesScored() != null) {
                for (TeamAtGame.GamePieceScore score : game.getGamePiecesScored()) {
                    int pts = score.getPoints();
                    if (score.isInAuto()) autoPoints += pts;
                    else telePoints += pts;

                    GamePiece piece = GamePiece.valueOf(score.getPiece().toString());
                    switch (piece) {
                        case L1:        l1++;   break;
                        case L2:        l2++;   break;
                        case L3:        l3++;   break;
                        case L4:        l4++;   break;
                        case NET:       net++;  break;
                        case PROCESSOR: proc++; break;
                    }
                }
                // Best height = highest level scored
                if (l4 > 0)       bestHeight = "L4";
                else if (l3 > 0)  bestHeight = "L3";
                else if (l2 > 0)  bestHeight = "L2";
                else if (l1 > 0)  bestHeight = "L1";
                else if (net > 0) bestHeight = "Net";
            }

            tvAutoPoints.setText(String.valueOf(autoPoints));
            tvTelePoints.setText(String.valueOf(telePoints));
            tvBestHeight.setText(bestHeight);
            tvL1.setText(String.valueOf(l1));
            tvL2.setText(String.valueOf(l2));
            tvL3.setText(String.valueOf(l3));
            tvL4.setText(String.valueOf(l4));
            tvNet.setText(String.valueOf(net));
            tvProc.setText(String.valueOf(proc));

            // Climb
            CLIMB climb = game.getClimb();
            if (climb == null) { tvClimb.setText("—"); return; }
            switch (climb) {
                case HIGH:      tvClimb.setText("✅ גבוה");   break;
                case LOW:       tvClimb.setText("✅ נמוך");   break;
                case FAILED:    tvClimb.setText("❌ נכשל");   break;
                case DIDNT_TRY: tvClimb.setText("⬜ לא ניסה"); break;
                default:        tvClimb.setText("—");          break;
            }
        }
    }
}