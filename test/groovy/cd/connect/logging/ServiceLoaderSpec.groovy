package cd.connect.logging

import spock.lang.Specification

/**
 *
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
class ServiceLoaderSpec extends Specification {
  def "finds the default"() {
    when: "i ask for the services"
      def services = nz.net.osnz.common.logging.EnhancerServiceLoader.findJsonLogEnhancers()
    then:
      services.size() == 1
      services[0] instanceof nz.net.osnz.common.logging.DefaultJsonLogEnhancer
  }
}
