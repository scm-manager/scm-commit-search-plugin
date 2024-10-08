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

import com.google.common.annotations.VisibleForTesting;
import sonia.scm.repository.Repository;
import sonia.scm.search.Index;
import sonia.scm.search.SerializableIndexTask;

import jakarta.inject.Inject;

@SuppressWarnings("UnstableApiUsage")
public class IndexerTask implements SerializableIndexTask<IndexedChangeset> {

  private final Repository repository;
  private final UpdatedChangesets changesets;

  private IndexSyncer syncer;

  public IndexerTask(Repository repository, UpdatedChangesets changesets) {
    this.repository = repository;
    this.changesets = changesets;
  }

  @Inject
  public void setSyncer(IndexSyncer syncer) {
    this.syncer = syncer;
  }

  @VisibleForTesting
  Repository getRepository() {
    return repository;
  }

  @Override
  public void update(Index<IndexedChangeset> index) {
    syncer.ensureIndexIsUpToDate(index, repository, changesets);
  }
}
