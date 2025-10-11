package com.example.contractfarmingapp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotificationWorker extends Worker {

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String email = getInputData().getString("email");
        if (email != null) {
            NotificationHelper.pollNotifications(getApplicationContext(), email);
        }
        return Result.success();
    }
}
