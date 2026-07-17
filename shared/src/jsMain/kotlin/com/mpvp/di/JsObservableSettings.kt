package com.mpvp.di

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SettingsListener
import com.russhwolf.settings.StorageSettings

class JsObservableSettings(private val delegate: Settings) : ObservableSettings {

    private val stringListeners = mutableMapOf<String, MutableSet<(String) -> Unit>>()
    private val intListeners = mutableMapOf<String, MutableSet<(Int) -> Unit>>()
    private val longListeners = mutableMapOf<String, MutableSet<(Long) -> Unit>>()
    private val floatListeners = mutableMapOf<String, MutableSet<(Float) -> Unit>>()
    private val doubleListeners = mutableMapOf<String, MutableSet<(Double) -> Unit>>()
    private val booleanListeners = mutableMapOf<String, MutableSet<(Boolean) -> Unit>>()

    override fun getString(key: String, defaultValue: String): String =
        delegate.getString(key, defaultValue)

    override fun getStringOrNull(key: String): String? =
        delegate.getStringOrNull(key)

    override fun putString(key: String, value: String) {
        delegate.putString(key, value)
        stringListeners[key]?.forEach { it(value) }
    }

    override fun getInt(key: String, defaultValue: Int): Int =
        delegate.getInt(key, defaultValue)

    override fun getIntOrNull(key: String): Int? =
        delegate.getIntOrNull(key)

    override fun putInt(key: String, value: Int) {
        delegate.putInt(key, value)
        intListeners[key]?.forEach { it(value) }
    }

    override fun getLong(key: String, defaultValue: Long): Long =
        delegate.getLong(key, defaultValue)

    override fun getLongOrNull(key: String): Long? =
        delegate.getLongOrNull(key)

    override fun putLong(key: String, value: Long) {
        delegate.putLong(key, value)
        longListeners[key]?.forEach { it(value) }
    }

    override fun getFloat(key: String, defaultValue: Float): Float =
        delegate.getFloat(key, defaultValue)

    override fun getFloatOrNull(key: String): Float? =
        delegate.getFloatOrNull(key)

    override fun putFloat(key: String, value: Float) {
        delegate.putFloat(key, value)
        floatListeners[key]?.forEach { it(value) }
    }

    override fun getDouble(key: String, defaultValue: Double): Double =
        delegate.getDouble(key, defaultValue)

    override fun getDoubleOrNull(key: String): Double? =
        delegate.getDoubleOrNull(key)

