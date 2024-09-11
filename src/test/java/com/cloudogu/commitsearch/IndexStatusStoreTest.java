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
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.InMemoryByteDataStoreFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class IndexStatusStoreTest {

  private final Repository repository = RepositoryTestData.create42Puzzle();
  private final InMemoryByteDataStoreFactory dataStoreFactory = new InMemoryByteDataStoreFactory();
  private IndexStatusStore store;

  @BeforeEach
  void initStore() {
    store = new IndexStatusStore(dataStoreFactory);
  }

  @Test
  void shouldGetEmptyOptionalIfNotYetIndexed() {
    Optional<IndexStatus> indexStatus = store.get(repository);

    assertThat(indexStatus).isNotPresent();
  }

  @Test
  void shouldSetStatusToEmpty() {
    store.empty(repository);

    Optional<IndexStatus> indexStatus = store.get(repository);

    assertThat(indexStatus).isPresent();
    assertThat(indexStatus.get().isEmpty()).isTrue();
  }

  @Test
  void shouldSetIndexStatusToLatestRevision() {
    store.update(repository, "42");

    Optional<IndexStatus> indexStatus = store.get(repository);

    assertThat(indexStatus).isPresent();
    assertThat(indexStatus.get().isEmpty()).isFalse();
    assertThat(indexStatus.get().getRevision()).isEqualTo("42");
    assertThat(indexStatus.get().getVersion()).isEqualTo(2);
  }

  @Test
  void shouldSetIndexStatusToLatestRevisionWithNewVersion() {
    store.update(repository, "42", 21);

    Optional<IndexStatus> indexStatus = store.get(repository);

    assertThat(indexStatus).isPresent();
    assertThat(indexStatus.get().isEmpty()).isFalse();
    assertThat(indexStatus.get().getRevision()).isEqualTo("42");
    assertThat(indexStatus.get().getVersion()).isEqualTo(21);
  }
}
