// --- MyTinyTPU Build Configuration ---


name := "MyTinyTPU"
version := "0.1"
scalaVersion := "2.12.15" 

val chiselVersion = "3.5.6"
val chiselTestVersion = "0.5.6"


libraryDependencies ++= Seq(

  "edu.berkeley.cs" %% "chisel3" % chiselVersion,
  

  "edu.berkeley.cs" %% "chiseltest" % chiselTestVersion % "test"
)


addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full)


scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:reflectiveCalls",
  "-Xsource:2.11"
)


Test / parallelExecution := false // 禁止平行測試，避免波形圖輸出衝突
Test / logBuffered := false       // 即時顯示測試 Log
