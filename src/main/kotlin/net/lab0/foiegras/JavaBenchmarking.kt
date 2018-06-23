package net.lab0.foiegras

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger

class JavaBenchmarking {
  companion object {

    val counter = AtomicInteger(1)

    @Throws(CompilationFailed::class)
    fun compileJava(files: List<Path>) {

      val absoluteFiles = files.map {
        it.toAbsolutePath().toFile().toString()
      }

      val argsFile = Paths.get("/tmp/compilefile_" + counter.getAndIncrement())
      val pw = PrintWriter(argsFile.toFile())
      absoluteFiles.forEach {
        pw.println(it)
      }
      pw.close()

      log.finer(
          "Compiling ${files.size} java files:\n" +
              absoluteFiles.take(5).joinToString("\n")
      )

      val command = arrayOf("javac", "@${argsFile.toFile().absolutePath}")
      log.info("Executing " + command.joinToString(" "))
      val javac = Runtime.getRuntime().exec(
          command
      )

      val stderr = BufferedReader(InputStreamReader(javac.errorStream))
      val expected = stderr.useLines {
        it.filter {
          it.contains("error: code too large")
        }.count() > 0
      }

      javac.waitFor()

      val error = javac.exitValue() != 0
      if (error) {
        val message = """
          |Compilation failed for @${argsFile.toFile().absolutePath}
        """.trimMargin()

        val compilationFailed = CompilationFailed(message)

        if (expected) {
          throw compilationFailed
        }
        else {
          throw RuntimeException("Unexpected exception ", compilationFailed)
        }
      }
    }
  }
}
