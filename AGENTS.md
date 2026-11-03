# xrpl4j: guidance for AI coding agents

## Git workflow

- Always use merge commits when possible. To bring a branch up to date with `main`, run
  `git merge origin/main` on the branch and push normally. Do not rebase a branch that has already
  been pushed, and never force-push (`--force` or `--force-with-lease`) to a shared branch.
- Rebasing is acceptable only for commits that have never left the local machine.
- Do all file-changing work in a git worktree under `.claude/worktrees/`. Remove the worktree only
  after the user says the work is done.
- Use the name `xrpld` (not `rippled`) for the server in new Javadoc, comments, and commit messages.
