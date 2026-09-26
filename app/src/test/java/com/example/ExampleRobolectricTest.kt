package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.calc.Calculators
import com.example.util.UpiHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PayCal UPI", appName)
    }

    @Test
    fun `test EMI calculation accuracy`() {
        // Loan of ₹1,00,000 at 12% per annum for 12 months
        val result = Calculators.calculateEmi(100000.0, 12.0, 12)
        // Monthly EMI should be approx 8884.88
        assertTrue(result.monthlyEmi in 8880.0..8890.0)
        assertTrue(result.totalInterest in 6600.0..6630.0)
        assertEquals(100000.0, result.principal, 0.01)
    }

    @Test
    fun `test Utility Slab calculation`() {
        // 150 units consumed:
        // Tier 1: 100 * 3.5 = 350
        // Tier 2: 50 * 5.0 = 250
        // Energy = 600, Fixed = 100, Tax (5%) = 35 -> Total = 735
        val result = Calculators.calculateUtilityBill(
            unitsConsumed = 150.0,
            fixedCharge = 100.0,
            taxPercent = 5.0,
            tier1Limit = 100.0, tier1Rate = 3.50,
            tier2Limit = 200.0, tier2Rate = 5.00
        )
        assertEquals(600.0, result.energyCharges, 0.01)
        assertEquals(100.0, result.fixedCharges, 0.01)
        assertEquals(35.0, result.taxDutyAmount, 0.01)
        assertEquals(735.0, result.totalPayable, 0.01)
    }

    @Test
    fun `test Daily Rate domestic service calculation`() {
        // ₹60/day for 30 days minus 3 leaves plus ₹120 extra
        // Billable days = 27 -> 27 * 60 = 1620 -> 1620 + 120 = 1740
        val result = Calculators.calculateDailyRate(
            dailyRate = 60.0,
            totalDays = 30,
            leaveDays = 3,
            extraCharges = 120.0
        )
        assertEquals(27, result.billableDays)
        assertEquals(1620.0, result.baseAmount, 0.01)
        assertEquals(1740.0, result.netPayable, 0.01)
    }

    @Test
    fun `test Split Bill calculation`() {
        // ₹4000 split by 4 people with 10% tip (₹400)
        // Grand total = 4400 -> Per person = 1100
        val result = Calculators.calculateSplitBill(
            billAmount = 4000.0,
            numberOfPeople = 4,
            tipPercent = 10.0,
            extraCharges = 0.0
        )
        assertEquals(400.0, result.tipAmount, 0.01)
        assertEquals(4400.0, result.grandTotal, 0.01)
        assertEquals(1100.0, result.perPersonShare, 0.01)
    }

    @Test
    fun `test UPI URI generator`() {
        val uri = UpiHelper.buildUpiUri(
            upiId = "merchant@upi",
            payeeName = "Electricity Board",
            amount = 1250.50,
            note = "Electricity Bill"
        )
        assertTrue(uri.startsWith("upi://pay?"))
        assertTrue(uri.contains("pa=merchant%40upi") || uri.contains("pa=merchant@upi"))
        assertTrue(uri.contains("am=1250.50"))
        assertTrue(uri.contains("cu=INR"))
    }
}
