package com.mtereshchuk.account.controller

import com.mtereshchuk.account.model.Account
import com.mtereshchuk.account.model.NewAccount
import com.mtereshchuk.account.model.Transfer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.math.BigDecimal

/**
 * @author mtereshchuk
 */
class RequestValidatorTest {
    private val validator = RequestValidator()

    @Test
    fun `validateNewAccount should return error for negative amount`() {
        val account = NewAccount(client = "Client", amount = BigDecimal(-1), additionalInfo = null)
        val error = validator.validateNewAccount(account)
        assertEquals("'amount' must be non-negative: -1", error)
    }

    @Test
    fun `validateNewAccount should return error for exceeding maximum amount`() {
        val account = NewAccount(client = "Client", amount = Account.MAX_AMOUNT + BigDecimal.ONE, additionalInfo = null)
        val error = validator.validateNewAccount(account)
        assertEquals("'amount' exceeds maximum value: 1000000000000001", error)
    }

    @Test
    fun `validateNewAccount should return null for valid account`() {
        val account = NewAccount(client = "Client", amount = BigDecimal(100), additionalInfo = null)
        val error = validator.validateNewAccount(account)
        assertNull(error)
    }

    @Test
    fun `validateNewAccount should return error for null amount`() {
        val account = NewAccount(client = "Client", amount = null, additionalInfo = null)
        val error = validator.validateNewAccount(account)
        assertNull(error)
    }

    @Test
    fun `validateTransfer should return error for same fromId and toId`() {
        val transfer = Transfer(fromId = 1L, toId = 1L, amount = BigDecimal(100), details = null)
        val error = validator.validateTransfer(transfer)
        assertEquals("'fromId' and 'toId' should differ: 1 == 1", error)
    }
}
