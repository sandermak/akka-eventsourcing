package net.branchandbound.eventsourcing

import java.util.Date

object ConcertProtocol {

  sealed trait Event
  case class ConcertCreated(price: Int, available: Int, startTime: Date) extends Event
  case class SoldOut(user: String) extends Event
  case class TicketsBought(user: String, quantity: Int) extends Event
  case class PriceChanged(newPrice: Int) extends Event
  case class CapacityIncreased(toBeAdded: Int) extends Event
  
}