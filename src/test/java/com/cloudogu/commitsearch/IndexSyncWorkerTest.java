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
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.ChangesetsCommandBuilder;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexSyncWorkerTest {

  private final Repository repository = RepositoryTestData.create42Puzzle();
  @Mock
  private Indexer indexer;
  @Mock
  private UpdatedChangesets updatedChangesets;
  @Mock
  private RepositoryService service;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ChangesetsCommandBuilder changesetsCommandBuilder;
  @Mock
  private IndexStatusStore store;
  @Mock
  private IndexingContext context;

  private IndexSyncWorker worker;

  @BeforeEach
  void initWorker() {
    when(context.getIndexer()).thenReturn(indexer);
    when(context.getRepositoryService()).thenReturn(service);
    when(service.getChangesetsCommand()).thenReturn(changesetsCommandBuilder);
    when(service.getRepository()).thenReturn(repository);
    when(context.getIndexStatusStore()).thenReturn(store);
    worker = new IndexSyncWorker(context);

    lenient().when(service.isSupported(Command.CHANGESETS)).thenReturn(true);
  }

  @Test
  void shouldClearIndexIfEmptyRepository() {
    when(store.get(repository)).thenReturn(Optional.empty());
    when(changesetsCommandBuilder.getLatestChangeset()).thenReturn(Optional.empty());

    worker.ensureIndexIsUpToDate(updatedChangesets);

    verify(indexer).deleteAll();
    verify(store).empty(repository);
  }

  @Test
  void shouldSetIndexToEmptyIfNoChangesetsAvailable() {
    when(changesetsCommandBuilder.getLatestChangeset()).thenReturn(Optional.empty());

    worker.reIndex();

    verify(indexer).deleteAll();
    verify(store).empty(repository);
  }

  @Test
  void shouldReindexIfChangesetsAvailable() {
    Changeset changeset = new Changeset("42", 0L, Person.toPerson("trillian"), "first commit");
    List<Changeset> changesets = List.of(changeset);
    when(changesetsCommandBuilder.getLatestChangeset()).thenReturn(Optional.of(changeset));
    when(changesetsCommandBuilder.getChangesets()).thenReturn(changesets);

    worker.reIndex();

    verify(indexer).deleteAll();
    verify(store).update(repository, "42");
    verify(indexer).store(changesets);
  }

  @Test
  void shouldReindexIfVersionChanged() {
    when(store.get(repository)).thenReturn(Optional.of(new IndexStatus("42", Instant.now(), 12)));

    worker.ensureIndexIsUpToDate(updatedChangesets);

    verify(indexer).deleteAll();
  }

  @Test
  void shouldReindexIfNoIndexStatusFound() {
    when(store.get(repository)).thenReturn(Optional.empty());
    worker.ensureIndexIsUpToDate(updatedChangesets);

    verify(indexer).deleteAll();
  }

  @Test
  void shouldReindexIfIndexStatusIsEmpty() {
    when(store.get(repository)).thenReturn(Optional.of(new IndexStatus(IndexStatus.EMPTY, Instant.now(), IndexedChangeset.VERSION)));

    worker.ensureIndexIsUpToDate(updatedChangesets);

    verify(indexer).deleteAll();
  }

  @Test
  void shouldDoNothingIfCurrentIndexIsUpToDate() {
    when(store.get(repository)).thenReturn(Optional.of(new IndexStatus("42", Instant.now(), IndexedChangeset.VERSION)));
    Changeset changeset = new Changeset("42", 0L, Person.toPerson("trillian"), "first commit");
    when(changesetsCommandBuilder.getLatestChangeset()).thenReturn(Optional.of(changeset));
    when(changesetsCommandBuilder.getChangesets()).thenReturn(emptyList());
    when(updatedChangesets.isEmpty()).thenReturn(true);

    worker.ensureIndexIsUpToDate(updatedChangesets);

    verify(indexer, never()).store(any());
    verify(indexer, never()).deleteAll();
    verify(indexer, never()).delete(any());
  }

  @Test
  void shouldClearIndexIfNoChangesetFound() {
    when(store.get(repository)).thenReturn(Optional.of(new IndexStatus("41", Instant.now(), IndexedChangeset.VERSION)));
    Changeset changeset = new Changeset("42", 0L, Person.toPerson("trillian"), "first commit");
    List<Changeset> changesets = List.of(changeset);
    when(changesetsCommandBuilder.getLatestChangeset()).thenReturn(Optional.empty());
    when(changesetsCommandBuilder.getChangesets()).thenReturn(changesets);

    worker.ensureIndexIsUpToDate(updatedChangesets);

    verify(indexer).deleteAll();
    verify(store).empty(repository);
  }
}
