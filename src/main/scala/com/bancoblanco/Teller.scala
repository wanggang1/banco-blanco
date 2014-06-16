package com.bancoblanco

import akka.actor.{Actor, ActorLogging, ActorRef}

object Teller {
  type Receive = PartialFunction[Any, Unit]
  case class Transfer(from: ActorRef, to: ActorRef, amount: Double)
  case object Done
  case object Failed
}

class Teller() extends Actor with ActorLogging {
  import Teller._
  
  def receive = {
    case Transfer(from, to, amount) => from ! Account.Withdraw(amount)
                                       context become waitForWithdraw(to, amount, sender)
  }

  def waitForWithdraw(to: ActorRef, amount: Double, customer: ActorRef): Receive = {
    case Account.Done => to ! Account.Deposit(amount)
                         context become waitForDeposit(customer)
    case Account.Failed => customer ! Failed
                           context stop self
  }

  def waitForDeposit(customer: ActorRef): Receive = {
    case Account.Done => customer ! Done
                         context stop self
  }
  
}