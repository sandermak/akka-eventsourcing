package net.branchandbound.eventsourcing

import java.util.Date

object ConcertProtocol {

  sealed trait ConcertEvent
  case class ConcertCreated(price: Int, available: Int, startTime: Date) extends ConcertEvent
  case class SoldOut(user: String) extends ConcertEvent
  case class TicketsBought(user: String, quantity: Int) extends ConcertEvent
  case class PriceChanged(newPrice: Int) extends ConcertEvent
  case class CapacityIncreased(toBeAdded: Int) extends ConcertEvent
  
}