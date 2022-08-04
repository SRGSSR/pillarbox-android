android_lint.skip_gradle_task = false
android_lint.report_file = "./build/reports/android-lint.xml"
android_lint.lint(inline_mode: true)
# If you're dealing with a legacy project, with tons of warnings, you may want to lint only new/modified files.
# You can easily achieve that, setting the filtering parameter to true.
android_lint.filtering = true
android_lint.lint

# Sometimes it's a README fix, or something like that - which isn't relevant for
# including in a project's CHANGELOG for example
#declared_trivial = github.pr_title.include? "#trivial"

# Make it more obvious that a PR is a work in progress and shouldn't be merged yet
#warn("PR is classed as Work in Progress") if github.pr_title.include? "[WIP]"

# Warn when there is a big PR
#warn("Big PR") if git.lines_of_code > 500

# Don't let testing shortcuts get into master by accident
#fail("fdescribe left in tests") if `grep -r fdescribe specs/ `.length > 1
#fail("fit left in tests") if `grep -r fit specs/ `.length > 1
