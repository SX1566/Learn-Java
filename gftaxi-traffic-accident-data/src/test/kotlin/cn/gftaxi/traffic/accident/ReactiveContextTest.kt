package cn.gftaxi.traffic.accident

import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import reactor.util.context.Context
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import java.util.*

/**
 * Test Reactive Context API.
 *
 * See ["8.8. Adding a Context to a Reactive Sequence"](http://projectreactor.io/docs/core/release/reference/#context)
 *
 * @author RJ
 */
class ReactiveContextTest {
  @Test
  fun `1 Base usage`() {
    val key = "message"
    val r = Mono.just("Hello")
      .flatMap { s -> Mono.subscriberContext().map { "$s ${it.get<Any>(key)}" } }
      .subscriberContext { it.put(key, "World") }

    StepVerifier.create(r)
      .expectNext("Hello World")
      .verifyComplete()
  }

  // the Context is immutable and its content can only be seen by operators above it
  @Test
  fun `2 Only be seen by operators above it`() {
    val key = "message"
    val r = Mono.just("Hello")
      .subscriberContext { it.put(key, "World") }
      .flatMap { s -> Mono.subscriberContext().map { "$s ${it.getOrDefault(key, "Stranger")}" } }

    StepVerifier.create(r)
      .expectNext("Hello Stranger")
      .verifyComplete()
  }

  // The key was never set to "Hello".
  @Test
  fun `3 No Context from downstream`() {
    val key = "message"
    val r = Mono.subscriberContext()
      .map { it.put(key, "Hello") }
      .flatMap { Mono.subscriberContext() }
      .map { it.getOrDefault(key, "Default") }

    StepVerifier.create(r)
      .expectNext("Default")
      .verifyComplete()
  }

  // operators reading the Context will see the value that was set closest to under them
  @Test
  fun `4 Reading Context from closest downstream`() {
    val key = "message"
    val r = Mono.just("Hello")
      .flatMap { s -> Mono.subscriberContext().map { "$s ${it.getOrDefault(key, "Stranger")}" } }
      .subscriberContext { it.put(key, "Reactor") }
      .subscriberContext { it.put(key, "World") }

    StepVerifier.create(r)
      .expectNext("Hello Reactor")
      .verifyComplete()
  }

  // the Context is associated to the Subscriber and each operator accesses the Context
  // by requesting it from its downstream Subscriber.
  @Test
  fun `5 Requesting Context from its downstream Subscriber`() {
    val key = "message"
    val r = Mono.just("Hello")
      .flatMap { s -> Mono.subscriberContext().map { "$s ${it.get<Any>(key)}" } }
      .subscriberContext { it.put(key, "Reactor") }
      .flatMap { s -> Mono.subscriberContext().map { "$s ${it.get<Any>(key)}" } }
      .subscriberContext { it.put(key, "World") }

    StepVerifier.create(r)
      .expectNext("Hello Reactor World")
      .verifyComplete()
  }

  // 1. This subscriberContext does not impact anything outside of its flatMap
  // 2. This subscriberContext impacts the main sequence’s Context
  @Test
  fun `6 Only impacts the main sequence`() {
    val key = "message"
    val r = Mono.just("Hello")
      .flatMap { s ->
        Mono.subscriberContext().map { "$s ${it.get<Any>(key)}" }
      }
      .flatMap { s ->
        Mono.subscriberContext().map { "$s ${it.get<Any>(key)}" }
          .subscriberContext { it.put(key, "Reactor") } // 1
      }
      .subscriberContext { it.put(key, "World") }       // 2

    StepVerifier.create(r)
      .expectNext("Hello World Reactor")
      .verifyComplete()
  }

  /** See ["8.8.5. Full Example"](http://projectreactor.io/docs/core/release/reference/#_full_example) */
  @Test
  fun `7 Full Example`() {
    val put = doPut("www.example.com", Mono.just("Walter"))
      .subscriberContext(Context.of(HTTP_CORRELATION_ID, "2-1111-3333"))
      .filter({ t -> t.t1 < 300 })
      .map { it.t2 }

    StepVerifier.create(put)
      .expectNext("PUT <Walter> sent to www.example.com with header X-Correlation-ID = 2-1111-3333")
      .verifyComplete()
  }

  private fun doPut(url: String, data: Mono<String>): Mono<Tuple2<Int, String>> {
    val dataAndContext: Mono<Tuple2<String, Optional<String>>> =
      data.zipWith(Mono.subscriberContext().map { it.getOrEmpty<String>(HTTP_CORRELATION_ID) })
    return dataAndContext.handle<String> { dac, sink ->
      if (dac.t2.isPresent) {
        sink.next("PUT <${dac.t1}> sent to $url with header X-Correlation-ID = ${dac.t2.get()}")
      } else {
        sink.next("PUT <${dac.t1}> sent to $url")
      }
      sink.complete()
    }.map { Tuples.of(200, it) }
  }

  companion object {
    private const val HTTP_CORRELATION_ID = "reactive.http.library.correlationId"
  }
}