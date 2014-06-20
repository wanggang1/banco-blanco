package com.bancoblanco

import akka.actor.{ Props, ActorRef, ActorSystem }
import org.scalatest.{ BeforeAndAfterAll, WordSpecLike }
import akka.testkit.{ TestProbe, ImplicitSender, TestKit }

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

object BankTestSpec {
  val bankId = Bank.store_type + UniqueNumber.generate
  val customerId1 = Customer.store_type + UniqueNumber.generate
  val customerId2 = Customer.store_type + UniqueNumber.generate
  
}

@RunWith(classOf[JUnitRunner])
class BankTestSpec(_system: ActorSystem) extends TestKit(_system) with WordSpecLike with BeforeAndAfterAll with ImplicitSender 
{
  import Bank._
  import BankTestSpec._

  def this() = this(ActorSystem("BankTestSystem"))

  override def afterAll: Unit = system.shutdown()
  
  "A Bank" must {
    "Be able to add customer" in {
      val bank = system.actorOf(Props(classOf[Bank], bankId, "Banco Blanco"))
      val customer = BankCustomer(customerId1, "John Doe")
      bank ! customer
      expectMsg(Done)
      
      val bank2 = system.actorOf(Props(classOf[Bank], bankId, "Banco Blanco"), s"BankActor-$bankId")
      val customer2 = BankCustomer(customerId2, "Jane Doe")
      bank2 ! customer2
      expectMsg(Done)
      
      val allCustomers = Bank.get(bankId)
      assert(allCustomers.size == 2)
      assert(allCustomers.exists( (x) => x.id == customerId1) )
      assert(allCustomers.exists( (x) => x.id == customerId2) )
    }
    
    "Be able to retrieve all customers previous added" in {
      val customers = List(BankCustomer(customerId2, "Jane Doe"), BankCustomer(customerId1, "John Doe"))
      val bank3 = system.actorOf(Props(classOf[Bank], bankId, "Banco Blanco"))
      bank3 ! AllCustomers
      expectMsg(CustomerResult(customers))
    }
  }
  
}