

import netcdf.CdfVarRange
import org.apache.log4j.{BasicConfigurator, Logger}
import scala.collection.mutable.ListBuffer
import spray.json._
import spray.json.DefaultJsonProtocol._

object Benchmark {

  def main(args: Array[String]): Unit = {

    // -----------------------------------------------------
    // https://www.unidata.ucar.edu/software/thredds/current/netcdf-java/reference/faq.html
    // search for "no appender could be found"
    // shut off the log facility from netcdf
    BasicConfigurator.configure()
    val logger = Logger.getRootLogger
    logger.setLevel(org.apache.log4j.Level.OFF)
    // -----------------------------------------------------


    val minmax= new CdfVarRange()

    var benchmarkfiles:ListBuffer[String] = ListBuffer()
    benchmarkfiles += "test_files/nep_qd_2010-2015.nc"
    benchmarkfiles += "test_files/ingos222.nc"
    benchmarkfiles += "test_files/edgar.nc"
    benchmarkfiles += "test_files/mrros_6hr_19920101-19921231.nc"
    benchmarkfiles += "test_files/pr_monthly_19900101-19901231.nc"

    // to big for standard heap size
    //benchmarkfiles += "test_files/tas_3hr_20010101-20011231.nc"

    println("benchmark")

    //var result = ArrayBuffer[Double]()
    var result:Map[String, Double] = Map()
    for (f <- benchmarkfiles) {
      minmax.fileName = f
      println("processing " + f)
      val t1 = System.nanoTime
      val vl = minmax.getVarList
      val timed = (System.nanoTime - t1) / 1e9d
      result += (f -> timed.toDouble)
    }
    println(result.toJson.prettyPrint)
  }
}


