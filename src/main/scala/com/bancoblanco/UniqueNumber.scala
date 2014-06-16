package com.bancoblanco

import scala.concurrent.stm.Ref

object UniqueNumber {
	private val atomicNumber = Ref(0)

	def generate = atomicNumber.single.transformAndGet(_ + 1)
}