package net.lab0.foiegras

import net.lab0.foiegras.Language.JAVA
import net.lab0.foiegras.Language.KOTLIN
import net.lab0.foiegras.caze.JavaBenchmarkCandidate
import net.lab0.foiegras.caze.JavaBenchmarkCaseImpl
import java.nio.file.Path
import java.nio.file.Paths
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger
import java.util.stream.Collectors
import javax.lang.model.element.Modifier.*

/**
 * Java logging is just madness X(
 */
val log: Logger by lazy {
  Logger.getGlobal().level = Level.OFF
  Logger.getGlobal().handlers.forEach {
    it.level = Level.OFF
  }

  val l = Logger.getLogger(Logger::class.java.name)
  l.level = Level.FINE

  val h = ConsoleHandler()
  h.level = Level.FINE

  l.addHandler(h)
  l.useParentHandlers = false

  println("Handlers: " + l.handlers.size)

  return@lazy l
}


fun main(args: Array<String>) {

  val outputFolder = Paths.get("generated")

  val cases = generateCases(outputFolder)

  log.info("On the way to test ${cases.size} cases")

  val onTheWay = mutableListOf<Any>()

  val best = cases.parallelStream().map {
    synchronized(onTheWay) {
      onTheWay += it
      log.info("Case ${onTheWay.size} out of ${cases.size}")
    }

    sweetspot(
        accuracy = 1000,
        testBuilder = { fieldsCount ->
          JavaBenchmarkCandidate(it, fieldsCount)
        },
        test = { generateAndCompile(it) }
    )
  }.collect(Collectors.toList()) as List<JavaBenchmarkCandidate>

  val result = best
      .groupBy { it.fieldsCount }
      .toSortedMap(Comparator { o1, o2 -> o2 - o1 })
      .entries
      .joinToString("\n") {

        """
          |
          |${it.key} fields
          |${it.value.size} elements
          |
          |${it.value.joinToString("\n") { it.verbose() }}
          |
          |
        """.trimMargin()
      }

  log.info(result)
}


fun JavaBenchmarkCandidate.verbose() =
    """
      |Keywords: ${this.keywords.size} ${this.keywords.joinToString { it.name.toLowerCase() }}
      |Initialisation ${this.initialized}
      |
    """.trimMargin()


private fun generateCases(outputFolder: Path): List<JavaBenchmarkCaseImpl> {
  return listOf(true, false).flatMap { initialized ->
    listOf(PUBLIC, STATIC, TRANSIENT, FINAL)
        .listProgression()
        .flatMap { keywords ->
          listOf(JAVA).map { language ->
            JavaBenchmarkCaseImpl(
                language,
                outputFolder,
                keywords,
                initialized
            )
          }
        }
  }
}

private fun <E> List<E>.listProgression() = listOf(this)
    .flatMap { list ->
      (0..list.size).map { quantity ->
        list.take(quantity)
      }
    }

/**
 * Binary search to find the highest possible fields count for each benchmark case
 */
fun <T> sweetspot(
    accuracy: Int = 1,
    low: Int = 0,
    high: Int = 65536,
    iteration: Int = 0,
    test: (T) -> Boolean,
    testBuilder: (Int) -> T
): T {
  val middle = (low + high) / 2
  val testCase = testBuilder(middle)

  log.fine("Sweet spot iteration $iteration")

  return if (high - low <= accuracy) testCase
  else {
    if (test(testCase)) {
      sweetspot(accuracy, middle, high, iteration + 1, test, testBuilder)
    } else {
      sweetspot(accuracy, low, middle, iteration + 1, test, testBuilder)
    }
  }
}


fun generateAndCompile(candidate: JavaBenchmarkCandidate): Boolean {
  log.finer(
      """
        |Generating a ${candidate.case.language} class with
        |${candidate.fieldsCount} fields
        |${candidate.keywords.size} keywords: ${candidate.keywords.joinToString { it.name.toLowerCase() }}
        |${if (candidate.initialized) "With initialization" else "Unintialized"}
        |
      """.trimMargin()
  )

  return try {
    when (candidate.language) {
      JAVA -> JavaBenchmarking.generateAndCompileJava(candidate)
      KOTLIN -> throw UnsupportedOperationException()
    }

    true
  } catch (e: CompilationFailed) {
    log.finer("Failed: ${e.message}")
    false
  }
}