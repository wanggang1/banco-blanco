package com.bancoblanco

import akka.actor.Actor

case class BankCustomer(id: String, name: String)
case class BankAccount(id: String, acctType: AccountType)

object Bank extends InMemoryStore[BankCustomer] {
  val store_type = "bank"
  
  case class CustomerResult(values: List[BankCustomer])
  case object AllCustomers
}

class Bank(bankId: String, name: String) extends Actor{
  import Bank._
  
  def receive = {
    case cust: BankCustomer => add(bankId, cust)
                               context stop self
    case AllCustomers => val values = get(bankId)
                         sender ! CustomerResult(values)
                         context stop self
  }
}