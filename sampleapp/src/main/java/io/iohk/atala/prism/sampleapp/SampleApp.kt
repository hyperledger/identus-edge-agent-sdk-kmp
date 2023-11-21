package io.iohk.atala.prism.sampleapp

import android.app.Application

class SampleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Sdk.getInstance()
    }
}
