package com.tomnocon.cs.model

object MachineEventType extends Enumeration {
  type MachineEventType = Value
  val Withdrawal, Deposit, Bet, Win = Value
}