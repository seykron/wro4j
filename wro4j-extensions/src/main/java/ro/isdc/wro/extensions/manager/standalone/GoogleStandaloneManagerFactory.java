/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.manager.standalone;

import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.manager.factory.standalone.DefaultStandaloneContextAwareManagerFactory;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.processor.impl.BomStripperPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;

import com.google.javascript.jscomp.CompilationLevel;


/**
 * A factory using google closure compressor for processing resources.
 *
 * @author Alex Objelean
 */
public class GoogleStandaloneManagerFactory extends DefaultStandaloneContextAwareManagerFactory {
  /**
   * {@inheritDoc}
   */
  @Override
  protected void configureProcessors(final GroupsProcessor groupsProcessor) {
    groupsProcessor.addPreProcessor(new BomStripperPreProcessor());
    groupsProcessor.addPreProcessor(new CssImportPreProcessor());
    groupsProcessor.addPreProcessor(new CssUrlRewritingProcessor());
    groupsProcessor.addPreProcessor(new SemicolonAppenderPreProcessor());
    groupsProcessor.addPreProcessor(new GoogleClosureCompressorProcessor(CompilationLevel.SIMPLE_OPTIMIZATIONS));
    groupsProcessor.addPreProcessor(new JawrCssMinifierProcessor());

    groupsProcessor.addPostProcessor(new CssVariablesProcessor());
  }
}
