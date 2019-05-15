

import eu.icoscp.netcdfminmax.cdfVarRange

object MinMax {

  def main(args: Array[String]): Unit = {
    val vr = new cdfVarRange()
    vr.fileName = "ingos222.nc"
    if (vr.sanityCheck == true){
      println(vr.getJson)
    } else {
      println(vr.sanityCheck)
    }
  }
}


