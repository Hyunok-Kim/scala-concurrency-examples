package org.learningconcurrency
package ch3

object LazyValsCreate extends App {
  import scala.concurrent._

  lazy val obj = new AnyRef
  lazy val nondeterministic = s"made by ${Thread.currentThread.getName}"

  execute {
    log(s"Execution context thread sees object = $obj")
    log(s"Execution context thread sees nondeterministic = $nondeterministic")
  }

  log(s"Main thread sees object = $obj")
  log(s"Main thread sees nondeterministic = $nondeterministic")
}

object LazyValsObject extends App {
  object Lazy {
    log("Running Lazy constructor.")
  }

  log("Main thread is about to reference Lazy.")
  Lazy
  log("Main thread completed.")
}

object LazyValsUnderTheHood extends App {
  @volatile private var _bitmap = false
  private var _obj: AnyRef = _
  def obj = if (_bitmap) _obj else this.synchronized {
    if (!_bitmap) {
      _obj = new AnyRef
      _bitmap = true
    }
    _obj
  }

  log(s"$obj"); log(s"$obj")
}

object LazyValsDeadlock extends App {
  object A {
    lazy val x: Int = B.y
  }
  object B {
    lazy val y: Int = A.x
  }

  execute { B.y }
  A.x
}

object LazyValsAndBlocking extends App {
  lazy val x: Int = {
    val t = ch2.thread {
      println(s"Initializing $x.")
    }
    t.join()
    1
  }
  x
}

object LazyValsAndMonitor extends App {
  lazy val x = 1
  this.synchronized {
    val t = ch2.thread { x }
    t.join()
  }
}
