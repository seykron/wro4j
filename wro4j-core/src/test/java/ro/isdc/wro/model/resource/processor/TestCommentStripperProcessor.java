/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Test;

import ro.isdc.wro.AbstractWroTest;
import ro.isdc.wro.model.resource.processor.impl.CommentStripperProcessor;
import ro.isdc.wro.util.ResourceProcessor;

/**
 * TestMultiLineCommentStripperPostProcessor.java.
 *
 * @author Alex Objelean
 */
public class TestCommentStripperProcessor extends AbstractWroTest {
  private final ResourcePostProcessor processor = new CommentStripperProcessor();

  @Test
  public void test() throws IOException {
    compareProcessedResourceContents(
        "classpath:ro/isdc/wro/processor/comment-input.js",
        "classpath:ro/isdc/wro/processor/comment-output.js",
        new ResourceProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            processor.process(reader, writer);
          }
        });
  }
}
