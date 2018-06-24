package net.lab0.foiegras.caze.iface

import com.squareup.javapoet.JavaFile
import net.lab0.foiegras.CompilationFailed
import net.lab0.foiegras.JavaBenchmarking
import net.lab0.foiegras.log
import net.lab0.foiegras.resolveFrom
import java.nio.file.Path

abstract class AbstractBenchmarkCase(
    val outputFolder:Path
) : BenchmarkCase {
  lateinit var lastCompilationFailure: Throwable
  var fieldsCount: Int = -1

  override fun getLastFailureCause() = lastCompilationFailure

  fun tryToCompile(sources: List<JavaFile>, outputFolder: Path): Boolean {
    return try {
      val files = sources.map {
        it.writeTo(outputFolder)
        return@map it.resolveFrom(outputFolder)
      }
      JavaBenchmarking.compileJava(files)

      true
    }
    catch (e: CompilationFailed) {
      log.finer("Failed: ${e.message}")
      lastCompilationFailure = e
      false
    }
  }

  override fun evaluateAt(fieldsCount: Int): Boolean {
    this.fieldsCount = fieldsCount
    return generateAndCompile()
  }

  private fun generateAndCompile(): Boolean {
    val sources = generateJavaClasses()
    return tryToCompile(sources, outputFolder)
  }

  abstract fun generateJavaClasses(): List<JavaFile>

  /**
   * Extracts the meaningful part of the error message
   */
  /*
   * FINER: Failed: Compilation failed for @/tmp/compilefile_41
   * /home/ununhexium/dev/kotlin/foiegras/generated/base/j/javaflatcaseimpl/kPublicFinal/init/tbyte/f11264/C11264Fields.java:3: error: code too large
   *
   * Will become
   *
   * code too large
   */
  fun getCauseString() =
      lastCompilationFailure
          .message!!
          .split("\n")
          .take(2)
          .drop(1)
          .first()
          .substringAfter("error: ")


}
