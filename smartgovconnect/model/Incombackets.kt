package com.example.smartgovconnect.model

/**
 * Preset income-cap options an admin picks from when publishing a scheme under
 * "Parent Income Based Scheme". `cap` is the MAXIMUM annual family income (in ₹)
 * a user can have and still be eligible — same convention real scholarship/scheme
 * rules use ("family income must be below ₹X").
 *
 * Matching rule used everywhere in the app: a user is shown a scheme if
 * user.income <= scheme.incomeCap. A user earning ₹1,20,000/year will see any
 * scheme capped at ₹1,20,000 or higher (e.g. the "Up to ₹2,00,000" bracket),
 * but NOT a scheme capped at "Up to ₹1,00,000" since that's below their income.
 */
data class IncomeBracket(val cap: Long, val label: String)

object IncomeBrackets {
    val ALL = listOf(
        IncomeBracket(50_000L, "Up to ₹50,000 / year"),
        IncomeBracket(100_000L, "Up to ₹1,00,000 / year"),
        IncomeBracket(200_000L, "Up to ₹2,00,000 / year"),
        IncomeBracket(500_000L, "Up to ₹5,00,000 / year"),
        IncomeBracket(Long.MAX_VALUE, "No income limit")
    )

    fun labelFor(cap: Long): String =
        ALL.firstOrNull { it.cap == cap }?.label ?: "Up to ₹${cap}"
}