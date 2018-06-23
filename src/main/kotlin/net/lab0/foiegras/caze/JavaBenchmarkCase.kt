package net.lab0.foiegras.caze

import net.lab0.foiegras.Language
import java.nio.file.Path
import javax.lang.model.element.Modifier

interface JavaBenchmarkCase {
  val language: Language
  val outputFolder: Path
  val keywords: List<Modifier>
  val init: Boolean
}