package nz.net.osnz.common.logging;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author Kefeng Deng (https://bit.ly/2JFoCO1)
 */
public class EnhancerServiceLoader {

  static List<JsonLogEnhancer> findJsonLogEnhancers() {
    List<JsonLogEnhancer> enhancers = new ArrayList<>();

    ServiceLoader<JsonLogEnhancer> serviceLoader = ServiceLoader.load(nz.net.osnz.common.logging.JsonLogEnhancer.class,
      Thread.currentThread().getContextClassLoader());

    for (JsonLogEnhancer jsonLogEnhancer : serviceLoader) {
      enhancers.add(jsonLogEnhancer);
    }

    // in place sort by priority
    enhancers.sort(new Comparator<JsonLogEnhancer>() {
      @Override
      public int compare(JsonLogEnhancer o1, JsonLogEnhancer o2) {
        return Integer.compare(o1.getMapPriority(), o2.getMapPriority());
      }
    });

    return enhancers;
  }

}
