import java.io.File
import java.nio.file.Path

fun which(executableName: String): Path {
  val pathEnv = System.getenv("PATH") ?: throw Error("PATH is not defined")  // 1. Access PATH
  val pathSeparator = if (System.getProperty("os.name").startsWith("Windows")) ";" else ":" // Determine the separator
  val pathDirs = pathEnv.split(pathSeparator) // 2. Split PATH

  for (dir in pathDirs) { // 3. Iterate and Check
    val executableFile = File(dir, executableName)
    if (executableFile.exists() && executableFile.canExecute()) {
      return executableFile.toPath()  // Found, return the file
    }
    val executableFileWithExtension = File(dir, "$executableName.exe") // check windows extension
    if (executableFileWithExtension.exists() && executableFileWithExtension.canExecute()) {
      return executableFileWithExtension.toPath()
    }
  }
  throw Error("Executable $executableName not found in $pathDirs")
  //return null // Not found in any PATH directory
}