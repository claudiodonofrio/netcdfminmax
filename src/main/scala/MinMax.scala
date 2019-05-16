

import netcdf.CdfVarRange
import org.apache.log4j.{BasicConfigurator, Logger}


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


    val minmax= new CdfVarRange()
    minmax.fileName = "test_files/ingos222.nc"
    if (minmax.sanityCheck){
      println(minmax.getJson)
    } else {
      println(minmax.checkMsg)
    }
  }
}


