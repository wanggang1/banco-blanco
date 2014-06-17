package com.bancoblanco

import akka.actor.{Actor, ActorLogging}

case class BankCustomer(id: String, name: String)
case class BankAccount(id: String, acctType: AccountType)

object Bank extends InMemoryStore[BankCustomer] {
  val store_type = "bank"
  
  case class CustomerResult(values: List[BankCustomer])
  case object AllCustomers
  case object Done
}

class Bank(bankId: String, name: String) extends Actor with ActorLogging{
  import Bank._
  
  def receive = {
    case cust: BankCustomer => add(bankId, cust)
                               sender ! Done
                               log.info("Customer added for: {}, Customer Id: {}, Customer name {}.", bankId, cust.id, cust.name)
                               context stop self
    case AllCustomers => val values = get(bankId)
                         sender ! CustomerResult(values)
                         log.info("All customers for: {}.", bankId)
                         context stop self
  }
}