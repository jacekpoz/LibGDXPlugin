package com.gmail.blueboxware.libgdxplugin.ui

import com.gmail.blueboxware.libgdxplugin.filetypes.atlas.AtlasFile
import com.gmail.blueboxware.libgdxplugin.filetypes.atlas.psi.AtlasRegion
import com.intellij.codeInsight.preview.PreviewHintProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ui.ImageUtil
import javax.swing.JComponent

/*
 * Copyright 2017 Blue Box Ware
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
class TextureRegionPreviewHintProvider: PreviewHintProvider {

  override fun isSupportedFile(file: PsiFile?): Boolean  = file is AtlasFile

  override fun getPreviewComponent(element: PsiElement): JComponent? {

    PsiTreeUtil.getParentOfType(element, AtlasRegion::class.java)?.let { atlasRegion ->
      return createPreviewComponent(atlasRegion)
    }

    return null

  }

  private fun createPreviewComponent(atlasRegion: AtlasRegion): JComponent? {
    atlasRegion.image?.let { image ->
      var previewImage = image
      var scale = 1
      if ((image.width < 20 || image.height < 20) && image.width < 100 && image.height < 100) {
        previewImage = ImageUtil.toBufferedImage(ImageUtil.scaleImage(image, 4.0f))
        scale = 4
      } else if ((image.width < 50 || image.height < 50) && image.width < 200 && image.height < 200) {
        previewImage = ImageUtil.toBufferedImage(ImageUtil.scaleImage(image, 2.0f))
        scale = 2
      }

      val txt = atlasRegion.name + " (" + atlasRegion.originalWidth + " x " + atlasRegion.originalHeight +
              (if (scale != 1) ", shown at scale ${scale}x" else "" ) + ")"
      val component = ImagePreviewComponent(previewImage, txt)

      return component
    }

    return null
  }

}