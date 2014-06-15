package com.bancoblanco

trait AccountType {
  val typeName: String
}

case class CheckingAccount(typeName: String = "Checking") extends AccountType
case class SavingsAccount(typeName: String = "Savings") extends AccountType
case class SuperSavingsAccount(typeName: String = "MaxiSavings") extends AccountType

object Account extends InMemoryStore[Transaction] {
  
  override val store_type = "account"
  
  def interestOf(acctType: AccountType, amount: Double): Double = acctType match {
    case _: CheckingAccount => checkingInterest(amount)
    case _: SavingsAccount => savingsInterest(amount)
    case _: SuperSavingsAccount => superSavingsInterest(amount)
  }
  
  private def checkingInterest(amount: Double) = {
    require(amount >= 0)
    amount * 0.001
  }
  
  private def savingsInterest(amount: Double): Double = {
    require(amount >= 0)
    if (amount <= 1000) 
      amount * 0.001
    else
      savingsInterest(1000) + (amount - 1000) * 0.002
  }
  
  private def superSavingsInterest(amount: Double): Double = {
    require(amount >= 0) 
    if (amount <= 1000) 
      amount * 0.02
    else if (amount > 1000 && amount <= 2000) 
      superSavingsInterest(1000) + (amount - 1000) * 0.05
    else
      superSavingsInterest(2000) + (amount - 2000) * 0.1
  }
}

class Account(acctId: String, acctType: AccountType) {
  import Account._
  
  //deposit
  //withdraw
  //balance
  //interest owed
  
  var balance = get(acctId).map(_.amount).foldLeft(0.0)(_ + _)
  
  private def deposit(amount: Double) = {
    require(amount >= 0)
    balance += amount
    //TODO: add deposit transaction
  }
  
  private def withdraw(amount: Double) = {
    require(amount >= 0)
    balance -= amount
    //TODO: add withdraw transaction
  }
}