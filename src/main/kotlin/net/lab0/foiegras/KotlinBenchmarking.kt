//package net.lab0.foiegras
//
//import com.squareup.kotlinpoet.ClassName
//import com.squareup.kotlinpoet.FileSpec
//import com.squareup.kotlinpoet.PropertySpec
//import com.squareup.kotlinpoet.TypeSpec
//import java.io.BufferedReader
//import java.io.InputStreamReader
//import java.nio.file.Path
//
//class KotlinBenchmarking
//{
//  companion object
//  {
//    fun generateAndCompileKotlin(case: JavaBenchmarkCaseImpl)
//    {
//      val sourceCode = generateKotlinClass(
//          case.packageName,
//          case.className,
//          case.fieldsCount
//      )
//      sourceCode.writeTo(case.outputFolder)
//
//      val paths = case.packageName.split(".") + "${case.className}.kt"
//      val outputFile = paths.fold(case.outputFolder) { path, s ->
//        path.resolve(s)
//      }
//
//      compileKotlin(outputFile)
//    }
//
//    fun generateKotlinClass(
//        packageName: String,
//        className: String,
//        fieldsCount: Int
//    ): FileSpec
//    {
//      val typeSpec = TypeSpec.classBuilder(
//          ClassName(
//              packageName,
//              className
//          )
//      )
//
//      (0 until fieldsCount).forEach {
//        typeSpec.addProperty(
//            PropertySpec.builder(
//                "field$it",
//                Byte::class
//            ).build()
//        )
//      }
//
//      return FileSpec
//          .builder(packageName, className)
//          .addType(typeSpec.build())
//          .build()
//    }
//
//
//    @Throws(CompilationFailed::class)
//    fun compileKotlin(file: Path)
//    {
//      println("Compiling kotlin")
//      val kotlinc = Runtime.getRuntime().exec(
//          arrayOf("/home/ununhexium/.sdkman/candidates/kotlin/1.2.50/bin/kotlinc",
//                  file.toAbsolutePath().toFile().toString())
//      )
//
//      val stderr = BufferedReader(InputStreamReader(kotlinc.errorStream))
//
//      kotlinc.waitFor()
//
//      val error = kotlinc.exitValue() != 0
//      if (error)
//      {
//        val message = """
//          |Compilation failed for file $file
//          |${stderr.readLines().joinToString("\n")}
//        """.trimMargin()
//
//        throw CompilationFailed(message)
//      }
//    }
//  }
//}
