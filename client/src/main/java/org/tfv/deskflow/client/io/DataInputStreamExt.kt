/*
 * deskflow -- mouse and keyboard sharing utility
 * Copyright (C) 2010 Shaun Patterson
 * Copyright (C) 2010 The Deskflow Project
 * Copyright (C) 2009 The Deskflow+ Project
 * Copyright (C) 2002 Chris Schoeneman
 * 
 * This package is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * found in the file COPYING that should have accompanied this file.
 * 
 * This package is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.tfv.deskflow.client.io

import java.io.DataInputStream
import java.io.IOException

@Throws(IOException::class)
fun DataInputStream.readString(length: Int = 4): String {

    // Read in the bytes and convert to a string
    val stringBytes = ByteArray(length)
    read(stringBytes, 0, stringBytes.size)
    return String(stringBytes)
}

@Throws(IOException::class)
fun DataInputStream.readExpectedString(expectedString: String): String {
    val str = readString(expectedString.length)

    require(str == expectedString) {
        "Expected string $expectedString not found.  Found: $str"
    }

    return str
}


