/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.locator.wildcard.WildcardUriLocatorSupport;
import ro.isdc.wro.util.StringUtils;


/**
 * Implementation of the {@link UriLocator} that is able to read a resource from a classpath.
 *
 * @author Alex Objelean
 * @created Created on Nov 6, 2008
 */
public class ClasspathUriLocator
    extends WildcardUriLocatorSupport {
  /**
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ClasspathUriLocator.class);

  /**
   * Prefix of the resource uri used to check if the resource can be read by this {@link UriLocator} implementation.
   */
  public static final String PREFIX = "classpath:";

  /**
   * {@inheritDoc}
   */
  public boolean accept(final String url) {
    return isValid(url);
  }

  /**
   * Check if a uri is a classpath resource.
   *
   * @param uri
   *          to check.
   * @return true if the uri is a classpath resource.
   */
  public static boolean isValid(final String uri) {
    return uri.trim().startsWith(PREFIX);
  }

  /**
   * {@inheritDoc}
   */
  public InputStream locate(final String uri)
      throws IOException {
    if (uri == null) {
      throw new IllegalArgumentException("URI cannot be NULL!");
    }
    LOG.debug("Reading uri: " + uri);
    // replace prefix & clean path by removing '..' characters if exists and
    // normalizing the location to use.
    final String location = StringUtils.cleanPath(uri.replaceFirst(PREFIX, "")).trim();

    if (getWildcardStreamLocator().hasWildcard(location)) {
      return locateWildcardStream(uri, location);
    }
    final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(location);
    if (is == null) {
      throw new IOException("Couldn't get InputStream from this resource: " + uri);
    }
    return is;
  }

  /**
   * @return an input stream for an uri containing a wildcard for a given location.
   */
  private InputStream locateWildcardStream(final String uri, final String location)
      throws IOException {
    LOG.debug("wildcard detected for location: " + location);
    // prefix with '/' because we use class relative resource retrieval. Using ClassLoader.getSystemResource doesn't
    // work well.
    final String fullPath = "/" + FilenameUtils.getFullPathNoEndSeparator(location);
    final URL url = getClass().getResource(fullPath);
    if (url == null) {
      final String message = "Couldn't get URL for the following path: " + fullPath;
      LOG.warn(message);
      throw new IOException(message);
    }
    return getWildcardStreamLocator().locateStream(uri, new File(url.getFile()));
  }
}
