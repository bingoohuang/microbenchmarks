package com.github.bingoohuang.bench;

import com.github.alexeyr.pcg.Pcg32;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class RandomGenerator {

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    Random r = new Random();
    long seed = System.nanoTime();
    Pcg32 rnd = new Pcg32();

    public int manualJavaNext(int bits) {
      seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
      return (int) (seed >>> (48 - bits));
    }
  }

  @Benchmark
  public int basicJavaRandom(BenchmarkState s) {
    return s.r.nextInt();
  }

  @Benchmark
  public int basicJavaRandomRanged1000(BenchmarkState s) {
    return s.r.nextInt(1000);
  }

  @Benchmark
  public int manualJavaRandom(BenchmarkState s) {
    return s.manualJavaNext(32);
  }

  @Benchmark
  public int basicThreadLocalRandom(BenchmarkState s) {
    return ThreadLocalRandom.current().nextInt();
  }

  @Benchmark
  public int basicThreadLocalRandomRanged1000(BenchmarkState s) {
    return ThreadLocalRandom.current().nextInt(1000);
  }

  @Benchmark
  public int pcgJavaRandom(BenchmarkState s) {
    return s.rnd.nextInt();
  }

  @Benchmark
  public int pcgJavaRandomRanged1000(BenchmarkState s) {
    return s.rnd.nextInt(1000);
  }

  public static void main(String[] args) throws RunnerException {
    Options opt =
        new OptionsBuilder()
            .include(RandomGenerator.class.getSimpleName())
            .warmupIterations(2)
            .measurementIterations(3)
            .forks(1)
            .build();

    new Runner(opt).run();
  }
}
