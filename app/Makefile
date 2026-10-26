.DEFAULT_GOAL := build

setup:
	./gradlew assemble

clean:
	./gradlew clean

build:
	./gradlew clean build

test:
	./gradlew test

report:
	./gradlew jacocoTestReport

lint:
	./gradlew spotlessCheck

lint-fix:
	./gradlew spotlessApply

check-deps:
	./gradlew dependencyUpdates -Drevision=release

.PHONY: setup clean build test report lint lint-fix check-deps
