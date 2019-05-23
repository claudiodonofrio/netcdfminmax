package netcdf

class CdfVarRange (var fileName: String = ""){
  /**
    * A class to extract all minimum and maximum values
    * for all variables in a netcdf file.
    * The class is dependent on the src from unidata to access netcdf files
    * You have to add a resolver and dependencies to "build.sbt
    * -> resolvers += "NetCDF specific release repo" at "https://artifacts.unidata.ucar.edu/repository/unidata-releases/"
    * and then add the library with
    * -> "edu.ucar" % "netcdf4" % "4.6.13",
    * -> "org.slf4j" % "slf4j-log4j12" % "1.7.26"
    *
    * @constructor fileName [String] or empty.
    * @param fileName (String) get or set    *
    *        provide the /full/path/and/name/to/the/netcdf.file
    * @param checkMsg returns (String). For sanityCheck == false, the corresponding
    *        error msg is stored. Otherwise empty string.
    * @method getVarList (List(Map(String, String) returns La json formatted string containing all
    *        variables with min / max in form of {"varname":{"min":0.5,"max":10.5}}
    * @method sanityCheck returns (Boolean)
    *        True: if 'fileName' is netcdf file. If false, checkMsg contains details
    */

  import java.io.File
  import scala.collection.mutable.ListBuffer
  import ucar.nc2.{NetcdfFile, Variable, Attribute}
  import ucar.ma2.MAMath

  // -------------------------------------------------------------------

  var checkMsg: String = ""
  val maxFileSize: Double = 2.0 // size in GB

  // -------------------------------------------------------------------
  def getVarList : List[Map[String, String]] = {
    /**
      * Extract min/max values for all Variables in "fileName"
      * @return [List[Map]] with map entries "varName": "value"
      */
    var ncfVars : ListBuffer[Map[String, String]] = ListBuffer()
    try {
      val ncf = NetcdfFile.open(this.fileName)
      val items = ncf.getVariables.listIterator
      while (items.hasNext) {
        ncfVars += calcMinMax(items.next())
      }
      ncf.close()
    } catch {
      case e: Exception => println(e)
    }
    ncfVars.toList
  }

  // -------------------------------------------------------------------
  def calcMinMax(v: Variable): Map[String,String] = {
    /**
      * @param v: Expect a "Variable" object from unidata netcdf package
      * @return Tuple with varname, minimum and maximum
      *         if _FillValue is defined in v:, _FilleValue is skipped
      *         TODO: mask "missing values" from the min max calculation
      *         val mv = v.findAttribute("missing_value").getNumericValue.doubleValue()
      */

    val data = v.read
    var varMap = collection.mutable.Map[String, String]()

    def SkipValue:Option[Attribute] = Option(v.findAttribute("_FillValue"))

    def addMinMax(mm: MAMath.MinMax) = {
      varMap += ("min" -> mm.min.toString)
      varMap += ("max" -> mm.max.toString)
    }

    // add information about the variable to the list
    varMap += ("shortName" -> Option(v.getShortName).getOrElse(""))
    varMap += ("unit" -> Option(v.getUnitsString).getOrElse(""))

    SkipValue match {
      case Some(skip) => {
        addMinMax(MAMath.getMinMaxSkipMissingData(data, skip.getNumericValue().doubleValue()))
      }
      case None => {addMinMax(MAMath.getMinMax(data))}
    }
    varMap.toMap
  }

  // -------------------------------------------------------------------
  def sanityCheck: Boolean = {
    /**
      *  Check if "fileName" does exist, open the file, check signature
      *  @return [Boolean]
      *          True, if the file exists and is a netcdf file
      *          False, if file not found, not readable or to big,
      *          not a netcdf or no data available.
      *          error code in parameter "checkMsg"
      */

    // ------------------------------------
    // check if the file exists
    if (!(new File(fileName).exists())) {
      checkMsg = "File not found."
      return false
    }

    // ------------------------------------
    // check if the file is readable
    if (!(new File(fileName).canRead)) {
      checkMsg = "Not allowed to read the file."
      return false
    }

    // ------------------------------------
    // check the file size
    // by default a 2GB restriction is set
    // see parameter maxFileSize
    // if the file is bigger, you might have to increase
    // the heap memory on the JVM

    val fileSize:Double = (new File(fileName).length())
    if ((fileSize / 1073741824.0) > this.maxFileSize) {     // to GB
      this.checkMsg = "File to big (" + "%.2f".format(fileSize) + " GB) , try to increase maxFileSize."
      return false
    }

    // ------------------------------------
    // try to access the file as netcdf object
    // be aware: this command is marked as experimental in version 4.6.13
    if (! NetcdfFile.canOpen(this.fileName)) {
      this.checkMsg = "Not a 'netcdf' file."
      return false
    }

    // ------------------------------------
    // make sure there is actually data inside the netcdf file
    val f = NetcdfFile.open(this.fileName)
    val vars = f.getVariables
    f.close()
    if (vars.isEmpty){
      this.checkMsg = "no variables found to process"
      return false
    }

    // if all goes well, finished checking and return true..
    true
  }
  // -------------------------------------------------------------------
}
