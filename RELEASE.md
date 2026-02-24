
# Release Guide

This document describes the manual release process for xrpl4j.

## Stable Releases

1. Checkout main and pull in latest changes from remote
   ```  
   git checkout main
   git pull
   ```
2. Create a new release branch from main (e.g. `releases/v6.0` for major version release, `releases/v6.1` for minor version release)
   ```  
   git checkout -b releases/v6.1  
   ```  

3. Set intended release version
   ```  
   mvn versions:set -DnewVersion=6.1.0
   ``` 
4. Commit and push changes in `pom.xml` files created by step #3.
5. Verify Integration tests that executes on `Devnet` passes in GitHub actions. Integration tests that executes on 
   `Testnet` might fail as the amendment would generally not be available on `Testnet` yet.
6. Create a new tag for the release and push to remote
   ```  
   git tag v6.1.0
   git push origin v6.1.0
   ```
7. Generate and deploy artifacts to [maven central repository](https://central.sonatype.com/) (This would prompt to sign the artifacts using private key)
   ```  
   mvn clean deploy -DskipTests -P release
   ```
8. Approve the release on [maven central repository](https://central.sonatype.com/)
9. Update the version to next patch version, commit and push to remote
   ```  
   mvn versions:set -DnewVersion=6.1.1
   git commit -m "update version to 6.1.1"
   git push
   ```
10. Generate release notes on GitHub using `v6.1.0` git tag.
11. Subsequent bug fixes if any are merged into `releases/v6.1` branch and released as patch version upgrades

## Release candidates

Steps remains same as stable release. We would follow Semantic Versioning and name the version something like `6.1.
0-rc.1`. In the event when the change we want to release as release candidate is not merged in the `main` branch, we 
would release it from feature branch itself and tag the commit of feature branch.