/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.wildcard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.DuplicateResourceDetector;


/**
 * Default implementation of {@link WildcardStreamLocator}.
 *
 * @author Alex Objelean
 * @created May 8, 2010
 */
public class DefaultWildcardStreamLocator
    implements WildcardStreamLocator {
  /**
   * Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultWildcardStreamLocator.class);
  /**
   * Character to distinguish wildcard inside the uri. If the file name contains '*' or '?' character, it is considered
   * a wildcard.
   * <p>
   * A string is considered to contain wildcard if it doesn't start with http(s) and contains at least one of the
   * following characters: [?*].
   */
  private static final String WILDCARD_REGEX = "^(?:(?!http))(.)*[\\*\\?]+(.)*";
  /**
   * Character to distinguish wildcard inside the uri.
   */
  private static final String RECURSIVE_WILDCARD = "**";
  /**
   * Responsible for detecting duplicated resources.
   */
  private DuplicateResourceDetector duplicateResourceDetector;


  /**
   * Creates a WildcardStream locator capable of detecting duplicate resources.
   *
   * @param duplicateResourceDetector
   */
  public DefaultWildcardStreamLocator(final DuplicateResourceDetector duplicateResourceDetector) {
    this.duplicateResourceDetector = duplicateResourceDetector;
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasWildcard(final String uri) {
    return uri.matches(WILDCARD_REGEX);
  }

  /**
   * {@inheritDoc}
   */
  public InputStream locateStream(final String uri, final File folder)
      throws IOException {
    if (uri == null || folder == null || !folder.isDirectory()) {
      final StringBuffer message = new StringBuffer("Invalid folder provided");
      if (folder != null) {
        message.append(", with path: " + folder.getPath());
      }
      message.append(", with fileNameWithWildcard: " + uri);
      throw new IOException(message.toString());
    }
    if (!hasWildcard(uri)) {
      throw new IOException("No wildcard detected for the uri: " + uri);
    }

    final String wildcard = FilenameUtils.getName(uri);
    LOG.debug("uri: " + uri);
    LOG.debug("folder: " + folder.getPath());
    LOG.debug("wildcard: " + wildcard);

    //maps resource uri's and corresponding file
    //this map have to be ordered
    final Map<String, File> uriToFileMap = new LinkedHashMap<String, File>();

    final String uriFolder = FilenameUtils.getFullPathNoEndSeparator(uri);
    final String parentFolderPath = folder.getPath();

    final IOFileFilter fileFilter = new IOFileFilterDecorator(new WildcardFileFilter(wildcard)) {
      @Override
      public boolean accept(final File file) {
        final boolean accept = super.accept(file);
        if (accept && !file.isDirectory()) {
          final String relativeFilePath = file.getPath().replace(parentFolderPath, "");
          final String resourceUri = uriFolder + relativeFilePath.replace('\\', '/');
          uriToFileMap.put(resourceUri, file);
          LOG.debug("======\tfoundUri: " + resourceUri);
        }
        return accept;
      }
    };
    FileUtils.listFiles(folder, fileFilter, getFolderFilter(wildcard));

    //TODO remove duplicates if needed:
    LOG.error("map files: " + uriToFileMap.keySet());
    final List<String> duplicateResourceList = new ArrayList<String>();
    for (final String resourceUri : uriToFileMap.keySet()) {
      if (isDuplicatedResourceUri(resourceUri)) {
        LOG.warn("Duplicate resource detected: " + resourceUri);
        duplicateResourceList.add(resourceUri);
      }
    }
    //if (config.removeDuplicates) {
    for (final String duplicateResourceUri : duplicateResourceList) {
      uriToFileMap.remove(duplicateResourceUri);
    }

    final Collection<File> files = uriToFileMap.values();

    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    if (files.isEmpty()) {
      final String message = "No files found inside the " + folder.getPath() + " for wildcard: " + wildcard;
      LOG.warn(message);
    }
    for (final File file : files) {
      final InputStream is = new FileInputStream(file);
      IOUtils.copy(is, out);
    }
    out.close();
    return new ByteArrayInputStream(out.toByteArray());
  }

  /**
   * Check if the resourceUri is already duplicated and afterwards add it to processed uri's.
   *
   * @return true if the resource is duplicated in the context of the current processing.
   */
  private boolean isDuplicatedResourceUri(final String resourceUri) {
    if (duplicateResourceDetector == null) {
      throw new WroRuntimeException("duplicateResourceDetector was not injected!");
    }
    final boolean result = duplicateResourceDetector.isDuplicateResourceUri(resourceUri);
    duplicateResourceDetector.addResourceUri(resourceUri);
    return result;
  }

  /**
   * @param wildcard
   *          to use to determine if the folder filter should be recursive or not.
   * @return filter to be used for folders.
   */
  private IOFileFilter getFolderFilter(final String wildcard) {
    final boolean recursive = wildcard.contains(RECURSIVE_WILDCARD);
    return recursive ? TrueFileFilter.INSTANCE : FalseFileFilter.INSTANCE;
  }
}
