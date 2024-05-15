package io.iohk.atala.prism.sampleapp

import android.app.Application
import io.iohk.atala.prism.sampleapp.db.DatabaseClient

class SampleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Sdk.getInstance()
        DatabaseClient.initializeInstance(applicationContext)
        DatabaseClient.getInstance()
    }
}
