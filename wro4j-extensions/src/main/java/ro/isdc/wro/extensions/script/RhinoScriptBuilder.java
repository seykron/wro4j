/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.ToolErrorReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Used to evaluate javascript on the serverside using rhino javascript engine. Encapsulate and hides all implementation
 * details used by rhino to evaluate javascript on the serverside.
 *
 * @author Alex Objelean
 */
/**
 * @author Admin
 *
 */
public class RhinoScriptBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(RhinoScriptBuilder.class);
  private Context context;
  private Scriptable scope;


  /**
   * Constructor.
   */
  private RhinoScriptBuilder() {
    initContext();
  }


  /**
   * Initialize the context.
   */
  private void initContext() {
    // remove any existing context.
    if (Context.getCurrentContext() != null) {
      Context.exit();
    }
    context = Context.enter();
    context.setOptimizationLevel(-1);
    // TODO redirect errors from System.err to LOG.error()
    context.setErrorReporter(new ToolErrorReporter(false));
    context.setLanguageVersion(Context.VERSION_1_7);
    scope = context.initStandardObjects();
    try {
      final InputStream script = getClass().getResourceAsStream("commons.min.js");
      evaluate(script, "common.js");
    } catch (final IOException e) {
      throw new RuntimeException("Problem while evaluationg commons script.", e);
    }
  }


  /**
   * Add a clinet side environment to the script context (client-side aware).
   *
   * @return {@link RhinoScriptBuilder} used to chain evaluation of the scripts.
   * @throws IOException
   */
  public RhinoScriptBuilder addClientSideEnvironment() {
    try {
      final String SCRIPT_ENV = "env.rhino.min.js";
      final InputStream script = getClass().getResourceAsStream(SCRIPT_ENV);
      evaluate(script, SCRIPT_ENV);
      return this;
    } catch (final IOException e) {
      throw new RuntimeException("Couldn't initialize env.rhino script", e);
    }
  }


  public RhinoScriptBuilder addJSON() {
    try {
      final String SCRIPT_ENV = "json2.min.js";
      final InputStream script = getClass().getResourceAsStream(SCRIPT_ENV);
      evaluate(script, SCRIPT_ENV);
      return this;
    } catch (final IOException e) {
      throw new RuntimeException("Couldn't initialize json2.min.js script", e);
    }
  }


  /**
   * Evaluates a script and return {@link RhinoScriptBuilder} for a chained script evaluation.
   *
   * @param stream {@link InputStream} of the script to evaluate.
   * @param sourceName the name of the evaluated script.
   * @return evaluated object.
   * @throws IOException if the script couldn't be retrieved.
   */
  public RhinoScriptBuilder evaluateChain(final InputStream stream, final String sourceName)
    throws IOException {
    try {
      context.evaluateReader(scope, new InputStreamReader(stream), sourceName, 1, null);
      return this;
    } finally {
      stream.close();
    }
  }


  /**
   * Evaluates a script and return {@link RhinoScriptBuilder} for a chained script evaluation.
   *
   * @param script the string representation of the script to evaluate.
   * @param sourceName the name of the evaluated script.
   * @return evaluated object.
   * @throws IOException if the script couldn't be retrieved.
   */
  public RhinoScriptBuilder evaluateChain(final String script, final String sourceName) {
    context.evaluateString(scope, script, sourceName, 1, null);
    return this;
  }


  /**
   * Evaluates a script from a stream.
   *
   * @param script {@link InputStream} of the script to evaluate.
   * @param sourceName the name of the evaluated script.
   * @return evaluated object.
   * @throws IOException if the script couldn't be retrieved.
   */
  public Object evaluate(final InputStream stream, final String sourceName)
    throws IOException {
    try {
      return context.evaluateReader(scope, new InputStreamReader(stream), sourceName, 1, null);
    } catch (final JavaScriptException e) {
      LOG.error("JavaScriptException occured: " + e.getMessage());
      throw e;
    } finally {
      stream.close();
    }
  }


  /**
   * Evaluates a script from a reader.
   *
   * @param reader {@link Reader} of the script to evaluate.
   * @param sourceName the name of the evaluated script.
   * @return evaluated object.
   * @throws IOException if the script couldn't be retrieved.
   */
  public Object evaluate(final Reader reader, final String sourceName)
    throws IOException {
    try {
      return context.evaluateReader(scope, reader, sourceName, 1, null);
    } catch (final JavaScriptException e) {
      LOG.error("JavaScriptException occured: " + e.getMessage());
      throw e;
    } finally {
      reader.close();
    }
  }


  /**
   * Evaluates a script.
   *
   * @param script string representation of the script to evaluate.
   * @param sourceName the name of the evaluated script.
   * @return evaluated object.
   * @throws IOException if the script couldn't be retrieved.
   */
  public Object evaluate(final String script, final String sourceName) {
    try {
      return context.evaluateString(scope, script, sourceName, 1, null);
    } catch (final JavaScriptException e) {
      LOG.error("JavaScriptException occured: " + e.getMessage());
      throw e;
    }
  }


  /**
   * @return default {@link RhinoScriptBuilder} for script evaluation chaining.
   */
  public static RhinoScriptBuilder newChain() {
    return new RhinoScriptBuilder();
  }


  /**
   * @return default {@link RhinoScriptBuilder} for script evaluation chaining.
   */
  public static RhinoScriptBuilder newClientSideAwareChain() {
    return new RhinoScriptBuilder().addClientSideEnvironment();
  }
}
