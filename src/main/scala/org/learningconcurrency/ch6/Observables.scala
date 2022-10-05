package org.learningconcurrency
package ch6

object ObservablesItems extends App {
  import rx.lang.scala._

  val o = Observable.just("Pascal", "Java", "Scala")
  log("test0...")
  o.subscribe(name => log(s"learned the $name language"))
  log("test1...")
  o.subscribe(name => log(s"forgot the $name language"))
  log("test2...")
}

object ObservablesTimer extends App {
  import rx.lang.scala._
  import scala.concurrent.duration._

  val o = Observable.timer(1.second)
  o.subscribe(_ => log(s"Timeout!"))
  log("test0...")
  o.subscribe(_ => log(s"Another timeout!"))
  log("test1...")
}

object ObservablesException extends App {
  import rx.lang.scala._

  val o = Observable.just(1, 2) ++ Observable.error(new RuntimeException) ++ Observable.just(3, 4)
  o.subscribe(
    x => log(s"number $x"),
    t => log(s"an error occured: $t")
  )
}

object ObservablesLifetime extends App {
  import rx.lang.scala._

  val classics = List("Il buono, il brutto, il cattivo.", "Back to the future", "Die Hard")
  val o = Observable.from(classics)

  o.subscribe(new Observer[String] {
    override def onNext(m: String) = log(s"Movies Watchlist - $m")
    override def onError(e: Throwable) = log(s"Ooops - Se!")
    override def onCompleted() = log(s"No more movies.")
  })
}

object ObservablesCreate extends App {
  import rx.lang.scala._

  val vms = Observable[String]{ sub =>
    sub.onNext("JVM")
    sub.onNext(".NET")
    sub.onNext("DartVM")
    //sub.onCompleted()
  }

  log(s"About to subscribe")
  vms.subscribe(log _, e => log(s"oops - $e"), () => log("Done!"))
  log(s"Subscription returned")
}

object ObserablesCreateFuture extends App {
  import rx.lang.scala._
  import scala.concurrent._
  import ExecutionContext.Implicits.global

  val f = Future {
    Thread.sleep(500)
    "Back to the Future(s)"
  }

  val o = Observable[String] { sub =>
    f foreach {
      case s =>
        sub.onNext(s)
        sub.onCompleted()
    }
    f.failed foreach {
      case t => sub.onError(t)
    }
  }

  o.subscribe(log _)
}

object ObservablesSubscriptions extends App {
  import rx.lang.scala._
  import org.apache.commons.io.monitor._

  def modifiedFiles(directory: String): Observable[String] = {
    Observable[String] { subscriber =>
      val fileMonitor = new FileAlterationMonitor(1000)
      val fileObs = new FileAlterationObserver(directory)
      val fileLis = new FileAlterationListenerAdaptor {
        override def onFileChange(file: java.io.File) = {
          subscriber.onNext(file.getName)
        }
      }
      fileObs.addListener(fileLis)
      fileMonitor.addObserver(fileObs)
      fileMonitor.start()

      subscriber.add { log("monitorng stop"); fileMonitor.stop() }
    }
  }

  log(s"starting to monitor files")
  val subscription = modifiedFiles(".").subscribe(filename => log(s"$filename modified!"))
  log(s"please modify and save a file")

  Thread.sleep(10000)

  subscription.unsubscribe()
  log(s"monitoring done")
}

object ObservablesHot extends App {
  import rx.lang.scala._
  import org.apache.commons.io.monitor._

  val fileMonitor = new FileAlterationMonitor(1000)
  fileMonitor.start()

  def modifiedFiles(directory: String): Observable[String] = {
    val fileObs = new FileAlterationObserver(directory)
    fileMonitor.addObserver(fileObs)
    Observable[String] { subscriber =>
      val fileLis = new FileAlterationListenerAdaptor {
        override def onFileChange(file: java.io.File) = {
          subscriber.onNext(file.getName)
        }
      }
      fileObs.addListener(fileLis)
      subscriber.add { log("remove listener"); fileObs.removeListener(fileLis) }
    }
  }

  log(s"first subscribe call")
  val subscription1 = modifiedFiles(".").subscribe(filename => log(s"$filename modified!"))

  Thread.sleep(6000)

  log(s"another subscribe call")
  val subscription2 = modifiedFiles(".").subscribe(filename => log(s"$filename changed!"))

  Thread.sleep(60000)

  log(s"unsubscribed first call")
  subscription1.unsubscribe()

  log(s"unsubscribed second call")
  subscription2.unsubscribe()

  Thread.sleep(6000)

  fileMonitor.stop()
}
