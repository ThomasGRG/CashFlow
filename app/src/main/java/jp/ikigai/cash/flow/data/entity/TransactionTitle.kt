package jp.ikigai.cash.flow.data.entity

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class TransactionTitle() : RealmObject {
    @PrimaryKey
    var uuid: String = ""
    var title: String = ""
    var frequency: Int = 0
    var lastUsed: Long = 0L
}