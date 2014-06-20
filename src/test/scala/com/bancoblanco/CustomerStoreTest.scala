package com.bancoblanco

import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

object CustomerStoreTestSpec {
  val customerId = Customer.store_type + UniqueNumber.generate
  val acctId1 = Account.store_type + UniqueNumber.generate
  val acctId2 = Account.store_type + UniqueNumber.generate
  val acctId3 = Account.store_type + UniqueNumber.generate
  val account1 = BankAccount(acctId1, CheckingAccount())
  val account2 = BankAccount(acctId2, SavingsAccount())
  val account3 = BankAccount(acctId3, SuperSavingsAccount())
  
  val account1ByID = (acct: BankAccount) => acct.id == acctId1
}

@RunWith(classOf[JUnitRunner])
class CustomStoreTestSpec extends FlatSpec with Matchers with BeforeAndAfterAll
{
  import Customer._
  import CustomerStoreTestSpec._

  override def beforeAll: Unit = {}
  override def afterAll: Unit = {}
  
  "A Customer store" should "be able to add BankAccount" in {
    Customer.add(customerId, account1)
    Customer.add(customerId, account2)
    Customer.add(customerId, account3)
    val allAccounts = Customer.get(customerId)
    allAccounts.size should be (3)
  }

  it should "return empty BankAccount list if customerId does not exist" in {
    val allAccounts = Customer.get("customer_abc")
    allAccounts should be (Nil)
  }
  
  it should "return BankAccount by ID" in {
    val account = Customer.getValue(customerId)( account1ByID )
    account should be (account1)
  }
  
  it should "return true if a customer exists by ID" in {
    val flag = Customer.hasValue(customerId)( account1ByID )
    flag should be (true)
  }
}