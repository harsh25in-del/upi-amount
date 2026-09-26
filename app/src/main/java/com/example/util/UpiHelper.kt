package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object UpiHelper {

    fun formatInr(amount: Double): String {
        return try {
            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            formatter.maximumFractionDigits = 2
            formatter.minimumFractionDigits = 0
            formatter.format(amount)
        } catch (_: Exception) {
            "₹ ${String.format(Locale.US, "%.2f", amount)}"
        }
    }

    fun buildUpiUri(
        upiId: String,
        payeeName: String,
        amount: Double,
        note: String
    ): String {
        val cleanUpi = upiId.trim()
        val cleanName = payeeName.trim().ifBlank { "Payee" }
        val cleanNote = note.trim().ifBlank { "Payment via PayCal" }
        val formattedAmount = String.format(Locale.US, "%.2f", amount)

        return "upi://pay?pa=${Uri.encode(cleanUpi)}&pn=${Uri.encode(cleanName)}&am=$formattedAmount&cu=INR&tn=${Uri.encode(cleanNote)}"
    }

    fun launchUpiPayment(
        context: Context,
        upiId: String,
        payeeName: String,
        amount: Double,
        note: String,
        onSuccessLaunched: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        if (upiId.isBlank()) {
            onFailure("Please provide a valid UPI ID (e.g., name@okhdfcbank)")
            return
        }

        val upiUriString = buildUpiUri(upiId, payeeName, amount, note)
        val uri = Uri.parse(upiUriString)
        val intent = Intent(Intent.ACTION_VIEW, uri)

        try {
            val chooser = Intent.createChooser(intent, "Pay via UPI App")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            onSuccessLaunched()
        } catch (e: Exception) {
            // No app can handle UPI or inside emulator without UPI apps
            // Copy UPI details to clipboard as fallback
            copyToClipboard(context, "UPI Payment Details", upiUriString)
            onFailure("No UPI application installed. Payment link copied to clipboard!")
        }
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun getDueDateLabel(dueTimestampMillis: Long): Pair<String, DueUrgency> {
        val nowCal = Calendar.getInstance()
        nowCal.set(Calendar.HOUR_OF_DAY, 0)
        nowCal.set(Calendar.MINUTE, 0)
        nowCal.set(Calendar.SECOND, 0)
        nowCal.set(Calendar.MILLISECOND, 0)

        val dueCal = Calendar.getInstance().apply { timeInMillis = dueTimestampMillis }
        dueCal.set(Calendar.HOUR_OF_DAY, 0)
        dueCal.set(Calendar.MINUTE, 0)
        dueCal.set(Calendar.SECOND, 0)
        dueCal.set(Calendar.MILLISECOND, 0)

        val diffMillis = dueCal.timeInMillis - nowCal.timeInMillis
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()

        val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(dueTimestampMillis))

        return when {
            diffDays < 0 -> Pair("Overdue by ${-diffDays}d ($formattedDate)", DueUrgency.OVERDUE)
            diffDays == 0 -> Pair("Due Today ($formattedDate)", DueUrgency.TODAY)
            diffDays == 1 -> Pair("Due Tomorrow ($formattedDate)", DueUrgency.TOMORROW)
            diffDays in 2..6 -> Pair("Due in $diffDays days ($formattedDate)", DueUrgency.SOON)
            else -> Pair("Due $formattedDate", DueUrgency.UPCOMING)
        }
    }
}

enum class DueUrgency {
    OVERDUE,
    TODAY,
    TOMORROW,
    SOON,
    UPCOMING
}
