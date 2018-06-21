package net.lab0.foiegras

import net.lab0.foiegras.Language.JAVA
import net.lab0.foiegras.Language.KOTLIN
import java.nio.file.Path
import java.nio.file.Paths


fun main(args: Array<String>)
{
  val outputFolder = Paths.get("generated")

  listOf(KOTLIN).forEach { language ->
    val best = sweetspot {
      generateAndCompile(language, outputFolder, "test.f$it", it)
    }
    println("Best $language: $best")
  }
}


fun sweetspot(
    low: Int = 0,
    high: Int = 65536,
    test: (Int) -> Unit
): Int
{
  val middle = (low + high) / 2

  return if (high - low <= 1) low
  else
  {
    try
    {
      test(middle)
      sweetspot(middle, high, test)
    }
    catch (e: Exception)
    {
      println("Failed with $middle: ${e.message}")
      sweetspot(low, middle, test)
    }
  }
}


fun generateAndCompile(
    language: Language,
    outputFolder: Path,
    packageName: String,
    fieldsCount: Int
)
{
  println("Generating a $language class with $fieldsCount fields")

  when (language)
  {
    JAVA -> JavaBenchmarking.generateAndCompileJava(
        outputFolder,
        packageName,
        fieldsCount
    )

    KOTLIN -> KotlinBenchmarking.generateAndCompileKotlin(
        outputFolder,
        packageName,
        fieldsCount
    )
  }
}

