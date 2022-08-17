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

import lombok.AllArgsConstructor;
import lombok.Getter;
import sonia.scm.search.Indexed;
import sonia.scm.search.IndexedType;

@AllArgsConstructor
@Getter
@IndexedType(value = "commit", repositoryScoped = true, namespaceScoped = true)
@SuppressWarnings({"UnstableApiUsage", "java:S2160"})
public class IndexedChangeset {
  static final int VERSION = 1;

  @Indexed(type = Indexed.Type.SEARCHABLE)
  private String id;
  @Indexed(type = Indexed.Type.SEARCHABLE)
  private String author;
  @Indexed(type = Indexed.Type.SEARCHABLE)
  private Long date;
  @Indexed(type = Indexed.Type.TOKENIZED, defaultQuery = true, highlighted = true)
  private String description;
}
