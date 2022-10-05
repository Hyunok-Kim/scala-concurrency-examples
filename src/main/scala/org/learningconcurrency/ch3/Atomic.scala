package org.learningconcurrency
package ch3

object AtomicUid extends App {
  import java.util.concurrent.atomic._
  private val uid = new AtomicLong(0L)

  def getUniqueId(): Long = uid.incrementAndGet()

  execute {
    log(s"Got a unique id asynchronously: ${getUniqueId()}")
  }

  log(s"Got a unique id: ${getUniqueId()}")
}

object AtomicUidCAS extends App {
  import java.util.concurrent.atomic._
  import scala.annotation.tailrec
  private val uid = new AtomicLong(0L)

  @tailrec def getUniqueId(): Long = {
    val oldUid = uid.get
    val newUid = oldUid + 1
    if (uid.compareAndSet(oldUid, newUid)) newUid
    else getUniqueId()
  }

  execute {
    log(s"Get a unique id asynchronously: ${getUniqueId()}")
  }

  log(s"Got a unique id: ${getUniqueId()}")
}

object AtomicLock extends App {
  import java.util.concurrent.atomic._
  private val lock = new AtomicBoolean(false)
  def mySynchronized(body: =>Unit): Unit = {
    while (!lock.compareAndSet(false, true)) {}
    try body
    finally lock.set(false)
  }

  var count = 0
  for (i <- 0 until 10) execute {
    mySynchronized { count += 1 }
  }
  Thread.sleep(1000)
  log(s"Count is: $count")
}
