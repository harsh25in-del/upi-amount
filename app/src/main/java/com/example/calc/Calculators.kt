package com.example.calc

import java.util.Calendar
import kotlin.math.pow
import kotlin.math.roundToLong

object Calculators {

    // 1. EMI & Loan Calculator
    data class EmiResult(
        val monthlyEmi: Double,
        val totalInterest: Double,
        val totalPayment: Double,
        val principal: Double,
        val tenureMonths: Int,
        val interestRateAnnual: Double
    )

    fun calculateEmi(principal: Double, annualRatePercent: Double, tenureMonths: Int): EmiResult {
        if (principal <= 0 || tenureMonths <= 0) {
            return EmiResult(0.0, 0.0, 0.0, principal, tenureMonths, annualRatePercent)
        }
        if (annualRatePercent <= 0.0) {
            val emi = principal / tenureMonths
            return EmiResult(emi, 0.0, principal, principal, tenureMonths, 0.0)
        }
        val monthlyRate = (annualRatePercent / 12.0) / 100.0
        val factor = (1.0 + monthlyRate).pow(tenureMonths.toDouble())
        val emi = (principal * monthlyRate * factor) / (factor - 1.0)
        val totalPayment = emi * tenureMonths
        val totalInterest = totalPayment - principal
        return EmiResult(
            monthlyEmi = (emi * 100).roundToLong() / 100.0,
            totalInterest = (totalInterest * 100).roundToLong() / 100.0,
            totalPayment = (totalPayment * 100).roundToLong() / 100.0,
            principal = principal,
            tenureMonths = tenureMonths,
            interestRateAnnual = annualRatePercent
        )
    }

    // 2. Utility / Electricity Slab Calculator
    data class SlabBreakdown(
        val slabDescription: String,
        val unitsInSlab: Double,
        val ratePerUnit: Double,
        val cost: Double
    )

    data class UtilityResult(
        val totalUnits: Double,
        val slabCosts: List<SlabBreakdown>,
        val energyCharges: Double,
        val fixedCharges: Double,
        val taxDutyAmount: Double,
        val totalPayable: Double
    )

    fun calculateUtilityBill(
        unitsConsumed: Double,
        fixedCharge: Double = 120.0,
        taxPercent: Double = 5.0,
        // Standard State Slabs (0-100 @ 3.50, 101-200 @ 5.00, 201-400 @ 7.20, 400+ @ 8.80)
        tier1Limit: Double = 100.0, tier1Rate: Double = 3.50,
        tier2Limit: Double = 200.0, tier2Rate: Double = 5.00,
        tier3Limit: Double = 400.0, tier3Rate: Double = 7.20,
        tier4Rate: Double = 8.80
    ): UtilityResult {
        if (unitsConsumed <= 0) {
            val tax = (fixedCharge * taxPercent) / 100.0
            return UtilityResult(0.0, emptyList(), 0.0, fixedCharge, tax, fixedCharge + tax)
        }

        val slabs = mutableListOf<SlabBreakdown>()
        var remainingUnits = unitsConsumed
        var energyCharges = 0.0

        // Tier 1
        val t1Units = remainingUnits.coerceAtMost(tier1Limit)
        val t1Cost = t1Units * tier1Rate
        slabs.add(SlabBreakdown("0 - ${tier1Limit.toInt()} units", t1Units, tier1Rate, t1Cost))
        energyCharges += t1Cost
        remainingUnits -= t1Units

        // Tier 2
        if (remainingUnits > 0) {
            val t2Span = tier2Limit - tier1Limit
            val t2Units = remainingUnits.coerceAtMost(t2Span)
            val t2Cost = t2Units * tier2Rate
            slabs.add(SlabBreakdown("${tier1Limit.toInt()} - ${tier2Limit.toInt()} units", t2Units, tier2Rate, t2Cost))
            energyCharges += t2Cost
            remainingUnits -= t2Units
        }

        // Tier 3
        if (remainingUnits > 0) {
            val t3Span = tier3Limit - tier2Limit
            val t3Units = remainingUnits.coerceAtMost(t3Span)
            val t3Cost = t3Units * tier3Rate
            slabs.add(SlabBreakdown("${tier2Limit.toInt()} - ${tier3Limit.toInt()} units", t3Units, tier3Rate, t3Cost))
            energyCharges += t3Cost
            remainingUnits -= t3Units
        }

        // Tier 4
        if (remainingUnits > 0) {
            val t4Cost = remainingUnits * tier4Rate
            slabs.add(SlabBreakdown("Above ${tier3Limit.toInt()} units", remainingUnits, tier4Rate, t4Cost))
            energyCharges += t4Cost
        }

        val subtotal = energyCharges + fixedCharge
        val taxDuty = (subtotal * taxPercent) / 100.0
        val total = ((subtotal + taxDuty) * 100).roundToLong() / 100.0

        return UtilityResult(
            totalUnits = unitsConsumed,
            slabCosts = slabs,
            energyCharges = (energyCharges * 100).roundToLong() / 100.0,
            fixedCharges = fixedCharge,
            taxDutyAmount = (taxDuty * 100).roundToLong() / 100.0,
            totalPayable = total
        )
    }

