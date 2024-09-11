/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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

import jakarta.inject.Inject;

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
