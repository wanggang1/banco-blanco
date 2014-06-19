package com.bancoblanco

import akka.actor.{Actor, ActorRef, Props, ActorLogging, ReceiveTimeout}
import scala.concurrent.duration._

object Customer extends InMemoryStore[BankAccount] {
  val store_type = "customer"
  
  case class Deposit(acctId: String, amount: Double)
  case class Withdraw(acctId: String, amount: Double)
  case class Transfer(fromAcctId: String, toAcctId: String, amount: Double)
  case class StatementResult(statement: String)
  case object Statement
  case object Done
  case object Failed
  
  def hasAccount(customerId: String)(acctId: String) = get(customerId).exists((x) => x.id == acctId)
  def getAccount(customerId: String)(acctId: String) = get(customerId).filter((x) => x.id == acctId)(0)
}

class Customer(customerId: String) extends Actor with ActorLogging {
  import Customer._
  
  context.setReceiveTimeout(30.seconds)
  
  val has_account = hasAccount(customerId) _
  val get_account = getAccount(customerId) _
  
  var numberOfAccount = 0
  var statements = List[String]()
  var client: ActorRef = _
  
  def receive = {
    case acct: BankAccount => acct match {
      case BankAccount(acctId, _) if (has_account(acctId)) => sender ! Failed
      case _ => add(customerId, acct); sender ! Done
    }
    case Deposit(acctId, amount) => {
      if ( !has_account(acctId) ) sender ! Failed
      log.info("Deposit request for account {}, amount {}", acctId, amount)

      getChildActor(acctId) ! Account.Deposit(amount)
      sender ! Done
    }
    case Withdraw(acctId, amount) => {
      if ( !has_account(acctId) ) sender ! Failed
      log.info("Withdraw request for account {}, amount {}", acctId, amount)

      getChildActor(acctId) ! Account.Withdraw(amount)
      client = sender
    }
    case Account.WithdrawResult(success) => if (success) client ! Done else client ! Failed
    case Transfer(fromAcctId, toAcctId, amount) => {
      if ( !has_account(fromAcctId) || !has_account(toAcctId)) sender ! Failed
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
      statements = Nil
      client = sender
      
      for (acct <- get(customerId)) {
        getChildActor(acct.id) ! Account.Statement
      }
    }
    case Account.StatementResult(statement) => {
      log.info("Statement resunt: {}", statement)
      statements = statement :: statements
      if (statements.size == numberOfAccount) client ! StatementResult(statements.mkString(","))
    }
    case ReceiveTimeout => {
      log.info("ReceiveTimeout, stop self and all children")
      context stop self
    }
  }

  def accountProps(acctId: String) = Props(classOf[Account], acctId, get_account(acctId).acctType)
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
