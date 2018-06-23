package net.lab0.foiegras.caze.iface

interface BenchmarkResult<T> where T: BenchmarkCase {
  val case: T
  val fieldsCount: Int
}
