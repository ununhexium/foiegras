package net.lab0.foiegras

import com.google.common.collect.Lists
import com.squareup.javapoet.TypeName
import net.lab0.foiegras.caze.BenchmarkResultImpl
import net.lab0.foiegras.caze.JavaFlatCaseImpl
import net.lab0.foiegras.caze.complex.NewClassAsField
import net.lab0.foiegras.caze.complex.NewClassHierarchyForFields
import net.lab0.foiegras.caze.complex.NewObjectAsField
import net.lab0.foiegras.caze.iface.BenchmarkCase
import net.lab0.foiegras.caze.iface.BenchmarkResult
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import javax.lang.model.element.Modifier
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PROTECTED
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC
import javax.lang.model.element.Modifier.TRANSIENT
import javax.lang.model.element.Modifier.VOLATILE

/*
 * Enable or disable these options to filter which tests to run
 */
const val flatBench = false
const val objectBench = true
const val classBench = false

// warning, this one can theoretically have an unlimited amount of fields
const val hierarchyBench = false

// test absolutely all combinations. Will take a long time to finish
const val fullBench = false

/**
 * Margin of error to find the maximum number of fields.
 * Must be > 0
 */
const val ACCURACY = 1

/**
 * To be faster if your computer isn't dying of heat exhaustion
 */
const val parallelEvaluation = false


fun main(args: Array<String>) {
  val outputFolder = Paths.get("generated")

  val todo = listOf(
      flatBench to generateFlatCases(outputFolder),
      objectBench to generateObjectCases(outputFolder),
      classBench to generateClassAsFieldCases(outputFolder),
      hierarchyBench to listOf(NewClassHierarchyForFields(outputFolder))
  )

  val cases = todo
      .filter { it.first }
      .flatMap { it.second }

  evaluateCases(cases)
}

private fun generateClassAsFieldCases(outputFolder: Path): List<NewClassAsField> {
  return listOf(true, false).map {
    NewClassAsField(outputFolder, it)
  }
}


fun generateObjectCases(outputFolder: Path) =
    listOf(true, false).map {
      NewObjectAsField(outputFolder, it)
    }


fun evaluateCases(cases: List<BenchmarkCase>) {
  log.info("On the way to test ${cases.size} cases")
  val onTheWay = mutableListOf<Any>()
  val best = evaluate(cases, onTheWay)
  showResult(best)
}


private fun evaluate(
    cases: List<BenchmarkCase>,
    onTheWay: MutableList<Any>
) =
    if (parallelEvaluation) {
      cases.parallelStream()
    }
    else {
      cases.stream()
    }
        .map { case ->
          synchronized(onTheWay) {
            onTheWay += case
            log.info("Case ${onTheWay.size} out of ${cases.size}")
          }

          val (low, high) = slowStart { case.evaluateAt(it) }

          val sweet = sweetspot(
              ACCURACY,
              low,
              high
          ) { case.evaluateAt(it) }

          return@map BenchmarkResultImpl(case, sweet)
        }.collect(Collectors.toList())


private fun showResult(best: List<BenchmarkResult<*>>) {
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

/**
 * @return `Pair<LowerBound, HigherBound>`
 */
fun slowStart(test: (Int) -> Boolean): Pair<Int, Int> {
  (0..30).forEach {
    val attempt = power(2, it)
    log.info("Slow start at $attempt")
    if (!test(attempt)) {
      return Pair(attempt / 2, attempt)
    }
  }
  throw RuntimeException(
      "Ahm, ... I'm not going to generate 2 billion fields!"
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

  val access = buildAccessibilityList()
  val modification = buildModificatorsList()
  val static = buildStaticList()
  val transient = buildTransientList()
  val types = buildTypesList()

  val allCombinations = Lists.cartesianProduct(
      access,
      modification,
      static,
      transient
  ).map {
    it.filterIsInstance( Modifier::class.java )
  }.distinct()

  return listOf(true, false).flatMap { initialized ->
    types.flatMap { type ->
      allCombinations
          .map { keywords ->
            JavaFlatCaseImpl(
                outputFolder,
                type,
                keywords,
                initialized
            )
          }
    }
  }
}

private fun buildTypesList(): List<TypeName> {
  return if (fullBench) {
    listOf(
        TypeName.BYTE,
        TypeName.SHORT,
        TypeName.INT,
        TypeName.LONG,
        TypeName.OBJECT
    )
  }
  else {
    listOf(TypeName.BYTE)
  }
}

private fun buildStaticList() = listOf(STATIC, NULL)

private fun buildTransientList() =
    if (fullBench) {
      listOf(TRANSIENT, NULL)
    }
    else {
      listOf(NULL)
    }

private fun buildModificatorsList() =
    if (fullBench) {
      listOf(VOLATILE, FINAL, NULL)
    }
    else {
      listOf(FINAL, NULL)
    }

private fun buildAccessibilityList() =
    if (fullBench) {
      listOf(PUBLIC, PROTECTED, PRIVATE, NULL)
    }
    else {
      listOf(PUBLIC)
    }

/**
 * Binary search to find the highest possible fields count for each benchmark case
 */
fun sweetspot(
    accuracy: Int = 1,
    low: Int = Int.MIN_VALUE,
    high: Int = Int.MAX_VALUE,
    test: (Int) -> Boolean
) = binarySearch(
    Math.max(accuracy, 1),
    Math.max(low, 1),
    high,
    1,
    test
)


fun binarySearch(
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
      binarySearch(accuracy, middle, high, iteration + 1, test)
    }
    else {
      binarySearch(accuracy, low, middle, iteration + 1, test)
    }
  }
}
