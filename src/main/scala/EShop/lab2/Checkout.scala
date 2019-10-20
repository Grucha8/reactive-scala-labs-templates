package EShop.lab2

import EShop.lab2.Checkout.{CancelCheckout, ExpireCheckout, SelectDeliveryMethod, StartCheckout}
import akka.actor.{Actor, ActorRef, Cancellable, Props}
import akka.event.Logging

import scala.concurrent.duration._
import scala.language.postfixOps

object Checkout {

  sealed trait Data
  case object Uninitialized                               extends Data
  case class SelectingDeliveryStarted(timer: Cancellable) extends Data
  case class ProcessingPaymentStarted(timer: Cancellable) extends Data

  sealed trait Command
  case object StartCheckout                       extends Command
  case class SelectDeliveryMethod(method: String) extends Command
  case object CancelCheckout                      extends Command
  case object ExpireCheckout                      extends Command
  case class SelectPayment(payment: String)       extends Command
  case object ExpirePayment                       extends Command
  case object ReceivePayment                      extends Command

  sealed trait Event
  case object CheckOutClosed                   extends Event
  case class PaymentStarted(payment: ActorRef) extends Event

  def props(cart: ActorRef) = Props(new Checkout())
}

class Checkout extends Actor {

  private val scheduler = context.system.scheduler
  private val log       = Logging(context.system, this)

  val checkoutTimerDuration = 1 seconds
  val paymentTimerDuration  = 1 seconds

  def receive: Receive = {
    case StartCheckout => {
      val timer = scheduler.scheduleOnce(checkoutTimerDuration, self, ExpireCheckout)(context.system.dispatcher)
      context become selectingDelivery(timer)
    }
  }

  def selectingDelivery(timer: Cancellable): Receive = {
    case CancelCheckout | ExpireCheckout => context become cancelled
    case SelectDeliveryMethod(method)    => context become selectingPaymentMethod(timer)
  }

  def selectingPaymentMethod(timer: Cancellable): Receive =
    null

  def processingPayment(timer: Cancellable): Receive = ???

  def cancelled: Receive =
    null

  def closed: Receive = ???

}
