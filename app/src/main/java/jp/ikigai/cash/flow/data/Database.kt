package jp.ikigai.cash.flow.data

import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.dynamic.DynamicMutableRealmObject
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Item
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.Transaction
import jp.ikigai.cash.flow.data.entity.TransactionItem
import jp.ikigai.cash.flow.data.entity.TransactionTemplate
import jp.ikigai.cash.flow.data.entity.TransactionTitle
import java.util.UUID

object Database {
    val config = RealmConfiguration
        .Builder(
            schema = setOf(
                Category::class,
                CounterParty::class,
                Item::class,
                Method::class,
                Source::class,
                TransactionItem::class,
                Transaction::class,
                TransactionTemplate::class,
                TransactionTitle::class
            )
        )
        .schemaVersion(3)
        .migration(
            { migrationContext ->
                val oldRealm = migrationContext.oldRealm
                val newRealm = migrationContext.newRealm

                if (oldRealm.schemaVersion() <= 1L && newRealm.schemaVersion() >= 2L) {
                    val transactions = oldRealm.query("Transaction").find()
                    val titles = mutableMapOf<String, Pair<Long, Long>>()
                    for (transaction in transactions) {
                        val title: String = transaction.getValue("title", String::class)
                        val dateTime: Long = transaction.getValue("time", Long::class)
                        val titleMeta = titles.getOrDefault(title, Pair(0L, 0L))
                        titles[title] =
                            titleMeta.copy(titleMeta.first + 1, maxOf(dateTime, titleMeta.second))
                    }
                    for (entry in titles) {
                        newRealm.copyToRealm(
                            DynamicMutableRealmObject.create(
                                type = "TransactionTitle",
                                mapOf(
                                    "uuid" to UUID.randomUUID().toString(),
                                    "title" to entry.key,
                                    "frequency" to entry.value.first,
                                    "lastUsed" to entry.value.second
                                )
                            )
                        )
                    }
                }

                if (oldRealm.schemaVersion() <= 2L && newRealm.schemaVersion() >= 3L) {
                    val emptyTransactionTitle = newRealm.query("TransactionTitle", "title==$0", "").find()
                    if (emptyTransactionTitle.isNotEmpty()) {
                        newRealm.delete(emptyTransactionTitle)
                    }
                }
            }
        )
        .build()
}