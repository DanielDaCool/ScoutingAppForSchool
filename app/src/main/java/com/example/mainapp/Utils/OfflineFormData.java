package com.example.mainapp.Utils;

public record OfflineFormData(
        int teamNumber,
        int gameNumber,
        String gamePiecesJson,
        String climb,
        long timestamp,
        String assignmentKey,
        String userId
) {}