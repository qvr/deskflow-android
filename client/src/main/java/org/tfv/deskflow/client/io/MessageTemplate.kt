package org.tfv.deskflow.client.io

import org.tfv.deskflow.client.io.msgs.BadMessage
import org.tfv.deskflow.client.io.msgs.BusyMessage
import org.tfv.deskflow.client.io.msgs.ClipboardDataMessage
import org.tfv.deskflow.client.io.msgs.ClipboardMessage
import org.tfv.deskflow.client.io.msgs.CloseMessage
import org.tfv.deskflow.client.io.msgs.EnterMessage
import org.tfv.deskflow.client.io.msgs.HelloBackMessage
import org.tfv.deskflow.client.io.msgs.HelloMessage
import org.tfv.deskflow.client.io.msgs.IncompatibleMessage
import org.tfv.deskflow.client.io.msgs.InfoAckMessage
import org.tfv.deskflow.client.io.msgs.InfoMessage
import org.tfv.deskflow.client.io.msgs.KeepAliveMessage
import org.tfv.deskflow.client.io.msgs.KeyDownMessage
import org.tfv.deskflow.client.io.msgs.KeyRepeatMessage
import org.tfv.deskflow.client.io.msgs.KeyUpMessage
import org.tfv.deskflow.client.io.msgs.LeaveMessage
import org.tfv.deskflow.client.io.msgs.Message
import org.tfv.deskflow.client.io.msgs.MouseDownMessage
import org.tfv.deskflow.client.io.msgs.MouseMoveMessage
import org.tfv.deskflow.client.io.msgs.MouseRelMoveMessage
import org.tfv.deskflow.client.io.msgs.MouseUpMessage
import org.tfv.deskflow.client.io.msgs.MouseWheelMessage
import org.tfv.deskflow.client.io.msgs.NoOpMessage
import org.tfv.deskflow.client.io.msgs.QueryInfoMessage
import org.tfv.deskflow.client.io.msgs.ResetOptionsMessage
import org.tfv.deskflow.client.io.msgs.ScreenSaverMessage
import org.tfv.deskflow.client.io.msgs.SetOptionsMessage
import org.tfv.deskflow.client.io.msgs.UnknownMessage
import kotlin.reflect.KClass

enum class MessageTemplate(template: String, clazz: KClass<out Message>?) {
//    Hello("Synergy%2i%2i", HelloMessage::class),
    Hello("Barrier%2i%2i", HelloMessage::class),
    HelloBack("Synergy%2i%2i%s", HelloBackMessage::class),
    CNoop("CNOP", NoOpMessage::class),
    CClose("CBYE", CloseMessage::class),
    CEnter("CINN%2i%2i%4i%2i", EnterMessage::class),
    CLeave("COUT", LeaveMessage::class),
    CClipboard("CCLP%1i%4i", ClipboardMessage::class),
    CScreenSaver("CSEC%1i", ScreenSaverMessage::class),
    CResetOptions("CROP", ResetOptionsMessage::class),
    CInfoAck("CIAK", InfoAckMessage::class),
    CKeepAlive("CALV", KeepAliveMessage::class),
    DKeyDownLang("DKDL%2i%2i%2i%s", KeyDownMessage::class),
    DKeyDown("DKDN%2i%2i%2i", KeyDownMessage::class),
    DKeyDown1_0("DKDN%2i%2i", KeyDownMessage::class),
    DKeyRepeat("DKRP%2i%2i%2i%2i%s", KeyRepeatMessage::class),
    DKeyRepeat1_0("DKRP%2i%2i%2i", KeyRepeatMessage::class),
    DKeyUp("DKUP%2i%2i%2i", KeyUpMessage::class),
    DKeyUp1_0("DKUP%2i%2i", KeyUpMessage::class),
    DMouseDown("DMDN%1i", MouseDownMessage::class),
    DMouseUp("DMUP%1i", MouseUpMessage::class),
    DMouseMove("DMMV%2i%2i", MouseMoveMessage::class),
    DMouseRelMove("DMRM%2i%2i", MouseRelMoveMessage::class),
    DMouseWheel("DMWM%2i%2i", MouseWheelMessage::class),
    DMouseWheel1_0("DMWM%2i", MouseWheelMessage::class),
    DClipboard("DCLP%1i%4i%1i%s", ClipboardDataMessage::class),
    DInfo("DINF%2i%2i%2i%2i%2i%2i%2i", InfoMessage::class),
    DSetOptions("DSOP%4i", SetOptionsMessage::class),
    DFileTransfer("DFTR%1i%s", null),
    DDragInfo("DDRG%2i%s", null),
    DSecureInputNotification("SECN%s", null),
    DLanguageSynchronisation("LSYN%s", null),
    QInfo("QINF", QueryInfoMessage::class),
    EIncompatible("EICV%2i%2i", IncompatibleMessage::class),
    EBusy("EBSY", BusyMessage::class),
    EUnknown("EUNK", UnknownMessage::class),
    EBad("EBAD", BadMessage::class);

    val prefix: String
    val code: String
    val template: String
    val specifiers: List<Specifier>
    val clazz: KClass<out Message>? = clazz

    init {
        require(template.length >= 4) { "Message template must be at least 4 characters long." }
        val parts = template.split("%")
        this.code = parts.first()
        this.prefix = code.substring(0, 4)

        this.template = template
        this.specifiers = parts.drop(1).map {
            if (it[0].isDigit() && it.length == 2) Specifier(
                SpecifierType.fromSpec(it[1].toString()),
                it[0].digitToInt()
            ) else Specifier(SpecifierType.fromSpec(it[0].toString()))

        }
    }



    enum class SpecifierType {
            INT("i"),
            STRING("s");

            val spec: String
            constructor(spec: String) {
                this.spec = spec
            }

            companion object {
                fun fromSpec(spec: String):SpecifierType =
                        entries.first { it.spec == spec }
            }
        }
    data class Specifier(val type: SpecifierType, val size: Int = 0) {

    }

    companion object {
        fun templateFromCode(code: String) =
                entries.firstOrNull { it.code == code }

        fun templateFromPrefix(prefix: String) =
            entries.firstOrNull { it.prefix == prefix }

    }

    override fun toString(): String {
        return "MessageTemplate(template='$template', code='$code', prefix='$prefix', clazz=$clazz)"
    }
}