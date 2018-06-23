package net.lab0.foiegras

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Path

class JavaBenchmarking {
  companion object {

    @Throws(CompilationFailed::class)
    fun compileJava(files: List<Path>) {

      val absoluteFiles = files.map {
        it.toAbsolutePath().toFile().toString()
      }

      log.finer("Compiling java files:\n ${absoluteFiles.joinToString("\n")}")

      val javac = Runtime.getRuntime().exec(
          arrayOf("javac", *absoluteFiles.toTypedArray())
      )

      val stderr = BufferedReader(InputStreamReader(javac.errorStream))

      javac.waitFor()

      val error = javac.exitValue() != 0
      if (error) {
        val message = """
          |Compilation failed for files $absoluteFiles
          |${stderr.readLines().joinToString("\n")}
        """.trimMargin()

        throw CompilationFailed(message)
      }
    }
  }
}
