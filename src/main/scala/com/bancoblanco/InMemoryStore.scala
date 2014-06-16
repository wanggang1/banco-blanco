package com.bancoblanco

import scala.concurrent.stm.Ref

trait InMemoryStore[T] {
  private val atominList = Ref( Map[String, List[T]]() )
  
  def add(key: String, value: T): Unit = atominList.single.transform(map => {
    val values = value :: map.getOrElse(key, List[T]())
    map + (key -> values)
  })
  
  def get(key: String): List[T] = atominList.single.get(key)
    
  def getAll(): Map[String, List[T]] = atominList.single.get
 
}