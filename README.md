# microbenchmarks

1. `mvn install:install-file -Dfile=ca-engine-sdk-test.jar -DgroupId=ca -DartifactId=ca -Dversion=0.0.1 -Dpackaging=jar`
1. `java -cp bench-1.0-jar-with-dependencies.jar com.github.bingoohuang.bench.RangeMinimum`

## Thanks to

1. [lemire/microbenchmarks](https://github.com/lemire/microbenchmarks)
1. [JMH benchmark with examples](https://javadevcentral.com/jmh-benchmark-with-examples)
1. [jmh-tutorial](https://github.com/guozheng/jmh-tutorial/blob/master/README.md)
    ```bash
    java -jar target/benchmarks.jar #default behavior -wi 20 -i 20 -f 10
    java -jar target/benchmarks.jar -wi 5 -i 5 -f 1
    ```
