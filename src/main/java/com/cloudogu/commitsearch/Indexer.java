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

import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.search.Id;
import sonia.scm.search.Index;

@SuppressWarnings("UnstableApiUsage")
class Indexer {

  private static final Logger LOG = LoggerFactory.getLogger(Indexer.class);

  private final Index<IndexedChangeset> index;
  private final Repository repository;

  public Indexer(Index<IndexedChangeset> index, RepositoryService repositoryService) {
    this.index = index;
    this.repository = repositoryService.getRepository();
  }

  void store(Iterable<Changeset> changesets) {
    if (Iterables.isEmpty(changesets)) {
      return;
    }

    for (Changeset changeset : changesets) {
      LOG.trace("store {} to index", changeset);
      IndexedChangeset indexedChangeset = new IndexedChangeset(changeset);
      index.store(id(indexedChangeset.getId()), permission(), indexedChangeset);
    }
  }

  void delete(Iterable<Changeset> changesets) {
    if (Iterables.isEmpty(changesets)) {
      return;
    }
    Index.Deleter<IndexedChangeset> deleter = index.delete();
    for (Changeset changeset : changesets) {
      LOG.trace("delete {} from index", changeset);
      deleter.byId(id(changeset.getId()));
    }
  }

  void deleteAll() {
    index.delete().by(Repository.class, repository).execute();
  }

  private String permission() {
    return RepositoryPermissions.read(repository).asShiroString();
  }

  private Id<IndexedChangeset> id(String id) {
    return Id.of(IndexedChangeset.class, id).and(Repository.class, repository);
  }
}
