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

import { hri } from "human-readable-ids";

describe("Git Search", () => {
  let namespace: string;
  let repoName: string;

  beforeEach(() => {
    namespace = hri.random();
    repoName = hri.random();
    cy.restCreateRepo("git", namespace, repoName);
    cy.login("scmadmin", "scmadmin");
  });

  it("should find commits", () => {
    // Given
    cy.exec(`sh cypress/fixtures/git-example.sh ssh://scmadmin@localhost:2222 /tmp ${namespace} ${repoName}`);

    // When
    cy.visit(`/search/commit/?q=boulder`);

    // Then
    cy
      .contains("div.media-content", `${namespace}/${repoName}`)
      .contains("mark", "Boulder")
      .should("exist");
  });

  it("should not find commits of deleted branch", () => {
    // Given
    cy.exec(`sh cypress/fixtures/git-example-b.sh ssh://scmadmin@localhost:2222 /tmp ${namespace} ${repoName}`);

    // When
    cy.visit(`/search/commit/?q=fridolin`);

    // Then
    cy
      .contains("div.media-content", `${namespace}/${repoName}`)
      .should("not.exist");
  });
});
