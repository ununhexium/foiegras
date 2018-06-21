package net.lab0.foiegras

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Path

class KotlinBenchmarking
{
  companion object
  {
    fun generateAndCompileKotlin(
        outputFolder: Path,
        packageName: String,
        fieldsCount: Int
    )
    {
      val className = "Test"
      val sourceCode = generateKotlinClass(
          packageName,
          className,
          fieldsCount = fieldsCount
      )
      sourceCode.writeTo(outputFolder)

      val paths = packageName.split(".") + "$className.kt"
      val outputFile = paths.fold(outputFolder) { path, s -> path.resolve(s) }

      compileKotlin(outputFile)
    }

    fun generateKotlinClass(
        packageName: String,
        className: String,
        fieldsCount: Int
    ): FileSpec
    {
      val typeSpec = com.squareup.kotlinpoet.TypeSpec.classBuilder(
          com.squareup.kotlinpoet.ClassName(
              packageName,
              className
          )
      )

      (0 until fieldsCount).forEach {
        typeSpec.addProperty(
            PropertySpec.builder(
                "field$it",
                Byte::class
            ).build()
        )
      }

      return FileSpec
          .builder(packageName, className)
          .addType(typeSpec.build())
          .build()
    }



    @Throws(CompilationFailed::class)
    fun compileKotlin(file: Path)
    {
      val kotlinc = Runtime.getRuntime().exec(
          arrayOf("kotlinc", file.toAbsolutePath().toFile().toString())
      )

      val stderr = BufferedReader(InputStreamReader(kotlinc.errorStream))

      kotlinc.waitFor()

      val error = kotlinc.exitValue() != 0
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
