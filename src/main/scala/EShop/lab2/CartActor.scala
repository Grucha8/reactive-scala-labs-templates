package EShop.lab2

import EShop.lab2.CartActor.{AddItem, CancelCheckout, RemoveItem, StartCheckout}
import akka.actor.{Actor, ActorRef, Cancellable, Props}
import akka.event.Logging

import scala.concurrent.duration._
import scala.language.postfixOps

object CartActor {

  sealed trait Command
  case class AddItem(item: Any)    extends Command
  case class RemoveItem(item: Any) extends Command
  case object ExpireCart           extends Command
  case object StartCheckout        extends Command
  case object CancelCheckout       extends Command
  case object CloseCheckout        extends Command

  sealed trait Event
  case class CheckoutStarted(checkoutRef: ActorRef) extends Event

  def props = Props(new CartActor())
}

class CartActor extends Actor {

  private val log       = Logging(context.system, this)
  val cartTimerDuration = 5 seconds

  private def scheduleTimer: Cancellable = ???

  def receive: Receive = empty

  def empty: Receive = {
    case AddItem(item) =>
      context become nonEmpty(Cart(List(item)), null)
  }

  def nonEmpty(cart: Cart, timer: Cancellable): Receive = {
    case RemoveItem(item) if cart.contains(item) =>
      val newCart = cart.removeItem(item)
      newCart.size match {
        case 0 => context become empty
        case _ => context become nonEmpty(newCart, timer)
      }
    case StartCheckout =>
      context become inCheckout(cart)
  }

  def inCheckout(cart: Cart): Receive = {
    case CancelCheckout =>
      context become nonEmpty(cart, null)
  }

}
