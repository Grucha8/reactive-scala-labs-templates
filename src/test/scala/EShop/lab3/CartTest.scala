package EShop.lab3

import EShop.lab2.CartActor.GetItems
import EShop.lab2.{Cart, CartActor}
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Success

class CartTest
  extends TestKit(ActorSystem("CartTest"))
  with FlatSpecLike
  with ImplicitSender
  with BeforeAndAfterAll
  with Matchers
  with ScalaFutures {

  override def afterAll: Unit =
    TestKit.shutdownActorSystem(system)

  val item = "Gothic"
  //use GetItems command which was added to make test easier
  it should "add item properly" in {
    val testActorRef = createTestActorRef()

    testActorRef ! CartActor.AddItem(item)
    testActorRef.receive(GetItems, self)

    expectMsg(Cart(Seq(item)))
  }

  it should "be empty after adding and removing the same item" in {
    val testActorRef = createTestActorRef()

    testActorRef ! CartActor.AddItem(item)
    testActorRef ! CartActor.RemoveItem(item)
    testActorRef.receive(GetItems, self)

    expectMsg(Cart.empty)
  }

  it should "start checkout" in {
    val testActorRef = createTestActorRef()

    testActorRef ! CartActor.AddItem(item)
    val future = testActorRef.ask(CartActor.StartCheckout)(1 seconds)

    val Success(result) = future.value.get
    result shouldBe a[CartActor.CheckoutStarted]
  }

  private def createTestActorRef() = TestActorRef(CartActor.props)
}
