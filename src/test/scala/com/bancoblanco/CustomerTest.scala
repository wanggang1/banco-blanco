package com.bancoblanco

import scala.concurrent.duration._
import akka.actor.{Actor, Props, ActorRef, ActorSystem }
import org.scalatest.{ BeforeAndAfterAll, WordSpecLike, ConfigMap }
import akka.testkit.{ TestProbe, ImplicitSender, TestKit }
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

object CustomerTestSpec {
  val customerId = Customer.store_type + UniqueNumber.generate
  val acctId1 = Account.store_type + UniqueNumber.generate
  val acctId2 = Account.store_type + UniqueNumber.generate
  val acctId3 = Account.store_type + UniqueNumber.generate
  
  class FakeAccountActor extends Actor {
    import context.dispatcher
    import Account._
    
    def receive = {
      case Deposit(amount) => context.system.scheduler.scheduleOnce(1.second, sender, Done)
      case Withdraw(10000.0) => context.system.scheduler.scheduleOnce(1.second, sender, WithdrawResult(false))
      case Withdraw(amount) => context.system.scheduler.scheduleOnce(1.second, sender, WithdrawResult(true))
      case Statement => context.system.scheduler.scheduleOnce(1.second, sender, StatementResult("Statement"))
    }
  }
  
  class FakeTellerActor extends Actor {
    import context.dispatcher
    
    def receive = {
      case Teller.Transfer(from, to, amount) => context.system.scheduler.scheduleOnce(1.second, sender, Teller.TransferResult(true))
    }
  }
  
  def fakeCustomerProps = Props(new Customer(customerId){
      override def accountProps(acctId: String) = Props[FakeAccountActor]
      override def tellerProps = Props[FakeTellerActor]
    })
}

@RunWith(classOf[JUnitRunner])
class CustomerTestSpec(_system: ActorSystem) extends TestKit(_system) with WordSpecLike with BeforeAndAfterAll with ImplicitSender 
{
  import Customer._
  import CustomerTestSpec._

  def this() = this(ActorSystem("CustomerTestSystem"))

  override def beforeAll: Unit = {
    println("beforeAll")
    Customer.add(customerId, BankAccount(acctId1, CheckingAccount()))
    Customer.add(customerId, BankAccount(acctId2, SavingsAccount()))
  }
  
  override def afterAll: Unit = {
    println("afterAll")
    system.shutdown()
  }
  
  "A customer" can {
    "only opne an accout with unique id" in {
      val customer = system.actorOf(Props(classOf[Customer], customerId), s"CustomerActor-$customerId")
      val account = BankAccount(acctId3, SuperSavingsAccount())
      customer ! account
      expectMsg(Done)
      
      val account1 = BankAccount(acctId2, SuperSavingsAccount())
      customer ! account1
      expectMsg(Failed)
      
      //Total 3 accounts
      assert(3 == Customer.get(customerId).size)
    }
  }
  
  "A customer" can {
    "do deposit" in {
      val customer = system.actorOf(fakeCustomerProps)
      customer ! Deposit(acctId1, 1000.0)
      expectMsg(Done)
    }
  } 
  
  "A customer" can {
    "do withdraw" in {
      val customer = system.actorOf(fakeCustomerProps)
      customer ! Withdraw(acctId1, 100.0)
      expectMsg(Done)
    }
    
    "not overdraw" in {
      val customer = system.actorOf(fakeCustomerProps)
      customer ! Withdraw(acctId1, 10000.0)
      expectMsg(Failed)
    }
  }
  
  "A customer" can {
    "do transfer" in {
      val customer = system.actorOf(fakeCustomerProps)
      customer ! Transfer(acctId1, acctId2, 50.0)
      expectMsg(Done)
    }
  }
  
  "A customer" can {
    "request statement" in {
      val customer = system.actorOf(fakeCustomerProps)
      customer ! Statement
      expectMsg(StatementResult("Statement,Statement,Statement"))
    }
  }

}