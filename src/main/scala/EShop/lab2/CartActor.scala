package EShop.lab2

import EShop.lab2.CartActor._
import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}
import akka.event.LoggingReceive

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
  case object GetItems             extends Command // command made to make testing easier

  sealed trait Event
  case class CheckoutStarted(checkoutRef: ActorRef) extends Event

  def props(orderManager: ActorRef) = Props(new CartActor(orderManager))
}

class CartActor(orderManager: ActorRef) extends Actor with ActorLogging {

  val cartTimerDuration = 5 seconds

  private def scheduleTimer: Cancellable =
    context.system.scheduler.scheduleOnce(cartTimerDuration, self, ExpireCart)(context.system.dispatcher)

  def receive: Receive = empty

  def empty: Receive = LoggingReceive {
    case AddItem(item) =>
      context become nonEmpty(Cart.empty.addItem(item), scheduleTimer)
  }

  def nonEmpty(cart: Cart, timer: Cancellable): Receive = LoggingReceive {
    case AddItem(item) =>
      context become nonEmpty(cart.addItem(item), timer)
    case RemoveItem(item) if cart.contains(item) =>
      val newCart = cart.removeItem(item)
      newCart.size match {
        case 0 => context become empty
        case _ => context become nonEmpty(newCart, timer)
      }
    case StartCheckout =>
      timer.cancel()
      val checkoutRef = context.actorOf(Checkout.props(self), "checkoutActor")
      checkoutRef ! Checkout.StartCheckout
      orderManager ! CheckoutStarted(checkoutRef)
      context become inCheckout(cart)
    case ExpireCart =>
      context become empty
  }

  def inCheckout(cart: Cart): Receive = LoggingReceive {
    case CancelCheckout =>
      context become nonEmpty(cart, scheduleTimer)
    case CloseCheckout =>
      orderManager ! CloseCheckout
      context become empty
  }

}
