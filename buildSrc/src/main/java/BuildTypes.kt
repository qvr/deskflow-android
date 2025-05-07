import javax.inject.Inject
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import com.android.ddmlib.IShellOutputReceiver

interface InjectedOps {
  @get:Inject
  val execOps: ExecOperations
}

val adbOutputReceiver = object : IShellOutputReceiver {
  val outputData = ByteArrayOutputStream()
  override fun addOutput(data: ByteArray?, offset: Int, length: Int) {
    outputData.write(data!!, offset, length)
  }

  override fun flush() {
    if (outputData.size() > 0) {
      println(String(outputData.toByteArray()))
    }
  }

  override fun isCancelled(): Boolean {
    return false
  }
}
