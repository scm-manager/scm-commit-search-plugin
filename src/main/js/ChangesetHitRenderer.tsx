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
import { Hit, Notification, RepositoryAvatar, TextHitField, useStringHitFieldValue } from "@scm-manager/ui-components";
import styled from "styled-components";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { extensionPoints } from "@scm-manager/ui-extensions";

const Container = styled(Hit.Content)`
  overflow-x: auto;
`;

const FileContent = styled.div`
  border: var(--scm-border);
  border-radius: 0.25rem;
`;

const ChangesetHitRenderer: FC<extensionPoints.SearchHitRenderer["props"]> = ({ hit }) => {
  const commitId = useStringHitFieldValue(hit, "id");
  const [t] = useTranslation("plugins");

  const repository = hit._embedded?.repository;

  if (!commitId || !repository) {
    return <Notification type="danger">Found incomplete commit search result</Notification>;
  }

  return (
    <Hit>
      <Container className={`${repository.namespace}/${repository.name}`}>
        <div className="is-flex">
          <RepositoryAvatar repository={repository} size={48} />
          <div className="ml-2">
            <Link to={`/repo/${repository.namespace}/${repository.name}`}>
              <Hit.Title>
                {repository.namespace}/{repository.name}
              </Hit.Title>
            </Link>
          </div>
        </div>
        <FileContent className="my-2">
          <pre>
            <code>
              <TextHitField hit={hit} field="description" truncateValueAt={1024} />
            </code>
          </pre>
        </FileContent>
        <div className="is-flex is-justify-content-space-between is-size-7">
          <small className="is-size-7">
            Revision:{" "}
            <Link
              className="is-ellipsis-overflow"
              to={`/repo/${repository.namespace}/${repository.name}/code/changeset/${commitId}`}
            >
              <TextHitField hit={hit} field="id" />
            </Link>
          </small>
          {t("scm-commit-search-plugin.author")}: <TextHitField hit={hit} field="author" />
        </div>
      </Container>
    </Hit>
  );
};

export default ChangesetHitRenderer;
