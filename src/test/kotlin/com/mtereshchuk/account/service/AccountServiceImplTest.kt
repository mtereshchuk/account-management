package com.mtereshchuk.account.service

import com.mtereshchuk.account.model.NewAccount
import com.mtereshchuk.account.util.Failure
import com.mtereshchuk.account.util.Success
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

/**
 * @author mtereshchuk
 */
class AccountServiceImplTest {
    private val service = AccountServiceImpl()

    @Test
    fun `createAccount should return success result`() {
        val newAccount = NewAccount(client = "Client", amount = BigDecimal(100), additionalInfo = null)
        val result = service.createAccount(newAccount)
        assertTrue(result is Success)

        val account = (result as Success).value
        assertTrue(account.amount == BigDecimal(100))
    }

    @Test
    fun `getAccount should return not found for non-existing account`() {
        val result = service.getAccount(999L) // Assuming 999L does not exist
        assertTrue(result is Failure)
    }
}
