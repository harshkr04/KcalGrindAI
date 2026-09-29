package com.kcalgrindai.app.domain.model

data class SubscriptionPlan(
    val id: String,
    val title: String,
    val priceFormatted: String,
    val billingPeriod: String,
    val pricePerMonthEquivalent: String,
    val trialDays: Int = 7,
    val discountPercent: Int = 0,
    val isPopular: Boolean = false,
    val badge: String? = null,
    val features: List<String> = emptyList()
)
