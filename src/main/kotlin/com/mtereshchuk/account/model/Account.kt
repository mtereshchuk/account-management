package com.mtereshchuk.account.model

import java.math.BigDecimal

/**
 * @author mtereshchuk
 */
typealias AccountId = Long

data class Account(
    val id: AccountId,
    val client: String,
    var amount: BigDecimal,
    var additionalInfo: String?
) {
    companion object {
        const val ID = "id"
        val MAX_AMOUNT = BigDecimal(1_000_000_000_000_000L)
    }
}
