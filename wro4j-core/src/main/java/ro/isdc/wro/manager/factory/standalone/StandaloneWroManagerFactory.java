/*
 * Copyright (c) 2009.
 */
package ro.isdc.wro.manager.factory.standalone;

import javax.servlet.ServletContext;

import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;

/**
 * This factory will create a WroManager which is able to run itself outside of
 * a webContainer.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class StandaloneWroManagerFactory extends BaseWroManagerFactory {
  /**
   * {@inheritDoc}
   */
  @Override
  protected WroModelFactory newModelFactory(final ServletContext servletContext) {
    return new XmlModelFactory();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected WroManager newManager() {
    return new WroManager() {
      /**
       * Just return false, without checking the request headers.
       */
      @Override
      protected boolean isGzipSupported() {
        return false;
      }
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void configureUriLocatorFactory(final UriLocatorFactory factory) {
    factory.addUriLocator(newServletContextUriLocator());
    factory.addUriLocator(new ClasspathUriLocator());
    factory.addUriLocator(new UrlUriLocator());
  }

  /**
   * @return {@link ServletContextUriLocator} or a derivate locator which will be responsible for locating resources
   *         starting with '/' character. Ex: /static/resource.js
   */
  protected ServletContextUriLocator newServletContextUriLocator() {
    return new ServletContextUriLocator();
  }
}
