/*
CashFlow - Expense Tracker
Copyright (C) 2025 ThomasGRG

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

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