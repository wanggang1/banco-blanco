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
    case acct: BankAccount => add(customerId, acct)
    case Deposit(acctId, amount) => {
      if ( !has_account(acctId) ) sender ! Failed
      log.info("Deposit request for account {}, amount {}", acctId, amount)

      getChildActor(acctId) ! Account.Deposit(amount)
    }
    case Withdraw(acctId, amount) => {
      if ( !has_account(acctId) ) sender ! Failed
      log.info("Withdraw request for account {}, amount {}", acctId, amount)

      getChildActor(acctId) ! Account.Withdraw(amount)
    }
    case Transfer(fromAcctId, toAcctId, amount) => {
      if ( !has_account(fromAcctId) || !has_account(toAcctId)) sender ! Failed
      log.info("Transfer request from account {} to account {}, amount {}", fromAcctId, toAcctId, amount)
      
      val uid = UniqueNumber.generate
      val teller = context.actorOf(Props[Teller], s"TellerActor-$uid")
      val fromAcct = getChildActor(fromAcctId)
      val toAcct = getChildActor(toAcctId)
      teller ! Teller.Transfer(fromAcct, toAcct, amount)
    }
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
      if (statements.size == numberOfAccount) client ! StatementResult(statements.mkString(" ,"))
    }
    case Account.Done => sender ! Done
    case Account.Failed => sender ! Failed
    case ReceiveTimeout => context stop self
  }

  def accountProps(acctId: String) = Props(classOf[Account], acctId, get_account(acctId).acctType)
  
  private def getChildActor(acctId: String): ActorRef = {
    context.child("AccountActor-" + acctId) match {
      case Some(actor) => actor
      case None => context.actorOf(accountProps(acctId), s"AccountActor-$acctId")
    }
  }
  
}
