/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.AbstractWroTest;
import ro.isdc.wro.model.resource.processor.impl.css.VariablizeColorsCssProcessor;
import ro.isdc.wro.util.ResourceProcessor;
import ro.isdc.wro.util.WroUtil;


/**
 * TestVariablizeColorsCssProcessor.
 *
 * @author Alex Objelean
 * @created Created on Aug 15, 2010
 */
public class TestVariablizeColorsCssProcessor extends AbstractWroTest {
  private ResourcePostProcessor processor;

  @Before
  public void setUp() {
    processor = new VariablizeColorsCssProcessor();
  }

  @Test
  public void testVariablizeColors()
    throws IOException {
    compareProcessedResourceContents("classpath:" + WroUtil.toPackageAsFolder(getClass()) + "/variablizeColors-input.css",
      "classpath:" + WroUtil.toPackageAsFolder(getClass()) + "/variablizeColors-output.css", new ResourceProcessor() {
        public void process(final Reader reader, final Writer writer)
          throws IOException {
          processor.process(reader, writer);
        }
      });
  }
}
