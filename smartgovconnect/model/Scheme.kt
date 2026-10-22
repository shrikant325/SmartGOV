package com.example.smartgovconnect.model

data class Scheme(
    val id: String = "",
    val title: String = "",
    val domain: String = "",
    val level: String = "",
    val state: String = "",
    val eligibility: String = "",
    val benefits: String = "",
    val applyLink: String = "",
    val imageUrl: String = "",
    val incomeCap: Long = -1L,
    val createdAt: Long = 0L
)