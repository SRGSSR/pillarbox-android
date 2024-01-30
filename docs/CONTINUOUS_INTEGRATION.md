# Continuous integration

The project provides support for continuous integration with GitHub Actions.

## Code quality checks

Every time a Pull Request is made or something is pushed to the `main` branch,  the [`build.yml`](https://github.com/SRGSSR/pillarbox-android/blob/main/.github/workflows/build.yml)
workflow is triggered by GitHub Actions. It checks that the project builds on various platforms, runs code linters, dependencies check and finally 
run tests. Result are posted directly in the Pull Request.

## Libraries Deliveries

The libraries are delivered to GitHub Packages, the build is triggered when a git tag is published.
Not every tag will trigger a build. The tag have to follow a semantic release pattern.
Valid tags could be `1.2.30` or `5.4.3-beta01`.

## Demo Deliveries

There are two kind of deliveries:
- Nightly builds are triggered manually or every day if there are some changes on `main`.
- Release builds are triggered when a libraries build is triggered.

## Setup GitHub to run workflows

To build and publish the Demo, several variables needs to be setup as GitHub Actions secrets:
- `DEMO_KEY_ALIAS`: the key alias for the Gradle signature config.
- `DEMO_KEY_PASSWORD`: the key password for the Gradle signature config.
- `FIREBASE_CREDENTIAL_FILE_CONTENT`: the Firebase CLI credential file content.
- `NIGHTLY_APP_ID`: the Firebase project ID to publish the nightly.
- `NIGHTLY_GROUPS`: the Firebase testers group associated to the nightly demo.
- `RELEASE_APP_ID`: the Firebase project ID to publish the release.
- `RELEASE_GROUPS`: the Firebase testers group associated to the release demo.
