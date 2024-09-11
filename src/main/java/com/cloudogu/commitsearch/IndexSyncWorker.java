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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;

import java.util.Optional;

class IndexSyncWorker {

  private static final Logger LOG = LoggerFactory.getLogger(IndexSyncWorker.class);

  private final IndexStatusStore indexStatusStore;
  private final Indexer indexer;
  private final RepositoryService repositoryService;
  private final Repository repository;

  IndexSyncWorker(IndexingContext indexingContext) {
    this.indexStatusStore = indexingContext.getIndexStatusStore();
    this.indexer = indexingContext.getIndexer();
    this.repositoryService = indexingContext.getRepositoryService();
    this.repository = repositoryService.getRepository();
  }

  public void ensureIndexIsUpToDate(UpdatedChangesets changesets) {
    Optional<IndexStatus> status = indexStatusStore.get(repository);
    if (status.isPresent()) {
      IndexStatus indexStatus = status.get();
      if (indexStatus.getVersion() != IndexedChangeset.VERSION) {
        LOG.debug(
          "found index of repository {} in version {} required is {}, trigger reindex",
          repository, indexStatus.getVersion(), IndexedChangeset.VERSION
        );
        reIndex();
      } else if (indexStatus.isEmpty()) {
        LOG.trace("no previous index found for repository {}, trigger reindex", repository);
        reIndex();
      } else {
        LOG.trace("previous index exists for repository {}, trigger update", repository);
        ensureIndexIsUpToDate(indexStatus.getRevision(), changesets);
      }
    } else {
      LOG.debug("no index status present for repository {} trigger reindex", repository);
      reIndex();
    }
  }

  private void ensureIndexIsUpToDate(String revision, UpdatedChangesets changesets) {
    Optional<Changeset> latestChangeset = repositoryService.getChangesetsCommand().getLatestChangeset();
    if (latestChangeset.isPresent()) {
      LOG.trace("found latest changeset for repository {} with id {}", repository, latestChangeset.get().getId());
      ensureIndexIsUpToDate(revision, latestChangeset.get().getId(), changesets, latestChangeset.get());
    } else {
      LOG.trace("no commits found for repository {}", repository);
      emptyRepository();
    }
  }

  private void ensureIndexIsUpToDate(String from, String to, UpdatedChangesets changesets, Changeset latestChangeset) {
    if (noChangesDetected(from, to, changesets)) {
      LOG.debug("index of repository {} is up to date", repository);
      return;
    }
    if (changesets.isEmpty()) {
      throw new IllegalArgumentException("A new 'top changeset' without updated changesets is not possible");
    }

    indexer.delete(changesets.getRemovedChangesets());
    indexer.store(changesets.getAddedChangesets());
    indexStatusStore.update(repository, latestChangeset.getId());
  }

  private static boolean noChangesDetected(String from, String to, UpdatedChangesets changesets) {
    return from.equals(to) && (changesets == null || changesets.isEmpty());
  }

  void reIndex() {
    LOG.debug("start reindexing for repository {}", repository);
    indexer.deleteAll();

    if (repositoryService.isSupported(Command.CHANGESETS)) {
      Optional<Changeset> latestChangeset = repositoryService.getChangesetsCommand().getLatestChangeset();
      if (latestChangeset.isPresent()) {
        indexer.store(repositoryService.getChangesetsCommand().getChangesets());
        indexStatusStore.update(repository, latestChangeset.get().getId());
      } else {
        indexStatusStore.empty(repository);
      }
    }
  }

  private void emptyRepository() {
    LOG.debug("repository {} looks empty, delete all to clean up", repository);
    indexer.deleteAll();
    indexStatusStore.empty(repository);
  }
}
