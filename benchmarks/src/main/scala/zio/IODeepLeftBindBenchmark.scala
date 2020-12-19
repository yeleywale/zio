package zio

import org.openjdk.jmh.annotations._
import zio.IOBenchmarks._

import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class IODeepLeftBindBenchmark {
  @Param(Array("10000"))
  var depth: Int = _

  @Benchmark
  def monixDeepLeftBindBenchmark(): Int = {
    import monix.eval.Task

    var i  = 0
    var io = Task.eval(i)
    while (i < depth) {
      io = io.flatMap(i => Task.eval(i))
      i += 1
    }

    io.runSyncStep.fold(_ => sys.error("Either.right.get on Left"), identity)
  }

  @Benchmark
  def zioDeepLeftBindBenchmark(): Int = zioDeepLeftBindBenchmark(IOBenchmarks)

  @Benchmark
  def zioTracedDeepLeftBindBenchmark(): Int = zioDeepLeftBindBenchmark(TracedRuntime)

  def zioDeepLeftBindBenchmark(runtime: Runtime[Any]): Int = {
    var i  = 0
    var io = IO.effectTotal(i)
    while (i < depth) {
      io = io.flatMap(i => IO.effectTotal(i))
      i += 1
    }

    runtime.unsafeRun(io)
  }

  @Benchmark
  def catsDeepLeftBindBenchmark(): Int = {
    import cats.effect.IO

    var i  = 0
    var io = IO(i)
    while (i < depth) {
      io = io.flatMap(i => IO(i))
      i += 1
    }

    io.unsafeRunSync()
  }

}
