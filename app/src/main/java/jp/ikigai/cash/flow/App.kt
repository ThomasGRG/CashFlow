package jp.ikigai.cash.flow

import android.app.Application
import jp.ikigai.cash.flow.data.store.DataStore
import jp.ikigai.cash.flow.koin.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        DataStore.init(this)
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModule)
        }
    }
}