# Claude Code settings

[`settings.json`](settings.json) holds the shared [Claude Code](https://docs.claude.com/en/docs/claude-code) permission rules for this project. They apply to everyone who uses Claude Code in this repository. Personal overrides go in `.claude/settings.local.json`, which is not committed.

- **Deny** rules are never allowed, whatever the permission mode.
- **Ask** rules always prompt for confirmation, even when a broader allow rule matches.

When you add or change a rule in `settings.json`, update the tables below.

## Deny

| Rule | Reason |
|------|--------|
| `Bash(git push --force *)`, `Bash(git push -f *)` | Force-pushing rewrites shared history and can destroy other contributors' commits. |
| `Read(./**/local.properties)` | Machine-specific SDK paths, often with local API keys or signing passwords. |
| `Read(./**/*.keystore)`, `Read(./**/*.jks)` | Android signing keys. A leaked release key lets anyone ship updates in our name. |
| `Read(./**/*.p12)` | PKCS#12 certificates and private keys (signing, push notifications, service accounts). |
| `Read(./**/google-services.json)` | Firebase project configuration and API keys. |
| `Read(./**/*service-account*.json)` | Google Cloud, Firebase or Play Console service-account private keys. |
| `Read(~/.gradle/gradle.properties)` | Global Gradle credentials, such as the Sonatype (Maven Central) login and signing passwords. |

## Ask

| Rule | Reason |
|------|--------|
| `Bash(git push)`, `Bash(git push *)` | Pushing publishes commits to the shared remote. |
| `Bash(git reset --hard)`, `Bash(git reset --hard *)` | Discards uncommitted local changes irreversibly. |
| `Bash(./gradlew *publish*)` | Publishes artifacts to Maven Central, which cannot be undone. This also matches the harmless `publishToMavenLocal`. |
| `Bash(./gradlew *appDistributionUpload*)` | Uploads a build to Firebase App Distribution testers. |
| `Bash(gh release *)` | Creates or edits GitHub releases. |
| `Bash(gh pr merge *)` | Merges pull requests into shared branches. |
| `Edit(.github/workflows/**)` | Workflows have access to repository secrets (Sonatype, Firebase, signing). |
| `Edit(gradle/wrapper/**)`, `Edit(gradlew)`, `Edit(gradlew.bat)` | A modified wrapper runs arbitrary code on every build. |
| `Edit(gradle/libs.versions.toml)` | Dependency changes affect the supply chain and should be reviewed. |

## Limitations

- The Gradle rules only match commands starting with `./gradlew`.
- `Read` rules apply to Claude's file tools. Shell commands such as `cat` are only blocked when Claude Code's sandbox is enabled.
