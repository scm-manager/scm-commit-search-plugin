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
      IndexedChangeset indexedChangeset = new IndexedChangeset(changeset.getId(), String.valueOf(changeset.getAuthor()), changeset.getDate(), changeset.getDescription());
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
