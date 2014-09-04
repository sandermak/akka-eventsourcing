package net.branchandbound.eventsourcing

import akka.persistence.PersistentView
import akka.actor.Props

object ConcertHistoryView {
  import ConcertProtocol._
  
  case class ConcertHistoryState(priceHistory: Seq[Int] = Nil, 
                                 ticketsPerPrice: Map[Int, Int] = Map.empty.withDefaultValue(0), 
                                 soldOuts: Int = 0) {
    
    def updated(evt: ConcertEvent): ConcertHistoryState = evt match {
      case ConcertCreated(price, _, _) => copy(priceHistory :+ price, ticketsPerPrice, soldOuts)
      case SoldOut(_)                  => copy(priceHistory, ticketsPerPrice, soldOuts + 1) 
      case TicketsBought(_, quantity)  => {
        val currentPrice = priceHistory.last 
        val newTicketsPerPrice = ticketsPerPrice.updated(currentPrice, ticketsPerPrice(currentPrice) + quantity)
        copy(priceHistory, newTicketsPerPrice, soldOuts)
      }
      case PriceChanged(newPrice)      => copy(priceHistory :+  newPrice, ticketsPerPrice, soldOuts)
      case CapacityIncreased(_)        => this
    }
  }
  
  case object GetConcertHistory
  
  def props(id: String) = Props(new ConcertHistoryView(id))
}

class ConcertHistoryView(id: String) extends PersistentView {
  
  import ConcertHistoryView._
  import ConcertProtocol._
  
  override def persistenceId: String = "Concert." + id
  override def viewId: String = "Concert.view" + id
 
  var state = ConcertHistoryState()
  
  def receive: Receive = {
    case evt: ConcertEvent if isPersistent => state = state.updated(evt)
    case GetConcertHistory                 => sender() ! state;
  }
  
}