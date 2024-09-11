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

import com.github.legman.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.HookChangesetBuilder;
import sonia.scm.search.SearchEngine;
import sonia.scm.web.security.AdministrationContext;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Extension
@Singleton
@SuppressWarnings("UnstableApiUsage")
public class IndexListener implements ServletContextListener {

  private static final Logger LOG = LoggerFactory.getLogger(IndexListener.class);

  private final AdministrationContext administrationContext;
  private final RepositoryManager repositoryManager;
  private final SearchEngine searchEngine;

  @Inject
  public IndexListener(AdministrationContext administrationContext, RepositoryManager repositoryManager, SearchEngine searchEngine) {
    this.administrationContext = administrationContext;
    this.repositoryManager = repositoryManager;
    this.searchEngine = searchEngine;
  }

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    administrationContext.runAsAdmin(() -> {
      for (Repository repository : repositoryManager.getAll()) {
        LOG.debug("startup check if index of repository {}, requires update", repository);
        submit(repository, null);
      }
    });
  }

  @Subscribe
  public void handle(PostReceiveRepositoryHookEvent event) {
    LOG.debug("received hook event for repository {}, update index if necessary", event.getRepository());
    submit(event.getRepository(), extractChangesets(event.getContext().getChangesetProvider()));
  }

  private UpdatedChangesets extractChangesets(HookChangesetBuilder changesetProvider) {
    return new UpdatedChangesets(toList(changesetProvider.getChangesets()), toList(changesetProvider.getRemovedChangesets()));
  }

  private List<Changeset> toList(Iterable<Changeset> changesets) {
    return StreamSupport.stream(changesets.spliterator(), false)
      .collect(Collectors.toList());
  }

  private void submit(Repository repository, UpdatedChangesets changesets) {
    searchEngine.forType(IndexedChangeset.class)
      .forResource(repository)
      .update(new IndexerTask(repository, changesets));
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    // nothing to destroy here
  }
}
