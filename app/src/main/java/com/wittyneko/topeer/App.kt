package com.wittyneko.topeer

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.androidModule
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton


class App : Application(), KodeinAware {

    override val kodein: Kodein = Kodein.lazy {
        bind<Context>() with singleton { this@App }
        bind() from singleton { MainService() }

        import(androidModule(this@App))
        import(androidXModule(this@App))
    }

    override fun attachBaseContext(base: Context?) {
        INSTANCE = this
        MultiDex.install(this)
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        lateinit var INSTANCE: App
    }
}