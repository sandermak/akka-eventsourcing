package net.branchandbound.eventsourcing

import akka.persistence.PersistentActor
import akka.actor.ActorLogging
import java.util.Date
import akka.persistence.SnapshotOffer
import akka.actor.Props

object ConcertActor {
  sealed trait Command
  case class BuyTickets(user: String, quantity: Int) extends Command
  case class ChangePrice(newPrice: Int) extends Command
  case class ChangeCapacity(newCapacity: Int) extends Command
  
  sealed trait CommandResponse
  case object SoldOut extends CommandResponse
  case object Success extends CommandResponse
  
  sealed trait Query
  case object GetSalesRecords extends Query
  
  sealed trait Event
  case class SoldOut(user: String) extends Event
  case class TicketsBought(user: String, quantity: Int) extends Event
  
  
  case class ConcertState(price: Int, available: Int, startTime: Date, sales: Seq[SalesRecord]) {
    def updated(evt: Event): ConcertState = evt match {
      case TicketsBought(user, quant)  => copy(price, available - quant, startTime, 
                                            SalesRecord(user, quant, price) +: sales)
      case SoldOut(user)               => this                                         
    }
  }
  case class SalesRecord(user: String, quantity: Int, price: Int)
  
  def props(id: String, price: Int, capacity: Int, startTime: Date) =
    Props(new ConcertActor(id, price, capacity, startTime))
  
}

class ConcertActor(id: String, price: Int, available: Int, startTime: Date) 
    extends PersistentActor with ActorLogging {
  
  import ConcertActor._
  
  def persistenceId = "Concert." + id
  
  var state = ConcertState(price, available, startTime, Nil)
  def updateState(evt: Event) = state = state.updated(evt)
  
  val receiveRecover: Receive = {
    case evt: Event                               => updateState(evt)
    case SnapshotOffer(_, snapshot: ConcertState) => state = snapshot
  }

  val receiveCommand: Receive = {
    // Commands
    case BuyTickets(user, quant) if quant <= available => {
      log.info(s"Current state: $state")
      persist(TicketsBought(user, quant))(evt =>{
        updateState(evt)
        sender() ! Success
      });
    }
    case BuyTickets(user, _)                           => {
      log.warning("Sold out!")
      persist(SoldOut(user))(evt => {
        updateState(evt)
        sender() ! SoldOut
      })
    }
    
    // Queries
    case GetSalesRecords                               => {
      sender() ! state.sales
    }
  }
  
}