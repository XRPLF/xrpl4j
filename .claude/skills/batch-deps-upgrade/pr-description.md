# chore(deps): Q1 2026 batch dependency upgrade

Consolidates all open Dependabot Maven PRs into a single upgrade. Two dependencies were upgraded, three were no-ops (version inherited from root pom), and two were skipped due to Java 8 CI incompatibility.

Validated with `mvn clean install`: 343 test classes, 0 failures, 0 errors.

## Superseded Dependabot PRs

| PR | Package | From | To | Status | Major Version Upgrade |
|----|---------|------|----|--------|-----------------------|
| [#762](https://github.com/XRPLF/xrpl4j/pull/762) | `org.bouncycastle:bcprov-jdk18on` | 1.78.1 | 1.84 | Upgraded | No |
| [#763](https://github.com/XRPLF/xrpl4j/pull/763) | `org.bouncycastle:bcprov-jdk18on` (xrpl4j-client) | 1.78.1 | 1.84 | No-op (version inherited from root pom) | — |
| [#764](https://github.com/XRPLF/xrpl4j/pull/764) | `org.bouncycastle:bcprov-jdk18on` (xrpl4j-integration-tests) | 1.78.1 | 1.84 | No-op (version inherited from root pom) | — |
| [#765](https://github.com/XRPLF/xrpl4j/pull/765) | `org.bouncycastle:bcprov-jdk18on` (xrpl4j-core) | 1.78.1 | 1.84 | No-op (version inherited from root pom) | — |
| [#700](https://github.com/XRPLF/xrpl4j/pull/700) | `io.github.openfeign:feign-bom` | 12.3 | 13.8 | Upgraded | Yes ([v13](https://github.com/OpenFeign/feign/releases/tag/13.0)) |
| [#685](https://github.com/XRPLF/xrpl4j/pull/685) | `org.junit:junit-bom` | 5.14.1 | 6.0.2 | Skipped (requires Java 17+, incompatible with Java 8 CI) | — |
| [#699](https://github.com/XRPLF/xrpl4j/pull/699) | `com.github.ben-manes.caffeine:caffeine` | 2.9.3 | 3.0.0 | Skipped (requires Java 11+, incompatible with Java 8 CI) | — |

## Closing instructions

After merging, close the following superseded PRs: [#762](https://github.com/XRPLF/xrpl4j/pull/762), [#763](https://github.com/XRPLF/xrpl4j/pull/763), [#764](https://github.com/XRPLF/xrpl4j/pull/764), [#765](https://github.com/XRPLF/xrpl4j/pull/765), [#700](https://github.com/XRPLF/xrpl4j/pull/700).

The following PRs were Skipped and should remain open:

- [#685](https://github.com/XRPLF/xrpl4j/pull/685) (`org.junit:junit-bom`) — requires Java 17+
- [#699](https://github.com/XRPLF/xrpl4j/pull/699) (`com.github.ben-manes.caffeine:caffeine`) — requires Java 11+
