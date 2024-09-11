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

import lombok.AllArgsConstructor;
import lombok.Getter;
import sonia.scm.repository.Changeset;
import sonia.scm.search.Indexed;
import sonia.scm.search.IndexedType;

@AllArgsConstructor
@Getter
@IndexedType(value = "commit", repositoryScoped = true, namespaceScoped = true)
@SuppressWarnings({"UnstableApiUsage", "java:S2160"})
public class IndexedChangeset {
  static final int VERSION = 2;

  IndexedChangeset(Changeset changeset) {
    this(
      changeset.getId(),
      String.valueOf(changeset.getAuthor()),
      changeset.getDate(),
      changeset.getDescription(),
      String.join(", ", changeset.getParents())
    );
  }

  @Indexed(type = Indexed.Type.SEARCHABLE)
  private String id;
  @Indexed(type = Indexed.Type.TOKENIZED)
  private String author;
  @Indexed(type = Indexed.Type.SEARCHABLE)
  private Long date;
  @Indexed(type = Indexed.Type.TOKENIZED, defaultQuery = true, highlighted = true)
  private String description;
  @Indexed(name = "parent", type = Indexed.Type.TOKENIZED)
  private String parents;
}
