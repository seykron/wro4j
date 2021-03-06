/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.factory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.DuplicateResourceDetector;
import ro.isdc.wro.model.resource.locator.UriLocator;


/**
 * Default implementation of UriLocator. Holds a list of uri locators. The uriLocator will be created based on the first
 * uriLocator from the supplied list which will accept the url.
 *
 * @author Alex Objelean
 * @created Created on Nov 4, 2008
 */
public class UriLocatorFactory {
  private static final Logger LOG = LoggerFactory.getLogger(UriLocatorFactory.class);
  private final List<UriLocator> uriLocators = new ArrayList<UriLocator>();
  private final DuplicateResourceDetector duplicateResourceDetector;


  public UriLocatorFactory(final DuplicateResourceDetector duplicateResourceDetector) {
    this.duplicateResourceDetector = duplicateResourceDetector;
  }


  /**
   * Locates an InputStream for the given uri.
   *
   * @param uri to locate.
   * @return {@link InputStream} of the resource.
   * @throws IOException if uri is invalid or resource couldn't be located.
   */
  public InputStream locate(final String uri)
    throws IOException {
    final UriLocator uriLocator = getInstance(uri);
    if (uriLocator == null) {
      throw new IOException("No locator is capable of handling uri: " + uri);
    }
    LOG.debug("locating uri: " + uri + ", using locator: " + uriLocator);
    return new AutoCloseInputStream(uriLocator.locate(uri));
  }


  /**
   * @param uri to handle by the locator.
   * @return an instance of {@link UriLocator} which is capable of handling provided uri. Returns null if no locator
   *         found.
   */
  private UriLocator getInstance(final String uri) {
    for (final UriLocator uriLocator : uriLocators) {
      if (uriLocator.accept(uri)) {
        return uriLocator;
      }
    }
    return null;
  }


  /**
   * Add a single resource to the list of supported resource locators.
   *
   * @param uriLocator {@link UriLocator} object to add.
   */
  private final void addUriLocator(final UriLocator uriLocator) {
    if (uriLocator == null) {
      throw new IllegalArgumentException("ResourceLocator cannot be null!");
    }
    processInjectAnnotation(uriLocator);
    // inject duplicateResourceDetector
    uriLocators.add(uriLocator);
  }


  /**
   * Add a
   *
   * @param locators
   */
  public final void addUriLocator(final UriLocator... locators) {
    for (final UriLocator locator : locators) {
      addUriLocator(locator);
    }
  }


  /**
   * Check for each field from the passed object if @Inject annotation is present & inject the required field if
   * supported, otherwise warns about invalid usage.
   *
   * @param locator object to check for annotation presence.
   */
  // TODO move this method to WroUtils
  private void processInjectAnnotation(final Object locator) {
    try {
      final Collection<Field> fields = getAllFields(locator);
      for (final Field field : fields) {
        if (field.isAnnotationPresent(Inject.class)) {
          if (field.getType() != DuplicateResourceDetector.class) {
            throw new IllegalStateException("@Inject can be applied only on fields of "
              + DuplicateResourceDetector.class.getName() + " type");
          }
          if (duplicateResourceDetector == null) {
            throw new IllegalStateException(DuplicateResourceDetector.class.getSimpleName() + " cannot be null!");
          }
          LOG.debug("Injecting " + DuplicateResourceDetector.class.getSimpleName() + " in the locator: "
            + locator.getClass().getSimpleName());
          field.setAccessible(true);
          field.set(locator, duplicateResourceDetector);
        }
      }
    } catch (final Exception e) {
      throw new WroRuntimeException("Exception while trying to process Inject annotation", e);
    }
  }


  /**
   * Return all fields for given object, also those from the super classes.
   */
  private Collection<Field> getAllFields(final Object object) {
    final Collection<Field> fields = new ArrayList<Field>();
    fields.addAll(Arrays.asList(object.getClass().getDeclaredFields()));
    // inspect super classes
    Class<?> superClass = object.getClass().getSuperclass();
    do {
      fields.addAll(Arrays.asList(superClass.getDeclaredFields()));
      superClass = superClass.getSuperclass();
    } while (superClass != null);
    return fields;
  }
}
