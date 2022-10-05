package org.learningconcurrency
package ch7

object TransactionLocals extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.concurrent.stm._
  import CompositionSortedList._

  val myLog = TxnLocal("")

  def clearList(lst: TSortedList) = atomic { implicit txn =>
    while (lst.head() != null) {
      myLog() = myLog() + "\nremoved " + lst.head().elem
      lst.head() = lst.head().next()
    }
  }

  val myList = new TSortedList().insert(14).insert(22)
  def clearWithLog(): String = atomic { implicit txn =>
    clearList(myList)
    myLog()
  }

  val f = Future { clearWithLog() }
  val g = Future { clearWithLog() }
  for (h1 <- f; h2 <- g) {
    log(s"Log for f: $h1\nLog for g: $h2")
  }
}

object TransactionalArray extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.concurrent.stm._

  val pages = Seq.fill(5)("Scala 2.10 is out, " * 7)
  val website = TArray(pages)

  def replace(pat: String, txt: String): Unit = atomic { implicit txn =>
    for (i <- 0 until website.length) website(i) = website(i).replace(pat, txt)
  }

  def asString = atomic { implicit txn =>
    var s: String = ""
    for (i <- 0 until website.length) s += s"Page $i\n======\n${website(i)}\n\n"
    s
  }

  Future { replace("2.10", "2.11") }
  Thread.sleep(30)
  Future { replace("2.11", "2.12") }
  Thread.sleep(250)
  Future { log(s"Document\n$asString") }
}

object TransactionalMap extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.concurrent.stm._

  val tmap = TMap("a" -> 1, "B" -> 2, "C" -> 3)

  Future {
    atomic { implicit txn =>
      tmap("A") = 1
      tmap.remove("a")
    }
  }
  Thread.sleep(10)
  Future {
    val snap = tmap.single.snapshot
    log(s"atomic snapshot: $snap")
  }
}
