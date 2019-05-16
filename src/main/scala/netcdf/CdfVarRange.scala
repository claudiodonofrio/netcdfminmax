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
    * @param fileName [String] provide the /full/path/and/name/to/the/netcdf.file
    * @method getJson [String] returns a json formatted string containing all
    *        variables with min / max in form of {"varname":{"min":0.5,"max":10.5}}
    * @method sanityCheck returns [Boolean | String]
    *        True: if 'fileName' is a file and the first 8 bytes contain either 'cdf' or 'hd5'
    *        String: if file not found or file not a NetCDF file
    * @method
    */

  import java.io.{FileInputStream, IOException}
  import ucar.nc2.{NetcdfFile, Variable, Attribute}
  import ucar.ma2.MAMath

  // -------------------------------------------------------------------

  var checkMsg: String = ""

  // -------------------------------------------------------------------
  def getJson : String = {
    /**
      * Extract min/max values for all Variables in "fileName"
      * @return [String] Json formatted string of form
      *         {"varname":{"min":0.5,"max":10.5}}
      */
    var jsonStr:String = "{\n"
    try {
      val ncf = NetcdfFile.open(this.fileName)
      val items = ncf.getVariables.listIterator
      while (items.hasNext) {
        jsonStr += calcMinMax(items.next())
      }
      ncf.close()
      // remove the last 'comma' and close the json object
      jsonStr = jsonStr.dropRight(2) + "\n}"
    } catch {
      case e: Exception => println(e)
    }
    jsonStr
  }

  // -------------------------------------------------------------------
  def calcMinMax(v: Variable): String = {
    /**
      * @param v: Expect a "Variable" object from unidata netcdf package
      * @return String with varname, minimum and maximum
      *         if _FillValue is defined in v:, they will be skipped
      *         TODO: mask "missing values" from the min max calculation
      *         val mv = v.findAttribute("missing_value").getNumericValue.doubleValue()
      */

    var min:Any = None
    var max: Any = None
    val data = v.read

    def SkipValue:Option[Attribute] = Option(v.findAttribute("_FillValue"))

    SkipValue match {
      case Some(skip) => {
        min = MAMath.getMinimumSkipMissingData(data, skip.getNumericValue().doubleValue())
        max = MAMath.getMaximumSkipMissingData(data, skip.getNumericValue().doubleValue())
      }
      case None => {
        min = MAMath.getMinimum(data)
        max = MAMath.getMaximum(data)
      }
    }
    val varStr:String = "\"" + v.getShortName + "\":{\"min\":" + min.toString + ",\"max\":" + max.toString + "},\n"
    varStr
  }

  // -------------------------------------------------------------------
  def sanityCheck: Boolean = {
    /**
      *  Check if "fileName" does exist, open the file, check signature
      *  @return [Boolean]
      *          True, if first 8 bytes contain the netCDF file signature
      *          False, if file not found or wrong signature.
      *          Check parameter checkMsg      *
      *  TODO: check signature at offset 512, 1024, 2048 etc.
      */

    val header = List("cdf","hdf")
    var in = None: Option[FileInputStream]

    try {
      in = Some(new FileInputStream(this.fileName))
      val signature: Array[Byte] = new Array[Byte](8)
      in.get.read(signature, 0, 8)
      val signatureStr = (signature.map(_.toChar)).mkString.toLowerCase()
      if ( header.exists(signatureStr.contains) != true){
        this.checkMsg = "File signature wrong"
        false
      } else {true}
    } catch {
      case e: IOException => this.checkMsg = e.toString
        false
    }
  }

  // -------------------------------------------------------------------
}
