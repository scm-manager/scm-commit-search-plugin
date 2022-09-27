export class GitTreeBuilder {

  private readonly directory = `cypress/fixtures/${this.namespace}-${this.name}`;
  private readonly gitDirectory = `./${this.directory}`;

  constructor(private readonly namespace: string, private readonly name: string) {
  }

  init() {
    cy.exec(`git init -b main ./${this.gitDirectory}`);
    cy.exec(`git -C ${this.gitDirectory} remote add origin "http://scmadmin:scmadmin@localhost:8081/scm/repo/${this.namespace}/${this.name}"`);
    return this;
  }

  createAndCommitFile(path: string, content: string, commitMessage: string) {
    cy.writeFile(`${this.directory}/${path}`, content);
    cy.exec(`git -C ${this.gitDirectory} add ${path}`);
    cy.exec(`git -C ${this.gitDirectory} commit -m "${commitMessage}"`);
    return this;
  }

  createAndCheckoutBranch(branchName: string) {
    cy.exec(`git -C ${this.gitDirectory} checkout -b "${branchName}"`);
    return this;
  }

  checkoutBranch(branchName: string) {
    cy.exec(`git -C ${this.gitDirectory} checkout "${branchName}"`);
    return this;
  }

  deleteBranchLocallyAndRemote(branchName: string) {
    cy.exec(`git -C ${this.gitDirectory} branch -D "${branchName}"`);
    cy.exec(`git -C ${this.gitDirectory} push -d origin "${branchName}"`);
    return this;
  }

  pushAll() {
    cy.exec(`git -C ${this.gitDirectory} push --all origin`);
    return this;
  }
}
