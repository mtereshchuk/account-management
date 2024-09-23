package com.mtereshchuk.account.util

import java.util.concurrent.ConcurrentHashMap

/**
 * ConcurrentHashMap use containsValue for contains by default
 *
 * @author mtereshchuk
 */
infix fun <K, V> K.notIn(map: ConcurrentHashMap<K, V>) = !map.containsKey(this)
