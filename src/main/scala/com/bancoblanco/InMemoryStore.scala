package com.bancoblanco

import scala.concurrent.stm.Ref

trait InMemoryStore[T] {
  private val atominList = Ref( Map[String, List[T]]() )
  
  val store_type = ""
  
  def create(id: String): String = {
    val key = store_type + id
    atominList.single.transform(map => map + (key -> List[T]()))
    key
  }
  
  def getAll(): Map[String, List[T]] = atominList.single.get
  
  def get(key: String): List[T] = atominList.single.get(key)
  
  def add(key: String, value: T): Unit = atominList.single.transform(map => {
    val values = value :: map.getOrElse(key, List[T]())
    map + (key -> values)
  })
}