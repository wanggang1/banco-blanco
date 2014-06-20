package com.bancoblanco

import scala.concurrent.duration._
import akka.actor.{Actor, Props, ActorRef, ActorSystem }
import org.scalatest.{ BeforeAndAfterAll, WordSpecLike }
import akka.testkit.{ TestProbe, ImplicitSender, TestKit }
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

object AccountTestSpec {
  val acctId = Account.store_type + UniqueNumber.generate

}

@RunWith(classOf[JUnitRunner])
class AccountTestSpec(_system: ActorSystem) extends TestKit(_system) with WordSpecLike with BeforeAndAfterAll with ImplicitSender 
{
  import Account._
  import AccountTestSpec._

  def this() = this(ActorSystem("AccountTestSystem"))

  override def afterAll: Unit = system.shutdown()
  
  "An Account" can {
    "do deposit" in {
      val account = system.actorOf(Props(classOf[Account], acctId, CheckingAccount()), s"AccountActor-$acctId")
      account ! Deposit(500.0)
      expectMsg(Done)
      
      account ! Deposit(150.0)
      expectMsg(Done)
      
      assert(sumOfDeposits(acctId) == 650.0)
    }
    
    "do withdraw" in {
      val account = system.actorOf(Props(classOf[Account], acctId, CheckingAccount()))
      account ! Withdraw(200.0)
      expectMsg(WithdrawResult(true))
      
      account ! Withdraw(200.0)
      expectMsg(WithdrawResult(true))

      assert(sumOfWithdraws(acctId) == -400.0)
    }
    
    "not be overdrawn" in {
      val account = system.actorOf(Props(classOf[Account], acctId, CheckingAccount()))
      account ! Withdraw(251.0)
      expectMsg(WithdrawResult(false))
    }
  }
  
  "An Account" should {
    "keep all transactions" in {
      assert(sumOfAllTransactions(acctId) == 250.0)
    }
    
    "provide statement" in {
      val balance = 250.0
      val deposit = 650.0
      val withdraw = 400.0
      val interest = interestOf(CheckingAccount(), balance)
      val account = system.actorOf(Props(classOf[Account], acctId, CheckingAccount()))
      account ! Statement
      expectMsg(StatementResult(CheckingAccount(), balance, deposit, withdraw, interest))
    }
  }
}