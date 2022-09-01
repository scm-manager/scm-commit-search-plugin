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
