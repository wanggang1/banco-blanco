package com.bancoblanco

/**
 * A transaction on an Account, which includes a transaction date.
 * @param amount Double the amount of the transaction.
 * @param timeService TimeService the implicit time service.
 */
case class Transaction(amount: Double)(implicit timeService: TimeService) {
  val transactionDate: Long = timeService.now
  println(f"Transaction: $amount%2.2fUSD on $transactionDate%2.0f")
}