package com.bancoblanco

import scala.concurrent.duration._
import akka.actor.{Actor, Props, ActorRef, ActorSystem }
import org.scalatest.{ BeforeAndAfterAll, WordSpecLike, ConfigMap }
import akka.testkit.{ TestProbe, ImplicitSender, TestKit }
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

object TransactionSpec {
  val customerId = Customer.store_type + UniqueNumber.generate
  val acctId1 = Account.store_type + UniqueNumber.generate
  val acctId2 = Account.store_type + UniqueNumber.generate
  val acctId3 = Account.store_type + UniqueNumber.generate
}

@RunWith(classOf[JUnitRunner])
class TransactionSpec(_system: ActorSystem) extends TestKit(_system) with WordSpecLike with BeforeAndAfterAll with ImplicitSender 
{
  import Customer._
  import TransactionSpec._

  def this() = this(ActorSystem("TransactionTestSystem"))
  
  var customer: ActorRef = _

  override def beforeAll: Unit = {
    println("beforeAll")
    Customer.add(customerId, BankAccount(acctId1, CheckingAccount()))
    Customer.add(customerId, BankAccount(acctId2, SavingsAccount()))
    Customer.add(customerId, BankAccount(acctId3, SuperSavingsAccount()))
    customer = system.actorOf(Props(classOf[Customer], customerId), s"CustomerActor-$customerId")
  }
  
  override def afterAll: Unit = {
    println("afterAll")
    system.shutdown()
  }
  
  "A customer" can {
    "deposit to different account" in {
      customer ! Deposit(acctId1, 1000.0)
      expectMsg(Done)
      customer ! Deposit(acctId2, 2000.0)
      expectMsg(Done)
      customer ! Deposit(acctId3, 10000.0)
      expectMsg(Done)
    }
    
    "withdraw from an accout" in {
      customer ! Withdraw(acctId1, 100.0)
      expectMsg(Done)
    }
    
    "not overdrawn" in {
      val account = BankAccount(acctId3, SuperSavingsAccount())
      customer ! Withdraw(acctId2, 3000.0)
      expectMsg(Failed)
    }
    
    "transfer $50" in {
      customer ! Transfer(acctId1, acctId2, 50.0)
      expectMsg(Done)
    }
    
    "not transfer $1001" in {
      customer ! Transfer(acctId1, acctId2, 1001)
      expectMsg(Failed)
    }
  }

}