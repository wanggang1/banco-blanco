Programming Sample
===================

Instructions
------------

* This is your code, do with it as you like to solve the use cases.
* Implement all the use cases in the below section as you see fit and using all the Scala tools and open source technologies necessary or desired.
* In order to work on this take a fork into your own GitHub area; make whatever changes you feel are necessary and when you are satisfied merge the code back into your **own** repo and email me the link. **DO NOT** issue a pull request back to this repo.
* The only hard dependencies are Scala 2.11.0 and Scalatest 2.1.6.
* There is no storage necessary for this exercise so the application need only have a lifecycle for the duration of the specs.
* Security is not necessary here so you need not concern yourself with who or what might spin up your traits and classes to do stuff.
* Take your time and have fun with this.  We will use it as a measure of your current capabilities and also use it to lead you to various other solutions if appropriate.
* Any doubts or questions please asked them and as many of them as you like.

Banco Blanco
-------------

This is a dummy application to simulate a simple bank model.

### Features/Use Cases

* A Bank has a unique id and a name (Banco Blanco or whatever you want).
* A Customer has a unique id and a name.
* Account has a unique id.  There are three types of accounts, Checking, Savings, and SuperSavings.
  * **Accounts maintain a history of all Transactions, the Transaction case class is implemented for you.
  * **Each transaction automatically assigns the transaction date via the implicit TimeService but you will need to implement a TimeService for your specs.
  * **Accounts must support deposit of money.
  * **Accounts must support withdrawal of money.
  * **Accounts must provide a current balance.
  * **Accounts must provide current interest owed applied to the current balance (no compounding necessary).
* The different account types have interest calculated in different ways
  * **Checking accounts** have a flat rate of 0.1%
  * **Savings accounts** have a rate of 0.1% for the first $1,000 then 0.2%
  * **SuperSavings accounts** have a rate of 2% for the first $1,000 then 5% for the next $1,000 then 10%.
* Customers have one to many accounts of any account type and must support the opening of new Accounts.
* Customers have the ability to provide a statement of current Account balances and must be able to transfer money between accounts.  The statement must match Statement.txt, which you'll find in src/main/resources.  Please calculate the interest in that file as found in the TODOs and write a test that asserts that your statement generation logic matches it.
* Bank has a collection of Customer and must support the addition of new Customers.
