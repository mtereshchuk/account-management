package com.mtereshchuk.account.controller

import com.mtereshchuk.account.model.*
import java.math.BigDecimal

/**
 * @author mtereshchuk
 */
typealias ValidationError = String

class RequestValidator {
    fun validateNewAccount(newAccount: NewAccount): ValidationError? {
        val amount = newAccount.amount ?: return null

        if (amount < BigDecimal.ZERO)
            return "'amount' must be non-negative: $amount"
        if (amount > Account.MAX_AMOUNT)
            return "'amount' exceeds maximum value: $amount"

        return null
    }

    fun validateUpdateAccount(id: AccountId, updAccount: Account): ValidationError? {
        if (id != updAccount.id)
            return "'id's are not consistent: $id != ${updAccount.id}"
        return null
    }

    fun validateDeposit(deposit: Deposit) =
        validateTransaction(deposit)

    fun validateWithdraw(withdraw: Withdraw) =
        validateTransaction(withdraw)

    fun validateTransfer(transfer: Transfer): ValidationError? {
        val (fromId, toId, _, _) = transfer
        if (fromId == toId)
            return "'fromId' and 'toId' should differ: $fromId == $toId"
        return validateTransaction(transfer)
    }

    private fun validateTransaction(transaction: Transaction): ValidationError? {
        val amount = transaction.amount
        if (amount <= BigDecimal.ZERO)
            return "'amount' must be positive: $amount"
        return null
    }
}
