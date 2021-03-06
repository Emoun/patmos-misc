// Process emulation output for generation of bar charts

import scala.sys.process._
import java.io._
import scala.collection.mutable
import scala.io.Source

val log = args.length == 0

val cache = List("mcache", "icache", "pcache")
val sizes = List("1024", "2048", "4096", "8192")

def getMap() {
  Map("mcache" -> mutable.Map[String, Int](), "icache" -> mutable.Map[String, Int](), "pcache" -> mutable.Map[String, Int]())
}

val onek =  Map("mcache" -> mutable.Map[String, Int](), "icache" -> mutable.Map[String, Int](), "pcache" -> mutable.Map[String, Int]())
val twok =  Map("mcache" -> mutable.Map[String, Int](), "icache" -> mutable.Map[String, Int](), "pcache" -> mutable.Map[String, Int]())
val fourk =  Map("mcache" -> mutable.Map[String, Int](), "icache" -> mutable.Map[String, Int](), "pcache" -> mutable.Map[String, Int]())
val eightk =  Map("mcache" -> mutable.Map[String, Int](), "icache" -> mutable.Map[String, Int](), "pcache" -> mutable.Map[String, Int]())

val all = Map("1024" -> onek, "2048" -> twok, "4096" -> fourk, "8192" -> eightk)

val bench = mutable.Set[String]()

val files = new File(".").listFiles
val txtFiles = files.filter( f => f.getName.endsWith(".txt") )
txtFiles.map{ file => addResult(file.getName) }

def addResult(f: String) {
  val s = Source.fromFile(f)
  val l = s.getLines()
  val v = l.next().split(" ")
  bench += v(0)
  val cycles = l.next().split(" ")(1).toInt
  if (log) println(v.toList + ": " + cycles)
  all(v(3))(v(1)) += (v(0) -> cycles)
}

if (log) println()
if (log) println(bench)

def printStat(t: String) {
  println("Results for " + t + ": mcache icache")
  val sortedBench = bench.toSeq.sorted
  for (b <- sortedBench) {
    val v1 = all(t)("pcache")(b)
    val v2 = all(t)("icache")(b)
    val fac = v2.toDouble / v1
    println(b + " " + v1 + " " + v2 + " " + fac)
  }
}


//// execution time in cycles therefore v2/v1 normalizes to v2
//def printData(t: String, c1: String, c2: String) {
//  println("sym y")
//  val sortedBench = bench.toSeq.sorted
//  var geoMean: Double = 1
//  for (b <- sortedBench) {
//    val v1 = all(t)(c1)(b)
//    val v2 = all(t)(c2)(b)
//    val fac = v2.toDouble / v1
//    val n = b.flatMap { case '_' => "\\_" case c => s"$c" }
//    println(n + " " + fac)
//    geoMean = geoMean * fac
//  }
//  // println("geom. mean: " + scala.math.pow(geoMean,1./sortedBench.size))
//}

def printData(c1: String, c2: String) {
  println("sym 1kB 2kB 4kB 8kB")
  val sortedBench = bench.toSeq.sorted
  var geoMean: Double = 1
  for (b <- sortedBench) {
    val v11 = all("1024")(c1)(b)
    val v12 = all("1024")(c2)(b)
    val fac1 = v12.toDouble / v11
    val v21 = all("2048")(c1)(b)
    val v22 = all("2048")(c2)(b)
    val fac2 = v22.toDouble / v21
    val v41 = all("4096")(c1)(b)
    val v42 = all("4096")(c2)(b)
    val fac4 = v42.toDouble / v41
    val v81 = all("8192")(c1)(b)
    val v82 = all("8192")(c2)(b)
    val fac8 = v82.toDouble / v81
    val n = b.flatMap { case '_' => "-" case c => s"$c" }
    println(n + " " + fac1 + " " + fac2 + " " + fac4 + " " + fac8)
//    geoMean = geoMean * fac
  }
  // println("geom. mean: " + scala.math.pow(geoMean,1./sortedBench.size))
}

if (log) sizes.map{ t => printStat(t) }

if (!log) printData(args(0), args(1))
