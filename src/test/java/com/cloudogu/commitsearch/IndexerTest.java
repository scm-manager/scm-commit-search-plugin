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
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.search.Id;
import sonia.scm.search.Index;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("UnstableApiUsage")
class IndexerTest {

  private final Repository repository = RepositoryTestData.create42Puzzle();
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Index<IndexedChangeset> index;
  @Mock
  private RepositoryService service;

  private Indexer indexer;

  @BeforeEach
  void initService() {
    lenient().when(service.getRepository()).thenReturn(repository);
    indexer = new Indexer(index, service);
  }

  @Test
  void shouldNotStoreForEmptyList() {
    indexer.store(emptyList());

    verify(index, never()).store(any(), any(), any());
  }

  @Test
  void shouldNotOpenIndexForEmptyDeletes() {
    indexer.delete(Collections.emptyList());

    verifyNoInteractions(index);
  }

  @Test
  void shouldUpdateIndexForChangesets() {
    List<Changeset> changesets = List.of(
      new Changeset("42", 0L, Person.toPerson("scmadmin"), "initial commit"),
      new Changeset("84", 0L, Person.toPerson("trillian"), "implement heart of gold")
    );

    indexer.store(changesets);

    String repoPermission = "repository:read:" + repository.getId();

    verify(index).store(eq(id("42")), eq(repoPermission), argThat(changeset -> {
      assertThat(changeset.getId()).isEqualTo("42");
      assertThat(changeset.getDate()).isZero();
      assertThat(changeset.getAuthor()).isEqualTo("scmadmin");
      assertThat(changeset.getDescription()).isEqualTo("initial commit");
      return true;
    }));
    verify(index).store(eq(id("84")), eq(repoPermission), argThat(changeset -> {
      assertThat(changeset.getId()).isEqualTo("84");
      assertThat(changeset.getDate()).isZero();
      assertThat(changeset.getAuthor()).isEqualTo("trillian");
      assertThat(changeset.getDescription()).isEqualTo("implement heart of gold");
      return true;
    }));
  }

  @Test
  void shouldDeleteAll() {
    indexer.deleteAll();

    verify(index.delete().by(Repository.class, repository)).execute();
  }

  @Test
  void shouldDelete() {
    Index.Deleter<IndexedChangeset> deleter = mock(Index.Deleter.class);
    when(index.delete()).thenReturn(deleter);

    indexer.delete(Arrays.asList(
      new Changeset("a", 0L,  Person.toPerson("scmadmin")),
      new Changeset("b", 0L,  Person.toPerson("scmadmin"))
    ));

    verify(deleter).byId(id("a"));
    verify(deleter).byId(id("b"));
  }

  private Id<IndexedChangeset> id(String a) {
    return Id.of(IndexedChangeset.class, a).and(Repository.class, repository);
  }
}
