package com.mtereshchuk.account.service

import com.mtereshchuk.account.model.Transfer
import com.mtereshchuk.account.model.Deposit
import com.mtereshchuk.account.model.NewAccount
import com.mtereshchuk.account.model.Withdraw
import com.mtereshchuk.account.util.Success
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

/**
 * @author mtereshchuk
 */
class AccountServiceConcurrencyTest {
    private val service = AccountServiceImpl()

    @Test
    fun `doDeposit should handle concurrent deposits correctly`() {
        val accountId = 1L
        val amount = BigDecimal(50)

        // Setup initial account
        service.createAccount(NewAccount("Client", BigDecimal(100), null))

        val latch = CountDownLatch(2)
        val executor = Executors.newFixedThreadPool(2)

        executor.submit {
            service.doDeposit(Deposit(accountId, amount, null))
            latch.countDown()
        }

        executor.submit {
            service.doDeposit(Deposit(accountId, amount, null))
            latch.countDown()
        }

        latch.await() // Wait for both deposits to complete

        // Check final balance
        val accountResult = service.getAccount(accountId)
        assertTrue(accountResult is Success)

        val account = (accountResult as Success).value
        assertEquals(BigDecimal(200).compareTo(account.amount), 0) // Should be 200 after two deposits
    }

    @Test
    fun `doWithdraw should handle concurrent withdrawals correctly`() {
        val accountId = 1L
        val amount = BigDecimal(50)

        // Setup initial account
        service.createAccount(NewAccount("Client", BigDecimal(100), null))

        val latch = CountDownLatch(2)
        val executor = Executors.newFixedThreadPool(2)

        executor.submit {
            service.doWithdraw(Withdraw(accountId, amount, null))
            latch.countDown()
        }

        executor.submit {
            service.doWithdraw(Withdraw(accountId, amount, null))
            latch.countDown()
        }

        latch.await() // Wait for both withdrawals to complete

        // Check final balance
        val accountResult = service.getAccount(accountId)
        assertTrue(accountResult is Success)

        val account = (accountResult as Success).value
        assertEquals(BigDecimal.ZERO.compareTo(account.amount), 0) // Should be 0 after two withdrawals
    }

    @Test
    fun `doTransfer should handle concurrent transfers correctly`() {
        val fromId = 1L
        val toId = 2L
        val amount = BigDecimal(50)

        // Setup initial accounts
        service.createAccount(NewAccount("Client1", BigDecimal(100), null))
        service.createAccount(NewAccount("Client2", BigDecimal(100), null))

        val latch = CountDownLatch(2)
        val executor = Executors.newFixedThreadPool(2)

        executor.submit {
            service.doTransfer(Transfer(fromId, toId, amount, null))
            latch.countDown()
        }

        executor.submit {
            service.doTransfer(Transfer(fromId, toId, amount, null))
            latch.countDown()
        }

        latch.await() // Wait for both transfers to complete

        // Check final balances
        val fromAccountResult = service.getAccount(fromId)
        val toAccountResult = service.getAccount(toId)

        assertTrue(fromAccountResult is Success)
        assertTrue(toAccountResult is Success)

        val fromAccount = (fromAccountResult as Success).value
        val toAccount = (toAccountResult as Success).value

        assertEquals(BigDecimal.ZERO.compareTo(fromAccount.amount), 0) // Should be 0 after two transfers
        assertEquals(BigDecimal(200).compareTo(toAccount.amount), 0) // Should be 200 after two transfers
    }
}
