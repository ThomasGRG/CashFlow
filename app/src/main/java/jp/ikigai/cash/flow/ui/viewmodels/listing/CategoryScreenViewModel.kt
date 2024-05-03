package jp.ikigai.cash.flow.ui.viewmodels.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.ui.screenStates.listing.CategoryScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoryScreenViewModel(
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryScreenState())
    val state: StateFlow<CategoryScreenState> = _state.asStateFlow()

//    private val categoryQuery = realm.query<Category>("SORT((frequency * (1 - $0 - lastUsed)/$1) ASC)") current epoch time and decayConstant 0.00005f
    private val categoryQuery = realm.query<Category>().sort("frequency", Sort.DESCENDING)

    init {
        getCategories()
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }

    private fun getCategories() = viewModelScope.launch {
        categoryQuery.asFlow().collectLatest { changes ->
            _state.update {
                it.copy(
                    categories = changes.list,
                    loading = false
                )
            }
        }
    }
}