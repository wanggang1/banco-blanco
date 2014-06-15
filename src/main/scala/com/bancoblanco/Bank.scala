package com.bancoblanco

object Bank extends InMemoryStore[String] {
  override val store_type = "bank"
}

class Bank(id: String, name: String) {
  import Bank._
  
  //add new customer
}