package com.mtereshchuk.account.service

import com.mtereshchuk.account.model.*
import com.mtereshchuk.account.util.Result

/**
 * @author mtereshchuk
 */
interface AccountService {
    fun getAccounts(): List<Account>
    fun createAccount(newAccount: NewAccount): Result<Account>
    fun getAccount(id: AccountId): Result<Account>
    fun updateAccount(id: AccountId, updAccount: Account): Result<Account>
    fun deleteAccount(id: AccountId): Result<Unit>

    fun doDeposit(deposit: Deposit): Result<Unit>
    fun doWithdraw(withdraw: Withdraw): Result<Unit>
    fun doTransfer(transfer: Transfer): Result<Unit>
}
