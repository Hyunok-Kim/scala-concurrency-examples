package org.learningconcurrency

import akka.actor.ActorSystem
import com.typesafe.config._

package object ch8 {
  lazy val ourSystem = ActorSystem("OurExampleSystem")

  def remotingConfig(port: Int) = ConfigFactory.parseString(s"""
akka {
  actor {
    provider = cluster
    allow-java-serialization = on
  }
  remote.artery.enabled = false
  remote.classic {
    enabled-transports = ["akka.remote.classic.netty.tcp"]
    netty.tcp {
      hostname = "127.0.0.1"
      port = $port
    }
  }
}
  """)
  def remotingSystem(name: String, port: Int) = ActorSystem(name, remotingConfig(port))
}
