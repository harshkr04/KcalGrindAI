package com.kcalgrindai.app.core.util

interface WidgetUpdater {
    /**
     * Triggers an asynchronous update of all active Kcal Grind AI widgets.
     */
    fun requestUpdate()

    /**
     * Clears or masks sensitive data immediately upon sign-out.
     */
    fun clearWidgetDataOnSignOut()
}
