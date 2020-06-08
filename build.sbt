name := "RtSolarTask"

version := "0.1"

val akkaVersion = "2.5.26"
val akkaHttpVersion = "10.1.12"

scalaVersion := "2.13.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"     % akkaVersion,
  "com.typesafe.akka" %% "akka-stream"    % akkaVersion,
  "com.typesafe.akka" %% "akka-http"      % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
)

mainClass in assembly := Some("ru.solar.rt.Main")

assemblyMergeStrategy in assembly := {
  case "META-INF/io.netty.versions.properties" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
