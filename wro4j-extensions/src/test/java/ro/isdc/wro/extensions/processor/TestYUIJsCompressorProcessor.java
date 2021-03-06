/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import ro.isdc.wro.extensions.AbstractWroTest;
import ro.isdc.wro.extensions.processor.js.YUIJsCompressorProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.util.WroTestUtils;
import ro.isdc.wro.util.WroUtil;


/**
 * Test YUI js compressor processor.
 *
 * @author Alex Objelean
 * @created Created on Nov 28, 2008
 */
public class TestYUIJsCompressorProcessor extends AbstractWroTest {
  private final ResourcePostProcessor processor = YUIJsCompressorProcessor.doMungeCompressor();

  @Test
  public void testWithMungeFromFolder()
    throws IOException {
    final ResourcePostProcessor processor = YUIJsCompressorProcessor.doMungeCompressor();

    final URL url = getClass().getResource("yui");
    final File sourceFolder = new File(url.getFile());
    WroTestUtils.compareSameFolderByExtension(sourceFolder, "js", "munge.js", WroUtil.newResourceProcessor(processor));
  }

  @Test
  public void testWithNoMungeFromFolder()
    throws IOException {
    final ResourcePostProcessor processor = YUIJsCompressorProcessor.noMungeCompressor();

    final URL url = getClass().getResource("yui");
    final File sourceFolder = new File(url.getFile());
    WroTestUtils.compareSameFolderByExtension(sourceFolder, "js", "nomunge.js", WroUtil.newResourceProcessor(processor));
  }

  @Test
  public void testInvalidJsShouldBeUnchanged()
    throws IOException {
    final String resourceUri = "classpath:" + WroUtil.toPackageAsFolder(getClass()) + "/invalid.js";
    compareProcessedResourceContents(resourceUri, resourceUri, WroUtil.newResourceProcessor(processor));
  }
}
