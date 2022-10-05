package org.learningconcurrency
package ch6

object SubjectsOS extends App {
  import rx.lang.scala._
  import scala.concurrent.duration._
  import ObservablesSubscriptions._

  object RxOS {
    val messageBus = Subject[String]()
    messageBus.subscribe(log _)
  }

  object TimeModule {
    val systemClock = Observable.interval(1.seconds).map(t => s"systime: $t")
  }

  object FileSystemModule {
    val fileModifications = modifiedFiles(".").map(filename => s"file modification: $filename")
  }

  log(s"RxOS booting...")
  val modules = List(
    TimeModule.systemClock,
    FileSystemModule.fileModifications
  )

  val loadedModules = modules.map(_.subscribe(RxOS.messageBus))
  log(s"RxOS boot sequence finished!")

  Thread.sleep(10000)
  for (mod <- loadedModules) mod.unsubscribe()
  log(s"RxOS going for shutdown")
}

object SubjectsOSLog extends App {
  import rx.lang.scala._
  import SubjectsOS.{TimeModule, FileSystemModule}

  object RxOS {
    val messageBus = Subject[String]()
    val messageLog = subjects.ReplaySubject[String]()
    messageBus.subscribe(log _)
    messageBus.subscribe(messageLog)
  }

  val loadedModules = List(
    TimeModule.systemClock,
    FileSystemModule.fileModifications
  ).map(_.subscribe(RxOS.messageBus))

  log(s"RxOS booting")
  Thread.sleep(1000)
  log(s"RxOS booted!")
  Thread.sleep(10000)
  for (mod <- loadedModules) mod.unsubscribe()
  log(s"RxOS dumping the complete event log")
  RxOS.messageLog.subscribe(log _)
  log(s"RxOS going for shutdown")
}
