package net.lab0.foiegras.caze.iface

/**
 * How well the test case performed.
 */
interface BenchmarkResult<T> where T : BenchmarkCase {
  val case: T
  val fieldsCount: Int
}
