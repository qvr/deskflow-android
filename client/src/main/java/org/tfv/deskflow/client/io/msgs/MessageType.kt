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
@file:Suppress("SpellCheckingInspection")

package org.tfv.deskflow.client.io.msgs

import org.tfv.deskflow.client.io.MessageTemplate

/**
 * For more information on the Deskflow message format, see
 * http://deskflow-foss.org/code/filedetails.php?repname=deskflow&path=%2Ftrunk%2Fsrc%2Flib%2Fdeskflow%2FProtocolTypes.cpp
 */
enum class MessageType(val value: String, val commonName: String, template: MessageTemplate? = null) {
    HELLO("Synergy", "[Init] Hello"),  // Not a standard message
    HELLOBACK("Synergy", "[Init] Hello Back"),  // Not a standard message
    CNOOP("CNOP", "[Command] NoOp"),
    CCLOSE("CBYE", "[Command] Close"),
    CENTER("CINN", "[Command] Enter"),
    CLEAVE("COUT", "[Command] Leave"),
    CCLIPBOARD("CCLP", "[Command] Clipboard"),
    CSCREENSAVER("CSEC", "[Command] Screensaver"),
    CRESETOPTIONS("CROP", "[Command] Reset Options"),
    CINFOACK("CIAK", "[Command] Info Ack"),
    CKEEPALIVE("CALV", "[Command] Keep Alive"),
    DKEYDOWN("DKDN", "[Data] Key Down"),
    DKEYREPEAT("DKRP", "[Data] Key Repeat"),
    DKEYUP("DKUP", "[Data] Key Up"),
    DMOUSEDOWN("DMDN", "[Data] Mouse Down"),
    DMOUSEUP("DMUP", "[Data] Mouse Up"),
    DMOUSEMOVE("DMMV", "[Data] Mouse Move"),
    DMOUSERELMOVE("DMRM", "[Data] Mouse Relative Move"),
    DMOUSEWHEEL("DMWM", "[Data] Mouse Wheel"),
    DCLIPBOARD("DCLP", "[Data] Clipboard"),
    DINFO("DINF", "[Data] Info"),
    DSETOPTIONS("DSOP", "[Data] Set Options"),
    QINFO("QINF", "[Query] Info"),
    EINCOMPATIBLE("EICV", "[Error] Incompatible"),
    EBUSY("EBSY", "[Error] Busy"),
    EUNKNOWN("EUNK", "[Error] Unknown"),
    EBAD("EBAD", "[Error] Bad");

    val template: MessageTemplate = template ?: MessageTemplate.templateFromCode(value)!!

    companion object {
        fun fromString(messageValue: String): MessageType {
            for (t in entries) {
                if (messageValue.equals(t.value, ignoreCase = true)) {
                    return t
                }
            }
            throw IllegalArgumentException("No MessageType with value $messageValue")
        }
    }

    override fun toString(): String {
        return "MessageType(value='$value', commonName='$commonName', template=$template)"
    }
}
