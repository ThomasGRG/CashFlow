package jp.ikigai.cash.flow.data

import io.realm.kotlin.RealmConfiguration
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Item
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.Transaction
import jp.ikigai.cash.flow.data.entity.TransactionItem

object Database {
    val config = RealmConfiguration.create(
        schema = setOf(
            Category::class,
            CounterParty::class,
            Item::class,
            Method::class,
            Source::class,
            TransactionItem::class,
            Transaction::class
        )
    )
}