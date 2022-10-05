name := "concurrency-examples"

version := "1.0"

scalaVersion := "2.13.6"

scalacOptions ++= Seq(
	"-deprecation"
)

resolvers ++= Seq(
	"Sonatype OSS Snapshots" at
		"https://oss.sonatype.org/content/repositories/snapshots",
	"Sonatype OSS Release" at
		"https://oss.sonatype.org/content/repositories/releases",
	"Typesafe Repository" at
		"https://repo.typesafe.com/typesafe/maven-release/"
)

libraryDependencies ++= Seq(
	"commons-io" % "commons-io" % "2.11.0",
	"org.scala-lang.modules" %% "scala-async" % "0.10.0",
	"org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided,
	"org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4",
	"io.reactivex" %% "rxscala" % "0.27.0",
	"org.scala-lang.modules" %% "scala-swing" % "3.0.0",
	"com.lihaoyi" %% "ujson" % "2.0.0",
	"org.scala-stm" %% "scala-stm" % "0.11.0",
	"com.typesafe.akka" %% "akka-actor-typed" % "2.6.20",
	"ch.qos.logback" % "logback-classic" % "1.2.3",
	"com.typesafe.akka" %% "akka-remote" % "2.6.20",
	"io.netty" % "netty" % "3.10.6.Final",
	"com.typesafe.akka" %% "akka-cluster-typed" % "2.6.20",
	"com.storm-enroute" %% "scalameter-core" % "0.21"
)

fork := false
