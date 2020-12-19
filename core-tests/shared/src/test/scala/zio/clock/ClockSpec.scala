package zio.clock

import zio._
import zio.test._

import java.time.DateTimeException

object ClockSpec extends ZIOBaseSpec {

  def spec: Spec[Clock, TestFailure[DateTimeException], TestSuccess] = suite("ClockSpec")(
    testM("currentDateTime does not throw a DateTimeException") {
      for {
        _ <- clock.currentDateTime
      } yield assertCompletes
    }
  )
}
