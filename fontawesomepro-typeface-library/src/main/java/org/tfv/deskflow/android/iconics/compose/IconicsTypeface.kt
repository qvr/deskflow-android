package org.tfv.deskflow.android.iconics.compose

import androidx.annotation.FontRes


/**
 * Created by mikepenz on 01.11.14.
 */
interface IconicsTypeface {

    @get:FontRes val fontRes: Int

    /** The Mapping Prefix to identify this font must have a length of 3 */
    val mappingPrefix: String

    val fontName: String

    val version: String

    val iconCount: Int

    val author: String

    val url: String

    val description: String

    val license: String

    val licenseUrl: String

    fun getIcon(key: String): IconicsIcon
}
