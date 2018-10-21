package com.tomnocon.cs.model

object MachineEventType extends Enumeration {
  type MachineEventType = Value
  val CashIn, CashOut, Bet, Win = Value
}