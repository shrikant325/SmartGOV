package com.example.smartgovconnect.model

/**
 * The 5 scheme domains this app supports.
 * `key` is what's stored in Firestore (kept stable even when the display label
 * changes — "Child" stays "Child" internally even though it now displays as
 * "Parent Income Based Scheme", so existing data/colors/queries never break).
 * `label` is what's shown to the user.
 * `emoji` is used as a lightweight icon (no image assets needed).
 */
data class Domain(val key: String, val label: String, val emoji: String)

object Domains {
    const val PARENT_INCOME_KEY = "Child" // internal key kept stable, see note above

    val ALL = listOf(
        Domain(PARENT_INCOME_KEY, "Parent Income Based Scheme", "👶"),
        Domain("Farmer", "Farmer", "🌾"),
        Domain("Handicapped", "Handicapped (PwD)", "♿"),
        Domain("SeniorCitizen", "Senior Citizen", "👴"),
        Domain("Health", "Health Scheme", "🏥")
    )

    fun labelOf(key: String): String = ALL.firstOrNull { it.key == key }?.label ?: key
    fun emojiOf(key: String): String = ALL.firstOrNull { it.key == key }?.emoji ?: "📋"
    fun isIncomeDomain(key: String): Boolean = key == PARENT_INCOME_KEY
}