package nz.net.osnz.common.logging

import groovy.json.JsonSlurper
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.message.Message
import org.apache.logging.log4j.util.ReadOnlyStringMap
import spock.lang.Specification

/**
 * @author Kefeng Deng (https://bit.ly/2JFoCO1)
 */
class CloudJsonLayoutTests extends Specification {

  def "test the layout"() {
    given: "i have an instance"
      def layout = new CloudJsonLayout()

    when: "i replace the enhancers"
      layout.loggingProcessors = []

    and: "create a fake log event"
      def event = [
        getTimeMillis : { -> System.currentTimeMillis() },
        getContextData: { ->
          return [toMap: { -> [:] }] as ReadOnlyStringMap
        }
        ,
        getMessage    : { -> return [getFormattedMessage: { -> 'my message' }] as Message },
        getLevel      : { -> Level.ERROR },
        getLoggerName : { -> 'nz.co.blah' },
        getThreadName : { -> 'main' },
        getSource     : { -> new StackTraceElement('Blah', 'sausage', 'Blah.java', 42) },
        getThrownProxy: { -> return null }
      ] as LogEvent

    and: 'i pass it to the layout manager'
      def data = new JsonSlurper().parse(layout.toByteArray(event))

    then:
      data.message == event.message.formattedMessage
      data.severity == Level.ERROR.toString()
  }

}
