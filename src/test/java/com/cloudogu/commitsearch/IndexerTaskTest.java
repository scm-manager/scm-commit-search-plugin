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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.search.Index;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("UnstableApiUsage")
class IndexerTaskTest {

  private final Repository repository = RepositoryTestData.create42Puzzle();
  private final UpdatedChangesets changesets = new UpdatedChangesets(emptyList(), emptyList());
  private final IndexerTask task = new IndexerTask(repository, changesets);

  @Mock
  private Index<IndexedChangeset> index;
  @Mock
  private IndexSyncer syncer;

  @Test
  void shouldTriggerSyncerToUpdateIndex() {
    task.setSyncer(syncer);

    task.update(index);

    verify(syncer).ensureIndexIsUpToDate(index, repository, changesets);
  }

}
