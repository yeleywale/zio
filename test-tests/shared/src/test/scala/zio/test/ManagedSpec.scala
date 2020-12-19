package zio.test

import zio._
import zio.test.Assertion.equalTo
import zio.test.TestAspect.sequential

object ManagedSpec extends ZIOBaseSpec {

  type Counter = Has[Counter.Service]

  object Counter {

    trait Service {
      def incrementAndGet: UIO[Int]
    }

    val live: Layer[Nothing, Counter] =
      ZLayer.fromManaged {
        Ref.make(1).toManaged(_.set(-10)).map { ref =>
          new Counter.Service {
            val incrementAndGet: UIO[Int] = ref.updateAndGet(_ + 1)
          }
        }
      }

    val incrementAndGet: URIO[Counter, Int] =
      ZIO.accessM[Counter](_.get[Counter.Service].incrementAndGet)
  }

  def spec: Spec[Any, TestFailure[Any], TestSuccess] = suite("ManagedSpec")(
    suite("managed shared")(
      suite("first suite")(
        testM("first test") {
          assertM(Counter.incrementAndGet)(equalTo(2))
        },
        testM("second test") {
          assertM(Counter.incrementAndGet)(equalTo(3))
        }
      ),
      suite("second suite")(
        testM("third test") {
          assertM(Counter.incrementAndGet)(equalTo(4))
        },
        testM("fourth test") {
          assertM(Counter.incrementAndGet)(equalTo(5))
        }
      )
    ).provideLayerShared(Counter.live) @@ sequential,
    suite("managed per test")(
      suite("first suite")(
        testM("first test") {
          assertM(Counter.incrementAndGet)(equalTo(2))
        },
        testM("second test") {
          assertM(Counter.incrementAndGet)(equalTo(2))
        }
      ),
      suite("second suite")(
        testM("third test") {
          assertM(Counter.incrementAndGet)(equalTo(2))
        },
        testM("fourth test") {
          assertM(Counter.incrementAndGet)(equalTo(2))
        }
      )
    ).provideLayer(Counter.live)
  )
}
