package com.example.androiduni.select

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.selection.SelectionTracker
import com.example.androiduni.R

class ActionModeController(
    private val tracker: SelectionTracker<*>
) : ActionMode.Callback {

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.action_menu, menu)
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        tracker.clearSelection()
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = true

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_clear -> {
            mode.finish()
            true
        }
        else -> false
    }

}