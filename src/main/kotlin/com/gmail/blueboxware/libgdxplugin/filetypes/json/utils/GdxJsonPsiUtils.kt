package com.gmail.blueboxware.libgdxplugin.filetypes.json.utils

import com.gmail.blueboxware.libgdxplugin.filetypes.json.psi.GdxJsonElement
import com.gmail.blueboxware.libgdxplugin.filetypes.json.psi.GdxJsonJobject
import com.gmail.blueboxware.libgdxplugin.filetypes.json.psi.impl.GdxJsonFileImpl
import com.gmail.blueboxware.libgdxplugin.utils.findParentWhichIsChildOf
import com.intellij.psi.PsiComment
import com.intellij.psi.impl.source.tree.TreeUtil.skipWhitespaceAndComments
import com.intellij.psi.util.PsiTreeUtil


/*
 * Copyright 2019 Blue Box Ware
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
fun GdxJsonElement.factory() = (containingFile as? GdxJsonFileImpl)?.factory

fun GdxJsonJobject.addComment(comment: PsiComment) {

  if (firstChild?.text == "{") {

    PsiTreeUtil.nextLeaf(firstChild)?.node?.let { nextNode ->
      skipWhitespaceAndComments(nextNode, true)?.let { anchor ->
        factory()?.createNewline()?.let { newLine ->
          addAfter(newLine, addBefore(comment, anchor.psi.findParentWhichIsChildOf(this)))
        }
      }
    }

  }

}