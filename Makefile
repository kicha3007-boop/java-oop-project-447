.DEFAULT_GOAL := build

setup:
	make -C app setup

build:
	make -C app build

test:
	make -C app test

lint:
	make -C app lint

report:
	make -C app report

.PHONY: setup build test lint report
