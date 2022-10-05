package org.learningconcurrency
package ch4

object FuturesComputation extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  
  Future {
    log(s"the future is here")
  }

  log(s"the future is coming")
}

object FuturesDataType extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source

  val buildFile: Future[String] = Future {
    val f = Source.fromFile("build.sbt")
    try f.getLines().mkString("\n") finally f.close()
  }

  log(s"started reading build file asynchronously")
  log(s"status: ${buildFile.isCompleted}")
  Thread.sleep(250)
  log(s"status: ${buildFile.isCompleted}")
  log(s"status: ${buildFile.value}")
}

object FuturesCallbacks extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source

  def getUrlSpec(): Future[Seq[String]] = Future {
    val f = Source.fromURL("http://www.w3.org/Addressing/URL/url-spec.txt")
    try f.getLines().toList finally f.close()
  }

  val urlSpec: Future[Seq[String]] = getUrlSpec()

  def find(lines: Seq[String], word: String) = lines.zipWithIndex collect {
    case (line, n) if line.contains(word) => (n, line)
  } mkString("\n")

  urlSpec foreach {
    lines => log(s"Found occurences of 'telnet'\n${find(lines, "telnet")}\n")
  }

  urlSpec foreach {
    lines => log(s"Found occurrences of 'password'\n${find(lines, "password")}\n")
  }

  log("callbacks intalled, continuing with other work")
}

object FuturesFailure extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source
  import scala.util.{Success,Failure}

  val urlSpec: Future[String] = Future {
    Source.fromURL("http://www.w3.org/non-existent-url-spec.txt").mkString
  }

  /*
  urlSpec.failed foreach {
    case t => log(s"exception occured - $t")
  }
  */
  urlSpec onComplete {
    case Success(_) =>
    case Failure(t) => log(s"exception occured - $t")
  }
}

object FuturesTry extends App {
  import scala.util._

  val threadName: Try[String] = Try(Thread.currentThread.getName)
  val someText: Try[String] = Try("Try objects are created synchronously")
  val message: Try[String] = for {
    tn <- threadName
    st <- someText
  } yield s"$st, t = $tn"

  message match {
    case Success(msg) => log(msg)
    case Failure(error) => log(s"There should be no $error here.")
  }
}

object FuturesNonFatal extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.util.control.NonFatal

  val f = Future { throw new InterruptedException }
  val g = Future { throw new IllegalArgumentException }
  //f.failed foreach { case t => log(s"error - $t") }
  //g.failed foreach { case t => log(s"error - $t") }
  f.failed foreach { case NonFatal(t) => log(s"error - $t") }
  g.failed foreach { case NonFatal(t) => log(s"error - $t") }
  Thread.sleep(1000)
}

object FuturesClumsyCallback extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import org.apache.commons.io.FileUtils._
  import java.io._
  import scala.io.Source
  import scala.jdk.CollectionConverters._

  def blacklistFile(filename: String) = Future {
    val lines = Source.fromFile(filename).getLines()
    lines.filter(!_.startsWith("#")).toList
  }

  def findFiles(patterns: List[String]): List[String] = {
    val root = new File(".")
    for {
      f <- iterateFiles(root, null, true).asScala.toList
      pat <- patterns
      abspat = root.getCanonicalPath + File.separator + pat
      if f.getCanonicalPath.contains(abspat)
    } yield f.getCanonicalPath
  }

  blacklistFile(".gitignore") foreach {
    case lines =>
      val files = findFiles(lines)
      log(s"matches: ${files.mkString("\n")}")
  }
}

object FuturesMap extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source
  import scala.util.Success

  val buildFile = Future { Source.fromFile("build.sbt").getLines() }
  val gitignoreFile = Future { Source.fromFile(".gitignore-SAMPLE").getLines() }

  val longestBuildLine = buildFile.map(lines => lines.maxBy(_.length))
  val longestGitignoreLine = for (lines <- gitignoreFile) yield lines.maxBy(_.length)

  longestBuildLine onComplete {
    case Success(line) => log(s"the longest line is '$line'")
  }

  longestGitignoreLine.failed foreach {
    case t => log(s"no longest line, because ${t.getMessage}")
  }
}

object FuturesFlatMapRaw extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source

  val netiquette = Future { Source.fromURL("https://www.ietf.org/rfc/rfc1855.txt").mkString }
  val urlSpec = Future { Source.fromURL("http://www.w3.org/Addressing/URL/url-spec.txt").mkString }
  val answer = netiquette.flatMap { nettext =>
    urlSpec.map { urltext =>
      "First, read this: " + nettext + ". Now, try this: " + urltext
    }
  }

  answer foreach {
    case contents => log(contents)
  }
}

object FuturesFlatMap extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source

  val netiquette = Future { Source.fromURL("https://www.ietf.org/rfc/rfc1855.txt").mkString }
  val urlSpec = Future { Source.fromURL("http://www.w3.org/Addressing/URL/url-spec.txt").mkString }
  val answer = for {
    nettext <- netiquette
    urltext <- urlSpec
  } yield {
    "First of all, read this: " + nettext + " Once you're done, try this: " + urltext
  }

  answer foreach {
    case contents => log(contents)
  }
}

object FuturesDifferentFlatMap extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source

  val answer = for {
    nettext <- Future { Source.fromURL("https://www.ietf.org/rfc/rfc1855.txt").mkString }
    urltext <- Future { Source.fromURL("http://www.w3.org/Addressing/URL/url-spec.txt").mkString }
  } yield {
    "First of all, read this: " + nettext + " Once you're done, try this: " + urltext
  }

  answer foreach {
    case contents => log(contents)
  }
}

object FuturesRecover extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source

  val netiquetteUrl = "https://www.ietf.org/rfc/rfc1855.doc"
  val netiquette = Future { Source.fromURL(netiquetteUrl).mkString } recover {
    case f: java.io.FileNotFoundException =>
      "Dear boss, thank you for your e-mail." +
      "You might be interested to know that ftp links " +
      "can also point to regular files we keep on our servers."
  }

  netiquette foreach {
    case contents => log(contents)
  }
}
