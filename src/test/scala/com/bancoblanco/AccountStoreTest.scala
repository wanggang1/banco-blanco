package com.bancoblanco

import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

object AccountStoreTestSpec {
  val accountId = Account.store_type + UniqueNumber.generate
}

@RunWith(classOf[JUnitRunner])
class AccountStoreTestSpec extends FlatSpec with Matchers with BeforeAndAfterAll
{
  import Account._
  import AccountStoreTestSpec._
  import com.bancoblanco.SimpleTimeService._

  override def beforeAll: Unit = {}
  override def afterAll: Unit = {}
  
  "A Account store" should "be able to add Transaction" in {
    Account.add(accountId, Transaction(1000.0))
    Account.add(accountId, Transaction(100.0))
    Account.add(accountId, Transaction(-300.0))
    Account.add(accountId, Transaction(50.0))
    val allTransactions = Account.get(accountId)
    allTransactions.size should be (4)
  }

  it should "return empty Transaction list if customerId does not exist" in {
    val allTransactions = Account.get("account_abc")
    allTransactions should be (Nil)
  }
}