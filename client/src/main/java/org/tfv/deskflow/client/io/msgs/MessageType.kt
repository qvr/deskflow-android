/*
 * MIT License
 *
 * Copyright (c) 2025 Jonathan Glanz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
