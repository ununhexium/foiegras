package net.lab0.foiegras.caze.iface

interface BenchmarkCase {
  /**
   * Generates classes and compiles them.
   *
   * @return `true` if it succeeds and `false` if it fails
   */
  fun evaluateAt(fieldsCount: Int): Boolean

  /**
   * Explicitly tell what has been tested.
   */
  fun verboseString(): String

  val upperBoundHint: Int
}
