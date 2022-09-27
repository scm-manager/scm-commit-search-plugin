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
import { GitTreeBuilder } from "./_helpers/git-tree-builder";

// A---B---C---D  master
//      \
//       E        develop
//        \
//         F---G  feature

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
    new GitTreeBuilder(
      namespace,
      repoName
    )
      .init()
      .createAndCommitFile("Antelope.txt", "I am an animal", "Release the Antelope")
      .createAndCommitFile("Boulder.txt", "I am rock solid", "Lift a Boulder")
      .createAndCheckoutBranch("develop")
      .checkoutBranch("main")
      .createAndCommitFile("Catfish.txt", "Am I a cat, am I a fish ? Who knows!", "Actually, a Catfish is a fish")
      .createAndCommitFile("Dollar.txt", "Which dollar am I, Canadian, US, Australian ?", "A Dollar is what I need")
      .checkoutBranch("develop")
      .createAndCommitFile("Elegant.txt", "We even have adjectives!", "An elegant panda is counting to 42")
      .createAndCheckoutBranch("feature")
      .createAndCommitFile("Fridolin.txt", "I like this name", "Fridolin likes the SCM-Manager, be more like Fridolin")
      .createAndCommitFile("Grog.txt", "Yaarrgh! I am a pirate", "Pirates drink Grog")
      .pushAll()
    ;

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
    new GitTreeBuilder(
      namespace,
      repoName
    )
      .init()
      .createAndCommitFile("Antelope.txt", "I am an animal", "Release the Antelope")
      .createAndCommitFile("Boulder.txt", "I am rock solid", "Lift a Boulder")
      .createAndCheckoutBranch("develop")
      .checkoutBranch("main")
      .createAndCommitFile("Catfish.txt", "Am I a cat, am I a fish ? Who knows!", "Actually, a Catfish is a fish")
      .createAndCommitFile("Dollar.txt", "Which dollar am I, Canadian, US, Australian ?", "A Dollar is what I need")
      .checkoutBranch("develop")
      .createAndCommitFile("Elegant.txt", "We even have adjectives!", "An elegant panda is counting to 42")
      .createAndCheckoutBranch("feature")
      .createAndCommitFile("Fridolin.txt", "I like this name", "Fridolin likes the SCM-Manager, be more like Fridolin")
      .createAndCommitFile("Grog.txt", "Yaarrgh! I am a pirate", "Pirates drink Grog")
      .pushAll()
      .checkoutBranch("main")
      .deleteBranchLocallyAndRemote("feature")
    ;

    // When
    cy.visit(`/search/commit/?q=fridolin`);

    // Then
    cy
      .contains("div.media-content", `${namespace}/${repoName}`)
      .should("not.exist");
  });
});
