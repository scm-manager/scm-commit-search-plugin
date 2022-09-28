export class GitBuilder {

  private readonly directory = `cypress/fixtures/${this.namespace}-${this.name}`;
  private readonly gitDirectory = `./${this.directory}`;

  constructor(private readonly namespace: string, private readonly name: string, private readonly defaultBranch = "main") {
  }

  init() {
    const [protocol, url] = Cypress.config('baseUrl').split("//");
    const credentials = `${Cypress.env("USERNAME")}:${Cypress.env("PASSWORD")}`;
    const urlWithCredentials = `${protocol}//${credentials}@${url}`;
    cy.exec(`git init -b ${this.defaultBranch} ./${this.gitDirectory}`);
    cy.exec(`git -C ${this.gitDirectory} remote add origin "${urlWithCredentials}/repo/${this.namespace}/${this.name}"`);
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

  checkoutDefaultBranch() {
    return this.checkoutBranch(this.defaultBranch);
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

  rebase(branch: string, onto: string) {
    cy.exec(`git -C ${this.gitDirectory} rebase ${branch} ${onto}`);
    return this;
  }
}