    // 3. Daily Rate Multiplier (Domestic Services e.g. Milk, Maid, Cook, Tiffin)
    data class DailyRateResult(
        val dailyRate: Double,
        val totalDays: Int,
        val leaveDays: Int,
        val billableDays: Int,
        val baseAmount: Double,
        val extraCharges: Double,
        val netPayable: Double
    )

    fun calculateDailyRate(
        dailyRate: Double,
        totalDays: Int = 30,
        leaveDays: Int = 0,
        extraCharges: Double = 0.0
    ): DailyRateResult {
        val billableDays = (totalDays - leaveDays).coerceAtLeast(0)
        val base = billableDays * dailyRate
        val net = ((base + extraCharges) * 100).roundToLong() / 100.0
        return DailyRateResult(
            dailyRate = dailyRate,
            totalDays = totalDays,
            leaveDays = leaveDays,
            billableDays = billableDays,
            baseAmount = (base * 100).roundToLong() / 100.0,
            extraCharges = extraCharges,
            netPayable = net
        )
    }

    // 4. Split Bill / Group Share Calculator
    data class SplitBillResult(
        val billAmount: Double,
        val tipAmount: Double,
        val extraCharges: Double,
        val grandTotal: Double,
        val numberOfPeople: Int,
        val perPersonShare: Double
    )

    fun calculateSplitBill(
        billAmount: Double,
        numberOfPeople: Int = 2,
        tipPercent: Double = 0.0,
        extraCharges: Double = 0.0
    ): SplitBillResult {
        val people = numberOfPeople.coerceAtLeast(1)
        val tip = (billAmount * tipPercent) / 100.0
        val grandTotal = billAmount + tip + extraCharges
        val perPerson = ((grandTotal / people) * 100).roundToLong() / 100.0
        return SplitBillResult(
            billAmount = billAmount,
            tipAmount = (tip * 100).roundToLong() / 100.0,
            extraCharges = extraCharges,
            grandTotal = (grandTotal * 100).roundToLong() / 100.0,
            numberOfPeople = people,
            perPersonShare = perPerson
        )
    }

    // 5. Plan Proration / Prepaid Recharge Calculator
    data class ProratedPlanResult(
        val totalCost: Double,
        val durationDays: Int,
        val effectiveDailyCost: Double,
        val effectiveMonthlyCost: Double,
        val nextRenewalTimestamp: Long
    )

    fun calculateProratedPlan(
        totalCost: Double,
        durationDays: Int = 84,
        startDateMillis: Long = System.currentTimeMillis()
    ): ProratedPlanResult {
        val days = durationDays.coerceAtLeast(1)
        val daily = totalCost / days
        val monthly = daily * 30.0
        val cal = Calendar.getInstance().apply {
            timeInMillis = startDateMillis
            add(Calendar.DAY_OF_YEAR, days)
        }
        return ProratedPlanResult(
            totalCost = totalCost,
            durationDays = days,
            effectiveDailyCost = (daily * 100).roundToLong() / 100.0,
            effectiveMonthlyCost = (monthly * 100).roundToLong() / 100.0,
            nextRenewalTimestamp = cal.timeInMillis
        )
    }
}
