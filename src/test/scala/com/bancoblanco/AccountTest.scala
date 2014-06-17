package com.bancoblanco

import scala.concurrent.duration._
import akka.actor.{Actor, Props, ActorRef, ActorSystem }
import org.scalatest.{ BeforeAndAfterAll, WordSpecLike }
import akka.testkit.{ TestProbe, ImplicitSender, TestKit }
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AccountTestSpec(_system: ActorSystem) extends TestKit(_system) with WordSpecLike with BeforeAndAfterAll with ImplicitSender 
{
  import Teller._
  import TellerTestSpec._

  def this() = this(ActorSystem("TellerTestSystem"))

  override def afterAll: Unit = system.shutdown()
  
}