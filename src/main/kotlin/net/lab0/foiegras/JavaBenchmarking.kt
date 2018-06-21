package net.lab0.foiegras

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Path

class JavaBenchmarking
{
  companion object
  {
    fun generateJavaClass(
        packageName: String,
        className: String,
        fieldsCount: Int = 0
    ): JavaFile
    {
      val typeSpec = TypeSpec.classBuilder(
          ClassName.get(
              packageName,
              className
          )
      )

      (0 until fieldsCount).forEach {
        typeSpec.addField(
            FieldSpec.builder(
                TypeName.BYTE, "field$it"
            ).build()
        )
      }

      return JavaFile.builder(
          packageName,
          typeSpec.build()
      ).build()
    }


    fun generateAndCompileJava(
        outputFolder: Path,
        packageName: String,
        fieldsCount: Int
    )
    {
      val className = "Test"
      val sourceCode = generateJavaClass(
          packageName,
          className,
          fieldsCount = fieldsCount
      )
      sourceCode.writeTo(outputFolder)

      val paths = packageName.split(".") + "$className.java"
      val outputFile = paths.fold(outputFolder) { path, s -> path.resolve(s) }

      compileJava(outputFile)
    }

    @Throws(CompilationFailed::class)
    fun compileJava(file: Path)
    {
      val javac = Runtime.getRuntime().exec(
          arrayOf("javac", file.toAbsolutePath().toFile().toString())
      )

      val stderr = BufferedReader(InputStreamReader(javac.errorStream))

      javac.waitFor()

      val error = javac.exitValue() != 0
      if (error)
      {
        val message = """
          |Compilation failed for file $file
          |${stderr.readLines().joinToString("\n")}
        """.trimMargin()

        throw CompilationFailed(message)
      }
    }

  }
}