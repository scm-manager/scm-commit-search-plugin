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

import { hri } from "human-readable-ids";
import { GitBuilder } from "./__helpers/git-builder";

describe("Git Search", () => {
  let namespace: string;
  let repoName: string;

  const findSearchHit = () => cy.contains("li", `${namespace}/${repoName}`);

  const findSearchHits = () => cy.get(`.${namespace}\\/${repoName}`);

  const findSearchMark = (keyword: string) => findSearchHit().contains("mark", keyword);

  const findRevision = () =>
    findSearchHit()
      .contains("a", /b[0-9a-f]{5,40}/)
      .then($revision => $revision.text());

  const visitSearch = (term: string) => cy.visit(`/search/commit/?q=${term}&namespace=${namespace}&name=${repoName}`);

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
  const prepareAndPushBaseExample = () =>
    new GitBuilder(namespace, repoName)
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
    cy.restLogin("scmadmin", "scmadmin");
  });

  it("should find commits", () => {
    // Given
    prepareAndPushBaseExample();

    // When
    visitSearch("boulder");

    // Then
    findSearchMark("Boulder").should("exist");
  });

  it("should not find commits of deleted branch", () => {
    // Given
    const git = prepareAndPushBaseExample();
    git.deleteBranchLocallyAndRemote("feature");

    // When
    visitSearch("fridolin");

    // Then
    findSearchHit().should("not.exist");
  });

  it("should find rebased commits under same revision", () => {
    let revision: string;

    // Given
    const git = prepareAndPushBaseExample();
    visitSearch("fridolin");
    findRevision().then($revision => (revision = $revision));

    git.rebase("feature", git.defaultBranch).pushAllWithForce();

    // When
    visitSearch("fridolin");

    // Then
    findRevision().then($revision => expect($revision).to.eq(revision));
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
    findSearchHits().should("have.length", 2);
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
    findSearchHit().should("exist");
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
    findSearchHit().should("not.exist");
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
    findSearchHit().should("exist");
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
    findSearchHit().should("exist");
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
    findSearchHit().should("exist");

    // When
    visitSearch("grog");

    // Then
    findSearchHit().should("not.exist");
  });
});
