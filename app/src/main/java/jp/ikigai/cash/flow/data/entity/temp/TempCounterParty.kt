package jp.ikigai.cash.flow.data.entity.temp

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class TempCounterParty() : RealmObject {
    @PrimaryKey
    var uuid: String = ""
    var name: String = ""
    var frequency: Int = 0
    var lastUsed: Long = 0L
}