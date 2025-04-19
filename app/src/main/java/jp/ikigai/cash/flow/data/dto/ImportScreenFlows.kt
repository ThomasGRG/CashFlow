package jp.ikigai.cash.flow.data.dto

import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.temp.TempCategory
import jp.ikigai.cash.flow.data.entity.temp.TempCounterParty
import jp.ikigai.cash.flow.data.entity.temp.TempMethod
import jp.ikigai.cash.flow.data.entity.temp.TempSource
import jp.ikigai.cash.flow.data.entity.temp.TempTransaction
import jp.ikigai.cash.flow.data.entity.temp.TempTransactionTemplate

data class ImportScreenFlows(
    val categories: List<Category> = emptyList(),
    val counterParties: List<CounterParty> = emptyList(),
    val methods: List<Method> = emptyList(),
    val sources: List<Source> = emptyList(),
    val tempCategories: List<TempCategory> = emptyList(),
    val tempCounterParties: List<TempCounterParty> = emptyList(),
    val tempMethods: List<TempMethod> = emptyList(),
    val tempSources: List<TempSource> = emptyList(),
    val tempTransactionTemplates: List<TempTransactionTemplate> = emptyList(),
    val tempTransactions: List<TempTransaction> = emptyList(),
)
