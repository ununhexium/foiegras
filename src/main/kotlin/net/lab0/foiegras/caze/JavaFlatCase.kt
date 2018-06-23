package net.lab0.foiegras.caze

import net.lab0.foiegras.caze.iface.BenchmarkCase
import java.nio.file.Path
import javax.lang.model.element.Modifier

interface JavaFlatCase : BenchmarkCase {
  val outputFolder: Path
  val keywords: List<Modifier>
  val init: Boolean
  val initialized: Boolean
}