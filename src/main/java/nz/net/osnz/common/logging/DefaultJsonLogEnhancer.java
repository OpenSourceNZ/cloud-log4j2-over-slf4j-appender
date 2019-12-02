package nz.net.osnz.common.logging;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

/**
 * @author Kefeng Deng (https://bit.ly/2JFoCO1)
 */
public class DefaultJsonLogEnhancer implements JsonLogEnhancer {
  public static final String JSON_PREFIX = "json:";
  static int prefixLen = JSON_PREFIX.length();
  private final String hostName;

  public DefaultJsonLogEnhancer() {
    try {
      hostName = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int getMapPriority() {
    return 100;
  }

  @Override
  public void map(Map<String, String> context, Map<String, Object> log, List<String> alreadyEncodedJsonObjects) {
    log.put("host", hostName);

    context.entrySet().forEach(entry -> {
      if (entry.getKey().startsWith(JSON_PREFIX)) {
        alreadyEncodedJsonObjects.add(
          "\"" + entry.getKey().substring(prefixLen) + "\":" + entry.getValue());
      } else {
        log.put(entry.getKey(), entry.getValue());
      }
    });
  }

  @Override
  public void failed(Map<String, String> context, Map<String, Object> log, List<String> alreadyEncodedJsonObjects, Throwable e) {
    // meh
  }
}
