package netcdf

import java.nio.file.{Files, Path}
import java.io.File

import scala.collection.JavaConverters._
import ucar.nc2.{NetcdfFile, Variable}
import ucar.ma2.{MAMath, Section}

/**
  * A class to extract all minimum and maximum values
  * for all variables in a netcdf file. Files of type:
  * https://www.unidata.ucar.edu/software/thredds/current/netcdf-java/reference/formats/FileTypes.html
  * are supported.
  * The class is dependent on the source from unidata to access netcdf files
  * You have to add a resolver and dependencies to "build.sbt
  * -> resolvers += "NetCDF specific release repo" at "https://artifacts.unidata.ucar.edu/repository/unidata-releases/"
  * and then add the library with
  * -> "edu.ucar" % "netcdf4" % "4.6.13",
  * -> "org.slf4j" % "slf4j-log4j12" % "1.7.26"
  *
  * @constructor fileName [String] or empty.
  * @param file (Path) get or set    *
  *                 provide the /full/path/and/name/to/the/netcdf.file
  * @method getVarList (List(Map(String, String) returns La json formatted string containing all
  *         variables with min / max in form of {"varname":{"min":0.5,"max":10.5}}
  * @method sanityCheck returns (Boolean)
  *         True: if 'fileName' is netcdf file. If false, checkMsg contains details
  */
class CdfVarRange (file: Path){
  import CdfVarRange._
  // -------------------------------------------------------------------

  val maxFileSize = 2.0 // size in GB

  def fileName: String = file.toAbsolutePath.toString
  // -------------------------------------------------------------------
  /**
    * Extract min/max values for all Variables in "fileName"
    * @return Seq[Map] with map entries "varName": "value"
    */
  def getVarList : Seq[Stats] = {
    val ncf = NetcdfFile.open(fileName)
    try {
      ncf.getVariables.asScala.map(calcMinMax)
    }
    finally ncf.close()
  }

  // -------------------------------------------------------------------
  /**
    * @param v: Expect a "Variable" object from unidata netcdf package
    * @return Tuple with varname, minimum and maximum
    *         if _FillValue is defined in v:, _FilleValue is skipped
    *         TODO: mask "missing values" from the min max calculation
    *         val mv = v.findAttribute("missing_value").getNumericValue.doubleValue()
    */
  def calcMinMax(v: Variable): Stats = {

    //val section = new Section(new Array[Int](v.getSize.toInt))
    val data = v.read()
    val skipValue = Option(v.findAttribute("_FillValue"))


    val size = v.getSize
    val shape = v.getShape


    val minMax = skipValue match {
      case Some(skip) =>
        MAMath.getMinMaxSkipMissingData(data, skip.getNumericValue().doubleValue())
      case None =>
        MAMath.getMinMax(data)
    }
    Stats(v.getShortName, minMax.min, minMax.max, Option(v.getUnitsString))
  }

  // -------------------------------------------------------------------
  /**
    *  Check if "fileName" does exist, open the file, check signature
    *  @return Option[String]
    *          None, if the file exists and is a netcdf file
    *          Some(error message), if file not found, not readable or to big,
    *          not a netcdf or no data available.
    */
  def sanityCheck: Option[String] = {
    // ------------------------------------
    // check if the file exists
    if (!Files.exists(file)) return Some("File not found.")

    // ------------------------------------
    // check if the file is readable
    if (!new File(fileName).canRead) return Some("Not allowed to read the file.")

    // ------------------------------------
    // check the file size
    // by default a 2GB restriction is set
    // see parameter maxFileSize
    // if the file is bigger, you might have to increase
    // the heap memory on the JVM
    val fileSize = file.toFile.length()
    if ((fileSize / 1073741824.0) > this.maxFileSize) {     // to GB
      return Some("File to big (" + "%.2f".format(fileSize) + " GB) , try to increase maxFileSize.")
    }

    // ------------------------------------
    // try to access the file as netcdf object
    // be aware: this command is marked as experimental in version 4.6.13
    if (! NetcdfFile.canOpen(fileName)) return Some("Not a 'netcdf' file.")

    // ------------------------------------
    // make sure there is actually data inside the netcdf file
    val f = NetcdfFile.open(fileName)
    val vars = f.getVariables
    f.close()

    if (vars.isEmpty) Some("no variables found to process")
    else None //all ok, no errors
  }
  // -------------------------------------------------------------------
}

object CdfVarRange{
  case class Stats(val name: String, min: Double, max: Double, unit: Option[String])
}
