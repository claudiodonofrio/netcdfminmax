package eu.icoscp.netcdfminmax

class cdfVarRange {

  import java.io.{FileInputStream, IOException}
  import ucar.nc2._
  import ucar.ma2.MAMath

  // -------------------------------------------------------------------
  var fileName: String = ""
  var jsonString = ""

  // -------------------------------------------------------------------
  def getJson : String = {
    try {
      val ncf = NetcdfFile.open(this.fileName)
      val iter = ncf.getVariables.listIterator
      jsonString = "{\n"
      while (iter.hasNext) {calcMinMax(iter.next())}
      jsonString += "}"
      ncf.close()
    } catch {
      case e: Exception => println(e)
    }
    jsonString
  }

  // -------------------------------------------------------------------
  def calcMinMax(v: Variable) = {

    // TODO: mask "missing values" from the min max calculation
    // val mv = v.findAttribute("missing_value").getNumericValue.doubleValue()

    var min:Any = None
    var max: Any = None

    def SkipValue:Option[Attribute] = Option(v.findAttribute("_FillValue"))

    SkipValue match {
      case Some(skip) => {
        min = MAMath.getMinimumSkipMissingData(v.read, skip.getNumericValue().doubleValue())
        max = MAMath.getMaximumSkipMissingData(v.read, skip.getNumericValue().doubleValue())
      }
      case None => {
        min = MAMath.getMinimum(v.read())
        max = MAMath.getMaximum(v.read())
      }
    }
    jsonString += "'" + v.getShortName + "':{'min':" + min.toString + ",'max':" + max.toString + "}\n"
  }


  // -------------------------------------------------------------------
  def sanityCheck: Any = {

    /*
    val currentDirectory = new java.io.File(".").getCanonicalPath
    println(currentDirectory)
    println(fileName)
    */
    println(new java.io.File(".").getCanonicalPath)

    // TODO: check signature at offset 512, 1024, 2048 etc.

    val header = List("cdf","hdf")
    var in = None: Option[FileInputStream]

    try {
      in = Some(new FileInputStream(this.fileName))
      val signature: Array[Byte] = new Array[Byte](8)
      in.get.read(signature, 0, 8)
      val signatureStr = (signature.map(_.toChar)).mkString.toLowerCase()
      header.exists(signatureStr.contains)
    } catch {
      case returnError: IOException => returnError
    }
  }

  // -------------------------------------------------------------------
}
