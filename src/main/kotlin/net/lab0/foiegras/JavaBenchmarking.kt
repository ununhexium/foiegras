package net.lab0.foiegras

import com.squareup.javapoet.*
import net.lab0.foiegras.caze.JavaBenchmarkCandidate
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Path

class JavaBenchmarking {
  companion object {
    fun generateJavaClass(candidate: JavaBenchmarkCandidate): JavaFile {
      val typeSpec = TypeSpec.classBuilder(
          ClassName.get(
              candidate.packageName,
              candidate.className
          )
      )

      (0 until candidate.fieldsCount).forEach {

        val fieldSpec = FieldSpec.builder(
            TypeName.BYTE, "field$it", *candidate.keywords.toTypedArray()
        )

        if (candidate.initialized) {
          fieldSpec.initializer("\$L", "$it % Byte.MAX_VALUE")
        }

        typeSpec.addField(fieldSpec.build())
      }

      return JavaFile.builder(
          candidate.packageName,
          typeSpec.build()
      ).build()
    }


    fun generateAndCompileJava(candidate: JavaBenchmarkCandidate) {
      val sourceCode = generateJavaClass(candidate)
      sourceCode.writeTo(candidate.case.outputFolder)

      val paths = candidate.packageName.split(".") + "${candidate.className}.java"
      val outputFile = paths.fold(candidate.outputFolder) { path, s ->
        path.resolve(s)
      }

      compileJava(outputFile)
    }


    @Throws(CompilationFailed::class)
    fun compileJava(file: Path) {
      val absoluteFile = file.toAbsolutePath().toFile().toString()
      log.finer("Compiling java $absoluteFile")

      val javac = Runtime.getRuntime().exec(
          arrayOf("javac", absoluteFile)
      )

      val stderr = BufferedReader(InputStreamReader(javac.errorStream))

      javac.waitFor()

      val error = javac.exitValue() != 0
      if (error) {
        val message = """
          |Compilation failed for file $absoluteFile
          |${stderr.readLines().joinToString("\n")}
        """.trimMargin()

        throw CompilationFailed(message)
      }
    }
  }
}
