package me.breidenbach.footrade

import akka.actor.{Actor, ActorRef}

/**
 * Date: 3/11/15
 * Time: 10:11 PM
 * Copyright 2015 Kevin E. Breidenbach
 * @author Kevin E. Breidenbach
 */
class Trader(name: String, exchange: ActorRef) extends Actor {
  override def receive: Receive = {
    case TradeRequestAck(trade) =>
      println("Trader " + name + " trade request acknowledged with id " + trade.id + " Trade(" + trade + ")")
    case TradeConfirm(trade) =>
      println("Trader " + name + " trade complete with trade id " + trade.id + " Trade(" + trade + ")")
    case "start" =>
      start()
  }

  def start(): Unit = name match {
    case "Dave" =>
      val buyTrade1 = Trade("BAC", 1, TradeDirection.Buy, OrderType.Market)
      exchange ! TradeRequest(buyTrade1)
      val buyTrade2 = Trade("C", 1, TradeDirection.Buy, OrderType.Market)
      exchange ! TradeRequest(buyTrade2)
    case "Annie" =>
      val sellTrade = Trade("C", 1, TradeDirection.Sell, OrderType.Market)
      exchange ! TradeRequest(sellTrade)

  }
}
