package com.gmail.blueboxware.libgdxplugin.filetypes.skin.formatter

import com.gmail.blueboxware.libgdxplugin.filetypes.skin.LibGDXSkinLanguage
import com.gmail.blueboxware.libgdxplugin.filetypes.skin.SkinElementTypes
import com.gmail.blueboxware.libgdxplugin.filetypes.skin.SkinElementTypes.*
import com.gmail.blueboxware.libgdxplugin.filetypes.skin.SkinParserDefinition
import com.gmail.blueboxware.libgdxplugin.filetypes.skin.formatter.SkinCodeStyleSettings.Companion.ALIGN_PROPERTY_ON_COLON
import com.gmail.blueboxware.libgdxplugin.filetypes.skin.formatter.SkinCodeStyleSettings.Companion.ALIGN_PROPERTY_ON_VALUE
import com.gmail.blueboxware.libgdxplugin.filetypes.skin.psi.SkinArray
import com.gmail.blueboxware.libgdxplugin.filetypes.skin.psi.SkinObject
import com.gmail.blueboxware.libgdxplugin.filetypes.skin.psi.SkinProperty
import com.gmail.blueboxware.libgdxplugin.filetypes.skin.psi.SkinPropertyValue
import com.gmail.blueboxware.libgdxplugin.filetypes.skin.psi.SkinPsiUtil.hasElementType
import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.tree.FileElement
import com.intellij.psi.tree.TokenSet

/*
 *
 * Adapted from https://github.com/JetBrains/intellij-community/blob/171.2152/json/src/com/intellij/json/formatter/JsonBlock.java
 *
 */
class SkinBlock(
        val parent: SkinBlock?,
        val myNode: ASTNode,
        val settings: CodeStyleSettings,
        val myAlignment: Alignment?,
        val myIndent: Indent,
        val myWrap: Wrap?
) : ASTBlock {

  companion object {
    val SKIN_OPEN_BRACES = TokenSet.create(L_BRACKET, L_CURLY)
    val SKIN_CLOSE_BRACES = TokenSet.create(R_BRACKET, R_CURLY)
    val SKIN_ALL_BRACES = TokenSet.orSet(SKIN_OPEN_BRACES, SKIN_CLOSE_BRACES)
  }

  private val spacingBuilder: SpacingBuilder = SkinFormattingBuilderModel.createSpacingBuilder(settings)

  private val psiElement = node.psi

  private val childWrap: Wrap? = if (psiElement is SkinObject) {
    if (psiElement.asColor(false) != null && getCustomSettings().DO_NOT_WRAP_COLORS) {
      null
    } else {
      Wrap.createWrap(getCustomSettings().OBJECT_WRAPPING, true)
    }
  } else if (psiElement is SkinArray) {
    Wrap.createWrap(getCustomSettings().ARRAY_WRAPPING, true)
  } else {
    null
  }

  private val propertyValueAlignment = if (psiElement is SkinObject) {
    Alignment.createAlignment(true)
  } else {
    null
  }

  private var subBlocks: List<Block>? = null

  override fun isIncomplete() = when {
    hasElementType(myNode, OBJECT, RESOURCE, CLASS_SPECIFICATION) -> myNode.lastChildNode?.elementType != R_CURLY
    hasElementType(myNode, ARRAY)                                 -> myNode.lastChildNode?.elementType != R_BRACKET
    hasElementType(myNode, PROPERTY)                              -> (psiElement as? SkinProperty)?.value == null
    else                              -> false
  }

  override fun getSubBlocks(): List<Block> {
    if (subBlocks == null) {
      subBlocks = myNode.getChildren(null).mapNotNull { node ->
        if (isWhiteSpaceOrEmpty(node)) {
          null
        } else {
          makeSubBlock(node)
        }
      }
    }
    return subBlocks?.toMutableList() ?: listOf()
  }

  override fun getChildAttributes(newChildIndex: Int) = if (hasElementType(myNode, SkinParserDefinition.SKIN_CONTAINERS)) {
    ChildAttributes(Indent.getNormalIndent(), null)
  } else if (myNode.psi is PsiFile) {
    ChildAttributes(Indent.getNoneIndent(), null)
  } else {
    ChildAttributes(null, null)
  }

  override fun isLeaf() = myNode.firstChildNode == null

  override fun getTextRange(): TextRange = myNode.textRange

  override fun getNode() = myNode

  override fun getWrap() = myWrap

  override fun getIndent() = myIndent

  override fun getAlignment() = myAlignment

  private fun getCustomSettings() = settings.getCustomSettings(SkinCodeStyleSettings::class.java)

  private fun getCommonSettings() = settings.getCommonSettings(LibGDXSkinLanguage.INSTANCE)

  override fun getSpacing(child1: Block?, child2: Block) = spacingBuilder.getSpacing(this, child1, child2)

  private fun isWhiteSpaceOrEmpty(node: ASTNode) = node.elementType == TokenType.WHITE_SPACE || node.textLength == 0

  private fun makeSubBlock(childNode: ASTNode): Block {
    var indent = Indent.getNoneIndent()
    val customSettings = getCustomSettings()
    var wrap: Wrap? = null
    var alignment: Alignment? = null

    if (hasElementType(myNode, SkinParserDefinition.SKIN_CONTAINERS)
            || (myNode is FileElement && childNode !is PsiComment)
            || (myNode.elementType == SkinElementTypes.CLASS_SPECIFICATION && childNode is PsiComment)
    ) {
      if (hasElementType(childNode, COMMA)) {
        wrap = Wrap.createWrap(WrapType.NONE, true)
      } else if (!hasElementType(childNode, SKIN_ALL_BRACES)) {
        wrap = childWrap ?: Wrap.createWrap(WrapType.NONE, true)
        indent = Indent.getNormalIndent()
      } else if (hasElementType(childNode, SKIN_OPEN_BRACES)) {
        if (psiElement is SkinPropertyValue && customSettings.PROPERTY_ALIGNMENT == ALIGN_PROPERTY_ON_VALUE) {
          alignment = parent?.parent?.propertyValueAlignment
        }
      }
    } else if (hasElementType(myNode, PROPERTY)) {
      if (hasElementType(childNode, COLON) && customSettings.PROPERTY_ALIGNMENT == ALIGN_PROPERTY_ON_COLON) {
        alignment = parent?.propertyValueAlignment
      } else if (childNode.psi is SkinPropertyValue && customSettings.PROPERTY_ALIGNMENT == ALIGN_PROPERTY_ON_VALUE) {
        if (!hasElementType(childNode, SkinParserDefinition.SKIN_CONTAINERS)) {
          alignment = parent?.propertyValueAlignment
        }
      }
    }

    return SkinBlock(this, childNode, settings, alignment, indent, wrap)
  }
}