package com.vertil.pos

import android.app.Application
import com.vertil.pos.di.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class VertilPosApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            ServiceLocator.seedIfEmpty()
        }
    }
}
