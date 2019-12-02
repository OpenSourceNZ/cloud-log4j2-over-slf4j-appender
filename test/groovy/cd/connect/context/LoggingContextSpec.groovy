package cd.connect.context

import spock.lang.Specification

import java.util.function.Consumer
import java.util.function.Supplier

/**
 *
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
class LoggingContextSpec extends Specification {
  def "static loading of logging context works as expected"() {
    when: "i set the request id"
      nz.net.osnz.common.context.ConnectContext.requestId.set("x")
    and: "the scenario id"
      nz.net.osnz.common.context.ConnectContext.scenarioId.set("y")
    then:
      nz.net.osnz.common.context.ConnectContext.requestId.get() == "x"
      nz.net.osnz.common.context.ConnectContext.scenarioId.get() == "y"
  }

  def "async consumer context works as expected"() {
    given: "an existing context"
      assert nz.net.osnz.common.context.ConnectContext.get("a") == null
      nz.net.osnz.common.context.ConnectContext.set("a", "b")
    when: "i generate a consumer wrapper"
      def called = false
      Consumer<Void> data = new Consumer<Void>() {
        @Override
        void accept(Void aVoid) {
          called = true
          assert nz.net.osnz.common.context.ConnectContext.get("a") == "b"
        }
      }
    and: "i pass it into the context"
      Consumer<Void> copy = nz.net.osnz.common.context.ContextAsync.chainContext(data)
    and: "i clean the context"
      nz.net.osnz.common.context.ConnectContext.clear()
      assert nz.net.osnz.common.context.ConnectContext.get("a") == null
    and: "i accept the context copy"
      copy.accept(null)
    then: "called is true"
       called == true
  }

  def "async supplier context works as expected"() {
    given: "an existing context"
      assert nz.net.osnz.common.context.ConnectContext.get("a") == null
      nz.net.osnz.common.context.ConnectContext.set("a", "b")
    when: "i generate a consumer wrapper"
      def called = false
      Supplier<Void> data = new Supplier<Void>() {
        @Override
        Void get() {
          called = true
          assert nz.net.osnz.common.context.ConnectContext.get("a") == "b"
          return null
        }
      }
    and: "i pass it into the context"
    Supplier<Void> copy = nz.net.osnz.common.context.ContextAsync.chainContext(data)
    and: "i clean the context"
      nz.net.osnz.common.context.ConnectContext.clear()
      assert nz.net.osnz.common.context.ConnectContext.get("a") == null
    and: "i accept the context copy"
      copy.get()
    then: "called is true"
      called == true
  }

  def "async runnable context works as expected"() {
    given: "an existing context"
      assert nz.net.osnz.common.context.ConnectContext.get("a") == null
      nz.net.osnz.common.context.ConnectContext.set("a", "b")
    when: "i generate a consumer wrapper"
      def called = false
      Runnable data = new Runnable() {
        @Override
        void run() {
          called = true
          assert nz.net.osnz.common.context.ConnectContext.get("a") == "b"
        }
      }
    and: "i pass it into the context"
      Runnable copy = nz.net.osnz.common.context.ContextAsync.chainContext(data)
    and: "i clean the context"
      nz.net.osnz.common.context.ConnectContext.set("a", "z")
      assert nz.net.osnz.common.context.ConnectContext.get("a") == "z"
    and: "i accept the context copy"
      copy.run()
    then: "called is true"
      called == true
  }

}
