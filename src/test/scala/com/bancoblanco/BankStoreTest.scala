package com.bancoblanco

import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

object BankStoreTestSpec {
  val bankId = Bank.store_type + UniqueNumber.generate
  val customerId1 = Customer.store_type + UniqueNumber.generate
  val customerId2 = Customer.store_type + UniqueNumber.generate
  val customer1 = BankCustomer(customerId1, "John Doe")
  val customer2 = BankCustomer(customerId2, "Jane Doe")
  
  val customer1ByID = (cust: BankCustomer) => cust.id == customerId1
}

@RunWith(classOf[JUnitRunner])
class BankStoreTestSpec extends FlatSpec with Matchers with BeforeAndAfterAll
{
  import Bank._
  import BankStoreTestSpec._

  override def beforeAll: Unit = {}
  override def afterAll: Unit = {}
  
  "A Bank store" should "be able to add BankCustomer" in {
    Bank.add(bankId, customer1)
    Bank.add(bankId, customer2)
    val allCustomers = Bank.get(bankId)
    allCustomers.size should be (2)
  }

  it should "return empty BankCustomer list if bankId does not exist" in {
    val allCustomers = Bank.get("bank_xyz")
    allCustomers should be (Nil)
  }
  
  it should "return BankCustomer by ID" in {
    val customer = Bank.getValue(bankId)( customer1ByID )
    customer should be (customer1)
  }
  
  it should "return true if a customer exists by ID" in {
    val flag = Bank.hasValue(bankId)( customer1ByID )
    flag should be (true)
  }
}