    override fun putDouble(key: String, value: Double) {
        delegate.putDouble(key, value)
        doubleListeners[key]?.forEach { it(value) }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        delegate.getBoolean(key, defaultValue)

    override fun getBooleanOrNull(key: String): Boolean? =
        delegate.getBooleanOrNull(key)

    override fun putBoolean(key: String, value: Boolean) {
        delegate.putBoolean(key, value)
        booleanListeners[key]?.forEach { it(value) }
    }

    override fun remove(key: String) {
        delegate.remove(key)
        stringListeners[key]?.forEach { it(getString(key, "")) }
        intListeners[key]?.forEach { it(getInt(key, 0)) }
        longListeners[key]?.forEach { it(getLong(key, 0)) }
        floatListeners[key]?.forEach { it(getFloat(key, 0f)) }
        doubleListeners[key]?.forEach { it(getDouble(key, 0.0)) }
        booleanListeners[key]?.forEach { it(getBoolean(key, false)) }
    }

    override fun hasKey(key: String): Boolean =
        delegate.hasKey(key)

    override val keys: Set<String>
        get() = delegate.keys

    override val size: Int
        get() = keys.size

    override fun clear() {
        val keysList = keys.toList()
        delegate.clear()
        keysList.forEach { key ->
            stringListeners[key]?.forEach { it(getString(key, "")) }
            intListeners[key]?.forEach { it(getInt(key, 0)) }
            longListeners[key]?.forEach { it(getLong(key, 0)) }
            floatListeners[key]?.forEach { it(getFloat(key, 0f)) }
            doubleListeners[key]?.forEach { it(getDouble(key, 0.0)) }
            booleanListeners[key]?.forEach { it(getBoolean(key, false)) }
        }
    }

    override fun addStringListener(key: String, defaultValue: String, callback: (String) -> Unit): SettingsListener {
        stringListeners.getOrPut(key) { mutableSetOf() }.add(callback)
        callback(getString(key, defaultValue))
        return object : SettingsListener {
            override fun deactivate() {
                stringListeners[key]?.remove(callback)
            }
        }
    }

    override fun addIntListener(key: String, defaultValue: Int, callback: (Int) -> Unit): SettingsListener {
        intListeners.getOrPut(key) { mutableSetOf() }.add(callback)
        callback(getInt(key, defaultValue))
        return object : SettingsListener {
            override fun deactivate() {
                intListeners[key]?.remove(callback)
            }
        }
    }

    override fun addLongListener(key: String, defaultValue: Long, callback: (Long) -> Unit): SettingsListener {
        longListeners.getOrPut(key) { mutableSetOf() }.add(callback)
        callback(getLong(key, defaultValue))
        return object : SettingsListener {
            override fun deactivate() {
                longListeners[key]?.remove(callback)
            }
        }
    }

    override fun addFloatListener(key: String, defaultValue: Float, callback: (Float) -> Unit): SettingsListener {
        floatListeners.getOrPut(key) { mutableSetOf() }.add(callback)
        callback(getFloat(key, defaultValue))
        return object : SettingsListener {
            override fun deactivate() {
                floatListeners[key]?.remove(callback)
            }
        }
    }

    override fun addDoubleListener(key: String, defaultValue: Double, callback: (Double) -> Unit): SettingsListener {
        doubleListeners.getOrPut(key) { mutableSetOf() }.add(callback)
        callback(getDouble(key, defaultValue))
        return object : SettingsListener {
            override fun deactivate() {
                doubleListeners[key]?.remove(callback)
            }
        }
    }

    override fun addBooleanListener(key: String, defaultValue: Boolean, callback: (Boolean) -> Unit): SettingsListener {
        booleanListeners.getOrPut(key) { mutableSetOf() }.add(callback)
        callback(getBoolean(key, defaultValue))
        return object : SettingsListener {
            override fun deactivate() {
                booleanListeners[key]?.remove(callback)
            }
        }
    }

    override fun addStringOrNullListener(key: String, callback: (String?) -> Unit): SettingsListener {
        stringListeners.getOrPut(key) { mutableSetOf() }.add { callback(it) }
        callback(getStringOrNull(key))
        return object : SettingsListener {
            override fun deactivate() {
                stringListeners[key]?.remove(callback)
            }
        }
    }

    override fun addIntOrNullListener(key: String, callback: (Int?) -> Unit): SettingsListener {
        intListeners.getOrPut(key) { mutableSetOf() }.add { callback(it) }
        callback(getIntOrNull(key))
        return object : SettingsListener {
            override fun deactivate() {
                intListeners[key]?.remove(callback)
            }
        }
    }

    override fun addLongOrNullListener(key: String, callback: (Long?) -> Unit): SettingsListener {
        longListeners.getOrPut(key) { mutableSetOf() }.add { callback(it) }
        callback(getLongOrNull(key))
        return object : SettingsListener {
            override fun deactivate() {
                longListeners[key]?.remove(callback)
            }
        }
    }

    override fun addFloatOrNullListener(key: String, callback: (Float?) -> Unit): SettingsListener {
        floatListeners.getOrPut(key) { mutableSetOf() }.add { callback(it) }
        callback(getFloatOrNull(key))
        return object : SettingsListener {
            override fun deactivate() {
                floatListeners[key]?.remove(callback)
            }
        }
    }

    override fun addDoubleOrNullListener(key: String, callback: (Double?) -> Unit): SettingsListener {
        doubleListeners.getOrPut(key) { mutableSetOf() }.add { callback(it) }
        callback(getDoubleOrNull(key))
        return object : SettingsListener {
            override fun deactivate() {
                doubleListeners[key]?.remove(callback)
            }
        }
    }

    override fun addBooleanOrNullListener(key: String, callback: (Boolean?) -> Unit): SettingsListener {
        booleanListeners.getOrPut(key) { mutableSetOf() }.add { callback(it) }
        callback(getBooleanOrNull(key))
        return object : SettingsListener {
            override fun deactivate() {
                booleanListeners[key]?.remove(callback)
            }
        }
    }
}
