package zio.internal

import org.openjdk.jcstress.annotations._
import org.openjdk.jcstress.infra.results.{IIII_Result, II_Result, I_Result}

object OneElementConcurrentQueueConcurrencyTests {
  /*
   * Tests that offer writes are atomic (simple case, doesn't consider
   * concurrent polls)
   */
  @JCStressTest
  @Outcome.Outcomes(
    Array(
      new Outcome(id = Array("1"), expect = Expect.ACCEPTABLE),
      new Outcome(id = Array("2"), expect = Expect.ACCEPTABLE)
    )
  )
  @State
  class OfferSimpleTest {
    val q = new OneElementConcurrentQueue[Int]()

    @Actor
    def actor1(): Unit = {
      q.offer(1)
      ()
    }

    @Actor
    def actor2(): Unit = {
      q.offer(2)
      ()
    }

    @Arbiter
    def arbiter(r: I_Result): Unit =
      r.r1 = q.poll(-1)
  }

  /*
   * Tests that offer is atomic and values don't get overridden in
   * presence of concurrent polls.
   *
   * The invariant is: # of successful offers should equal to # of
   * successful polls.
   */
  @JCStressTest
  @Outcome.Outcomes(
    Array(
      new Outcome(id = Array("1, 1"), expect = Expect.ACCEPTABLE),
      new Outcome(id = Array("2, 2"), expect = Expect.ACCEPTABLE),
      new Outcome(id = Array("3, 3"), expect = Expect.ACCEPTABLE)
    )
  )
  @State
  class OfferNoOverwritesTest {
    val q                        = new OneElementConcurrentQueue[Int]()
    var (o1, o2, o3, p1, p2, p3) = (-42, -42, -42, -42, -42, -42)

    @Actor
    def actor1(): Unit = {
      o1 = if (q.offer(1)) 1 else 0
      ()
    }

    @Actor
    def actor2(): Unit = {
      o2 = if (q.offer(1)) 1 else 0
      ()
    }

    @Actor
    def actor3(): Unit = {
      o3 = if (q.offer(1)) 1 else 0
      ()
    }

    @Actor
    def actor4(): Unit = {
      p1 = q.poll(0)
      p2 = q.poll(0)
      p3 = q.poll(0)
    }

    @Arbiter
    def arbiter(r: II_Result): Unit = {
      r.r1 = o1 + o2 + o3
      r.r2 = p1 + p2 + p3 + q.poll(0) + q.poll(0) + q.poll(0)
      ()
    }
  }

  /*
   * Tests that polls are atomic (simple case, no concurrent offer + poll).
   */
  @JCStressTest
  @Outcome.Outcomes(
    Array(
      new Outcome(
        id = Array("-10, -20"),
        expect = Expect.ACCEPTABLE,
        desc = "Both pollers finish before offer starts"
      ),
      new Outcome(
        id = Array("1, -20"),
        expect = Expect.ACCEPTABLE,
        desc = "First poller polls offered value"
      ),
      new Outcome(
        id = Array("-10, 1"),
        expect = Expect.ACCEPTABLE,
        desc = "Second poller polls offered value"
      )
    )
  )
  @State
  class PollSimpleTest {
    val q        = new OneElementConcurrentQueue[Int]()
    var (p1, p2) = (0, 0)

    @Actor
    def actor1(): Unit = {
      q.offer(1)
      ()
    }

    @Actor
    def actor2(): Unit =
      p1 = q.poll(-10)

    @Actor
    def actor3(): Unit =
      p2 = q.poll(-20)

    @Arbiter
    def arbiter(r: II_Result): Unit = {
      r.r1 = p1
      r.r2 = p2
    }
  }

  /*
   * Tests that successful polls return only values actually written
   * to the queue.  A case when poll returns `null.asInstanceOf[A]`
   * thinking that it is returning a proper value should not happen.
   */
  @JCStressTest
  @Outcome.Outcomes(
    Array(
      // Both pollers finish before any offer
      new Outcome(id = Array("1, -2, -3, -4"), expect = Expect.ACCEPTABLE),
      new Outcome(id = Array("-1, 2, -3, -4"), expect = Expect.ACCEPTABLE),
      // Only first poller succeeds
      new Outcome(id = Array("1, -2, 1, -4"), expect = Expect.ACCEPTABLE),
      new Outcome(id = Array("1, 2, 1, -4"), expect = Expect.ACCEPTABLE),
      new Outcome(id = Array("-1, 2, 2, -4"), expect = Expect.ACCEPTABLE),
      new Outcome(id = Array("1, 2, 2, -4"), expect = Expect.ACCEPTABLE),
      // Only second poller succeeds
      new Outcome(id = Array("1, -2, -3, 1"), expect = Expect.ACCEPTABLE),
      new Outcome(id = Array("1, 2, -3, 1"), expect = Expect.ACCEPTABLE),
      new Outcome(id = Array("-1, 2, -3, 2"), expect = Expect.ACCEPTABLE),
      new Outcome(id = Array("1, 2, -3, 2"), expect = Expect.ACCEPTABLE),
      // Both pollers succeed
      new Outcome(id = Array("1, 2, 1, 2"), expect = Expect.ACCEPTABLE),
      new Outcome(id = Array("1, 2, 2, 1"), expect = Expect.ACCEPTABLE)
    )
  )
  @State
  class PollNoNullsTest {
    val q                = new OneElementConcurrentQueue[Int]()
    var (o1, o2, p1, p2) = (-42, -42, -42, -42)

    @Actor
    def actor1(): Unit = {
      o1 = if (q.offer(1)) 1 else -1
      ()
    }

    @Actor
    def actor2(): Unit = {
      o2 = if (q.offer(2)) 2 else -2
      ()
    }

    @Actor
    def actor3(): Unit =
      p1 = q.poll(-3)

    @Actor
    def actor4(): Unit =
      p2 = q.poll(-4)

    @Arbiter
    def arbiter(r: IIII_Result): Unit = {
      r.r1 = o1
      r.r2 = o2
      r.r3 = p1
      r.r4 = p2
    }
  }
}
