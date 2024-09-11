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

import React, { FC } from "react";
import { Notification, RepositoryAvatar, TextHitField, useStringHitFieldValue } from "@scm-manager/ui-components";
import styled from "styled-components";
import { Link } from "react-router-dom";
import { Trans } from "react-i18next";
import { extensionPoints } from "@scm-manager/ui-extensions";
import { CardList } from "@scm-manager/ui-layout";
import classNames from "classnames";

const StyledLink = styled(Link)`
  gap: 0.5rem;
`;

const FileContent = styled.div`
  border: var(--scm-border);
  border-radius: 0.25rem;
`;

const ChangesetHitRenderer: FC<extensionPoints.SearchHitRenderer["props"]> = ({ hit }) => {
  const commitId = useStringHitFieldValue(hit, "id");

  const repository = hit._embedded?.repository;
  const title = `${repository?.namespace}/${repository?.name}`;

  if (!commitId || !repository) {
    return <Notification type="danger">Found incomplete commit search result</Notification>;
  }

  return (
    <CardList.Card key={title} className={`${repository.namespace}/${repository.name}`}>
      <CardList.Card.Row>
        <CardList.Card.Title>
          <StyledLink
            to={`/repo/${repository.namespace}/${repository.name}`}
            className={classNames("is-flex", "is-justify-content-flex-start", "is-align-items-center")}
          >
            <RepositoryAvatar repository={repository} size={16} /> {title}
          </StyledLink>
        </CardList.Card.Title>
      </CardList.Card.Row>
      <CardList.Card.Row>
        <FileContent className="my-2">
          <pre className="is-relative">
            <code>
              <TextHitField hit={hit} field="description" truncateValueAt={1024} />
            </code>
          </pre>
        </FileContent>
      </CardList.Card.Row>
      <CardList.Card.Row className="is-size-7 has-text-secondary">
        <Trans i18nKey={"scm-commit-search-plugin.commit-details"}>
          Revision{" "}
          <Link
            className="is-ellipsis-overflow is-relative"
            to={`/repo/${repository.namespace}/${repository.name}/code/changeset/${commitId}`}
          >
            <TextHitField hit={hit} field="id" />
          </Link>{" "}
          by <TextHitField hit={hit} field="author" />
        </Trans>
      </CardList.Card.Row>
    </CardList.Card>
  );
};

export default ChangesetHitRenderer;
