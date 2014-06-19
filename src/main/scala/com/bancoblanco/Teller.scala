package com.bancoblanco

import akka.actor.{Actor, ActorLogging, ActorRef}

object Teller {
  type Receive = PartialFunction[Any, Unit]
  case class Transfer(from: ActorRef, to: ActorRef, amount: Double)
  case class TransferResult(value: Boolean)
}

class Teller() extends Actor with ActorLogging {
  import Teller._
  import Account.WithdrawResult
  
  def receive = {
    case Transfer(from, to, amount) => from ! Account.Withdraw(amount)
                                       log.info("Withdraw from: {}.", amount)
                                       context become waitForWithdraw(to, amount, sender)
  }

  def waitForWithdraw(to: ActorRef, amount: Double, customer: ActorRef): Receive = {
    case WithdrawResult(true) => to ! Account.Deposit(amount)
                         log.info("Deposit to: {}.", amount)
                         context become waitForDeposit(customer)
    case WithdrawResult(false) => customer ! TransferResult(false)
                           log.info("Withdraw failed.")
                           context stop self
  }

  def waitForDeposit(customer: ActorRef): Receive = {
    case Account.Done => customer ! TransferResult(true)
                         log.info("Transaction Done.")
                         context stop self
  }
  
}