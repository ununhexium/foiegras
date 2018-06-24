package net.lab0.foiegras.caze.iface

/**
 * Describes a benchmark case.
 * That is, a source code generation model which will be tested
 * for the maximum number of fields it can contain.
 */
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

  /**
   * @return the exception that cause the last compilation failure
   */
  fun getLastFailureCause(): Throwable
}
