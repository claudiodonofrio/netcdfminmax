
import java.nio.file.Paths

import netcdf.CdfVarRange
import org.apache.log4j.{BasicConfigurator, Logger}
import spray.json._
import spray.json.DefaultJsonProtocol._

object MinMax {

  def main(args: Array[String]): Unit = {

    // -----------------------------------------------------
    // https://www.unidata.ucar.edu/software/thredds/current/netcdf-java/reference/faq.html
    // search for "no appender could be found"
    // shut off the log facility from netcdf
    BasicConfigurator.configure()
    val logger = Logger.getRootLogger
    logger.setLevel(org.apache.log4j.Level.OFF)
    // -----------------------------------------------------


    // val f: String = "varRange.fileName = "test_files/tas_3hr_20010101-20011231.nc"
    // val f: String = "varRange.fileName = "test_files/pr_monthly_19900101-19901231.nc"
    // val f: String = "varRange.fileName = "test_files/ncap2_tmp_dmm.nc.pid7252"
    val f: String = "test_files/ingos222.nc"


    val varRange= new CdfVarRange(Paths.get(f))

    println(varRange.sanityCheck.getOrElse(
      for (v <- varRange.getVarList) println(v))
    )



    /*
    if (varRange.sanityCheck){
      for (v <- varRange.getVarList) {
        println(v)
      }
    } else {
      println(varRange.checkMsg)
    }
    */

  }
}


