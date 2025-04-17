package com.example.lab_exam_3_personal_finance_tracker.model

import java.io.Serializable

data class Transaction(
    val id: Long,
    var title: String,
    var amount: Double,
    var category: String,
    var date: String,
    var type: String
) : Serializable
