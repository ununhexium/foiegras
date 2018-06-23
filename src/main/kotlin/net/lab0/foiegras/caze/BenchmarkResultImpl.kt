package net.lab0.foiegras.caze

import net.lab0.foiegras.caze.iface.BenchmarkCase
import net.lab0.foiegras.caze.iface.BenchmarkResult

class BenchmarkResultImpl<T>(
    override val case: T,
    override val fieldsCount: Int
) : BenchmarkResult<T> where T : BenchmarkCase
