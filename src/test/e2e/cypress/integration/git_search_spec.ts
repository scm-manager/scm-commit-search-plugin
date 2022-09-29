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
import { GitBuilder } from "./__helpers/git-builder";

describe("Git Search", () => {
  let namespace: string;
  let repoName: string;

  const findSearchHit = () => cy
    .contains("div.media-content", `${namespace}/${repoName}`);

  const findSearchHits = () => cy
    .get(`.${namespace}\\/${repoName}`);

  const findSearchMark = (keyword: string) => cy
    .contains("div.media-content", `${namespace}/${repoName}`)
    .contains("mark", keyword);

  const findRevision = () => cy
    .contains("div.media-content", `${namespace}/${repoName}`)
    .contains("a", /b[0-9a-f]{5,40}/)
    .then($revision => $revision.text());

  const visitSearch = (term: string) => {
    const requestAlias = `searchApiRequest-${term}`;
    cy.intercept('GET', `/scm/api/v2/search/query/commit?q=${term}*`).as(requestAlias);
    cy.visit(`/search/commit/?q=${term}`);
    cy.wait(`@${requestAlias}`);
  }


  /**
   * Creates and pushes the following git tree:
   *
   *   A---B---C---D  master
   *        \
   *         E        develop
   *          \
   *           F---G  feature
   *
   * The files, file contents and commits all contain words starting with the given letter.
   * A {@link GitBuilder} is returned to perform further git operations on the repository.
   */
  const prepareAndPushBaseExample = () => new GitBuilder(
    namespace,
    repoName
  )
    .init()
    .createAndCommitFile("Antelope.txt", "I am an animal", "Release the Antelope")
    .createAndCommitFile("Boulder.txt", "I am rock solid", "Lift a Boulder")
    .createAndCheckoutBranch("develop")
    .checkoutDefaultBranch()
    .createAndCommitFile("Catfish.txt", "Am I a cat, am I a fish ? Who knows!", "Actually, a Catfish is a fish")
    .createAndCommitFile("Dollar.txt", "Which dollar am I, Canadian, US, Australian ?", "A Dollar is what I need")
    .checkoutBranch("develop")
    .createAndCommitFile("Elegant.txt", "We even have adjectives!", "An elegant panda is counting to 42")
    .createAndCheckoutBranch("feature")
    .createAndCommitFile("Fridolin.txt", "I like this name", "Fridolin likes the SCM-Manager, be more like Fridolin")
    .createAndCommitFile("Grog.txt", "Yaarrgh! I am a pirate", "Pirates drink Grog")
    .pushAllWithForce()
    .checkoutDefaultBranch();

  beforeEach(() => {
    namespace = hri.random();
    repoName = hri.random();
    cy.restCreateRepo("git", namespace, repoName);
    cy.login("scmadmin", "scmadmin");
  });

  it("should find commits", () => {
    // Given
    prepareAndPushBaseExample();

    // When
    visitSearch("boulder");

    // Then
    findSearchMark("Boulder")
      .should("exist");
  });

  it("should not find commits of deleted branch", () => {
    // Given
    const git = prepareAndPushBaseExample();
    git.deleteBranchLocallyAndRemote("feature");

    // When
    visitSearch("fridolin");

    // Then
    findSearchHit()
      .should("not.exist");
  });

  it("should find rebased commits under same revision", () => {
    let revision: string;

    // Given
    const git = prepareAndPushBaseExample();
    visitSearch("fridolin");
    findRevision()
      .then($revision => revision = $revision);

    git
      .rebase("feature", git.defaultBranch)
      .pushAllWithForce();

    // When
    visitSearch("fridolin");

    // Then
    findRevision()
      .then($revision => expect($revision).to.eq(revision));
  });

  it("should find rebased commits twice", () => {
    // Given
    const git = prepareAndPushBaseExample();
    git
      .checkoutBranch("develop")
      .rebaseOnto(git.defaultBranch)
      .pushAllWithForce();

    // When
    visitSearch("elegant");

    // Then
    findSearchHits()
      .should('have.length', 2);
  });

  it("should find commit for tag", () => {
    // Given
    const git = prepareAndPushBaseExample();
    git
      .checkoutBranch("feature")
      .createAndCommitFile("Hello.txt", "Hello hello, turn the radio on", "Hello World")
      .tag("illegal")
      .pushTags();

    // When
    visitSearch("hello");

    // Then
    findSearchHit()
      .should("exist");
  });

  it("should not find commit for tag after tag is deleted but commit wasn't pushed", () => {
    // Given
    const git = prepareAndPushBaseExample();
    git
      .checkoutBranch("feature")
      .createAndCommitFile("Hello.txt", "Hello hello, turn the radio on", "Hello World")
      .tag("illegal")
      .pushTags()
      .deleteTagLocallyAndRemote("illegal");

    // When
    visitSearch("hello");

    // Then
    findSearchHit()
      .should("not.exist");
  });

  it("should find commit for tag after deleting it again", () => {
    // Given
    const git = prepareAndPushBaseExample();
    git
      .checkoutBranch("feature")
      .tag("hermit")
      .pushTags()
      .deleteTagLocallyAndRemote("hermit");

    // When
    visitSearch("grog");

    // Then
    findSearchHit()
      .should("exist");
  });

  it("should find commit after renaming branch", () => {
    // Given
    const git = prepareAndPushBaseExample();
    git
      .checkoutBranch("feature")
      .createAndCheckoutBranch("new")
      .renameBranchLocallyAndRemote("feature", "new");

    // When
    visitSearch("fridolin");

    // Then
    findSearchHit()
      .should("exist");
  });

  it("should find amended commit message", () => {
    // Given
    const git = prepareAndPushBaseExample();
    git
      .checkoutBranch("feature")
      .amendLatestCommitMessage("Halloween is soon")
      .pushAllWithForce();

    // When
    visitSearch("halloween");

    // Then
    findSearchHit()
      .should("exist");

    // When
    visitSearch("grog");

    // Then
    findSearchHit()
      .should("not.exist");
  });
});
