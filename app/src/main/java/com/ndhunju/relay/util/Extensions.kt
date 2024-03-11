package com.ndhunju.relay.util

import android.database.Cursor
import kotlinx.coroutines.flow.Flow

/**
 * Returns String value for passed [column] if it exists.
 * Otherwise, empty string is returned
 */
fun Cursor.getStringForColumn(column: String): String {
    val columnIndex = getColumnIndex(column)
    if (columnIndex >= 0) {
        return getString(columnIndex) ?: ""
    }

    return ""
}

/**
 * Returns String value for passed [column] if it exists.
 * Otherwise, empty string is returned
 */
fun Cursor.getLongForColumn(column: String): Long {
    val columnIndex = getColumnIndex(column)
    if (columnIndex >= 0) {
        return getLong(columnIndex)
    }

    return 0
}

/**
 * Returns a [Flow] whose values are generated with [transform] function by combining
 * the most recently emitted values by each flow.
 */
inline fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7, reified T8, reified T9, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    flow8: Flow<T8>,
    flow9: Flow<T9>,
    crossinline transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R
): Flow<R> =
    kotlinx.coroutines.flow.combine(flow, flow2, flow3, flow4, flow5, flow6, flow7, flow8, flow9) { args: Array<*> ->
        transform(
            args[0] as T1,
            args[1] as T2,
            args[2] as T3,
            args[3] as T4,
            args[4] as T5,
            args[5] as T6,
            args[6] as T7,
            args[7] as T8,
            args[8] as T9,
        )
    }

fun <K,V> MutableMap<K,V>.getOrPut(key: K, default: V): V {
    if (this[key] == null) {
        this[key] = default
    }

    return  this[key] ?: default
}