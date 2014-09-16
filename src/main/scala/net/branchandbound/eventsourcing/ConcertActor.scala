package net.branchandbound.eventsourcing

import akka.persistence.PersistentActor
import akka.actor.ActorLogging
import java.util.Date
import akka.persistence.SnapshotOffer
import akka.actor.Props

object ConcertActor {
  import ConcertProtocol._
  
  sealed trait Command
  case class BuyTickets(user: String, quantity: Int) extends Command
  case class ChangePrice(newPrice: Int) extends Command
  case class AddCapacity(newCapacity: Int) extends Command
  case class CreateConcert(price: Int, available: Int, startTime: Date) extends Command
  
  sealed trait Query
  case object GetSalesRecords extends Query
  
  case class ConcertState(price: Int, available: Int, startTime: Date, sales: Seq[SalesRecord] = Nil) {
    def updated(evt: ConcertEvent): ConcertState = evt match {
      case TicketsBought(user, quant)   => copy(price, available - quant, startTime, 
                                             SalesRecord(user, quant, price) +: sales)
      case PriceChanged(newPrice)       => copy(price = newPrice, available, startTime, sales)
      case CapacityIncreased(toBeAdded) => copy(price, available = available + toBeAdded, startTime, sales)
      case _                            => this                                         
    }
  }
  case class SalesRecord(user: String, quantity: Int, price: Int)
  
  def props(id: String) = Props(new ConcertActor(id))
  
}

class ConcertActor(id: String) extends PersistentActor with ActorLogging {
  
  import ConcertActor._
  import ConcertProtocol._
  
  def persistenceId = "Concert." + id
  
  var state: Option[ConcertState] = None
  def updateState(evt: ConcertEvent) = state = state.map(_.updated(evt))
  def setInitialState(evt: ConcertCreated) = {
    state = Some(ConcertState(evt.price, evt.available, evt.startTime))
    context.become(receiveCommands)
  }
  
  val receiveRecover: Receive = {
    case evt: ConcertCreated                      => setInitialState(evt)
    case evt: ConcertEvent                        => updateState(evt)
    case SnapshotOffer(_, snapshot: ConcertState) => {
      state = Some(snapshot)
      context.become(receiveCommands)
    }
  }

  val receiveCreate: Receive = {
    case c@CreateConcert(price, available, startTime)        => {
      log.info(s"Creating concert with $c")
      persist(ConcertCreated(price, available, startTime))(setInitialState(_))
    }
  }
  
  val receiveCommands: Receive = {
    case BuyTickets(user, quant) if quant <= state.get.available => {
      log.info(s"Current state: $state")
      persist(TicketsBought(user, quant))(evt =>{
        updateState(evt)
        sender() ! evt
      });
    }
    case BuyTickets(user, _)                                 => {
      log.warning("Sold out!")
      persist(SoldOut(user))(evt => {
        updateState(evt)
        sender() ! evt
      })
    }
    case ChangePrice(newPrice)                       => {
      log.info(s"Price changed to $newPrice")
      persist(PriceChanged(newPrice))(evt => updateState(evt))
    }
    case AddCapacity(toBeAdded)                       => {
      log.info(s"Capacity increased with $toBeAdded")
      persist(CapacityIncreased(toBeAdded))(evt => updateState(evt))
    }
    
      
    // Queries
    case GetSalesRecords                               => {
      sender() ! state.get.sales
    }
  }
  
    // Initially we expect a CreateConcert command
  val receiveCommand: Receive = receiveCreate
  
}