

import java.nio.file.{Path, Paths}

import netcdf.{CdfVarRange, fileHash}
import org.apache.log4j.{BasicConfigurator, Logger}

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
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


    val hash= new fileHash()

    val benchmarkfiles: Seq[Path] = Seq(
      "test_files/pr_monthly_19900101-19901231.nc",
      "test_files/ingos222.nc",
      "test_files/nep_qd_2010-2015.nc",
      "test_files/edgar.nc",
      "test_files/mrros_6hr_19920101-19921231.nc"
    ).map(fn => Paths.get(fn))

    // to big for standard heap size
    //benchmarkfiles += "test_files/tas_3hr_20010101-20011231.nc"

    println("benchmark")


    var resultMinMax:Map[String, Double] = Map()
    var resultHash:Map[String, Double] = Map()
    var diff:ArrayBuffer[Double] = ArrayBuffer()
    var t1:Double = 0.0
    var t2:Double = 0.0
    var t3:Double = 0.0

    for (f <- benchmarkfiles) {
      val minmax= new CdfVarRange(f)
      println("processing " + f)
      t1 = System.nanoTime.toDouble
      minmax.getVarList
      t2 = (System.nanoTime - t1) / 1e9d
      resultMinMax += (f.toString -> t2)

      t1 = System.nanoTime
      println(hash.computeHash(f))
      t3 = (System.nanoTime - t1) / 1e9d
      resultHash += (f.toString -> t3)

      diff += (t2 - t3)
    }

    println(resultMinMax.toJson.prettyPrint)
    println(resultHash.toJson.prettyPrint)
    println(diff)

  }
}


