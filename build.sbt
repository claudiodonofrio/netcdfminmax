import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"


//resolvers += "Unidata NetCDF All" at "https://artifacts.unidata.ucar.edu/repository/unidata-all/"
resolvers += "NetCDF specific release repo" at "https://artifacts.unidata.ucar.edu/repository/unidata-releases/"

lazy val root = (project in file("."))
  .settings(
    name := "NetCdfMinMax",
    libraryDependencies ++= Seq(
        scalaTest % Test,
        "edu.ucar" % "netcdf4" % "4.6.13",
        "org.slf4j" % "slf4j-log4j12" % "1.7.26",
        "io.spray" %% "spray-json" % "1.3.4"
)
)
// https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12
// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
