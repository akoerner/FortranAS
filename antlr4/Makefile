.DEFAULT_GOAL := all

.PHONY: all
all: build copy_generated

.PHONY: build
build: clean build_antlr4 generate_lexer generate_parser

.PHONY: build_antlr4
build_antlr4:
	cd antlr4/docker && docker build -t antlr/antlr4 --platform linux/amd64 .

.PHONY: generate_parser
generate_parser: 
	docker run --rm -u $$(id -u $${USER}):$(id -g $${USER}) -v $$(pwd):/work antlr/antlr4 -Dlanguage=Java grammars-v4/fortran/fortran*/Fortran*Parser.g4 

.PHONY: generate_lexer
generate_lexer:
	docker run --rm -u $$(id -u $${USER}):$(id -g $${USER}) -v $$(pwd):/work antlr/antlr4 -Dlanguage=Java grammars-v4/fortran/fortran*/Fortran*Lexer.g4

.PHONY: clean
clean:
	rm -rf generated
	cd grammars-v4/ && git clean -f -d || true

.PHONY: copy_generated
copy_generated: clean
	mkdir -p generated
	cp -r grammars-v4/fortran generated/fortran
#	rm generated/fortran/pom.xml


