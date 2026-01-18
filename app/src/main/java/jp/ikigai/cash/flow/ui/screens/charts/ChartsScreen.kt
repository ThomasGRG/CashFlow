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

package jp.ikigai.cash.flow.ui.screens.charts

import android.icu.number.LocalizedNumberFormatter
import android.icu.number.Notation
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.window.core.layout.WindowSizeClass
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.stacked
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import jp.ikigai.cash.flow.AccountWithTransactionMetadata
import jp.ikigai.cash.flow.CategoryWithTransactionMetadata
import jp.ikigai.cash.flow.CounterPartyWithTransactionMetadata
import jp.ikigai.cash.flow.MethodWithTransactionMetadata
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.InsightsRoute
import jp.ikigai.cash.flow.data.enums.ChartType
import jp.ikigai.cash.flow.data.enums.SheetType
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.components.bottombars.ChartsScreenBottomAppBar
import jp.ikigai.cash.flow.ui.components.bottomsheets.CurrencyLandscapeSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.CurrencySheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.MonthRangePickerLandscapeSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.MonthRangePickerSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.SelectAccountLandscapeSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.SelectAccountSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.SelectCategoryLandscapeSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.SelectCategorySheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.SelectChartTypeLandscapeSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.SelectChartTypeSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.SelectCounterPartyLandscapeSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.SelectCounterPartySheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.SelectMethodLandscapeSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.SelectMethodSheet
import jp.ikigai.cash.flow.ui.components.common.LandscapeScaffold
import jp.ikigai.cash.flow.ui.screenStates.charts.ChartsScreenState
import jp.ikigai.cash.flow.ui.viewmodels.charts.ChartsScreenViewModel
import jp.ikigai.cash.flow.utils.getCurrencyFormatterMap
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.YearMonth
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(
    navigateBack: () -> Unit,
    setLocale: (Locale?) -> Unit,
    setChartType: (ChartType) -> Unit,
    setYearMonthRange: (YearMonth?, YearMonth?) -> Unit,
    setSelectedCurrency: (String) -> Unit,
    setSelectedAccount: (AccountWithTransactionMetadata) -> Unit,
    setSelectedCategory: (CategoryWithTransactionMetadata) -> Unit,
    setSelectedCounterParty: (CounterPartyWithTransactionMetadata) -> Unit,
    setSelectedMethod: (MethodWithTransactionMetadata) -> Unit,
    modelProducer: CartesianChartModelProducer,
    labelKey: ExtraStore.Key<List<String>>,
    state: ChartsScreenState,
    hasData: Boolean,
    loading: Boolean
) {
    val configuration = LocalConfiguration.current

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val locale by remember(key1 = configuration) {
        mutableStateOf(configuration.locales[0])
    }

    LaunchedEffect(key1 = locale) {
        setLocale(locale)
    }

    val shortCurrencyFormatterMap by remember(key1 = locale) {
        mutableStateOf(
            getCurrencyFormatterMap(locale, Notation.compactShort())
        )
    }

    val currencyFormatterMap by remember(key1 = locale) {
        mutableStateOf(
            getCurrencyFormatterMap(locale)
        )
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val labelComponent = rememberTextComponent(vicoTheme.textColor)

    val chartTypes by remember(key1 = state.chartTypes) {
        mutableStateOf(state.chartTypes)
    }

    val transactionCountChartTypes by remember {
        mutableStateOf(
            listOf(
                ChartType.ACCOUNT_TRANSACTION_COUNT_BAR_CHART,
                ChartType.CATEGORY_TRANSACTION_COUNT_BAR_CHART,
                ChartType.COUNTERPARTY_TRANSACTION_COUNT_BAR_CHART,
                ChartType.METHOD_TRANSACTION_COUNT_BAR_CHART,
                ChartType.TEMPLATE_TRANSACTION_COUNT_BAR_CHART,
            )
        )
    }

    val totalAmountChartTypes by remember {
        mutableStateOf(
            listOf(
                ChartType.ACCOUNT_DEBIT_CREDIT_BAR_CHART,
                ChartType.CATEGORY_DEBIT_CREDIT_BAR_CHART,
                ChartType.COUNTERPARTY_DEBIT_CREDIT_BAR_CHART,
                ChartType.METHOD_DEBIT_CREDIT_BAR_CHART,
            )
        )
    }

    val selectedChartType by remember(key1 = state.selectedChartType) {
        mutableStateOf(state.selectedChartType)
    }

    val yearMonthSelectionStart by remember(key1 = state.yearMonthSelectionStart) {
        mutableStateOf(state.yearMonthSelectionStart)
    }

    val startYearMonthString by remember(key1 = state.startYearMonthString) {
        mutableStateOf(state.startYearMonthString)
    }

    val yearMonthSelectionEnd by remember(key1 = state.yearMonthSelectionEnd) {
        mutableStateOf(state.yearMonthSelectionEnd)
    }

    val endYearMonthString by remember(key1 = state.endYearMonthString) {
        mutableStateOf(state.endYearMonthString)
    }

    val yearMonthRangeStringRes by remember(key1 = state.yearMonthRangeStringRes) {
        mutableIntStateOf(state.yearMonthRangeStringRes)
    }

    val currencies by remember(key1 = state.currencies) {
        mutableStateOf(state.currencies)
    }

    val selectedCurrency by remember(key1 = state.selectedCurrency) {
        mutableStateOf(state.selectedCurrency)
    }

    val accounts by remember(key1 = state.accounts) {
        mutableStateOf(state.accounts)
    }

    val selectedAccount by remember(key1 = state.selectedAccount) {
        mutableStateOf(state.selectedAccount)
    }

    val categories by remember(key1 = state.categories) {
        mutableStateOf(state.categories)
    }

    val selectedCategory by remember(key1 = state.selectedCategory) {
        mutableStateOf(state.selectedCategory)
    }

    val counterParties by remember(key1 = state.counterParties) {
        mutableStateOf(state.counterParties)
    }

    val selectedCounterParty by remember(key1 = state.selectedCounterParty) {
        mutableStateOf(state.selectedCounterParty)
    }

    val methods by remember(key1 = state.methods) {
        mutableStateOf(state.methods)
    }

    val selectedMethod by remember(key1 = state.selectedMethod) {
        mutableStateOf(state.selectedMethod)
    }

    var sheetType by remember {
        mutableStateOf(SheetType.NONE)
    }

    if (
        windowSizeClass.isWidthAtLeastBreakpoint(
            WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
        )
    ) {
        LandscapeScaffold(
            loading = loading,
            loadingIndicator = {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            },
            sheetState = sheetState,
            showBottomSheet = sheetType != SheetType.NONE,
            bottomSheetContent = {
                when (sheetType) {
                    SheetType.CHART -> {
                        SelectChartTypeLandscapeSheet(
                            index = chartTypes
                                .indexOfFirst { it == selectedChartType }
                                .coerceAtLeast(0),
                            selectedChartType = selectedChartType,
                            setSelectedChartType = setChartType,
                            chartTypes = chartTypes,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        sheetType = SheetType.NONE
                                    }
                            }
                        )
                    }

                    SheetType.MONTH_RANGE -> {
                        MonthRangePickerLandscapeSheet(
                            start = yearMonthSelectionStart,
                            end = yearMonthSelectionEnd,
                            filter = setYearMonthRange,
                            reset = {
                                setYearMonthRange(null, null)
                            },
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        sheetType = SheetType.NONE
                                    }
                            }
                        )
                    }

                    SheetType.CURRENCY -> {
                        CurrencyLandscapeSheet(
                            index = currencies.indexOfFirst { it.currency.currencyCode == selectedCurrency },
                            selectedCurrency = selectedCurrency,
                            setSelectedCurrency = setSelectedCurrency,
                            currencies = currencies,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        sheetType = SheetType.NONE
                                    }
                            }
                        )
                    }

                    SheetType.ACCOUNT -> {
                        SelectAccountLandscapeSheet(
                            index = accounts
                                .indexOfFirst { it.accountId == selectedAccount.accountId }
                                .coerceAtLeast(0),
                            selectedAccountId = selectedAccount.accountId,
                            setSelectedAccount = setSelectedAccount,
                            accounts = accounts,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        sheetType = SheetType.NONE
                                    }
                            }
                        )
                    }

                    SheetType.CATEGORY -> {
                        SelectCategoryLandscapeSheet(
                            index = categories
                                .indexOfFirst { it.categoryId == selectedCategory.categoryId }
                                .coerceAtLeast(0),
                            selectedCategoryId = selectedCategory.categoryId,
                            setSelectedCategory = setSelectedCategory,
                            categories = categories,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        sheetType = SheetType.NONE
                                    }
                            }
                        )
                    }

                    SheetType.COUNTERPARTY -> {
                        SelectCounterPartyLandscapeSheet(
                            index = counterParties
                                .indexOfFirst { it.counterPartyId == selectedCounterParty.counterPartyId }
                                .coerceAtLeast(0),
                            selectedCounterPartyId = selectedCounterParty.counterPartyId,
                            setSelectedCounterParty = setSelectedCounterParty,
                            counterParties = counterParties,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        sheetType = SheetType.NONE
                                    }
                            }
                        )
                    }

                    SheetType.METHOD -> {
                        SelectMethodLandscapeSheet(
                            index = methods
                                .indexOfFirst { it.methodId == selectedMethod.methodId }
                                .coerceAtLeast(0),
                            selectedMethodId = selectedMethod.methodId,
                            setSelectedMethod = setSelectedMethod,
                            methods = methods,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        sheetType = SheetType.NONE
                                    }
                            }
                        )
                    }

                    else -> {}
                }
            },
            onDismissSheet = {
                sheetType = SheetType.NONE
            },
            showEmptyPlaceholder = !hasData,
            emptyPlaceholderText = stringResource(id = R.string.charts_screen_empty_placeholder_label),
            firstColContent = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 10.dp, end = 10.dp, bottom = 10.dp, top = 2.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ChartsScreenBottomAppBar(
                        title = stringResource(id = R.string.charts_label),
                        enabled = !loading,
                        selectedChartType = selectedChartType,
                        startYearMonthString = startYearMonthString,
                        endYearMonthString = endYearMonthString,
                        yearMonthRangeStringRes = yearMonthRangeStringRes,
                        selectedCurrency = selectedCurrency,
                        selectedAccount = selectedAccount.accountName,
                        selectedCategory = selectedCategory.categoryName,
                        selectedCounterParty = selectedCounterParty.counterPartyName,
                        selectedMethod = selectedMethod.methodName,
                        navigateBack = navigateBack,
                        onSelectChartClick = {
                            sheetType = SheetType.CHART
                        },
                        onSelectCurrencyClick = {
                            sheetType = SheetType.CURRENCY
                        },
                        onSelectAccountClick = {
                            sheetType = SheetType.ACCOUNT
                        },
                        onSelectCategoryClick = {
                            sheetType = SheetType.CATEGORY
                        },
                        onSelectCounterPartyClick = {
                            sheetType = SheetType.COUNTERPARTY
                        },
                        onSelectMethodClick = {
                            sheetType = SheetType.METHOD
                        },
                        onSelectYearMonthClick = {
                            sheetType = SheetType.MONTH_RANGE
                        },
                    )
                }
            },
            secondColContent = {
                if (transactionCountChartTypes.contains(selectedChartType)) {
                    TransactionCountBarChart(
                        modelProducer = modelProducer,
                        labelComponent = labelComponent,
                        labelKey = labelKey,
                    )
                } else if (totalAmountChartTypes.contains(selectedChartType)) {
                    TotalAmountBarChart(
                        modelProducer = modelProducer,
                        labelComponent = labelComponent,
                        labelKey = labelKey,
                        shortCurrencyFormatter = shortCurrencyFormatterMap.getValue(
                            selectedCurrency
                        ),
                        currencyFormatter = currencyFormatterMap.getValue(
                            selectedCurrency
                        ),
                    )
                } else if (selectedChartType == ChartType.TRANSACTION_TYPE_AMOUNT_BAR_CHART) {
                    TotalDebitCreditBarChart(
                        modelProducer = modelProducer,
                        labelComponent = labelComponent,
                        shortCurrencyFormatter = shortCurrencyFormatterMap.getValue(
                            selectedCurrency
                        ),
                        currencyFormatter = currencyFormatterMap.getValue(
                            selectedCurrency
                        ),
                    )
                } else {
                    TrendsLineChart(
                        modelProducer = modelProducer,
                        labelComponent = labelComponent,
                        labelKey = labelKey,
                        shortCurrencyFormatter = shortCurrencyFormatterMap.getValue(
                            selectedCurrency
                        ),
                        currencyFormatter = currencyFormatterMap.getValue(
                            selectedCurrency
                        ),
                    )
                }
            }
        )
    } else {
        Scaffold(
            modifier = Modifier
                .animateContentSize()
                .navigationBarsPadding()
                .imePadding()
                .fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(id = R.string.charts_label)
                            )
                            Text(
                                text = stringResource(id = R.string.charts_subtext_label),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.alpha(0.8f)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                )
            },
            bottomBar = {
                ChartsScreenBottomAppBar(
                    enabled = !loading,
                    selectedChartType = selectedChartType,
                    startYearMonthString = startYearMonthString,
                    endYearMonthString = endYearMonthString,
                    yearMonthRangeStringRes = yearMonthRangeStringRes,
                    selectedCurrency = selectedCurrency,
                    selectedAccount = selectedAccount.accountName,
                    selectedCategory = selectedCategory.categoryName,
                    selectedCounterParty = selectedCounterParty.counterPartyName,
                    selectedMethod = selectedMethod.methodName,
                    navigateBack = navigateBack,
                    onSelectChartClick = {
                        sheetType = SheetType.CHART
                    },
                    onSelectCurrencyClick = {
                        sheetType = SheetType.CURRENCY
                    },
                    onSelectAccountClick = {
                        sheetType = SheetType.ACCOUNT
                    },
                    onSelectCategoryClick = {
                        sheetType = SheetType.CATEGORY
                    },
                    onSelectCounterPartyClick = {
                        sheetType = SheetType.COUNTERPARTY
                    },
                    onSelectMethodClick = {
                        sheetType = SheetType.METHOD
                    },
                    onSelectYearMonthClick = {
                        sheetType = SheetType.MONTH_RANGE
                    },
                )
            }
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    if (hasData) {
                        if (transactionCountChartTypes.contains(selectedChartType)) {
                            TransactionCountBarChart(
                                modelProducer = modelProducer,
                                labelComponent = labelComponent,
                                labelKey = labelKey,
                                padding = PaddingValues(all = 15.dp),
                            )
                        } else if (totalAmountChartTypes.contains(selectedChartType)) {
                            TotalAmountBarChart(
                                modelProducer = modelProducer,
                                labelComponent = labelComponent,
                                labelKey = labelKey,
                                shortCurrencyFormatter = shortCurrencyFormatterMap.getValue(
                                    selectedCurrency
                                ),
                                currencyFormatter = currencyFormatterMap.getValue(
                                    selectedCurrency
                                ),
                                padding = PaddingValues(all = 15.dp),
                            )
                        } else if (selectedChartType == ChartType.TRANSACTION_TYPE_AMOUNT_BAR_CHART) {
                            TotalDebitCreditBarChart(
                                modelProducer = modelProducer,
                                labelComponent = labelComponent,
                                shortCurrencyFormatter = shortCurrencyFormatterMap.getValue(
                                    selectedCurrency
                                ),
                                currencyFormatter = currencyFormatterMap.getValue(
                                    selectedCurrency
                                ),
                                padding = PaddingValues(all = 15.dp),
                            )
                        } else {
                            TrendsLineChart(
                                modelProducer = modelProducer,
                                labelComponent = labelComponent,
                                labelKey = labelKey,
                                shortCurrencyFormatter = shortCurrencyFormatterMap.getValue(
                                    selectedCurrency
                                ),
                                currencyFormatter = currencyFormatterMap.getValue(
                                    selectedCurrency
                                ),
                                padding = PaddingValues(all = 15.dp),
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(id = R.string.charts_screen_empty_placeholder_label),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
            if (sheetType != SheetType.NONE) {
                ModalBottomSheet(
                    onDismissRequest = {
                        sheetType = SheetType.NONE
                    },
                    sheetState = sheetState,
                    sheetGesturesEnabled = false,
                ) {
                    when (sheetType) {
                        SheetType.CHART -> {
                            SelectChartTypeSheet(
                                index = chartTypes
                                    .indexOfFirst { it == selectedChartType }
                                    .coerceAtLeast(0),
                                selectedChartType = selectedChartType,
                                setSelectedChartType = setChartType,
                                chartTypes = chartTypes,
                                dismiss = {
                                    scope
                                        .launch { sheetState.hide() }
                                        .invokeOnCompletion {
                                            sheetType = SheetType.NONE
                                        }
                                }
                            )
                        }

                        SheetType.MONTH_RANGE -> {
                            MonthRangePickerSheet(
                                start = yearMonthSelectionStart,
                                end = yearMonthSelectionEnd,
                                filter = setYearMonthRange,
                                reset = {
                                    setYearMonthRange(null, null)
                                },
                                dismiss = {
                                    scope
                                        .launch { sheetState.hide() }
                                        .invokeOnCompletion {
                                            sheetType = SheetType.NONE
                                        }
                                }
                            )
                        }

                        SheetType.CURRENCY -> {
                            CurrencySheet(
                                index = currencies.indexOfFirst { it.currency.currencyCode == selectedCurrency },
                                selectedCurrency = selectedCurrency,
                                setSelectedCurrency = setSelectedCurrency,
                                currencies = currencies,
                                dismiss = {
                                    scope
                                        .launch { sheetState.hide() }
                                        .invokeOnCompletion {
                                            sheetType = SheetType.NONE
                                        }
                                }
                            )
                        }

                        SheetType.ACCOUNT -> {
                            SelectAccountSheet(
                                index = accounts
                                    .indexOfFirst { it.accountId == selectedAccount.accountId }
                                    .coerceAtLeast(0),
                                selectedAccountId = selectedAccount.accountId,
                                setSelectedAccount = setSelectedAccount,
                                accounts = accounts,
                                dismiss = {
                                    scope
                                        .launch { sheetState.hide() }
                                        .invokeOnCompletion {
                                            sheetType = SheetType.NONE
                                        }
                                }
                            )
                        }

                        SheetType.CATEGORY -> {
                            SelectCategorySheet(
                                index = categories
                                    .indexOfFirst { it.categoryId == selectedCategory.categoryId }
                                    .coerceAtLeast(0),
                                selectedCategoryId = selectedCategory.categoryId,
                                setSelectedCategory = setSelectedCategory,
                                categories = categories,
                                dismiss = {
                                    scope
                                        .launch { sheetState.hide() }
                                        .invokeOnCompletion {
                                            sheetType = SheetType.NONE
                                        }
                                }
                            )
                        }

                        SheetType.COUNTERPARTY -> {
                            SelectCounterPartySheet(
                                index = counterParties
                                    .indexOfFirst { it.counterPartyId == selectedCounterParty.counterPartyId }
                                    .coerceAtLeast(0),
                                selectedCounterPartyId = selectedCounterParty.counterPartyId,
                                setSelectedCounterParty = setSelectedCounterParty,
                                counterParties = counterParties,
                                dismiss = {
                                    scope
                                        .launch { sheetState.hide() }
                                        .invokeOnCompletion {
                                            sheetType = SheetType.NONE
                                        }
                                }
                            )
                        }

                        SheetType.METHOD -> {
                            SelectMethodSheet(
                                index = methods
                                    .indexOfFirst { it.methodId == selectedMethod.methodId }
                                    .coerceAtLeast(0),
                                selectedMethodId = selectedMethod.methodId,
                                setSelectedMethod = setSelectedMethod,
                                methods = methods,
                                dismiss = {
                                    scope
                                        .launch { sheetState.hide() }
                                        .invokeOnCompletion {
                                            sheetType = SheetType.NONE
                                        }
                                }
                            )
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun TrendsLineChart(
    modelProducer: CartesianChartModelProducer,
    labelComponent: TextComponent,
    labelKey: ExtraStore.Key<List<String>>,
    shortCurrencyFormatter: LocalizedNumberFormatter,
    currencyFormatter: LocalizedNumberFormatter,
    padding: PaddingValues = PaddingValues(0.dp),
) {
    val creditLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(
            fill(TransactionType.CREDIT.color)
        ),
        areaFill = LineCartesianLayer.AreaFill.single(
            fill(
                ShaderProvider.verticalGradient(
                    arrayOf(TransactionType.CREDIT.color.copy(alpha = 0.4f), Color.Transparent)
                )
            )
        )
    )
    val debitLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(
            fill(TransactionType.DEBIT.color)
        ),
        areaFill = LineCartesianLayer.AreaFill.single(
            fill(
                ShaderProvider.verticalGradient(
                    arrayOf(TransactionType.DEBIT.color.copy(alpha = 0.4f), Color.Transparent)
                )
            )
        )
    )

    val lineProvider by remember {
        mutableStateOf(
            object : LineCartesianLayer.LineProvider {
                override fun getLine(
                    seriesIndex: Int,
                    extraStore: ExtraStore
                ): LineCartesianLayer.Line {
                    return if (seriesIndex == 0) {
                        debitLine
                    } else {
                        creditLine
                    }
                }
            }
        )
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = lineProvider,
            ),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = { _, value, _ ->
                    shortCurrencyFormatter.format(value).toString()
                }
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = { context, value, _ ->
                    context.model.extraStore[labelKey][value.toInt()]
                },
            ),
            marker = rememberDefaultCartesianMarker(
                label = labelComponent,
                valueFormatter = { _, targets ->
                    if (targets.isNotEmpty() && targets[0] is LineCartesianLayerMarkerTarget) {
                        SpannableStringBuilder().apply {
                            val points = (targets[0] as LineCartesianLayerMarkerTarget).points
                            points.forEachIndexed { index, point ->
                                append(
                                    currencyFormatter.format(point.entry.y).toString(),
                                    ForegroundColorSpan(point.color),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                                if (index != points.lastIndex) append(", ")
                            }
                        }
                    } else {
                        ""
                    }
                }
            ),
        ),
        zoomState = rememberVicoZoomState(
            zoomEnabled = true,
        ),
        scrollState = rememberVicoScrollState(
            scrollEnabled = true,
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
    )
}

@Composable
fun TransactionCountBarChart(
    modelProducer: CartesianChartModelProducer,
    labelComponent: TextComponent,
    labelKey: ExtraStore.Key<List<String>>,
    padding: PaddingValues = PaddingValues(0.dp),
) {
    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                dataLabel = labelComponent,
                columnCollectionSpacing = 3.dp
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                guideline = null,
                itemPlacer = HorizontalAxis.ItemPlacer.segmented(),
                valueFormatter = { context, value, _ ->
                    context.model.extraStore[labelKey][value.toInt()]
                },
            ),
            marker = rememberDefaultCartesianMarker(
                label = labelComponent,
                valueFormatter = { context, targets ->
                    if (targets.isNotEmpty() && targets[0] is ColumnCartesianLayerMarkerTarget) {
                        context.model.extraStore[labelKey][targets[0].x.toInt()]
                    } else {
                        ""
                    }
                }
            ),
        ),
        zoomState = rememberVicoZoomState(
            zoomEnabled = true,
            initialZoom = remember {
                Zoom.x(5.0)
            }
        ),
        scrollState = rememberVicoScrollState(
            scrollEnabled = true,
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
    )
}

@Composable
fun TotalAmountBarChart(
    modelProducer: CartesianChartModelProducer,
    labelComponent: TextComponent,
    labelKey: ExtraStore.Key<List<String>>,
    shortCurrencyFormatter: LocalizedNumberFormatter,
    currencyFormatter: LocalizedNumberFormatter,
    padding: PaddingValues = PaddingValues(0.dp),
) {
    val creditColumn = rememberLineComponent(
        fill = fill(TransactionType.CREDIT.color),
        thickness = 8.dp
    )
    val debitColumn = rememberLineComponent(
        fill = fill(TransactionType.DEBIT.color),
        thickness = 8.dp
    )

    val columnProvider by remember {
        mutableStateOf(
            object : ColumnCartesianLayer.ColumnProvider {
                override fun getColumn(
                    entry: ColumnCartesianLayerModel.Entry,
                    seriesIndex: Int,
                    extraStore: ExtraStore
                ): LineComponent {
                    return if (seriesIndex == 0) {
                        debitColumn
                    } else {
                        creditColumn
                    }
                }

                override fun getWidestSeriesColumn(
                    seriesIndex: Int,
                    extraStore: ExtraStore
                ): LineComponent {
                    return creditColumn
                }

            }
        )
    }

    CartesianChartHost(
        rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = columnProvider,
                columnCollectionSpacing = 3.dp,
                dataLabel = labelComponent,
                dataLabelValueFormatter = { _, value, _ ->
                    currencyFormatter.format(value).toString()
                },
                mergeMode = { ColumnCartesianLayer.MergeMode.stacked() }
            ),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = { _, value, _ ->
                    shortCurrencyFormatter.format(value).toString()
                }
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                guideline = null,
                itemPlacer = HorizontalAxis.ItemPlacer.segmented(),
                valueFormatter = { context, value, _ ->
                    context.model.extraStore[labelKey][value.toInt()]
                },
            ),
            marker = rememberDefaultCartesianMarker(
                label = labelComponent,
                valueFormatter = { context, targets ->
                    if (targets.isNotEmpty() && targets[0] is ColumnCartesianLayerMarkerTarget) {
                        context.model.extraStore[labelKey][targets[0].x.toInt()]
                    } else {
                        ""
                    }
                }
            ),
        ),
        zoomState = rememberVicoZoomState(
            zoomEnabled = true,
            initialZoom = remember {
                Zoom.x(5.0)
            }
        ),
        scrollState = rememberVicoScrollState(
            scrollEnabled = true,
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
    )
}

