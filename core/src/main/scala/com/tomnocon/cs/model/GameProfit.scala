package com.tomnocon.cs.model

case class GameProfit(gameId: String, value: Long, timestamp: Long) {

  override def toString: String = s"GameProfit{gameId=$gameId, value=$value, timestamp=$timestamp}"
}
