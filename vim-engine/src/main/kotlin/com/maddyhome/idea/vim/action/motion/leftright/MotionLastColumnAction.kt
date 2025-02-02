/*
 * Copyright 2003-2022 The IdeaVim authors
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE.txt file or at
 * https://opensource.org/licenses/MIT.
 */

package com.maddyhome.idea.vim.action.motion.leftright

import com.maddyhome.idea.vim.api.ExecutionContext
import com.maddyhome.idea.vim.api.VimCaret
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.api.VimMotionGroupBase
import com.maddyhome.idea.vim.api.injector
import com.maddyhome.idea.vim.command.Argument
import com.maddyhome.idea.vim.command.Command
import com.maddyhome.idea.vim.command.CommandFlags
import com.maddyhome.idea.vim.command.MotionType
import com.maddyhome.idea.vim.command.OperatorArguments
import com.maddyhome.idea.vim.handler.Motion
import com.maddyhome.idea.vim.handler.MotionActionHandler
import com.maddyhome.idea.vim.handler.toMotion
import com.maddyhome.idea.vim.helper.enumSetOf
import com.maddyhome.idea.vim.helper.inVisualMode
import com.maddyhome.idea.vim.helper.isEndAllowed
import com.maddyhome.idea.vim.options.OptionConstants
import com.maddyhome.idea.vim.options.OptionScope
import com.maddyhome.idea.vim.vimscript.model.datatypes.VimString
import java.util.*

class MotionLastColumnInsertAction : MotionLastColumnAction() {
  override val flags: EnumSet<CommandFlags> = enumSetOf(CommandFlags.FLAG_SAVE_STROKE)
}

open class MotionLastColumnAction : MotionActionHandler.ForEachCaret() {
  override val motionType: MotionType = MotionType.INCLUSIVE

  override fun getOffset(
    editor: VimEditor,
    caret: VimCaret,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Motion {
    val allow = if (editor.inVisualMode) {
      val opt = (
        injector.optionService.getOptionValue(
          OptionScope.LOCAL(editor),
          OptionConstants.selectionName
        ) as VimString
        ).value
      opt != "old"
    } else {
      if (operatorArguments.isOperatorPending) false else editor.isEndAllowed
    }

    return injector.motion.moveCaretToLineEndOffset(editor, caret, operatorArguments.count1 - 1, allow).toMotion()
  }

  override fun postMove(editor: VimEditor, caret: VimCaret, context: ExecutionContext, cmd: Command) {
    caret.vimLastColumn = VimMotionGroupBase.LAST_COLUMN
  }

  override fun preMove(editor: VimEditor, caret: VimCaret, context: ExecutionContext, cmd: Command) {
    caret.vimLastColumn = VimMotionGroupBase.LAST_COLUMN
  }
}
