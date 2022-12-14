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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.DefaultBranchChangedEvent;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.HookChangesetBuilder;
import sonia.scm.repository.api.HookContext;
import sonia.scm.search.SearchEngine;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("UnstableApiUsage")
class IndexListenerTest {
  @Mock
  AdministrationContext administrationContext;
  @Mock
  RepositoryManager repositoryManager;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  SearchEngine searchEngine;

  @InjectMocks
  private IndexListener indexListener;

  @Captor
  private ArgumentCaptor<IndexerTask> taskCaptor;

  @Test
  void shouldTriggerUpdateOnPostReceiveRepositoryHookEvent() {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();

    PostReceiveRepositoryHookEvent event = mock(PostReceiveRepositoryHookEvent.class);
    when(event.getRepository()).thenReturn(heartOfGold);
    HookContext context = mock(HookContext.class);
    when(event.getContext()).thenReturn(context);
    HookChangesetBuilder changesetBuilder = mock(HookChangesetBuilder.class);
    when(context.getChangesetProvider()).thenReturn(changesetBuilder);

    indexListener.handle(event);

    assertUpdate(heartOfGold);
  }

  private void assertUpdate(Repository repository) {
    verify(searchEngine.forType(IndexedChangeset.class).forResource(repository)).update(
      taskCaptor.capture()
    );

    IndexerTask task = taskCaptor.getValue();
    assertThat(task.getRepository()).isSameAs(repository);
  }

  @Test
  void shouldTriggerUpdateOnStart() {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    when(repositoryManager.getAll()).thenReturn(Collections.singleton(heartOfGold));
    doAnswer(ic -> {
      PrivilegedAction action = ic.getArgument(0);
      action.run();
      return null;
    }).when(administrationContext).runAsAdmin(any(PrivilegedAction.class));

    indexListener.contextInitialized(null);
    assertUpdate(heartOfGold);
  }
}
