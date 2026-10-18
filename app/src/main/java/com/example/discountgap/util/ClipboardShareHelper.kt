package com.example.discountgap.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

object ClipboardShareHelper {

    fun copyToClipboard(
        context: Context,
        text: String,
        label: String = "DiscountGap Result"
    ) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
    }

    fun shareText(
        context: Context,
        text: String,
        chooserTitle: String = "Share DiscountGap Result"
    ) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val shareIntent = Intent.createChooser(sendIntent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(shareIntent)
    }

    fun performHapticIfEnabled(
        enabled: Boolean,
        hapticFeedback: HapticFeedback,
        type: HapticFeedbackType = HapticFeedbackType.TextHandleMove
    ) {
        if (enabled) {
            hapticFeedback.performHapticFeedback(type)
        }
    }
}
