package com.mharizkia.masjidmupro.utils

import android.text.Html

object AppUtils {
    fun stripHtml(html: String): String {
        return try {
            Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT).toString().trim()
        } catch (e: Exception) {
            html
        }
    }

    fun formatDateOnly(dateStr: String): String {
        return try {
            // Handle ISO format like 2024-05-20T00:00:00.000000Z
            val cleanDate = dateStr.split("T")[0]
            val parts = cleanDate.split("-")
            if (parts.size == 3) {
                "${parts[2]}-${parts[1]}-${parts[0]}"
            } else {
                dateStr
            }
        } catch (e: Exception) {
            dateStr
        }
    }
}
