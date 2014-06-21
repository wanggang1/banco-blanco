package com.bancoblanco

import akka.actor.{Actor, ActorRef, Props, ActorLogging, ReceiveTimeout}
import scala.concurrent.duration._

object Customer extends InMemoryStore[BankAccount] {
  val store_type = "customer"

  def singleStatement(acctTypeName: String, total: Double, deposit: Double, withdraw: Double, interest: Double) = {
    val formatted = s"""$acctTypeName account
  deposit USD$deposit
  withdraw USD$withdraw
total USD$total
Interest Owed USD$interest"""
    formatted.replace("USD", "$")
  }
  
  def format(statements: List[String], customerId: String, total: Double) = {
    val withHeader = s"Statement for $customerId" :: statements
    val lineTotal = s"Total In All Accounts USD$total".replace("USD", "$")
    (withHeader ::: List(lineTotal)).mkString("\n\n")
  }
  
  case class Deposit(acctId: String, amount: Double)
  case class Withdraw(acctId: String, amount: Double)
  case class Transfer(fromAcctId: String, toAcctId: String, amount: Double)
  case class StatementResult(statement: String)
  case object Statement
  case object Done
  case object Failed
}

class Customer(customerId: String) extends Actor with ActorLogging {
  import Customer._
  
  context.setReceiveTimeout(30.seconds)
  
  val has_account = hasValue(customerId) _
  val get_account = getValue(customerId) _
  
  var numberOfAccount = 0
  var statements = List[String]()
  var totalInStatements = 0.0
  var client: ActorRef = _
  
  def receive = {
    case acct: BankAccount => acct match {
      case BankAccount(acctId, _) if (has_account( (acct: BankAccount) => acct.id == acctId ) ) => sender ! Failed
      case _ => add(customerId, acct); sender ! Done
    }
    case Deposit(acctId, amount) => {
      if ( !has_account( (acct: BankAccount) => acct.id == acctId) ) sender ! Failed
      log.info("Deposit request for account {}, amount {}", acctId, amount)

      getChildActor(acctId) ! Account.Deposit(amount)
      sender ! Done
    }
    case Withdraw(acctId, amount) => {
      if ( !has_account( (acct: BankAccount) => acct.id == acctId ) ) sender ! Failed
      log.info("Withdraw request for account {}, amount {}", acctId, amount)

      getChildActor(acctId) ! Account.Withdraw(amount)
      client = sender
    }
    case Account.WithdrawResult(success) => if (success) client ! Done else client ! Failed
    case Transfer(fromAcctId, toAcctId, amount) => {
      if ( !has_account( (acct: BankAccount) => acct.id == fromAcctId) || 
          !has_account( (acct: BankAccount) => acct.id == toAcctId)) sender ! Failed
      log.info("Transfer request from account {} to account {}, amount {}", fromAcctId, toAcctId, amount)
      
      val uid = UniqueNumber.generate
      val teller = context.actorOf(tellerProps, s"TellerActor-$uid")
      val fromAcct = getChildActor(fromAcctId)
      val toAcct = getChildActor(toAcctId)
      
      teller ! Teller.Transfer(fromAcct, toAcct, amount)
      client = sender
    }
    case Teller.TransferResult(success) => if (success) client ! Done else client ! Failed
    case Statement => {
      log.info("Statement request")
      val accounts = get(customerId)
      numberOfAccount = accounts.size
      totalInStatements = 0
      statements = Nil
      client = sender
      
      for (acct <- get(customerId)) {
        getChildActor(acct.id) ! Account.Statement
      }
    }
    case Account.StatementResult(acctType, total, deposit, withdraw, interest) => {
      log.info("Statement resunt: {}", acctType.typeName)
      statements = singleStatement(acctType.typeName, total, deposit, withdraw, interest) :: statements
      totalInStatements += total
      if (statements.size == numberOfAccount) client ! StatementResult( format(statements, customerId, totalInStatements) )
    }
    case ReceiveTimeout => {
      log.info("ReceiveTimeout, stop self and all children")
      context stop self
    }
  }

  def accountProps(acctId: String) = Props(classOf[Account], acctId, get_account( (acct: BankAccount) => acct.id == acctId ).acctType)
  def tellerProps = Props[Teller]
  
  private def getChildActor(acctId: String): ActorRef = {
    context.child("AccountActor-" + acctId) match {
      case Some(actor) => actor
      case None => {
        log.info("Create actor for: {}", acctId)
        context.actorOf(accountProps(acctId), s"AccountActor-$acctId")
      }
    }
  }
  
}
