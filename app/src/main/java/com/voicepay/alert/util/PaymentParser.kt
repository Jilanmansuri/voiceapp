package com.voicepay.alert.util

/**
 * Parses payment notification text to extract amount and sender name.
 */
object PaymentParser {

    private val PAYMENT_KEYWORDS = listOf(
        "received",
        "credited",
        "paid you",
        "money received",
        "received from",
        "sent you",
        "payment received",
        "upi"
    )

    // ₹500, Rs 500, INR 500, 500 rupees
    private val AMOUNT_PATTERNS = listOf(
        Regex("""[₹]\s*([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""(?:rs\.?|inr)\s*([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""([\d,]+(?:\.\d{1,2})?)\s*(?:rupees?|rs\.?)""", RegexOption.IGNORE_CASE),
        Regex("""([\d,]+(?:\.\d{1,2})?)\s*received""", RegexOption.IGNORE_CASE)
    )

    private val SENDER_PATTERNS = listOf(
        Regex("""received from\s+([A-Za-z0-9\s.@]+?)(?:\s+on|\s+via|\s*\.|$)""", RegexOption.IGNORE_CASE),
        Regex("""([A-Za-z0-9\s.@]+?)\s+paid you""", RegexOption.IGNORE_CASE),
        Regex("""([A-Za-z0-9\s.@]+?)\s+sent you""", RegexOption.IGNORE_CASE),
        Regex("""from\s+([A-Za-z0-9\s.@]+?)(?:\s+has|\s+sent|\s*\.|$)""", RegexOption.IGNORE_CASE)
    )

    data class ParsedPayment(
        val amount: String,
        val amountValue: Double,
        val sender: String?,
        val rawText: String
    )

    fun isPaymentNotification(title: String?, text: String?): Boolean {
        val combined = "${title.orEmpty()} ${text.orEmpty()}".lowercase()
        if (combined.isBlank()) return false
        return PAYMENT_KEYWORDS.any { combined.contains(it) } ||
            combined.contains("₹") ||
            AMOUNT_PATTERNS.any { it.containsMatchIn(combined) }
    }

    fun parse(title: String?, text: String?): ParsedPayment? {
        val combined = "${title.orEmpty()} ${text.orEmpty()}".trim()
        if (!isPaymentNotification(title, text)) return null

        val amount = extractAmount(combined) ?: return null
        val sender = extractSender(combined)

        return ParsedPayment(
            amount = amount,
            amountValue = amount.replace(",", "").toDoubleOrNull() ?: 0.0,
            sender = sender?.takeIf { it.isNotBlank() },
            rawText = combined
        )
    }

    private fun extractAmount(text: String): String? {
        for (pattern in AMOUNT_PATTERNS) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1].replace(",", "").trim()
            }
        }
        return null
    }

    private fun extractSender(text: String): String? {
        for (pattern in SENDER_PATTERNS) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1].trim()
                    .replace(Regex("""\s+"""), " ")
                    .take(80)
            }
        }
        return null
    }
}
