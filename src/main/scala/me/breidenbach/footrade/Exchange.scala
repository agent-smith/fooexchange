package me.breidenbach.footrade

import akka.actor._

/**
 * Date: 3/11/15
 * Time: 10:11 PM
 * Copyright 2015 Kevin E. Breidenbach
 * @author Kevin E. Breidenbach
 */
class Exchange(matchingEngine: ActorRef) extends Actor {

  override def receive: Receive = {
    case TradeRequest(trade) =>
      val tradeWithId = trade.copy(id = IdCounter.next)
      sender() ! TradeRequestAck(tradeWithId)
      matchingEngine ! Match(tradeWithId, sender())
  }
}

object Exchange extends App {

  val system = ActorSystem("Foo_Exchange")
  val matchingEngine = system.actorOf(Props[MatchingEngine], "Matching_Engine")
  val exchange = system.actorOf(Props(new Exchange(matchingEngine)), "Exchange")
  val traderDave = system.actorOf(Props(new Trader("Dave", exchange)), "Trader_Dave")
  val traderAnnie = system.actorOf(Props(new Trader("Annie", exchange)), "Trader_Annie")

  startExchange()

  def startExchange(): Unit = {
    println("Opening Bell...")
    traderDave ! "start"
    traderAnnie ! "start"
  }
}
