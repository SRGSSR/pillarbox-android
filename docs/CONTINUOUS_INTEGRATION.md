# Continuous integration

The project provides support for continuous integration. 

- The `Makefile` to initialize and execute [Danger](https://danger.systems/ruby/) locally.
- Gradle task to execute lint code using [Detekt](https://detekt.dev/).

We currently use TeamCity for continuous integration and GitHub for issue and pull request management. This document describes the steps required to fully integrate the tool suite with TeamCity and GitHub.

## Required tools

The continuous integration server should have the following tools installed:

- [gem](https://rubygems.org) and [bundler](https://bundler.io).
- gradle

## Continuous integration user

Our current workflow is based on pull requests, which TeamCity is able to automatically monitor with a dedicated [build feature](https://www.youtube.com/watch?v=4yFck9PvXI4). When a pull request is created TeamCity can automatically trigger various jobs which can post their result as pull request GitHub comments.

Proper integration with GitHub requires the use of a dedicated continuous integration user (a bot) with write access to the repository. We already have a dedicated [RTS devops](https://github.com/rts-devops) user, we therefore only need a few additional configuration steps:

1. Ensure the bot has write access to the GitHub repository.
2. Integration with GitHub requires the creation of a dedicated [personal access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token) with minimal permissions.

Of course a proper SSH setup is also required so that repositories can be pulled by the continuous integration server.

## Code quality checks

To have TeamCity run code quality checks for GitHub pull requests and post the corresponding status back to GitHub:

1. Create a TeamCity configuration called _Code quality_.
2. Add a VCS _Trigger_ on `+:pull/*`.
3. Add a _Command Line_ build step which simply executes `make danger`.
4. Add a _Pull Requests_ build feature that monitor GitHub (requires a personal access token).
5. Checks are performed by Danger, which requires a few [environment variables](https://danger.systems/guides/getting_started.html) to be properly set. Add the following three environment variable _Parameters_ to the configuration:
	- `env.GITHUB_PULL_REQUEST_ID` with value  `%teamcity.pullRequest.number%`.
	- `env.GITHUB_REPO_SLUG` with value `SRGSSR/pillarbox-android`.
	- `env.GITHUB_REPO_URL` with value `https://github.com/SRGSSR/pillarbox-android`.
6. Add two _Agent Requirements_ ensuring that `env.GEM_HOME` and `env.ANDROID_HOME` exist. Check that some agents are compatible and assignable (if agents are configured manually you might need to explicitly allow the configuration to be run).

## Unit tests

To have TeamCity run unit tests for GitHub pull requests and post the corresponding status back to GitHub:

1. Create a TeamCity configuration called _Pillarbox-tests_.
2. Add a VCS _Trigger_ on `+:pull/*`.
3. Add a _Gradle_ build step for Unit tests which simply executes task `clean test`.
4. Add a _Gradle_ build step for Instrumented tests which simply executes task `connectedTest mergeAndroidReports`. _(Currently disabled)_
5. Add a _Pull Requests_ build feature that monitor GitHub (requires a personal access token).
6. Add a _Commit status publisher_ build feature which posts to GitHub (requires a personal access token).
7. Add an _XML report processing_ build feature formatting test output as _Ant JUnit_.
8. Add two _Agent Requirements_ ensuring that `env.GEM_HOME` and `env.ANDROID_HOME` exist. Check that some agents are compatible and assignable (if agents are configured manually you might need to explicitly allow the configuration to be run).

## Deliveries

TBD
