package com.bancoblanco

import scala.concurrent.stm.Ref

trait InMemoryStore[T] {
  private val atominList = Ref( Map[String, List[T]]() )
  
  def add(key: String, value: T): Unit = atominList.single.transform(map => {
    val values = value :: map.getOrElse(key, List[T]())
    map + (key -> values)
  })
      
  def getAll(): Map[String, List[T]] = atominList.single.get
  
  def get(key: String): List[T] = {
    val map = getAll()
    map.getOrElse(key, List[T]())
  }
  
  def hasValue(key: String)(predicate: (T) => Boolean) = get(key).exists(predicate)
  def getValue(key: String)(predicate: (T) => Boolean) = get(key).filter(predicate)(0)
}