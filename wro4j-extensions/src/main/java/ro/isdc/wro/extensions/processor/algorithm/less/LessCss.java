/*
 *  Copyright 2010.
 */
package ro.isdc.wro.extensions.processor.algorithm.less;

import java.io.IOException;
import java.io.InputStream;

import org.mozilla.javascript.RhinoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.script.RhinoScriptBuilder;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;


/**
 * This class is inspired from Richard Nichols visural project.
 *
 * @author Alex Objelean
 */
public class LessCss {
  private static final Logger LOG = LoggerFactory.getLogger(LessCss.class);

  public LessCss() {}


  /**
   * Initialize script builder for evaluation.
   */
  private RhinoScriptBuilder initScriptBuilder() {
    try {
      final String SCRIPT_LESS = "less-1.0.41.min.js";
      final InputStream lessStream = getClass().getResourceAsStream(SCRIPT_LESS);
      final String SCRIPT_RUN = "run.js";
      final InputStream runStream = getClass().getResourceAsStream(SCRIPT_RUN);

      return RhinoScriptBuilder.newClientSideAwareChain().evaluateChain(lessStream, SCRIPT_LESS).evaluateChain(
        runStream, SCRIPT_RUN);
    } catch (final IOException ex) {
      throw new IllegalStateException("Failed reading javascript less.js", ex);
    } catch (final Exception e) {
      LOG.error("Processing error:" + e.getMessage(), e);
      throw new WroRuntimeException("Processing error", e);
    }
  }


  /**
   * @param data css content to process.
   * @return processed css content.
   */
  public String less(final String data) {
    final StopWatch stopWatch = new StopWatch();
    stopWatch.start("initContext");
    final RhinoScriptBuilder builder = initScriptBuilder();
    stopWatch.stop();

    stopWatch.start("lessify");
    try {
      final String execute = "lessIt(" + WroUtil.toJSMultiLineString(data) + ");";
      final Object result = builder.evaluate(execute, "lessIt");
      return String.valueOf(result);
    } catch (final RhinoException e) {
      throw new WroRuntimeException("Could not execute the script because: " + e.getMessage(), e);
    } finally {
      stopWatch.stop();
      LOG.debug(stopWatch.prettyPrint());
    }
  }
}
