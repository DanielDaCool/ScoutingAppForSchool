package com.example.mainapp.Utils;

public record OfflineFormData(
        int teamNumber,
        int gameNumber,
        String gamePiecesJson,  // JSON serialized list of GamePieceScore
        String climb,           // CLIMB enum name e.g. "HIGH", "LOW", "FAILED", "DIDNT_TRY"
        long timestamp          // when it was saved locally
) {}