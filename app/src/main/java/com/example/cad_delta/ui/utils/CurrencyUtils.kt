package com.example.cad_delta.ui.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    fun formatBrl(amount: Double): String {
        return try {
            val ptBr = Locale.forLanguageTag("pt-BR")
            val formatter = NumberFormat.getCurrencyInstance(ptBr)
            formatter.format(amount)
        } catch (_: Exception) {
            String.format(Locale.getDefault(), "R$ %.2f", amount)
        }
    }
}
