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
            <pre>
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
