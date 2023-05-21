package com.mikhael.kotlinkeyshelper.helper

import com.mikhael.kotlinkeyshelper.menu.RootMenu

class HelperEntrance(
    private val args: Array<String>
) {
    init {
        this.initialLoad()
    }

    private fun initialLoad() {
        if (args.isNotEmpty()) {
            println("Currently this helper doesn't accept args")
            return
        }

        RootMenu().start()
    }
}