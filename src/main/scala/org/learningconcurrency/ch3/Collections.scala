package org.learningconcurrency
package ch3

object CollectionsBad extends App {
  import scala.collection._

  val buffer = mutable.ArrayBuffer[Int]()

  def add(numbers: Seq[Int]) = execute {
    buffer ++= numbers
    log(s"buffer  = $buffer")
  }

  add(0 until 10)
  add(10 until 20)

  Thread.sleep(500)
}
