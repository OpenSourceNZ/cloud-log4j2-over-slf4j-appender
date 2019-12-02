package nz.net.osnz.common.logging;

import nz.net.osnz.common.jackson.JacksonHelper;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.ExtendedStackTraceElement;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.layout.AbstractLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Kefeng Deng (https://bit.ly/2JFoCO1)
 */
@Plugin(
  name = "CloudJsonLayout",
  category = "Core",
  elementType = "layout",
  printObject = true
)
public class CloudJsonLayout extends AbstractLayout<LogEvent> {
  private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
  protected List<JsonLogEnhancer> loggingProcessors = EnhancerServiceLoader.findJsonLogEnhancers();

  public CloudJsonLayout() {
    super(null, null, null);
  }

  @Override
  public byte[] toByteArray(LogEvent logEvent) {
    Map<String, Object> jsonContext = new HashMap<>();
    List<String> alreadyEncodedJsonObjects = new ArrayList<>();
    Map<String, String> processContext = new HashMap<>();

    // copy data out of read only map from ThreadContext (i.e. MDC)
    processContext.putAll(logEvent.getContextData().toMap());

    try {
      jsonContext.put("@timestamp", sdf.format(new Date(logEvent.getTimeMillis())));
      jsonContext.put("message", logEvent.getMessage().getFormattedMessage());
      jsonContext.put("priority", logEvent.getLevel().toString());
      jsonContext.put("severity", logEvent.getLevel().toString());
      jsonContext.put("path", logEvent.getLoggerName());
      jsonContext.put("thread", logEvent.getThreadName());

      if (logEvent.getSource() != null) {
        jsonContext.put("class", logEvent.getSource().getClassName());
        jsonContext.put("file", "${logEvent.source.fileName}:${logEvent.source.lineNumber}");
        jsonContext.put("method", logEvent.getSource().getMethodName());
      }

      if (logEvent.getThrownProxy() != null) {
        jsonContext.put("stack_trace", prettyPrintStackTrace(logEvent.getThrownProxy()));
      }

      loggingProcessors.forEach((JsonLogEnhancer p) -> p.map(processContext, jsonContext, alreadyEncodedJsonObjects));

      String json = JacksonHelper.serialize(jsonContext);
      if (alreadyEncodedJsonObjects != null && alreadyEncodedJsonObjects.size() > 0) {
        json = json.substring(0, json.length() - 1) + "," + String.join(",", alreadyEncodedJsonObjects) + "}";
      }

      return (json + "\n").getBytes();
    } catch (Exception ex) {
      try {
        loggingProcessors.forEach((JsonLogEnhancer p) -> p.failed(processContext, jsonContext, alreadyEncodedJsonObjects, ex));
      } finally {
        ex.printStackTrace();
      }
    }

    return new byte[0];
  }

  // extracted from key bits of the log4j2 libs
  private static String prettyPrintStackTrace(ThrowableProxy throwable) {
    final String offset = "\n\t";
    final StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(String.format("%s: %s%s%s",
      throwable.getName(),
      throwable.getMessage(),
      offset,
      String.join(offset, Arrays.stream(throwable.getExtendedStackTrace()).map(p -> p.toString()).collect(Collectors.toList()))));

    ThrowableProxy cause = throwable.getCauseProxy();
    while (cause != null) {
      stringBuilder.append(String.format("%sCaused by: %s: %s%s%s",
        offset,
        cause.getName(),
        cause.getMessage(),
        offset,
        String.join(offset, Arrays.stream(throwable.getExtendedStackTrace()).map(ExtendedStackTraceElement::toString).collect(Collectors.toList()))
      ));
      cause = cause.getCauseProxy();
    }
    return stringBuilder.toString();
  }

  @Override
  public LogEvent toSerializable(LogEvent logEvent) {
    return logEvent;
  }

  @Override
  public String getContentType() {
    return "application/octet-stream";
  }

  @PluginFactory
  public static CloudJsonLayout createLayout() {
    return new CloudJsonLayout();
  }

}
