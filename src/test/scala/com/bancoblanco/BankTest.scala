package com.bancoblanco

import akka.actor.{ Props, ActorRef, ActorSystem }
import org.scalatest.{ BeforeAndAfterAll, WordSpecLike }
import akka.testkit.{ TestProbe, ImplicitSender, TestKit }

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BankTestSuite(_system: ActorSystem) extends TestKit(_system) with WordSpecLike with BeforeAndAfterAll with ImplicitSender 
{
  import Bank._

  def this() = this(ActorSystem("BankTestSystem"))

  override def afterAll: Unit = system.shutdown()
  
  "A Bank" must {
    "Ba able to add customer" in {
      val bank = system.actorOf(Props(classOf[Bank], "bank1", "Banco Blanco"))
      val customer = BankCustomer("customer1", "John Doe")
      bank ! customer
      expectMsg(Done)
      
      val bank2 = system.actorOf(Props(classOf[Bank], "bank1", "Banco Blanco"))
      val customer2 = BankCustomer("customer2", "Jane Doe")
      bank2 ! customer2
      expectMsg(Done)
      
      val allCustomers = Bank.get("bank1")
      assert(allCustomers.size == 2)
      assert(allCustomers.exists( (x) => x.id == "customer1") )
      assert(allCustomers.exists( (x) => x.id == "customer1") )
    }
    
    "Ba able to retrieve all customers" in {
      val customers = List(BankCustomer("customer2", "Jane Doe"), BankCustomer("customer1", "John Doe"))
      val bank = system.actorOf(Props(classOf[Bank], "bank1", "Banco Blanco"))
      bank ! AllCustomers
      expectMsg(CustomerResult(customers))
    }
  }
  
}