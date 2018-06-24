package net.lab0.foiegras

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger

class JavaBenchmarking {
  companion object {

    private val counter = AtomicInteger(1)

    @Throws(CompilationFailed::class)
    fun compileJava(files: List<Path>) {
      val argsFile = generateCompilationFile(files)
      val (errorOutput, error) = executeAndWaitForResult(argsFile)
      reportStatus(errorOutput, error, argsFile)
    }

    private fun generateCompilationFile(files: List<Path>): Path {
      val absoluteFiles = files.map {
        it.toAbsolutePath().toFile().toString()
      }

      val argsFile = Paths.get("/tmp/compilefile_" + counter.getAndIncrement())
      PrintWriter(argsFile.toFile()).use { pw ->
        absoluteFiles.forEach {
          pw.println(it)
        }
      }

      log.finer(
          "Compiling ${files.size} java files:\n" +
              absoluteFiles.take(5).joinToString("\n")
      )
      return argsFile
    }

    private fun executeAndWaitForResult(argsFile: Path): Pair<MutableList<String>, Boolean> {
      val command = arrayOf("javac", "@${argsFile.toFile().absolutePath}")
      log.info("Executing " + command.joinToString(" "))
      val javac = Runtime.getRuntime().exec(command)

      val stderr = BufferedReader(InputStreamReader(javac.errorStream))
      val errorOutput = mutableListOf<String>()
      stderr.useLines {
        errorOutput.addAll(it)
      }

      javac.waitFor()
      val error = javac.exitValue() != 0
      return Pair(errorOutput, error)
    }

    private fun reportStatus(
        errorOutput: MutableList<String>,
        error: Boolean,
        argsFile: Path
    ) {
      val expected = errorOutput.filter {
        it.contains("error: code too large") ||
            it.contains("error: too many constants")
      }.count() > 0

      if (error) {
        val message = """
              |Compilation failed for @${argsFile.toFile().absolutePath}
              |${errorOutput.joinToString("\n")}
              |
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
