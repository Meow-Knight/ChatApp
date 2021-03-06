package com.lecaoviethuy.messengerapp.controllers

import android.app.Application
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.lecaoviethuy.messengerapp.databaseServices.OfflineDatabase
import com.lecaoviethuy.messengerapp.modelClasses.Status


class AppController : Application(), LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() {
        updateStatus(Status.ONLINE)
        isAppInBackground(false)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        updateStatus(Status.OFFLINE)
        isAppInBackground(true)
    }

    interface ValueChangeListener {
        fun onChanged(value: Boolean?)
    }

    private var visibilityChangeListener: ValueChangeListener? = null
    fun setOnVisibilityChangeListener(listener: ValueChangeListener?) {
        visibilityChangeListener = listener
    }

    private fun isAppInBackground(isBackground: Boolean) {
        if (null != visibilityChangeListener) {
            visibilityChangeListener!!.onChanged(isBackground)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // Caching database
        OfflineDatabase.enablePersistence();
        OfflineDatabase.keepSynced();
    }

    companion object {
        var instance: AppController? = null
            private set

        fun updateStatus(status: Status){
            val mUser = FirebaseAuth.getInstance().currentUser
            if (mUser != null){
                val ref = FirebaseDatabase.getInstance()
                    .reference
                    .child("Users")
                    .child(mUser.uid)
                val statusMap = HashMap<String, Any>()
                statusMap["status"] = status.statusString
                ref.updateChildren(statusMap)
            }
        }
    }
}