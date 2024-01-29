# Continuous integration

The project provides support for continuous integration with Github actions.


## Code quality checks

Every time a push request is made or something is pushed into main branch, `build.yml` workflow is called by Github actions. It checks the project 
builds on various platforms, run code linters, dependencies check and finally run tests. Result are posted directly in the pull request.

## Libraries Deliveries

The libraries are delivered to Github packages, the build is triggered when a git tag is published.
Not every tag will trigger a build. The tag have to follow a semantic release pattern.
Valid tags could be `1.2.30` or `5.4.3-beta01`.

## Demo Deliveries

There is two kind of deliveries
- Nightly build is triggered manually or every day if there is some changes on `main`.
- Release build is triggered when a libraries build is triggered.

## Setup Github to run workflows

To build and publish the Demo, several variables needs to be setup as Github actions secrets:
- `DEMO_KEY_ALIAS` The key alias for the gradle signature config.
- `DEMO_KEY_PASSWORD` The key password for the gradle signature config.
- `FIREBASE_CREDENTIAL_FILE_CONTENT` The firebase CLI credential file content.
- `NIGHTLY_APP_ID` The firebase project ID to publish the nightly.
- `NIGHTLY_GROUPS` The firebase testers group associated to the nightly demo.
- `RELEASE_APP_ID` The firebase project ID to publish the release.
- `RELEASE_GROUPS` The firebase testers group associated to the release demo.
