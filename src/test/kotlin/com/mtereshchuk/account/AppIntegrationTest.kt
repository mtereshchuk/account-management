package com.mtereshchuk.account

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mtereshchuk.account.model.*
import io.javalin.testtools.JavalinTest
import okhttp3.Response
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class AppIntegrationTest {
    @Test
    fun `full system test for account creation and transactions`() = JavalinTest.test(App.create()) { _, client ->
        // Create a new account
        val newAccount = NewAccount(client = "Client1", amount = BigDecimal(100))
        val createResponse = client.post("/api/v1/accounts", newAccount)

        assertEquals(201, createResponse.code)
        val createdAccount = createResponse.body<Account>()
        assertNotNull(createdAccount.id)

        // Verify account creation
        val getAccountResponse = client.get("/api/v1/accounts/${createdAccount.id}")
        assertEquals(200, getAccountResponse.code)

        val fetchedAccount = getAccountResponse.body<Account>()
        assertEquals(createdAccount.client, fetchedAccount.client)
        assertEquals(createdAccount.amount, fetchedAccount.amount)

        // Perform a deposit
        val deposit = Deposit(toId = createdAccount.id, amount = BigDecimal(50))
        val depositResponse = client.post("/api/v1/transactions/deposit", deposit)
        assertEquals(200, depositResponse.code)

        // Verify the deposit
        val updatedAccountResponse = client.get("/api/v1/accounts/${createdAccount.id}")
        val updatedAccount = updatedAccountResponse.body<Account>()
        assertEquals(BigDecimal(150), updatedAccount.amount)

        // Perform a withdrawal
        val withdraw = Withdraw(fromId = createdAccount.id, amount = BigDecimal(30))
        val withdrawResponse = client.post("/api/v1/transactions/withdraw", withdraw)
        assertEquals(200, withdrawResponse.code)

        // Verify the withdrawal
        val finalAccountResponse = client.get("/api/v1/accounts/${createdAccount.id}")
        val finalAccount = finalAccountResponse.body<Account>()
        assertEquals(BigDecimal(120), finalAccount.amount)

        // Create another account for transfer
        val newAccount2 = NewAccount(client = "Client2", amount = BigDecimal(100))
        val createResponse2 = client.post("/api/v1/accounts", newAccount2)
        assertEquals(201, createResponse2.code)

        val createdAccount2 = createResponse2.body<Account>()

        // Perform a transfer
        val transfer = Transfer(fromId = createdAccount.id, toId = createdAccount2.id, amount = BigDecimal(50))
        val transferResponse = client.post("/api/v1/transactions/transfer", transfer)
        assertEquals(200, transferResponse.code)

        // Verify the transfer
        val accountAfterTransferResponse = client.get("/api/v1/accounts/${createdAccount.id}")
        val accountAfterTransfer = accountAfterTransferResponse.body<Account>()
        assertEquals(BigDecimal(70), accountAfterTransfer.amount)

        val accountAfterTransfer2Response = client.get("/api/v1/accounts/${createdAccount2.id}")
        val accountAfterTransfer2 = accountAfterTransfer2Response.body<Account>()
        assertEquals(BigDecimal(150), accountAfterTransfer2.amount)
    }

    companion object {
        private val JSON = jacksonObjectMapper()

        private inline fun <reified T> Response.body() =
            JSON.readValue(body?.string(), T::class.java)
    }
}
