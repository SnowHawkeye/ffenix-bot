package logging

import com.diogonunes.jcolor.Ansi.colorize
import com.diogonunes.jcolor.Attribute
import java.text.SimpleDateFormat
import java.util.Date

object Log {

    private var DEBUG_ENABLED = false

    private enum class Tag {
        ERROR,
        SUCCESS,
        INFO,
        WARNING,
        DEBUG
    }

    private fun log(
        message: Any?,
        emitter: Any?,
        tag: Tag,
        vararg attributes: Attribute,
    ) {
        val timeStamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(Date())
        val fullMessage = "$timeStamp $tag $emitter : $message"
        val formattedMessage = colorize(fullMessage, *attributes)
        println(formattedMessage)
    }

    fun error(message: Any?, emitter: Any? = this) =
        log(message = message, emitter = emitter, tag = Tag.ERROR, attributes = arrayOf(Attribute.RED_TEXT()))

    fun success(message: Any?, emitter: Any? = this) =
        log(message = message, emitter = emitter, tag = Tag.SUCCESS, attributes = arrayOf(Attribute.GREEN_TEXT()))

    fun info(message: Any?, emitter: Any? = this) =
        log(message = message, emitter = emitter, tag = Tag.INFO, attributes = arrayOf(Attribute.NONE()))

    fun warning(message: Any?, emitter: Any? = this) =
        log(message = message, emitter = emitter, tag = Tag.WARNING, attributes = arrayOf(Attribute.YELLOW_TEXT()))

    fun debug(message: Any?, emitter: Any? = this) {
        if (DEBUG_ENABLED) {
            log(message = message, emitter = emitter, tag = Tag.DEBUG, attributes = arrayOf(Attribute.BLUE_TEXT()))
        }
    }

    fun <T> runWithDebugLogs(block: () -> T): T {
        DEBUG_ENABLED = true
        val result = block.invoke()
        DEBUG_ENABLED = false
        return result
    }
}
