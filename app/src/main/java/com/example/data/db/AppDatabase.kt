package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CalculationType
import com.example.data.model.PaymentCategory
import com.example.data.model.PaymentEntity
import com.example.data.model.PaymentFrequency
import com.example.data.model.PaymentHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [PaymentEntity::class, PaymentHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun paymentDao(): PaymentDao
    abstract fun paymentHistoryDao(): PaymentHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "upi_payments_calculator.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed initial starter data
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { prepopulate(it) }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prepopulate(database: AppDatabase) {
            val now = Calendar.getInstance()
            val dao = database.paymentDao()

            val cal1 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 2) }
            val cal2 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 5) }
            val cal3 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 10) }
            val cal4 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 14) }
            val cal5 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 22) }

            val initialPayments = listOf(
                PaymentEntity(
                    title = "Tata Power Electricity Bill",
                    payeeUpiId = "tatapower@icici",
                    payeeName = "Tata Power DDL",
                    amount = 2340.0,
                    frequency = PaymentFrequency.MONTHLY.name,
                    category = PaymentCategory.BILLS_UTILITY.name,
                    calculationType = CalculationType.UTILITY_SLABS.name,
                    calcDetails = "310 units (Tiers: 0-100@₹3.5, 101-200@₹5, 201+@₹7.5 + ₹140 fixed)",
                    dueTimestamp = cal1.timeInMillis,
                    isAutopayEnabled = true,
                    isPaid = false,
                    notes = "Meter Consumer #7489201"
                ),
                PaymentEntity(
                    title = "Weekend Villa Trip Split Share",
                    payeeUpiId = "rahul.gupta@paytm",
                    payeeName = "Rahul Gupta",
                    amount = 1425.0,
                    frequency = PaymentFrequency.ONCE.name,
                    category = PaymentCategory.OTHER.name,
                    calculationType = CalculationType.BILL_SPLIT.name,
                    calcDetails = "₹5,700 total bill split evenly among 4 friends",
                    dueTimestamp = cal2.timeInMillis,
                    isAutopayEnabled = false,
                    isPaid = false,
                    notes = "Villa booking + food split"
                ),
                PaymentEntity(
                    title = "HDFC Two-Wheeler EMI",
                    payeeUpiId = "hdfcloans@hdfcbank",
                    payeeName = "HDFC Bank Retail Loans",
                    amount = 4850.0,
                    frequency = PaymentFrequency.MONTHLY.name,
                    category = PaymentCategory.EMI_LOAN.name,
                    calculationType = CalculationType.EMI_LOAN.name,
                    calcDetails = "Loan ₹1,05,000 @ 10.25% for 24 months (EMI calculated: ₹4,850)",
                    dueTimestamp = cal3.timeInMillis,
                    isAutopayEnabled = true,
                    isPaid = false,
                    notes = "Loan A/C: 501009827"
                ),
                PaymentEntity(
                    title = "Daily Organic Milk Delivery",
                    payeeUpiId = "freshdairy@ybl",
                    payeeName = "Amrit Dairy Farm",
                    amount = 1860.0,
                    frequency = PaymentFrequency.MONTHLY.name,
                    category = PaymentCategory.SERVICES_DOMESTIC.name,
                    calculationType = CalculationType.DAILY_RATE.name,
                    calcDetails = "₹60/day × 31 days - 0 leaves = ₹1,860",
                    dueTimestamp = cal4.timeInMillis,
                    isAutopayEnabled = false,
                    isPaid = false,
                    notes = "1 Litre daily pouch delivery"
                ),
                PaymentEntity(
                    title = "Netflix Premium 4K",
                    payeeUpiId = "netflix@upi",
                    payeeName = "Netflix Entertainment",
                    amount = 649.0,
                    frequency = PaymentFrequency.MONTHLY.name,
                    category = PaymentCategory.SUBSCRIPTION.name,
                    calculationType = CalculationType.MANUAL.name,
                    calcDetails = "Fixed monthly recurring Autopay",
                    dueTimestamp = cal5.timeInMillis,
                    isAutopayEnabled = true,
                    isPaid = false,
                    notes = "Monthly Autopay active on UPI mandate"
                )
            )

            for (p in initialPayments) {
                dao.insertPayment(p)
            }
        }
    }
}
