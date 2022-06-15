/*
 * IdeaVim - Vim emulator for IDEs based on the IntelliJ platform
 * Copyright (C) 2003-2022 The IdeaVim authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.maddyhome.idea.vim.helper

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.components.Service
import com.maddyhome.idea.vim.api.ExecutionContext
import com.maddyhome.idea.vim.api.injector
import com.maddyhome.idea.vim.listener.SelectionVimListenerSuppressor
import com.maddyhome.idea.vim.newapi.ij
import com.maddyhome.idea.vim.newapi.vim
import com.maddyhome.idea.vim.options.OptionScope
import com.maddyhome.idea.vim.undo.UndoRedoBase
import com.maddyhome.idea.vim.vimscript.services.IjVimOptionService

/**
 * @author oleg
 */
@Service
class UndoRedoHelper : UndoRedoBase() {
  override fun undo(context: ExecutionContext): Boolean {
    val ijContext = context.context as DataContext
    val project = PlatformDataKeys.PROJECT.getData(ijContext) ?: return false
    val fileEditor = PlatformDataKeys.FILE_EDITOR.getData(ijContext)
    val undoManager = UndoManager.getInstance(project)
    if (fileEditor != null && undoManager.isUndoAvailable(fileEditor)) {
      if (injector.optionService.isSet(OptionScope.GLOBAL, IjVimOptionService.oldUndo)) {
        SelectionVimListenerSuppressor.lock().use { undoManager.undo(fileEditor) }
      } else {
        val editor = CommonDataKeys.EDITOR.getData(context.ij)?.vim
        undoManager.undo(fileEditor)
        editor?.carets()?.forEach {
          val ijCaret = it.ij
          val hasSelection = ijCaret.hasSelection()
          if (hasSelection) {
            val selectionStart = ijCaret.selectionStart
            it.ij.removeSelection()
            it.ij.moveToOffset(selectionStart)
          }
        }
      }
      return true
    }
    return false
  }

  override fun redo(context: ExecutionContext): Boolean {
    val ijContext = context.context as DataContext
    val project = PlatformDataKeys.PROJECT.getData(ijContext) ?: return false
    val fileEditor = PlatformDataKeys.FILE_EDITOR.getData(ijContext)
    val undoManager = UndoManager.getInstance(project)
    if (fileEditor != null && undoManager.isRedoAvailable(fileEditor)) {
      if (injector.optionService.isSet(OptionScope.GLOBAL, IjVimOptionService.oldUndo)) {
        SelectionVimListenerSuppressor.lock().use { undoManager.redo(fileEditor) }
      } else {
        val editor = CommonDataKeys.EDITOR.getData(context.ij)?.vim
        undoManager.redo(fileEditor)
        if (editor?.primaryCaret()?.ij?.hasSelection() == true) {
          undoManager.redo(fileEditor)
        }
        editor?.carets()?.forEach { it.ij.removeSelection() }
      }
      return true
    }
    return false
  }
}
