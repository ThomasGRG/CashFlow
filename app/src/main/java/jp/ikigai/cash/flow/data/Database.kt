package jp.ikigai.cash.flow.data

import android.content.Context
import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import jp.ikigai.cash.flow.CashFlowDatabase
import jp.ikigai.cash.flow.Category
import jp.ikigai.cash.flow.TempCategory
import jp.ikigai.cash.flow.TempTransaction
import jp.ikigai.cash.flow.TempTransactionTemplate
import jp.ikigai.cash.flow.Transaction
import jp.ikigai.cash.flow.TransactionTemplate
import jp.ikigai.cash.flow.data.adapters.ImageVectorAdapter
import jp.ikigai.cash.flow.data.adapters.ZonedDateTimeAdapter

object Database {
    lateinit var database: CashFlowDatabase

    fun init(context: Context) {
        val driver = AndroidSqliteDriver(CashFlowDatabase.Schema, context, "cash-flow-database.db")
        database = CashFlowDatabase(
            driver,
            CategoryAdapter = Category.Adapter(
                iconAdapter = ImageVectorAdapter
            ),
            TempCategoryAdapter = TempCategory.Adapter(
                tempCategoryIconAdapter = ImageVectorAdapter
            ),
            TransactionAdapter = Transaction.Adapter(
                transactionDateTimeAdapter = ZonedDateTimeAdapter,
                transactionTypeAdapter = EnumColumnAdapter()
            ),
            TempTransactionAdapter = TempTransaction.Adapter(
                tempTransactionDateTimeAdapter = ZonedDateTimeAdapter,
                tempTransactionTypeAdapter = EnumColumnAdapter()
            ),
            TransactionTemplateAdapter = TransactionTemplate.Adapter(
                templateTypeAdapter = EnumColumnAdapter()
            ),
            TempTransactionTemplateAdapter = TempTransactionTemplate.Adapter(
                tempTransactionTemplateTypeAdapter = EnumColumnAdapter()
            )
        )
    }
}