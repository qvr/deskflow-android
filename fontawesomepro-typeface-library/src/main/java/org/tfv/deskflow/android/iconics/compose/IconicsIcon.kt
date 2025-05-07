package org.tfv.deskflow.android.iconics.compose

interface IconicsIcon {

    val name: String

    val formattedName:String
        get() = name

    val character: Char

    val typeface: IconicsTypeface
}
