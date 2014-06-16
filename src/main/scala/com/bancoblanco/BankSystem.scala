/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bancoblanco

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object BankSystem {
  val actorSystem = ActorSystem("BankActorSystem", ConfigFactory.load.getConfig("bankprocess"))
}