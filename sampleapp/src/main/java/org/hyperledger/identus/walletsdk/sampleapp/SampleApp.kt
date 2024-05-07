package org.hyperledger.identus.walletsdk.sampleapp

import android.app.Application
import org.hyperledger.identus.walletsdk.db.DatabaseClient

class SampleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Sdk.getInstance()
        DatabaseClient.initializeInstance(applicationContext)
        DatabaseClient.getInstance()
    }
}
