package com.github.bingoohuang.bench;

import lombok.val;
import org.bjca.pki.module.provider.FishmanProvider;
import org.ca.engine.sdk.api.CaService;
import org.ca.engine.sdk.beans.Certification;
import org.ca.engine.sdk.exception.CaServiceException;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class CaServiceBench {
  // create a p10.sh with content like `export P10="abc"`
  // then exec `source p10.sh`
  // then check `echo $P10`

  public static String p10 = System.getenv("P10");
  public static CaService caService = new CaService();

  static {
    caService.init(new FishmanProvider());
  }

  @Threads(30)
  @Benchmark
  public void local() throws CaServiceException {
    Certification cert = caService.certRequest(p10, "bjca2", "sm2.xml");
  }

  @Threads(30)
  @Benchmark
  public void httpTomcat() {
    HttpRest rest = new HttpRest();
    val params = new HashMap<String, String>();
    params.put("p10", p10);
    rest.post("http://127.0.0.1:8088/fishman/cert/signCert", params);
  }

  @Threads(30)
  @Benchmark
  public void httpSpringboot() {
    HttpRest rest = new HttpRest();
    val params = new HashMap<String, String>();
    params.put("p10", p10);
    rest.post("http://127.0.0.1:8083", params);
  }

  public static void main(String[] args) throws RunnerException {
    Options opt =
        new OptionsBuilder()
            .include(CaServiceBench.class.getSimpleName())
            .warmupIterations(5)
            .measurementIterations(5)
            .forks(1)
            .build();

    new Runner(opt).run();
  }
}
