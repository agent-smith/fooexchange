package me.breidenbach

import akka.actor.ActorRef
import me.breidenbach.footrade.OrderType.OrderType
import me.breidenbach.footrade.TradeDirection.TradeDirection
import java.time.Instant

/**
 * Date: 3/11/15
 * Time: 10:13 PM
 * Copyright 2015 Kevin E. Breidenbach
 * @author Kevin E. Breidenbach
 */
package object footrade {
  
  object OrderType extends Enumeration {
    type OrderType = Value
    val Market, Limit = Value
  }
  
  object TradeDirection extends Enumeration {
    type TradeDirection = Value
    val Buy, Sell = Value
  }

  object IdCounter {
    var initial = 0
    def next: Int = {
      initial = initial + 1
      initial
    }
  }

  sealed trait TradeMessage
  sealed trait MatchingMessage

  case class Trade(ticker: String, qty: Int, tradeDirection: TradeDirection, orderType: OrderType,
                   partial: Boolean = false, price: BigDecimal = 0, id: Int = 0, tradeTime: Instant = null)
  case class TradeRequest(trade: Trade) extends TradeMessage
  case class TradeRequestAck(trade: Trade) extends TradeMessage
  case class TradeConfirm(trade: Trade) extends TradeMessage
  case class Match(trade: Trade, tradeRequestor: ActorRef) extends MatchingMessage
  case class Matched(trade: Trade, tradeRequestor: ActorRef) extends MatchingMessage
}
