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

// This is an automatically generated example, please remove or replace as you see fit

import { hri } from "human-readable-ids";

describe("Example", () => {
    it("should show a footer navigation for authenticated users", () => {
        // Given
        const namespace = hri.random();
        const repoName = hri.random();
        cy.restCreateRepo("git", namespace, repoName);
        cy.login("scmadmin", "scmadmin");
        cy.exec("pwd");
        cy.exec(`sh cypress/integration/git-example.sh ssh://scmadmin@localhost:2222 /tmp ${namespace} ${repoName}`);

        // When
        cy.visit(`/search/commit/?q=boulder`);

        // Then
        cy
          .contains("div.media-content", `${namespace}/${repoName}`)
          .contains("mark", "Boulder")
          .should("exist");
    });
});
