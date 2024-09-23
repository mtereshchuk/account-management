package com.mtereshchuk.account.service

import com.mtereshchuk.account.model.*
import com.mtereshchuk.account.util.Failure
import com.mtereshchuk.account.util.Result
import com.mtereshchuk.account.util.Success
import com.mtereshchuk.account.util.notIn
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * @author mtereshchuk
 */
data class LockAndAccount(
    val lock: Lock,
    val account: Account
)

class AccountServiceImpl : AccountService {
    private val idGenerator = AtomicLong(1)
    private val accounts = ConcurrentHashMap<AccountId, LockAndAccount>()

    override fun getAccounts(): List<Account> {
        val (locks, accounts) = accounts.values
            .sortedBy { it.account.id } // will make a copy of current state
            .map { it.lock to it.account }
            .unzip()

        for (lock in locks)
            lock.lock()

        try {
            return accounts.filter {
                this.accounts.containsKey(it.id)
            }
        } finally {
            for (lock in locks)
                lock.unlock()
        }
    }

    override fun createAccount(newAccount: NewAccount): Result<Account> {
        val id = idGenerator.getAndIncrement()
        val lock = ReentrantLock()
        val account = Account(
            id = id,
            client = newAccount.client,
            amount = newAccount.amount ?: BigDecimal.ZERO,
            additionalInfo = newAccount.additionalInfo
        )

        accounts[id] = LockAndAccount(lock, account)
        return Success.of(account)
    }

    override fun getAccount(id: AccountId): Result<Account> {
        val (lock, account) = accounts[id]
            ?: return Failure.notFound(id)

        lock.lock()
        try {
            if (id notIn accounts)
                return Failure.notFound(id)
            return Success.of(account)
        } finally {
            lock.unlock()
        }
    }

    override fun updateAccount(id: AccountId, updAccount: Account): Result<Account> {
        val (lock, account) = accounts[id]
            ?: return Failure.notFound(id)

        lock.lock()
        try {
            if (id notIn accounts)
                return Failure.notFound(id)

            if (account.client != updAccount.client)
                return Failure.illegalState(cannotBeUpdated("client", account.client, updAccount.client))
            if (account.amount.compareTo(updAccount.amount) != 0)
                return Failure.illegalState(cannotBeUpdated("amount", account.amount, updAccount.amount))

            account.additionalInfo = updAccount.additionalInfo
            return Success.of(account)
        } finally {
            lock.unlock()
        }
    }

    override fun deleteAccount(id: AccountId): Result<Unit> {
        val (lock, _) = accounts[id]
            ?: return Failure.notFound(id)

        lock.lock()
        try {
            if (id notIn accounts)
                return Failure.notFound(id)

            accounts.remove(id)
            return Success.empty()
        } finally {
            lock.unlock()
        }
    }

    override fun doDeposit(deposit: Deposit): Result<Unit> {
        val (id, amount, _) = deposit
        val (lock, account) = accounts[id]
            ?: return Failure.notFound(id)

        lock.lock()
        try {
            if (id notIn accounts)
                return Failure.notFound(id)

            val newAmount = account.amount + amount
            if (newAmount > Account.MAX_AMOUNT)
                return Failure.illegalState(exceedsMaximumValue(newAmount))

            account.amount = newAmount
            return Success.empty()
        } finally {
            lock.unlock()
        }
    }

    override fun doWithdraw(withdraw: Withdraw): Result<Unit> {
        val (id, amount, _) = withdraw
        val (lock, account) = accounts[id]
            ?: return Failure.notFound(id)

        lock.lock()
        try {
            if (id notIn accounts)
                return Failure.notFound(id)

            if (account.amount < amount)
                return Failure.illegalState(lessThanRequested(account.amount, amount))

            account.amount -= amount
            return Success.empty()
        } finally {
            lock.unlock()
        }
    }

    override fun doTransfer(transfer: Transfer): Result<Unit> {
        val (fromId, toId, amount, _) = transfer

        val (fromLock, fromAccount) = accounts[fromId]
            ?: return Failure.notFound(fromId)
        val (toLock, toAccount) = accounts[toId]
            ?: return Failure.notFound(toId)

        val (minLock, maxLock) = if (fromId < toId)
            fromLock to toLock
        else
            toLock to fromLock

        minLock.lock()
        maxLock.lock()

        try {
            if (fromId notIn accounts)
                return Failure.notFound(fromId)
            if (toId notIn accounts)
                return Failure.notFound(toId)

            val newFromAmount = fromAccount.amount - amount
            if (newFromAmount < BigDecimal.ZERO)
                return Failure.illegalState(lessThanRequested(fromAccount.amount, amount))

            val newToAmount = toAccount.amount + amount
            if (newToAmount > Account.MAX_AMOUNT)
                return Failure.illegalState(exceedsMaximumValue(newToAmount))

            fromAccount.amount = newFromAmount
            toAccount.amount = newToAmount

            return Success.empty()
        } finally {
            minLock.unlock() // here order doesn't matter
            maxLock.unlock()
        }
    }

    private fun cannotBeUpdated(field: String, expected: Any?, actual: Any?) =
        "'$field' cannot be updated: $expected != $actual"

    private fun lessThanRequested(actual: BigDecimal, requested: BigDecimal) =
        "Account 'amount' is less than requested: $actual < $requested"

    private fun exceedsMaximumValue(totalAmount: BigDecimal) =
        "Total amount exceeds the maximum value: $totalAmount > ${Account.MAX_AMOUNT}"
}
