package org.stepic.droid.util

import android.database.Cursor


fun Cursor.getString(columnName: String): String? =
        this.getString(this.getColumnIndexOrThrow(columnName))

fun Cursor.getBoolean(columnName: String): Boolean =
        this.getInt(columnName) > 0

fun Cursor.getLong(columnName: String): Long =
        this.getLong(this.getColumnIndexOrThrow(columnName))

fun Cursor.getInt(columnName: String): Int =
        this.getInt(this.getColumnIndexOrThrow(columnName))