package com.lecaoviethuy.messengerapp.controllers

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class OnStopService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null;
    }

    override fun onDestroy() {
        println("onDestroy services")
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        println("onDestroy services")
        Log.e("ClearFromRecentService", "END");
        stopSelf()
        println("onDestroy servicesssssssssssssssssssssss")
        Log.e("ClearFromRecentService", "END");
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("onStartCommanddddddddddd")
        return START_NOT_STICKY
    }
}