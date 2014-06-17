package com.bancoblanco

import akka.actor.{Actor, ActorRef, Props, ActorLogging, ReceiveTimeout, OneForOneStrategy, SupervisorStrategy}

object Customer extends InMemoryStore[BankAccount] {
  val store_type = "customer"
  
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
  
  var numberOfAccount = 0
  var statements = List[String]()
  var client: ActorRef = _
  
  def receive = {
    case acct: BankAccount => add(customerId, acct)
    case Deposit(acctId, amount) => {
      if ( !exist(acctId) ) sender ! Failed
      log.info("Deposit request for account {}, amount {}", acctId, amount)
      
      val uid = UniqueNumber.generate
      val account = context.actorOf(Props(classOf[Account], acctId, getAccount(acctId).acctType), s"AccountActor-$acctId")
      account ! Account.Deposit(amount)
    }
    case Withdraw(acctId, amount) => {
      if ( !exist(acctId) ) sender ! Failed
      log.info("Withdraw request for account {}, amount {}", acctId, amount)
      
      val uid = UniqueNumber.generate
      val account = context.actorOf(Props(classOf[Account], acctId, getAccount(acctId).acctType), s"AccountActor-$acctId")
      account ! Account.Withdraw(amount)
    }
    case Transfer(fromAcctId, toAcctId, amount) => {
      if ( !exist(fromAcctId) || !exist(toAcctId)) sender ! Failed
      log.info("Transfer request from account {} to account {}, amount {}", fromAcctId, toAcctId, amount)
      
      val uid = UniqueNumber.generate
      val teller = context.actorOf(Props[Teller], s"TellerActor$uid")
      val fromAcct = context.actorOf(Props(classOf[Account], fromAcctId, getAccount(fromAcctId).acctType), s"AccountActor-$fromAcctId")
      val toAcct = context.actorOf(Props(classOf[Account], toAcctId, getAccount(toAcctId).acctType), s"AccountActor-$toAcctId")
      teller ! Teller.Transfer(fromAcct, toAcct, amount)
    }
    case Statement => {
      log.info("Statement request")
      val accounts = get(customerId)
      numberOfAccount = accounts.size
      statements = Nil
      client = sender
      for (acct <- get(customerId)) {
        val uid = UniqueNumber.generate
        val account = context.actorOf(Props(classOf[Account], acct.id, acct.acctType), s"AccountActor-$acct.id")
        account ! Account.Statement
      }
    }
    case Account.StatementResult(statement) => {
      log.info("Statement resunt: {}", statement)
      statements = statement :: statements
      if (statements.size == numberOfAccount) client ! StatementResult(statements.mkString(" ,"))
    }
    case Account.Done => sender ! Done
    case Account.Failed => sender ! Failed
  }
  
  private def exist(acctId: String) = get(customerId).exists((x) => x.id == acctId)
  private def getAccount(acctId: String) = get(customerId).filter((x) => x.id == acctId)(0)
}
