.PHONY: danger
danger:
	@echo "Setting up the project..."
	@bundle version
	@bundle install --path vendor/bundle
	@echo "Execute danger"
	@bundle exec danger
	@echo "... done.\n"