SHELL:=/bin/bash

.DEFAULT_GOAL := all

ROOT_DIR:=$(shell dirname "$(realpath $(firstword $(MAKEFILE_LIST)))")

SOURCE_DIRECTORY?=${ROOT_DIR}/source
OUTPUT_DIRECTORY?=${ROOT_DIR}/output

.EXPORT_ALL_VARIABLES:
DOCKER_BUILDKIT?=1
DOCKER_CONFIG?=

export DOCKER_GID := $(shell getent group docker | cut -d: -f3)
export UID := $(shell id -u)
export GID := $(shell id -g)


MAKEFLAGS += --no-print-directory


#REQUIREMENTS_FILE:=requirements.system.dev.save
REQUIREMENTS_FILE:=requirements.system.dev

GIT_HASH := $(shell git rev-parse --short HEAD | tr '[:upper:]' '[:lower:]')

PROJECT:=fortran-as
TAG:=${GIT_HASH}

.PHONY: all
all: help

.PHONY: help
help:
	@awk 'BEGIN {FS = ":.*##"; printf "Usage: make \033[36m<target>\033[0m \n"} /^[a-zA-Z_-]+:.*?##/ { printf "  \033[36m%-10s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

.PHONY: generate_sources
generate_sources: ## Build Antler4 grammars and generate FortranAS generated classes
	cd antlr4 && make
	bash tools/generate_fortran_parsers.sh
	bash tools/generate_abstract_fortran_lexers.sh
	find "antlr4/generated" -type f -name "*.token" -exec cp {} build \;

.PHONY: build
build: clean generate_sources ## Build FortranAS
	mkdir -p build
	cp FortranAS.conf build
	docker build \
                  --build-arg BUILDKIT_INLINE_CACHE=1 \
                  --build-arg UID=${UID} \
                  --build-arg GID=${GID} \
                  --build-arg DOCKER_GID=${DOCKER_GID} \
                  --build-arg USER=${USER} \
                  -f docker/Dockerfile -t ${PROJECT}:${TAG} --build-arg REQUIREMENTS_FILE=${REQUIREMENTS_FILE} .
	make copy_build_artifacts
#	docker build -t dot-converter -f Dockerfile.dot-converter .

.PHONY: build_antlr4_fortran_grammars
build_antlr4_fortran_grammars: ## Build antlr4 Fortran grammars
	cd antlr4 && make build


.PHONY: package
package: build
	bash tools/package.sh

.PHONY: copy_build_artifacts
copy_build_artifacts:
	docker cp $$(docker create --rm ${PROJECT}:${TAG}):/app/build/ . >/dev/null 2>&1
	cp tools/fortranas build
	cp sql build/ -r
	rm build/*.sql

.PHONY: build_local
build_local: ## Build FortranAS locally sans docker using maven. Docker is used always to build the antlr4 grammars. 
	mvn compile
	mvn package

.PHONY: check_source_dir
check_source_dir:
	@[ ! -n "$$(find source -type f -not -name "README.txt")" ] && { echo "Error: The 'source' is empty, add Fortran sources and try again." >&2 && exit 1; }

#TEST_PROGRAM=TreeMergerTest
#TEST_PROGRAM=TreeCloner
#TEST_PROGRAM=TreeSerializer
#TEST_PROGRAM=Tree
TEST_PROGRAM=DOTGenerator
.PHONY: run_test_program
run_test_program:
	cd FortranAS/src && javac ${TEST_PROGRAM}.java && java ${TEST_PROGRAM} && rm ${TEST_PROGRAM}.class

.PHONY: run
run: 
	mkdir -p output
	docker run \
        --name ${PROJECT} \
        --rm \
        -v ${SOURCE_DIRECTORY}:/app/fortran_code_samples \
        -v ${OUTPUT_DIRECTORY}:/app/output \
        ${PROJECT}:${TAG}

.PHONY: build_fast
build_fast: 
	@if [ -n "$$(docker images -q ${PROJECT}:${TAG})" ]; then \
        echo "Docker image: ${PROJECT}:${TAG} already build, skipping build."; \
        make copy_build_artifacts;\
    else \
        make build;\
    fi

.PHONY: list_lexers
list_lexers: build_fast ## List available antlr4 lexers that FortranAS can provide
	@docker run \
        --name ${PROJECT} \
        --rm \
        -v ${SOURCE_DIRECTORY}:/tmp/src \
        -v ${OUTPUT_DIRECTORY}:/output \
        ${PROJECT}:${TAG} /bin/bash -c "/app/build/fortranas -l"

.PHONY:
run_demo: build_fast ## Run FortranAS on sample programs in ./fortran_code_samples. Output will be available in ./output
	make run SOURCE_DIRECTORY=${ROOT_DIR}/fortran_code_samples 

.PHONY: dot_files_to_html
dot_files_to_html: 
	docker run -v ${OUTPUT_DIRECTORY}:/tmp/src dot-converter

.PHONY: debug
debug:
	docker run \
        -it \
        --name ${PROJECT} \
        --rm \
        -v ${SOURCE_DIRECTORY}:/app/src \
        -v ${OUTPUT_DIRECTORY}:/app/output \
        ${PROJECT}:${TAG} /bin/bash


.PHONY: save_system_requirements
save_system_requirements:
	docker run --rm -v ${ROOT_DIR}:/tmp/save_system_requirements --rm --entrypoint="bash" fortran-as -c "cd /tmp/save_system_requirements && bash save_system_requirements.sh ${REQUIREMENTS_FILE}"

.PHONY: clean
clean: docker_clean ## Clean build directory, docker images, and output directory
	cd antlr4 && make clean
	rm -rf build
	bash tools/clean.sh
	rm -rf output
	cd ${SOURCE_DIRECTORY} && shopt -s nullglob && rm -f *.{pt,svg,dot,json,log,txt}; shopt -u nullglob
	rm -rf ${OUTPUT_DIRECTORY}

.PHONY: docker_clean
docker_clean:
	docker rm $$(docker ps -a -q --filter "ancestor=${PROJECT}:${TAG}") --force 2> /dev/null || true
	docker rmi $$(docker images -q ${PROJECT}:${TAG}) --force 2> /dev/null || true
	docker rmi $$(docker images --filter "dangling=true" -q) --force > /dev/null 2>&1 || true

.PHONY: test
test: ## Run unit tests
	mvn test
