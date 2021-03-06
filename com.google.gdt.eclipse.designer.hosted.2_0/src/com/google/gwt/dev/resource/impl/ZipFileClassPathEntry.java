/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.google.gwt.dev.resource.impl;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.dev.util.collect.IdentityHashMap;
import com.google.gwt.dev.util.collect.IdentityHashSet;
import com.google.gwt.dev.util.collect.IdentityMaps;
import com.google.gwt.dev.util.collect.Sets;
import com.google.gwt.dev.util.msg.Message1String;

import java.io.File;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A classpath entry that is a jar or zip file.
 */
public class ZipFileClassPathEntry extends ClassPathEntry {

  /**
   * Logger messages related to this class.
   */
  private static class Messages {
    static final Message1String BUILDING_INDEX = new Message1String(
        TreeLogger.TRACE, "Indexing zip file: $0");

    static final Message1String EXCLUDING_RESOURCE = new Message1String(
        TreeLogger.DEBUG, "Excluding $0");

    static final Message1String FINDING_INCLUDED_RESOURCES = new Message1String(
        TreeLogger.DEBUG, "Searching for included resources in $0");

    static final Message1String INCLUDING_RESOURCE = new Message1String(
        TreeLogger.DEBUG, "Including $0");

    static final Message1String READ_ZIP_ENTRY = new Message1String(
        TreeLogger.DEBUG, "$0");
  }

  private static class ZipFileSnapshot {
    private final int prefixSetSize;
    private final Map<AbstractResource, PathPrefix> cachedAnswers;

    ZipFileSnapshot(int prefixSetSize,
        Map<AbstractResource, PathPrefix> cachedAnswers) {
      this.prefixSetSize = prefixSetSize;
      this.cachedAnswers = cachedAnswers;
    }
  }

  private Set<ZipFileResource> allZipFileResources;

  /**
   * Currently gwt has just 2 ResourceOracles.
   */
  private final Map<PathPrefixSet, ZipFileSnapshot> cachedSnapshots = new IdentityHashMap<PathPrefixSet, ZipFileSnapshot>();

  private String cachedLocation;
  private final ZipFile zipFile;

  public ZipFileClassPathEntry(ZipFile zipFile) {
    this.zipFile = zipFile;
  }

  /**
   * Indexes the zip file on-demand, and only once over the life of the process.
   */
  @Override
  public Map<AbstractResource, PathPrefix> findApplicableResources(
      TreeLogger logger, PathPrefixSet pathPrefixSet) {
    // Never re-index.
    if (allZipFileResources == null) {
      allZipFileResources = buildIndex(logger);
    }

    ZipFileSnapshot snapshot = cachedSnapshots.get(pathPrefixSet);
    if (snapshot == null || snapshot.prefixSetSize != pathPrefixSet.getSize()) {
      snapshot = new ZipFileSnapshot(pathPrefixSet.getSize(),
          computeApplicableResources(logger, pathPrefixSet));
      cachedSnapshots.put(pathPrefixSet, snapshot);
    }
    return snapshot.cachedAnswers;
  }

  @Override
  public String getLocation() {
    if (cachedLocation == null) {
      cachedLocation = new File(zipFile.getName()).toURI().toString();
    }
    return cachedLocation;
  }

  public ZipFile getZipFile() {
    return zipFile;
  }

  private Set<ZipFileResource> buildIndex(TreeLogger logger) {
    logger = Messages.BUILDING_INDEX.branch(logger, zipFile.getName(), null);

    Set<ZipFileResource> results = new IdentityHashSet<ZipFileResource>();
    Enumeration<? extends ZipEntry> e = zipFile.entries();
    while (e.hasMoreElements()) {
      ZipEntry zipEntry = e.nextElement();
      if (zipEntry.isDirectory()) {
        // Skip directories.
        continue;
      }
      if (zipEntry.getName().startsWith("META-INF/")) {
        // Skip META-INF since classloaders normally make this invisible.
        continue;
      }
      ZipFileResource zipResource = new ZipFileResource(this,
          zipEntry.getName());
      results.add(zipResource);
      Messages.READ_ZIP_ENTRY.log(logger, zipEntry.getName(), null);
    }
    return Sets.normalize(results);
  }

  private Map<AbstractResource, PathPrefix> computeApplicableResources(
      TreeLogger logger, PathPrefixSet pathPrefixSet) {
    logger = Messages.FINDING_INCLUDED_RESOURCES.branch(logger,
        zipFile.getName(), null);

    Map<AbstractResource, PathPrefix> results = new IdentityHashMap<AbstractResource, PathPrefix>();
    for (ZipFileResource r : allZipFileResources) {
      String path = r.getPath();
      PathPrefix prefix = null;
      if ((prefix = pathPrefixSet.includesResource(path, r.getPathParts() /* >>> Instantiations*/)) != null) {
        Messages.INCLUDING_RESOURCE.log(logger, path, null);
        results.put(r, prefix);
      } else {
        Messages.EXCLUDING_RESOURCE.log(logger, path, null);
      }
    }
    return IdentityMaps.normalize(results);
  }
}
