# Release Guide

Follow this guide when releasing xrpl4j packages.

## Major/Minor Releases

Follow these steps to create a new major or minor release.

1. Checkout `main` and pull the latest changes.
   ```
   git checkout main
   git pull
   ```
2. Create a new release branch `releases/v[major].[minor]` from `main`.
   ```
   git checkout -b releases/v[major].[minor]
   ```
   > Example: `git checkout -b releases/v6.1`

3. Set the version to `[major].[minor].0-SNAPSHOT`.
   ```
   mvn versions:set -DnewVersion=[major].[minor].0-SNAPSHOT
   ```
   > Example: `mvn versions:set -DnewVersion=6.1.0-SNAPSHOT`

4. Commit and push the `pom.xml` changes from step #3.
5. Verify that integration tests executing on `Devnet` pass in GitHub Actions. Integration tests on `Testnet`
   might fail as the amendment may not yet be available there.
6. Create a tag `v[major].[minor].0` and push it to the remote.
   ```
   git tag v[major].[minor].0
   git push origin v[major].[minor].0
   ```
   > Example:
   > ```
   > git tag v6.1.0
   > git push origin v6.1.0
   > ```

7. Set the non-SNAPSHOT version to deploy and publish artifacts to
   [Maven Central](https://central.sonatype.com/). This will prompt you to sign the artifacts with your
   private key. **Do not commit this version change** — it exists only for the deploy.
   ```
   mvn versions:set -DnewVersion=[major].[minor].0
   mvn clean deploy -DskipTests -P release
   ```
   > Example:
   > ```
   > mvn versions:set -DnewVersion=6.1.0
   > mvn clean deploy -DskipTests -P release
   > ```

8. Approve the deployment on [Maven Central](https://central.sonatype.com/).
9. Bump the version from `[major].[minor].[patch]-SNAPSHOT` to `[major].[minor].[patch+1]-SNAPSHOT`,
   then commit and push.
   ```
   mvn versions:set -DnewVersion=[major].[minor].[patch+1]-SNAPSHOT
   git commit -m "Bump version to [major].[minor].[patch+1]-SNAPSHOT"
   git push
   ```
   > Example: Bump from `6.1.0-SNAPSHOT` to `6.1.1-SNAPSHOT`.
   > ```
   > mvn versions:set -DnewVersion=6.1.1-SNAPSHOT
   > git commit -m "Bump version to 6.1.1-SNAPSHOT"
   > git push
   > ```

10. Generate release notes on GitHub using the `v[major].[minor].0` tag.

## Patch Releases

Follow these steps to release a patch from an existing `releases/v[major].[minor]` branch. Bug fixes should
already be merged or cherry-picked into this branch.

1. Checkout the existing `releases/v[major].[minor]` branch and pull the latest changes. The branch should
   already be at the next `[major].[minor].[patch]-SNAPSHOT` version from the previous release.
   ```
   git checkout releases/v[major].[minor]
   git pull
   ```
   > Example: `git checkout releases/v6.1 && git pull`

2. Merge or cherry-pick the necessary bug fixes into the release branch.
3. Verify that integration tests executing on `Devnet` pass in GitHub Actions.
4. Create a tag `v[major].[minor].[patch]` and push it to the remote.
   ```
   git tag v[major].[minor].[patch]
   git push origin v[major].[minor].[patch]
   ```
   > Example:
   > ```
   > git tag v6.1.1
   > git push origin v6.1.1
   > ```

5. Set the non-SNAPSHOT version for deploy and publish artifacts to
   [Maven Central](https://central.sonatype.com/). **Do not commit this version change.**
   ```
   mvn versions:set -DnewVersion=[major].[minor].[patch]
   mvn clean deploy -DskipTests -P release
   ```
   > Example:
   > ```
   > mvn versions:set -DnewVersion=6.1.1
   > mvn clean deploy -DskipTests -P release
   > ```

6. Approve the deployment on [Maven Central](https://central.sonatype.com/).
7. Bump the version from `[major].[minor].[patch]-SNAPSHOT` to `[major].[minor].[patch+1]-SNAPSHOT`,
   then commit and push.
   ```
   mvn versions:set -DnewVersion=[major].[minor].[patch+1]-SNAPSHOT
   git commit -m "Bump version to [major].[minor].[patch+1]-SNAPSHOT"
   git push
   ```
   > Example: Bump from `6.1.1-SNAPSHOT` to `6.1.2-SNAPSHOT`.
   > ```
   > mvn versions:set -DnewVersion=6.1.2-SNAPSHOT
   > git commit -m "Bump version to 6.1.2-SNAPSHOT"
   > git push
   > ```

8. Generate release notes on GitHub using the `v[major].[minor].[patch]` tag.

## Release Candidates

The steps for a release candidate are the same as a major/minor or patch release above. The only difference
is that the version uses an `-rc.N` suffix instead of a final version number (e.g., `6.1.0-rc.1` instead
of `6.1.0`).

If the changes have not yet been merged into `main`, deploy the release candidate directly from the feature
branch instead of a release branch.