import sbt._
import Keys._
val scioVersion = "0.9.3"
val beamVersion = "2.23.0"
val flinkVersion = "1.10.1"

lazy val commonSettings = Def.settings(
  organization := "com.funny",
  // Semantic versioning http://semver.org/
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.12",
  scalacOptions ++= Seq("-target:jvm-1.8",
                        "-deprecation",
                        "-feature",
                        "-unchecked"),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
)

lazy val root: Project = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name := "ixirc-enricher",
    description := "ixirc-enricher",
    publish / skip := true,
    run / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat,
    libraryDependencies ++= Seq(
      "com.spotify"                                 %% "scio-core"                                    % scioVersion,
      "com.spotify"                                 %% "scio-test"                                    % scioVersion % Test,
      "org.apache.beam"                             % "beam-runners-direct-java"                      % beamVersion,
      "org.apache.beam"                             % "beam-runners-google-cloud-dataflow-java"       % beamVersion,
      "org.apache.beam"                             % "beam-runners-flink-1.10"                       % beamVersion excludeAll (
        ExclusionRule("com.twitter", "chill_2.11"),
        ExclusionRule("org.apache.flink", "flink-clients_2.11"),
        ExclusionRule("org.apache.flink", "flink-runtime_2.11"),
        ExclusionRule("org.apache.flink", "flink-streaming-java_2.11")
      ),
      "org.apache.flink"                            %% "flink-clients"                                % flinkVersion,
      "org.apache.flink"                            %% "flink-runtime"                                % flinkVersion,
      "org.apache.flink"                            %% "flink-streaming-java"                         % flinkVersion,
      "com.typesafe.scala-logging"                  %% "scala-logging"                                % "3.9.2",
      "ch.qos.logback"                              % "logback-classic"                               % "1.2.3",
      "com.github.pureconfig"                       %% "pureconfig"                                   % "0.11.1"
    )
  )
  .enablePlugins(JavaAppPackaging)

lazy val repl: Project = project
  .in(file(".repl"))
  .settings(commonSettings)
  .settings(
    name := "repl",
    description := "Scio REPL for ixirc-enricher",
    libraryDependencies ++= Seq(
      "com.spotify" %% "scio-repl" % scioVersion
    ),
    Compile / mainClass := Some("com.spotify.scio.repl.ScioShell"),
    publish / skip := true
  )
  .dependsOn(root)

