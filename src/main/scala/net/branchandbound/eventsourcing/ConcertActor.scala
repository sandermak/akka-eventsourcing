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
  case class CreateConcert(price: Int, available: Int, startTime: Date) extends Command
  
  sealed trait CommandResponse
  case object SoldOut extends CommandResponse
  case object Success extends CommandResponse
  
  sealed trait Query
  case object GetSalesRecords extends Query
  
  sealed trait Event
  case class ConcertCreated(price: Int, available: Int, startTime: Date)
  case class SoldOut(user: String) extends Event
  case class TicketsBought(user: String, quantity: Int) extends Event
  
  
  case class ConcertState(price: Int, available: Int, startTime: Date, sales: Seq[SalesRecord] = Nil) {
    def updated(evt: Event): ConcertState = evt match {
      case TicketsBought(user, quant)  => copy(price, available - quant, startTime, 
                                            SalesRecord(user, quant, price) +: sales)
      case SoldOut(user)               => this                                         
    }
  }
  case class SalesRecord(user: String, quantity: Int, price: Int)
  
  def props(id: String) = Props(new ConcertActor(id))
  
}

class ConcertActor(id: String) extends PersistentActor with ActorLogging {
  
  import ConcertActor._
  
  def persistenceId = "Concert." + id
  
  var state: ConcertState = null
  def updateState(evt: Event) = state = state.updated(evt)
  def setInitialState(evt: ConcertCreated) = {
    state = ConcertState(evt.price, evt.available, evt.startTime)
    context.become(receiveCommands)
  }
  
  val receiveRecover: Receive = {
    case evt: ConcertCreated                      => setInitialState(evt)
    case evt: Event                               => updateState(evt)
    case SnapshotOffer(_, snapshot: ConcertState) => state = snapshot
  }

  val receiveCreate: Receive = {
    case c@CreateConcert(price, available, startTime)        => {
      log.info(s"Creating concert with $c")
      persist(ConcertCreated(price, available, startTime))(setInitialState(_))
    }
  }
  
  val receiveCommands: Receive = {
    case BuyTickets(user, quant) if quant <= state.available => {
      log.info(s"Current state: $state")
      persist(TicketsBought(user, quant))(evt =>{
        updateState(evt)
        sender() ! Success
      });
    }
    case BuyTickets(user, _)                                 => {
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
  
    // Initially we expect a CreateConcert command
  val receiveCommand: Receive = receiveCreate
  
}