package com.mtereshchuk.account.util

import com.mtereshchuk.account.model.AccountId

/**
 * @author mtereshchuk
 */
sealed interface Result<T>

data class Success<T>(val value: T) : Result<T> {
    fun isNotEmpty() = this != EMPTY

    companion object {
        val EMPTY = Success(Unit)
        fun empty() = EMPTY
        fun <T> of(value: T) = Success(value)
    }
}

data class Failure<T>(val message: String, val type: Type = Type.CUSTOM) : Result<T> {
    enum class Type {
        CUSTOM, NOT_FOUND, ILLEGAL_STATE
    }

    companion object {
        fun <T> notFound(id: AccountId) =
            Failure<T>("Account id=$id not found", Type.NOT_FOUND)

        fun <T> illegalState(message: String) =
            Failure<T>(message, Type.ILLEGAL_STATE)
    }
}
