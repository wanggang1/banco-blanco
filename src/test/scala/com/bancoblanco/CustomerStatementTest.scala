package com.bancoblanco

import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class CustomerStatementTestSpec extends FlatSpec with Matchers with BeforeAndAfterAll
{
  import Customer._

  override def beforeAll: Unit = {}
  override def afterAll: Unit = {}
  
  "A Statement" should "look like this" in {
    val statement = singleStatement("Checkings", 500.0, 1000.0, 500.0, 0.5)
    val expected = """Checkings account
  deposit $1000.0
  withdraw $500.0
total $500.0
Interest Owed $0.5"""
    
    statement should be (expected)
  }
  
}