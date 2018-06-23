package net.lab0.foiegras

import net.lab0.foiegras.caze.BenchmarkResultImpl
import net.lab0.foiegras.caze.JavaFlatCaseImpl
import net.lab0.foiegras.caze.NewClassAsField
import net.lab0.foiegras.caze.iface.BenchmarkCase
import net.lab0.foiegras.caze.iface.BenchmarkResult
import java.nio.file.Path
import java.nio.file.Paths
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger
import java.util.stream.Collectors
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC
import javax.lang.model.element.Modifier.TRANSIENT

/**
 * Java util logging is just madness X(
 */
val log: Logger by lazy {
  Logger.getGlobal().level = Level.OFF
  Logger.getGlobal().handlers.forEach {
    it.level = Level.OFF
  }

  val l = Logger.getLogger(Logger::class.java.name)
  l.level = Level.FINE

  val h = ConsoleHandler()
  h.level = Level.ALL

  l.addHandler(h)
  l.useParentHandlers = false

  println("Handlers: " + l.handlers.size)

  return@lazy l
}


fun main(args: Array<String>) {
  val outputFolder = Paths.get("generated")

  val cases = listOf(
//      generateFlatCases(outputFolder),
      generateComplexCases(outputFolder)
  ).flatMap { it }

  evaluateCases(cases)
}


fun generateComplexCases(outputFolder: Path): List<BenchmarkCase> {
  return listOf(
//      NewObjectAsField(outputFolder),
      NewClassAsField(outputFolder)
  )
}


fun evaluateCases(cases:List<BenchmarkCase>) {
  log.info("On the way to test ${cases.size} cases")
  val onTheWay = mutableListOf<Any>()

  val best = cases.parallelStream().map { case ->
    synchronized(onTheWay) {
      onTheWay += case
      log.info("Case ${onTheWay.size} out of ${cases.size}")
    }

    val sweet = sweetspot(
        accuracy = 1,
        high = Math.min(case.upperBoundHint, 65536),
        test = { case.evaluateAt(it) }
    )

    return@map BenchmarkResultImpl(case, sweet)
  }.collect(Collectors.toList()) as List<BenchmarkResult<*>>

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
          |${it.value.joinToString("\n") { it.case.verboseString() }}
          |
          |
        """.trimMargin()
      }

  log.info(result)
}


private fun generateFlatCases(outputFolder: Path)
    : List<JavaFlatCaseImpl> {
  return listOf(true, false).flatMap { initialized ->
    listOf(PUBLIC, STATIC, TRANSIENT, FINAL)
        .listProgression()
        .map { keywords ->
          JavaFlatCaseImpl(
              outputFolder,
              keywords,
              initialized
          )
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
fun sweetspot(
    accuracy: Int = 1,
    low: Int = 0,
    high: Int = 65536,
    iteration: Int = 0,
    test: (Int) -> Boolean
): Int {
  val middle = (low + high) / 2

  log.fine("Sweet spot iteration $iteration: $middle")

  return if (high - low <= accuracy) middle
  else {
    if (test(middle)) {
      sweetspot(accuracy, middle, high, iteration + 1, test)
    } else {
      sweetspot(accuracy, low, middle, iteration + 1, test)
    }
  }
}
