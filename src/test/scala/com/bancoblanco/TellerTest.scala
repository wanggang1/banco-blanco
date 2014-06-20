package com.bancoblanco

import scala.concurrent.duration._
import akka.actor.{Actor, Props, ActorRef, ActorSystem }
import org.scalatest.{ BeforeAndAfterAll, WordSpecLike }
import akka.testkit.{ TestProbe, ImplicitSender, TestKit }
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

object TellerTestSpec {
  import Account._
  
  class FakeAcount extends Actor {
    import context.dispatcher
    def receive = {
      case Account.Withdraw(amount) => {
        context.system.scheduler.scheduleOnce(0.5.second, sender, WithdrawResult(true))
        context stop self
      }
      case Account.Deposit(amount) => {
        context.system.scheduler.scheduleOnce(0.5.second, sender, Account.Done)
        context stop self
      }
    }
  }
  
  class FakeFailedAcount extends Actor {
    import context.dispatcher
    def receive = {
      case Account.Withdraw(amount) => {
        context.system.scheduler.scheduleOnce(0.5.second, sender, WithdrawResult(false))
        context stop self
      }
    }
  }
  
}

@RunWith(classOf[JUnitRunner])
class TellerTestSpec(_system: ActorSystem) extends TestKit(_system) with WordSpecLike with BeforeAndAfterAll with ImplicitSender 
{
  import Teller._
  import TellerTestSpec._

  def this() = this(ActorSystem("TellerTestSystem"))

  override def afterAll: Unit = system.shutdown()
  
  "A Teller" can {
    "transfer money between 2 accounts" in {
      val acctFrom = system.actorOf(Props[FakeAcount])
      val acctTo = system.actorOf(Props[FakeAcount])
      val teller = system.actorOf(Props[Teller])
      teller ! Transfer(acctFrom, acctTo, 123.45)
      expectMsg(TransferResult(true))
    }
  }

  "A Teller" should {
    "fail when failing to withdraw money" in {
      val acctFrom = system.actorOf(Props[FakeFailedAcount])
      val acctTo = system.actorOf(Props[FakeAcount])
      val teller = system.actorOf(Props[Teller])
      teller ! Transfer(acctFrom, acctTo, 123.45)
      expectMsg(TransferResult(false))
    }
  }
  
}