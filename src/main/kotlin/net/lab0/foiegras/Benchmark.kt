package net.lab0.foiegras

import net.lab0.foiegras.caze.BenchmarkResultImpl
import net.lab0.foiegras.caze.JavaFlatCaseImpl
import net.lab0.foiegras.caze.NewClassAsField
import net.lab0.foiegras.caze.NewClassHierarchyForFields
import net.lab0.foiegras.caze.NewObjectAsField
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

  val l = Logger.getLogger("Benchmark")
  l.level = Level.FINE

  val h = ConsoleHandler()
  h.level = Level.ALL

  l.addHandler(h)
  l.useParentHandlers = false

  println("Handlers: " + l.handlers.size)

  return@lazy l // lots of work for a lazy init
}

/*
 * Enable or disable these options to filter which tests to run
 */
const val flatBench = false
const val objectBench = false
const val classBench = false
// warning, this one can theoretically have an unlimited amount of fields
const val hierarchyBench = true

/**
 * Margin of error to find the maximum number of fields.
 * Must be > 0
 */
const val ACCURACY = 1

fun main(args: Array<String>) {
  val outputFolder = Paths.get("generated")

  val todo = listOf(
      flatBench to generateFlatCases(outputFolder),
      objectBench to listOf(NewObjectAsField(outputFolder)),
      classBench to listOf(true, false).map {
        NewClassAsField(outputFolder, it)
      },
      hierarchyBench to listOf(NewClassHierarchyForFields(outputFolder))
  )

  val cases = todo
      .filter { it.first }
      .flatMap { it.second }

  evaluateCases(cases)
}


fun evaluateCases(cases: List<BenchmarkCase>) {
  log.info("On the way to test ${cases.size} cases")
  val onTheWay = mutableListOf<Any>()

  val best = cases.parallelStream().map { case ->
    synchronized(onTheWay) {
      onTheWay += case
      log.info("Case ${onTheWay.size} out of ${cases.size}")
    }

    val (low,high) = slowStart { case.evaluateAt(it) }

    val sweet = sweetspot(
        ACCURACY,
        low,
        high,
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


fun slowStart(test: (Int) -> Boolean): Pair<Int, Int> {
  (0..30).forEach {
    val attempt = power(2, it)
    log.info("Slow start at $attempt")
    if (!test(attempt)) {
      return Pair(attempt / 2, attempt)
    }
  }
  throw RuntimeException(
      "Ahm, ... I'm not going to generate 2 billion classes!"
  )
}


fun power(a: Int, b: Int): Int =
    when {
      b == 0 -> 1
      b % 2 == 0 -> square { power(a, b / 2) }
      else -> a * power(a, b - 1)
    }

fun square(a: () -> Int): Int {
  val tmp = a()
  return tmp * tmp
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
    }
    else {
      sweetspot(accuracy, low, middle, iteration + 1, test)
    }
  }
}
