package com.mtereshchuk.account.controller

import com.mtereshchuk.account.model.*
import com.mtereshchuk.account.service.AccountService
import com.mtereshchuk.account.util.Failure
import com.mtereshchuk.account.util.Result
import com.mtereshchuk.account.util.Success
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import io.javalin.http.bodyAsClass

/**
 * @author mtereshchuk
 */
const val API_PREFIX = "api/v1"

class AccountController(private val service: AccountService) {
    private val validator = RequestValidator()

    fun registerRoutes(app: Javalin): Javalin = app.routes {
        path(API_PREFIX) {
            get("/accounts", ::getAccounts)
            post("/accounts", ::createAccount)
            get("/accounts/{id}", ::getAccount)
            put("/accounts/{id}", ::updateAccount)
            delete("/accounts/{id}", ::deleteAccount)
            // delete("/accounts/{id}/transactions", ::getAccountTransactions)

            // get("/transactions", ::getTransactions)
            post("/transactions/deposit", ::doDeposit)
            post("/transactions/withdraw", ::doWithdraw)
            post("/transactions/transfer", ::doTransfer)
        }
    }

    private fun getAccounts(ctx: Context) {
        ctx.json(service.getAccounts())
    }

    private fun createAccount(ctx: Context) {
        val newAccount = ctx.bodyAsClass<NewAccount>()
        val error = validator.validateNewAccount(newAccount)
        if (error != null) {
            ctx.badRequest(error)
            return
        }

        val result = service.createAccount(newAccount)
        ctx.handle(result, HttpStatus.CREATED)
    }

    private fun getAccount(ctx: Context) {
        val id = ctx.pathAccountId()
        val result = service.getAccount(id)
        ctx.handle(result)
    }

    private fun updateAccount(ctx: Context) {
        val id = ctx.pathAccountId()
        val account = ctx.bodyAsClass<Account>()

        val error = validator.validateUpdateAccount(id, account)
        if (error != null) {
            ctx.badRequest(error)
            return
        }

        val result = service.updateAccount(id, account)
        ctx.handle(result)
    }

    private fun deleteAccount(ctx: Context) {
        val id = ctx.pathAccountId()
        val result = service.deleteAccount(id)
        ctx.handle(result, HttpStatus.NO_CONTENT)
    }

    private fun doDeposit(ctx: Context) {
        val deposit = ctx.bodyAsClass<Deposit>()
        val error = validator.validateDeposit(deposit)
        if (error != null) {
            ctx.badRequest(error)
            return
        }

        val result = service.doDeposit(deposit)
        ctx.handle(result)
    }

    private fun doWithdraw(ctx: Context) {
        val withdraw = ctx.bodyAsClass<Withdraw>()
        val error = validator.validateWithdraw(withdraw)
        if (error != null) {
            ctx.badRequest(error)
            return
        }

        val result = service.doWithdraw(withdraw)
        ctx.handle(result)
    }

    private fun doTransfer(ctx: Context) {
        val transfer = ctx.bodyAsClass<Transfer>()
        val error = validator.validateTransfer(transfer)
        if (error != null) {
            ctx.badRequest(error)
            return
        }

        val result = service.doTransfer(transfer)
        ctx.handle(result)
    }

    private fun Context.pathAccountId() =
        pathParam(Account.ID).toLong()

    private fun Context.badRequest(error: ValidationError) {
        status(HttpStatus.BAD_REQUEST)
        json(RestError(error))
    }

    private fun Context.handle(result: Result<*>, successStatus: HttpStatus = HttpStatus.OK) {
        if (result is Success) {
            status(successStatus)
            if (result.isNotEmpty())
                json(result.value as Any)
            return
        }

        val failure = result as Failure
        val httpStatus = when (failure.type) {
            Failure.Type.CUSTOM -> HttpStatus.BAD_REQUEST
            Failure.Type.NOT_FOUND -> HttpStatus.NOT_FOUND
            Failure.Type.ILLEGAL_STATE -> HttpStatus.CONFLICT
        }

        status(httpStatus)
        json(RestError(failure.message))
    }
}
