package com.jasypt.helper.plugin

import java.util.*

object I18n {

    private val bundle by lazy {
        try {
            ResourceBundle.getBundle("messages.JasyptBundle")
        } catch (e: Exception) {
            object : ResourceBundle() {
                override fun handleGetObject(key: String): Any? = null
                override fun getKeys(): Enumeration<String> = Collections.emptyEnumeration()
            }
        }
    }

    fun getMessage(key: String, vararg args: Any): String {
        return try {
            val message = bundle.getString(key)
            if (args.isNotEmpty()) {
                java.text.MessageFormat.format(message, *args)
            } else {
                message
            }
        } catch (e: Exception) {
            key
        }
    }

    fun getMessage(key: String): String {
        return getMessage(key, arrayOf<Any>())
    }
}
