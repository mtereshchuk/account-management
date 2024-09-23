package com.mtereshchuk.account.model

import java.math.BigDecimal

/**
 * @author mtereshchuk
 */
data class NewAccount(
    val client: String,
    val amount: BigDecimal?,
    val additionalInfo: String? = null
)

sealed interface Transaction {
    val amount: BigDecimal
    val details: String? // would be useful in case of storing transactions
}

data class Deposit(
    val toId: Long,
    override val amount: BigDecimal,
    override val details: String? = null
) : Transaction

data class Withdraw(
    val fromId: Long,
    override val amount: BigDecimal,
    override val details: String? = null
) : Transaction

data class Transfer(
    val fromId: Long,
    val toId: Long,
    override val amount: BigDecimal,
    override val details: String? = null
) : Transaction
