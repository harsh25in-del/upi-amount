package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calc.Calculators
import com.example.data.db.AppDatabase
import com.example.data.model.CalculationType
import com.example.data.model.PaymentCategory
import com.example.data.model.PaymentEntity
import com.example.data.model.PaymentFrequency
import com.example.data.model.PaymentHistoryEntity
import com.example.data.repository.PaymentRepository
import com.example.util.DueUrgency
import com.example.util.UpiHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class DayForecast(
    val dayTimestamp: Long,
    val dayLabel: String,
    val dateNum: Int,
    val monthName: String,
    val dailyDebitTotal: Double,
    val projectedBalance: Double,
    val payments: List<PaymentEntity>
)

data class PaymentSummaryMetrics(
    val totalUpcoming7Days: Double = 0.0,
    val count7Days: Int = 0,
    val totalUpcoming15Days: Double = 0.0,
    val totalUpcoming30Days: Double = 0.0,
    val monthlyAutopayTotal: Double = 0.0,
    val safeBalanceAfter30Days: Double = 0.0,
    val overdueCount: Int = 0
)

class PaymentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PaymentRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = PaymentRepository(db.paymentDao(), db.paymentHistoryDao())
    }

    val allPayments: StateFlow<List<PaymentEntity>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHistory: StateFlow<List<PaymentHistoryEntity>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User's Starting Bank Balance for Cash Flow Projections
    val userBankBalance = MutableStateFlow(50000.0)

    // Filters and Search
    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow<PaymentCategory?>(null)
    val selectedTabFilter = MutableStateFlow("ALL") // "ALL", "SOON", "AUTOPAY", "COMPLETED"

    // Filtered Payments Flow
    val filteredPayments: StateFlow<List<PaymentEntity>> = combine(
        allPayments,
        searchQuery,
        selectedCategoryFilter,
        selectedTabFilter
    ) { payments, query, category, tab ->
        val now = System.currentTimeMillis()
        val sevenDaysAhead = now + TimeUnit.DAYS.toMillis(7)

        payments.filter { payment ->
            val matchesQuery = query.isBlank() ||
                    payment.title.contains(query, ignoreCase = true) ||
                    payment.payeeName.contains(query, ignoreCase = true) ||
                    payment.payeeUpiId.contains(query, ignoreCase = true)

            val matchesCategory = category == null || payment.categoryEnum == category

            val matchesTab = when (tab) {
                "SOON" -> !payment.isPaid && payment.dueTimestamp <= sevenDaysAhead
                "AUTOPAY" -> payment.isAutopayEnabled
                "COMPLETED" -> payment.isPaid
                else -> !payment.isPaid // Default ALL shows active upcoming
            }

            matchesQuery && matchesCategory && matchesTab
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Summary Metrics Flow
    val summaryMetrics: StateFlow<PaymentSummaryMetrics> = combine(
        allPayments,
        userBankBalance
    ) { payments, bankBalance ->
        val now = System.currentTimeMillis()
        val sevenDays = now + TimeUnit.DAYS.toMillis(7)
        val fifteenDays = now + TimeUnit.DAYS.toMillis(15)
        val thirtyDays = now + TimeUnit.DAYS.toMillis(30)

        var sum7 = 0.0
        var cnt7 = 0
        var sum15 = 0.0
        var sum30 = 0.0
        var autopaySum = 0.0
        var overdue = 0

        for (p in payments) {
            if (!p.isPaid) {
                val due = p.dueTimestamp
                if (due <= sevenDays) {
                    sum7 += p.amount
                    cnt7++
                }
                if (due <= fifteenDays) {
                    sum15 += p.amount
                }
                if (due <= thirtyDays) {
                    sum30 += p.amount
                }
                if (p.isAutopayEnabled) {
                    autopaySum += p.amount
                }
                val (_, urgency) = UpiHelper.getDueDateLabel(p.dueTimestamp)
                if (urgency == DueUrgency.OVERDUE) {
                    overdue++
                }
            }
        }

        PaymentSummaryMetrics(
            totalUpcoming7Days = sum7,
            count7Days = cnt7,
            totalUpcoming15Days = sum15,
            totalUpcoming30Days = sum30,
            monthlyAutopayTotal = autopaySum,
            safeBalanceAfter30Days = bankBalance - sum30,
            overdueCount = overdue
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PaymentSummaryMetrics())

    // 30-Day Day-by-Day Cash Flow Projection
    val cashFlowForecast: StateFlow<List<DayForecast>> = combine(
        allPayments,
        userBankBalance
    ) { payments, startBalance ->
        val result = mutableListOf<DayForecast>()
        var runningBalance = startBalance
        val nowCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        // Project next 30 days
        for (i in 0 until 30) {
            val dayCal = (nowCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, i) }
            val dayStart = dayCal.timeInMillis
            val dayEnd = dayStart + TimeUnit.DAYS.toMillis(1) - 1

            val matchedPayments = payments.filter { p ->
                !p.isPaid && p.dueTimestamp in dayStart..dayEnd
            }
            val dayDebit = matchedPayments.sumOf { it.amount }
            runningBalance -= dayDebit

            val dayOfWeek = dayNames[dayCal.get(Calendar.DAY_OF_WEEK) - 1]
            val dateNum = dayCal.get(Calendar.DAY_OF_MONTH)
            val monthName = monthNames[dayCal.get(Calendar.MONTH)]

            result.add(
                DayForecast(
                    dayTimestamp = dayStart,
                    dayLabel = if (i == 0) "Today" else if (i == 1) "Tmrw" else dayOfWeek,
                    dateNum = dateNum,
                    monthName = monthName,
                    dailyDebitTotal = dayDebit,
                    projectedBalance = runningBalance,
                    payments = matchedPayments
                )
            )
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Interactive Calculators State ---

    // 1. EMI Calculator States
    val emiPrincipal = MutableStateFlow(150000.0)
    val emiRate = MutableStateFlow(10.5)
    val emiTenureMonths = MutableStateFlow(24)

    val emiResult: StateFlow<Calculators.EmiResult> = combine(
        emiPrincipal, emiRate, emiTenureMonths
    ) { p, r, t ->
        Calculators.calculateEmi(p, r, t)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Calculators.calculateEmi(150000.0, 10.5, 24))

    // 2. Utility / Electricity Slab Calculator States
    val utilityUnits = MutableStateFlow(320.0)
    val utilityFixedCharge = MutableStateFlow(140.0)
    val utilityTaxRate = MutableStateFlow(5.0)

    val utilityResult: StateFlow<Calculators.UtilityResult> = combine(
        utilityUnits, utilityFixedCharge, utilityTaxRate
    ) { units, fixed, tax ->
        Calculators.calculateUtilityBill(units, fixed, tax)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Calculators.calculateUtilityBill(320.0, 140.0, 5.0))

    // 3. Daily Rate Multiplier States
    val dailyRate = MutableStateFlow(65.0)
    val dailyTotalDays = MutableStateFlow(30)
    val dailyLeaves = MutableStateFlow(2)
    val dailyExtra = MutableStateFlow(120.0)

    val dailyResult: StateFlow<Calculators.DailyRateResult> = combine(
        dailyRate, dailyTotalDays, dailyLeaves, dailyExtra
    ) { rate, days, leaves, extra ->
        Calculators.calculateDailyRate(rate, days, leaves, extra)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Calculators.calculateDailyRate(65.0, 30, 2, 120.0))

    // 4. Split Bill Calculator States
    val splitBillAmount = MutableStateFlow(4800.0)
    val splitPeople = MutableStateFlow(4)
    val splitTipPercent = MutableStateFlow(5.0)
    val splitExtraCharge = MutableStateFlow(0.0)

    val splitResult: StateFlow<Calculators.SplitBillResult> = combine(
        splitBillAmount, splitPeople, splitTipPercent, splitExtraCharge
    ) { amount, people, tip, extra ->
        Calculators.calculateSplitBill(amount, people, tip, extra)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Calculators.calculateSplitBill(4800.0, 4, 5.0, 0.0))

    // 5. Prorated Plan / Recharge Calculator States
    val prorateCost = MutableStateFlow(1499.0)
    val prorateDays = MutableStateFlow(84)

    val prorateResult: StateFlow<Calculators.ProratedPlanResult> = combine(
        prorateCost, prorateDays
    ) { cost, days ->
        Calculators.calculateProratedPlan(cost, days)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Calculators.calculateProratedPlan(1499.0, 84))

    // Repository operations
    fun savePayment(payment: PaymentEntity) {
        viewModelScope.launch {
            repository.insertOrUpdatePayment(payment)
        }
    }

    fun deletePayment(id: Long) {
        viewModelScope.launch {
            repository.deletePayment(id)
        }
    }

    fun markPaid(payment: PaymentEntity, upiRef: String = "") {
        viewModelScope.launch {
            repository.markPaymentAsPaid(payment, upiRef)
        }
    }

    fun unmarkPaid(paymentId: Long) {
        viewModelScope.launch {
            repository.unmarkPaid(paymentId)
        }
    }

    fun toggleAutopay(payment: PaymentEntity) {
        viewModelScope.launch {
            repository.toggleAutopay(payment)
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun updateBankBalance(newBalance: Double) {
        userBankBalance.value = newBalance
    }
}
