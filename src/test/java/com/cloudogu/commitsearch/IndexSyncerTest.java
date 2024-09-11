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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.search.Index;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("UnstableApiUsage")
class IndexSyncerTest {

  private final Repository repository = RepositoryTestData.create42Puzzle();
  @Mock
  private Index<IndexedChangeset> index;
  @Mock
  private UpdatedChangesets changesets;
  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService service;
  @Mock
  private IndexSyncWorkerFactory syncWorkerFactory;
  @Mock
  private IndexSyncWorker worker;
  @InjectMocks
  private IndexSyncer syncer;

  @BeforeEach
  void initService() {
    lenient().when(serviceFactory.create(repository)).thenReturn(service);
    lenient().when(service.getRepository()).thenReturn(repository);
    lenient().when(syncWorkerFactory.create(any(), any())).thenReturn(worker);
  }

  @Test
  void shouldUpdateIndex() {
    when(service.isSupported(Command.CHANGESETS)).thenReturn(true);

    syncer.ensureIndexIsUpToDate(index, repository, changesets);

    verify(worker).ensureIndexIsUpToDate(changesets);
  }


  @Test
  void shouldSkipUpdateIfCommandNotSupported() {
    when(service.isSupported(Command.CHANGESETS)).thenReturn(false);

    syncer.ensureIndexIsUpToDate(index, repository, changesets);

    verify(worker, never()).ensureIndexIsUpToDate(changesets);
  }
}
