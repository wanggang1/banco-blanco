package com.bancoblanco

import akka.actor.{Actor, ActorLogging, ActorRef}

trait AccountType {
  val typeName: String
}

case class CheckingAccount(typeName: String = "Checking") extends AccountType
case class SavingsAccount(typeName: String = "Savings") extends AccountType
case class SuperSavingsAccount(typeName: String = "MaxiSavings") extends AccountType

object Account extends InMemoryStore[Transaction] {
  
  val store_type = "account"
  
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
    
  private def sumOfTransactions(predicate: Double => Boolean)(acctId: String) = {
    val total = get(acctId).map(_.amount).filter(predicate).foldLeft(0.0)(_ + _)
    total
  }
  val sumOfDeposits = sumOfTransactions((x: Double) => x > 0) _
  val sumOfWithdraws = sumOfTransactions((x: Double) => x < 0) _
  val sumOfAllTransactions = sumOfTransactions((x: Double) => true) _
  
  case class Deposit(amount: Double) {require(amount > 0)}
  case class Withdraw(amount: Double) {require(amount > 0)}
  case object Statement
  case class StatementResult(statement: String)
  case object Done
  case object Failed
}

class Account(acctId: String, acctType: AccountType) extends Actor with ActorLogging {
  import com.bancoblanco.SimpleTimeService._
  import Account._
  
  var balance = sumOfAllTransactions(acctId)
  
  def receive = {
    case Deposit(amount) => deposit(amount)
                            sender ! Done
    case Withdraw(amount) => if ( withdraw(amount) ) sender ! Done
                             else sender ! Failed
    case Statement => val statement = generateStatement()
                      sender ! StatementResult(statement)
    case _ => sender ! Failed
  }
  
  private def deposit(amount: Double) = {
    add(acctId, Transaction(amount))
    balance += amount
    log.info("Account: {}, Deposit amount: {}, Final balance {}.", acctId, amount, balance)
  }
  
  private def withdraw(amount: Double) = {
    if (amount > balance) false
    else {
      add(acctId, Transaction(-amount))
      balance -= amount
      log.info("Account: {}, Withdraw amount: {}, Final balance: {}.", acctId, amount, balance)
      true
    }
  }
  
  private def generateStatement() = {
    val interest = interestOf(acctType, balance)
    val total_deposits = sumOfDeposits(acctId)
    val total_withdrals = -sumOfWithdraws(acctId)
    val total = balance
    
    "Statement"
  }
}