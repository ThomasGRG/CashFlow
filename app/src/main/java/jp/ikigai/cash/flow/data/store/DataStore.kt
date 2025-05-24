package jp.ikigai.cash.flow.data.store

import android.content.Context
import io.objectbox.BoxStore
import jp.ikigai.cash.flow.data.store.entity.MyObjectBox

object DataStore {

    lateinit var store: BoxStore
        private set

    fun init(context: Context) {
        store = MyObjectBox
            .builder()
            .androidContext(context)
            .build()
    }
}