@Composable
fun TotalDebitCreditBarChart(
    modelProducer: CartesianChartModelProducer,
    labelComponent: TextComponent,
    shortCurrencyFormatter: LocalizedNumberFormatter,
    currencyFormatter: LocalizedNumberFormatter,
    padding: PaddingValues = PaddingValues(0.dp),
) {
    val creditLabel = stringResource(id = TransactionType.CREDIT.label)
    val debitLabel = stringResource(id = TransactionType.DEBIT.label)

    val creditColumn = rememberLineComponent(
        fill = fill(TransactionType.CREDIT.color),
        thickness = 8.dp
    )
    val debitColumn = rememberLineComponent(
        fill = fill(TransactionType.DEBIT.color),
        thickness = 8.dp
    )

    val columnProvider by remember {
        mutableStateOf(
            object : ColumnCartesianLayer.ColumnProvider {
                override fun getColumn(
                    entry: ColumnCartesianLayerModel.Entry,
                    seriesIndex: Int,
                    extraStore: ExtraStore
                ): LineComponent {
                    return if (entry.x == 0.0) {
                        creditColumn
                    } else {
                        debitColumn
                    }
                }

                override fun getWidestSeriesColumn(
                    seriesIndex: Int,
                    extraStore: ExtraStore
                ): LineComponent {
                    return creditColumn
                }

            }
        )
    }

    CartesianChartHost(
        rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = columnProvider,
                columnCollectionSpacing = 3.dp,
                dataLabel = labelComponent,
                dataLabelValueFormatter = { _, value, _ ->
                    currencyFormatter.format(value).toString()
                }
            ),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = { _, value, _ ->
                    shortCurrencyFormatter.format(value).toString()
                }
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                guideline = null,
                itemPlacer = HorizontalAxis.ItemPlacer.segmented(),
                valueFormatter = { _, value, _ ->
                    if (value == 0.0) {
                        creditLabel
                    } else {
                        debitLabel
                    }
                },
            ),
            marker = rememberDefaultCartesianMarker(
                label = labelComponent,
                valueFormatter = { _, targets ->
                    if (targets.isNotEmpty() && targets[0] is ColumnCartesianLayerMarkerTarget) {
                        if (targets[0].x == 0.0) {
                            creditLabel
                        } else {
                            debitLabel
                        }
                    } else {
                        ""
                    }
                }
            ),
        ),
        zoomState = rememberVicoZoomState(
            zoomEnabled = true,
            initialZoom = remember {
                Zoom.x(5.0)
            }
        ),
        scrollState = rememberVicoScrollState(
            scrollEnabled = true,
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
    )
}

fun EntryProviderScope<NavKey>.chartsScreen(backStack: NavBackStack<NavKey>) {
    entry<InsightsRoute> { route ->
        val viewModel: ChartsScreenViewModel = koinViewModel {
            parametersOf(route.type)
        }
        val state by viewModel.state.collectAsState()
        val loading by viewModel.loadingState.collectAsState()
        val hasData by viewModel.hasDataState.collectAsState()

        val modelProducer by remember {
            mutableStateOf(
                viewModel.modelProducer
            )
        }

        ChartsScreen(
            navigateBack = {
                backStack.removeLastOrNull()
            },
            setLocale = viewModel::setLocale,
            setChartType = viewModel::setChartType,
            setYearMonthRange = viewModel::setYearMonthRange,
            setSelectedCurrency = viewModel::setSelectedCurrency,
            setSelectedAccount = viewModel::setSelectedAccount,
            setSelectedCategory = viewModel::setSelectedCategory,
            setSelectedCounterParty = viewModel::setSelectedCounterParty,
            setSelectedMethod = viewModel::setSelectedMethod,
            modelProducer = modelProducer,
            labelKey = viewModel.labelKey,
            state = state,
            hasData = hasData,
            loading = loading
        )
    }
}
