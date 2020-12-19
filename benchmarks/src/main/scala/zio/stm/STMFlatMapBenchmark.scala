package zio.stm

import org.openjdk.jmh.annotations._
import zio._

import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 15, timeUnit = TimeUnit.SECONDS, time = 10)
@Warmup(iterations = 15, timeUnit = TimeUnit.SECONDS, time = 10)
@Fork(1)
class STMFlatMapBenchmark {
  @Param(Array("20"))
  var depth: Int = _

  @Benchmark
  def deepFlatMap(): BigInt = {
    def fib(n: Int): STM[Nothing, BigInt] =
      if (n <= 1) STM.succeedNow[BigInt](n)
      else
        fib(n - 1).flatMap(a => fib(n - 2).flatMap(b => STM.succeedNow(a + b)))

    IOBenchmarks.unsafeRun(fib(depth).commit)
  }
}
