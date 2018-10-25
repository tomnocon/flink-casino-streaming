package com.tomnocon.cs

import com.tomnocon.cs.Game.Game
import com.tomnocon.cs.Machine.Machine
import com.tomnocon.cs.Site.Site
import com.tomnocon.cs.model.{MachineEvent, MachineEventType}

import scala.util.Random

class MachineSimulator(site: Site, machine: Machine) {

  private val random = new Random()

  private var listeners: List[MachineEvent => Unit] = List()
  private var _credit: Long = 0
  private var _currentGame: Game = Game.apply(random.nextInt(Game.maxId))

  def credit: Long = _credit

  def currentGame: Game = _currentGame

  def win(winAmount: Long): Unit = {
    _credit += winAmount
    notify(MachineEvent(`type` = MachineEventType.Win, value = winAmount, credit = credit, machineId = machine.toString, gameId = currentGame.toString, siteId = site.toString, timestamp = now))
  }

  def deposit(depositAmount: Long): Unit = {
    _credit += depositAmount
    notify(MachineEvent(`type` = MachineEventType.Deposit, value = depositAmount, credit = credit, machineId = machine.toString, gameId = currentGame.toString, siteId = site.toString, timestamp = now))
  }

  def withdraw(): Unit = {
    if (_credit == 0) {
      throw new Exception("Nothing to withdraw.")
    }

    val withdrawAmount = _credit
    _credit = 0
    notify(MachineEvent(`type` = MachineEventType.Withdrawal, value = withdrawAmount, credit = credit, machineId = machine.toString, gameId = currentGame.toString, siteId = site.toString, timestamp = now))
  }

  def bet(betAmount: Long): Unit = {
    if (_credit < betAmount) {
      throw new Exception("Not enough founds.")
    }

    _credit -= betAmount
    notify(MachineEvent(`type` = MachineEventType.Bet, value = betAmount, credit = credit, machineId = machine.toString, gameId = currentGame.toString, siteId = site.toString, timestamp = now))
  }

  def runGame(game: Game): Unit = {
    _currentGame = game
  }

  def listen(listener: MachineEvent => Unit) {
    listeners ::= listener
  }

  def notify(ev: MachineEvent): Unit = for (listener <- listeners) listener(ev)

  private def now = System.currentTimeMillis
}
