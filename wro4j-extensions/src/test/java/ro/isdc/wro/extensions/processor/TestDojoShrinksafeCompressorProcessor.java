/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import ro.isdc.wro.extensions.AbstractWroTest;
import ro.isdc.wro.extensions.processor.js.DojoShrinksafeCompressorProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.util.WroTestUtils;
import ro.isdc.wro.util.WroUtil;


/**
 * Test Dojo Shrinksafe compressor processor.
 *
 * @author Alex Objelean
 * @created Created on Nov 6, 2010
 */
public class TestDojoShrinksafeCompressorProcessor extends AbstractWroTest {
  @Test
  public void testFromFolder()
    throws IOException {
    final ResourcePostProcessor processor = new DojoShrinksafeCompressorProcessor();
    final URL url = getClass().getResource("dojo");
    final File sourceFolder = new File(url.getFile());
    WroTestUtils.compareSameFolderByExtension(sourceFolder, "js", "pack.js", WroUtil.newResourceProcessor(processor));
  }
}
