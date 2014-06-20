package com.bancoblanco

import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

object AccountInterestTestSpec {
  
}

@RunWith(classOf[JUnitRunner])
class AccountInterestTestSpec extends FlatSpec with Matchers with BeforeAndAfterAll
{
  import Account._
  import AccountInterestTestSpec._
  import com.bancoblanco.SimpleTimeService._

  override def beforeAll: Unit = {}
  override def afterAll: Unit = {}
  
  "A Checking Account" should "have a flat interest rate of 0.1%" in {
    val accountId = Account.store_type + UniqueNumber.generate
    
    Account.add(accountId, Transaction(1234.0))
    val balance1 = sumOfAllTransactions(accountId)
    balance1 should be (1234.0)
    val interest1 = interestOf(CheckingAccount(), balance1)
    interest1 should be (1.234)
    
    Account.add(accountId, Transaction(1000.0))
    val balance2 = sumOfAllTransactions(accountId)
    balance2 should be (2234.0)
    val interest2 = interestOf(CheckingAccount(), balance2)
    interest2 should be (2.234)
    
    Account.add(accountId, Transaction(-2000.0))
    val balance3 = sumOfAllTransactions(accountId)
    balance3 should be (234.0)
    val interest3 = interestOf(CheckingAccount(), balance3)
    interest3 should be (0.234) 
  }
  
  "A Savings Account" should "have a flat interest rate of 0.1% for balance <= 1000 then 0.2%" in {
    val accountId = Account.store_type + UniqueNumber.generate
    
    Account.add(accountId, Transaction(2000))
    val balance1 = sumOfAllTransactions(accountId)
    balance1 should be (2000.0)
    val interest1 = interestOf(SavingsAccount(), balance1)
    val expected_interest1 = 1000 * 0.001 + (2000 - 1000) * 0.002
    interest1 should be (expected_interest1)
    
    Account.add(accountId, Transaction(10000))
    val balance2 = sumOfAllTransactions(accountId)
    balance2 should be (12000.0)
    val interest2 = interestOf(SavingsAccount(), balance2)
    val expected_interest2 = 1000 * 0.001 + (12000 - 1000) * 0.002
    interest2 should be (expected_interest2)
    
    Account.add(accountId, Transaction(-11000.0))
    val balance3 = sumOfAllTransactions(accountId)
    balance3 should be (1000.0)
    val interest3 = interestOf(SavingsAccount(), balance3)
    interest3 should be (1.0) 
    
    Account.add(accountId, Transaction(-800.0))
    val balance4 = sumOfAllTransactions(accountId)
    balance4 should be (200.0)
    val interest4 = interestOf(SavingsAccount(), balance4)
    interest4 should be (0.2) 
  }
  
  "A Super Savings Account" should "have a rate of 2% for the first $1,000 then 5% for the next $1,000 then 10%" in {
    val accountId = Account.store_type + UniqueNumber.generate
    
    Account.add(accountId, Transaction(20000))
    val balance1 = sumOfAllTransactions(accountId)
    balance1 should be (20000.0)
    val interest1 = interestOf(SuperSavingsAccount(), balance1)
    val expected_interest1 = 1000 * 0.02 + 1000 * 0.05 + (20000 - 2000) * 0.1
    interest1 should be (expected_interest1)
    
    Account.add(accountId, Transaction(10000))
    val balance2 = sumOfAllTransactions(accountId)
    balance2 should be (30000.0)
    val interest2 = interestOf(SuperSavingsAccount(), balance2)
    val expected_interest2 = 1000 * 0.02 + 1000 * 0.05 + (30000 - 2000) * 0.1
    interest2 should be (expected_interest2)
    
    Account.add(accountId, Transaction(-28000))
    val balance3 = sumOfAllTransactions(accountId)
    balance3 should be (2000.0)
    val interest3 = interestOf(SuperSavingsAccount(), balance3)
    val expected_interest3 = 1000 * 0.02 + 1000 * 0.05 
    interest3 should be (expected_interest3)
    
    Account.add(accountId, Transaction(-100.0))
    val balance4 = sumOfAllTransactions(accountId)
    balance4 should be (1900.0)
    val interest4 = interestOf(SuperSavingsAccount(), balance4)
    val expected_interest4 = 1000 * 0.02 + 900 * 0.05
    interest4 should be (expected_interest4) 
    
    Account.add(accountId, Transaction(-1000.0))
    val balance5 = sumOfAllTransactions(accountId)
    balance5 should be (900.0)
    val interest5 = interestOf(SuperSavingsAccount(), balance5)
    val expected_interest5 = 900 * 0.02
    interest5 should be (expected_interest5) 
  }

}