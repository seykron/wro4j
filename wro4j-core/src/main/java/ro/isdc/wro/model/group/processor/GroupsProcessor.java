/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.group.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.util.StopWatch;


/**
 * Default group processor which perform preProcessing, merge and postProcessing on groups resources.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class GroupsProcessor
    extends AbstractGroupsProcessor {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(GroupsProcessor.class);


  /**
   * Removes the generic processors of type <T> which have Minimize annotation from the provided collection of
   * processors.
   *
   * @param <T>
   *          type of processor
   * @param processors
   *          a collection of processors.
   */
  static <T> void removeMinimizeAwareProcessors(final Collection<T> processors) {
    final Collection<T> minimizeAwareProcessors = new ArrayList<T>();
    for (final T processor : processors) {
      if (processor.getClass().isAnnotationPresent(Minimize.class)) {
        minimizeAwareProcessors.add(processor);
      }
    }
    processors.removeAll(minimizeAwareProcessors);
  }


  /**
   * {@inheritDoc}
   * <p>
   * While processing the resources, if any exception occurs - it is wrapped in a RuntimeException.
   */
  public String process(final Collection<Group> groups, final ResourceType type, final boolean minimize) {
    if (groups == null) {
      throw new IllegalArgumentException("List of groups cannot be null!");
    }
    if (type == null) {
      throw new IllegalArgumentException("ResourceType cannot be null!");
    }
    //Supress spurious duplicate resource detection on reload
    getDuplicateResourceDetector().reset();
    final StopWatch stopWatch = new StopWatch();
    stopWatch.start("filter resources");
    // TODO find a way to reuse contents from cache
    final List<Resource> filteredResources = getFilteredResources(groups, type);
    try {
      stopWatch.stop();
      stopWatch.start("pre process and merge");
      // Merge
      final String result = getPreProcessorExecutor().processAndMerge(filteredResources, minimize);
      stopWatch.stop();
      stopWatch.start("post process");
      // postProcessing
      final String postProcessedResult = applyPostProcessors(type, result, minimize);
      stopWatch.stop();
      LOG.debug(stopWatch.prettyPrint());
      return postProcessedResult;
    } catch (final IOException e) {
      throw new WroRuntimeException("Exception while merging resources", e);
    }
  }

  /**
   * Perform postProcessing.
   *
   * @param resourceType
   *          the type of the resources to process. This value will never be null.
   * @param content
   *          the merged content of all resources which were pre-processed.
   * @param minimize
   *          whether minimize aware post processor must be applied.
   * @return the post processed contents.
   */
  private String applyPostProcessors(final ResourceType resourceType, final String content, final boolean minimize) throws IOException {
    if (content == null) {
      throw new IllegalArgumentException("content cannot be null!");
    }
    final Collection<ResourcePostProcessor> processors = getPostProcessorsByType(resourceType);
    processors.addAll(getPostProcessorsByType(null));
    if (!minimize) {
      removeMinimizeAwareProcessors(processors);
    }
    final String output = applyPostProcessors(processors, content);
    return output;
  }


  /**
   * Apply resourcePostProcessors.
   *
   * @param processors
   *          a collection of processors to apply on the content from the supplied writer.
   * @param content
   *          to process with all postProcessors.
   * @return the post processed content.
   */
  private String applyPostProcessors(final Collection<ResourcePostProcessor> processors, final String content) throws IOException {
    if (processors.isEmpty()) {
      return content;
    }
    Reader input = new StringReader(content.toString());
    Writer output = null;
    final StopWatch stopWatch = new StopWatch();
    for (final ResourcePostProcessor processor : processors) {
      stopWatch.start("Using " + processor.getClass().getSimpleName());
      output = new StringWriter();
      processor.process(input, output);
      input = new StringReader(output.toString());
      stopWatch.stop();
    }
    LOG.debug(stopWatch.prettyPrint());
    return output.toString();
  }
}
