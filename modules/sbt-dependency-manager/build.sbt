import sbtbuildinfo.Plugin._

// Taken from root ./build.sbt
// scalaVersion := "2.10.5"
// version := "0.4.1-SNAPSHOT"
// organization := "io.kensu"
// name := "sbt-dependency-manager"

crossScalaVersions := Seq("2.10.5", "2.11.6")

publishArtifact in Test := false

// to download deps at runtime
def depsToDownloadDeps(scalaBinaryVersion: String, sbtVersion: String) = scalaBinaryVersion match {
  case "2.10" => List(
    "org.scala-sbt" % "sbt" % sbtVersion excludeAll ExclusionRule("org.apache.ivy", "ivy"),
    ("com.frugalmechanic" % "fm-sbt-s3-resolver" % "0.5.0") // WARN ONLY 2.10 0.13 available !!!!
      .extra(
        CustomPomParser.SbtVersionKey -> sbtVersion.reverse.dropWhile(_ != '.').drop(".".length).reverse,
        CustomPomParser.ScalaVersionKey -> scalaBinaryVersion
      )
      .copy(crossVersion = CrossVersion.Disabled)
      .excludeAll(ExclusionRule("org.apache.ivy", "ivy"))
  )
  case _ =>
    val aetherApi = "org.sonatype.aether" % "aether-api" % "1.13"
    val jcabiAether = "com.jcabi" % "jcabi-aether" % "0.10.1"
    val mavenCore = "org.apache.maven" % "maven-core" % "3.0.5"
    List(aetherApi, jcabiAether, mavenCore)
}

//for aether
libraryDependencies <++= scalaBinaryVersion {
  case "2.10" => Nil
  case "2.11" => ("com.ning" % "async-http-client" % "[1.6.5, 1.6.5]" force())::Nil
}

libraryDependencies ++= depsToDownloadDeps(scalaBinaryVersion.value, sbtVersion.value)

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.12"

libraryDependencies += "com.typesafe" % "config" % "1.2.1"

libraryDependencies <++= scalaBinaryVersion {
  case "2.11" => List("org.scala-lang.modules" %% "scala-xml" % "1.0.3")
  case "2.10" => Nil
}

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

unmanagedSourceDirectories in Compile += (sourceDirectory in Compile).value / ("scala-" + scalaBinaryVersion.value)

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys :=  Seq[BuildInfoKey](
                    scalaVersion,
                    sbtVersion
                  )
buildInfoPackage := "datafellas.dependencyutils"
