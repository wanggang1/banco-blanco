package com.bancoblanco

object Customer extends InMemoryStore[String] {
  override val store_type = "customer"
}

class Customer(id: Long, name: String) {
  //list of accounts
  //open account
  //statement
  //transfer
}
