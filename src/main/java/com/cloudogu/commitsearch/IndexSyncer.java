/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cloudogu.commitsearch;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.search.Index;

import javax.inject.Inject;

@SuppressWarnings("UnstableApiUsage")
public class IndexSyncer {
  private static final Logger LOG = LoggerFactory.getLogger(IndexSyncer.class);

  private final RepositoryServiceFactory repositoryServiceFactory;
  private final IndexSyncWorkerFactory indexSyncWorkerFactory;

  @Inject
  public IndexSyncer(RepositoryServiceFactory repositoryServiceFactory, IndexSyncWorkerFactory indexSyncWorkerFactory) {
    this.repositoryServiceFactory = repositoryServiceFactory;
    this.indexSyncWorkerFactory = indexSyncWorkerFactory;
  }

  public void ensureIndexIsUpToDate(Index<IndexedChangeset> index, Repository repository, UpdatedChangesets changesets) {
    try (RepositoryService repositoryService = repositoryServiceFactory.create(repository)) {
      if (isSupported(repositoryService)) {
        ensureIndexIsUpToDate(index, repositoryService, changesets);
      } else {
        LOG.warn("repository {} could not index, because it does not support combined modifications", repository);
      }
    }
  }

  private boolean isSupported(RepositoryService repositoryService) {
    return repositoryService.isSupported(Command.CHANGESETS);
  }

  private void ensureIndexIsUpToDate(Index<IndexedChangeset> index, RepositoryService repositoryService, UpdatedChangesets changesets) {
    Stopwatch sw = Stopwatch.createStarted();
    try {
      IndexSyncWorker worker = indexSyncWorkerFactory.create(repositoryService, new Indexer(index, repositoryService));
      worker.ensureIndexIsUpToDate(changesets);
    } finally {
      LOG.debug("ensure index is up to date operation finished in {}", sw.stop());
    }
  }

}
