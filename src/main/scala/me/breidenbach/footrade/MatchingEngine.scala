package me.breidenbach.footrade

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import akka.actor.{ActorRef, Actor}
import me.breidenbach.footrade.MatchingEngine._
import me.breidenbach.footrade.TradeDirection.{Sell, Buy}

import scala.collection.mutable
import scala.collection.JavaConversions._

/**
 * Date: 3/11/15
 * Time: 10:11 PM
 * Copyright 2015 Kevin E. Breidenbach
 * @author Kevin E. Breidenbach
 */
class MatchingEngine() extends Actor {

  private case class MatchTrades()

  override def receive: Receive = {
    case Match(trade, trader) =>
      addTrade(trade, trader)
      self ! MatchTrades
    case MatchTrades =>
      matchTrades()
  }
}

object MatchingEngine {
  private type TradeMap = ConcurrentHashMap[Trade, ActorRef]
  val tradeMap: TradeMap = new ConcurrentHashMap[Trade, ActorRef]
  val completedTrades = new mutable.ListBuffer[Trade]

  def addTrade(trade: Trade, trader: ActorRef): Unit = {
    tradeMap.put(trade, trader)
  }

  def matchTrades(): Unit = {
    println("matching trades...")

    tradeMap.size() match {
      case 0 | 1 =>
        println("\tinsufficient trades to match")
      case _ =>
        checkStoredTrades()
    }
  }

  private def checkStoredTrades(): Unit = {
    val trades = asScalaSet(tradeMap.keySet())
    val groupedTrades = trades.groupBy(trade => trade.ticker)

    groupedTrades.foreach { case (ticker, tradesForTicker) =>
      val completedTrades = matchTradesForTicker(tradesForTicker.toList, Map.empty, Map.empty)

      println("\nBooked Trades:" + completedTrades)
      completeTrades(completedTrades)
    }
  }

  private def matchTradesForTicker(trades: List[Trade], buyTrades: Map[Int, List[Trade]],
                                   sellTrades: Map[Int, List[Trade]]): List[Trade] = {
    trades match {
      case head :: tail =>
        head.tradeDirection match {
          case Buy =>
            val buyTradesList =
              if (buyTrades.isEmpty || !buyTrades.containsKey(head.qty)) List(head) else buyTrades(head.qty) :+ head

            matchTradesForTicker(tail, buyTrades + (head.qty -> buyTradesList), sellTrades)

          case Sell =>
            val sellTradesList =
              if (sellTrades.isEmpty || !sellTrades.containsKey(head.qty)) List(head) else sellTrades(head.qty) :+ head

            matchTradesForTicker(tail, buyTrades, sellTrades + (head.qty -> sellTradesList))
        }
      case _ =>
        findCompletedTrades(buyTrades, sellTrades)
    }
  }

  private def findCompletedTrades(buyTrades: Map[Int, List[Trade]], sellTrades: Map[Int, List[Trade]]): List[Trade] = {
    buyTrades.keys.flatMap(tradeSize => {
      val buyTradeList = buyTrades(tradeSize)
      val sellTradeList = if (sellTrades.contains(tradeSize)) sellTrades(tradeSize) else List.empty
      val stuff = matchTradesFromList(buyTradeList, sellTradeList, List.empty)
      stuff
    }).toList
  }

  private def matchTradesFromList(buyTrades: List[Trade], sellTrades: List[Trade], matchedTrades: List[Trade]): List[Trade] = {
    buyTrades match {
      case buyHeadTrade :: buyTailTrades =>
        sellTrades match {
          case sellHeadTrade :: sellTailTrades =>
            matchTradesFromList(buyTailTrades, sellTailTrades, matchedTrades :+ buyHeadTrade :+ sellHeadTrade)
          case _ =>
            matchedTrades
        }
      case _ =>
        matchedTrades
    }
  }

  private def completeTrades(trades: List[Trade]): Unit = {
    val tradeTimestamp = Instant.now
    trades.foreach(trade => {
      val trader = tradeMap.get(trade)
      val completedTrade = trade.copy(tradeTime = tradeTimestamp)
      trader ! TradeConfirm(completedTrade)
      completedTrades.append(completedTrade)
      tradeMap.remove(trade)
    })
    println("\nTrade Report")
    println("Completed Trades: " + completedTrades)
    println("Trades To Match: " + tradeMap)
    println()
  }
}


