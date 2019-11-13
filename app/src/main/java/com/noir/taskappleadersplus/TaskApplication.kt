package com.noir.taskappleadersplus

import android.app.Application
import io.realm.Realm

class TaskApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    Realm.init(applicationContext)

  }
}