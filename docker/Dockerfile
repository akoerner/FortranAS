
ARG PROJECT="FortranAS"
ARG REQUIREMENTS_FILE="requirements.system"

ARG USER
ARG UID
ARG GID


## graphviz 2.49.0 is needed because support for conversions to png was dropped
## in newer versions e.g., `dot -T png some_file.dot some_file.png`
ARG GRAPHVIZ_SOURCE_PACKAGE_URL=https://gitlab.com/api/v4/projects/4207231/packages/generic/graphviz-releases/9.0.0/graphviz-9.0.0.tar.gz

FROM openjdk:22-slim-bookworm AS fortran_as_requirements_base

ARG PROJECT
ARG REQUIREMENTS_FILE

ARG GRAPHVIZ_SOURCE_PACKAGE_URL


RUN mkdir -p /tmp/${PROJECT}
WORKDIR /tmp/${PROJECT}
COPY docker/${REQUIREMENTS_FILE} . 
RUN apt-get update && \
    apt-get install --no-install-recommends -y $(sed '/^#/d' ${REQUIREMENTS_FILE}) && \
    rm -rf /var/lib/apt/lists/*

RUN mkdir -p /tmp/graphviz
WORKDIR /tmp/graphviz
RUN wget ${GRAPHVIZ_SOURCE_PACKAGE_URL} && \
    tar xzf *.tar.gz && \ 
    cd graphviz-* && \
    ./configure --with-cairo && \
    make && \
    make install

WORKDIR /tmp/${PROJECT}
COPY tools/save_system_requirements.sh . 
RUN bash save_system_requirements.sh "${REQUIREMENTS_FILE}"
#RUN pip3 install -r ${REQUIREMENTS_FILE_PIP3}


FROM fortran_as_requirements_base AS fortran_as_maven_dependencies_base

WORKDIR /app
COPY docker/pom.xml /app/pom.xml
COPY docker/settings.xml .
COPY docker/settings.xml /root/.m2/settings.xml

COPY docker/FortranAS/src/org/fortranas/test/InitTest.java FortranAS/src/org/fortranas/test/InitTest.java

RUN mkdir -p /app/repo && \
    mvn dependency:go-offline verify compile test -Dmaven.repo.local=/app/repo

RUN mvn verify compile test -Dmaven.repo.local=/app/repo

FROM fortran_as_maven_dependencies_base AS fortran_as_source_code_base 

WORKDIR /app
COPY FortranAS/ FortranAS 

FROM fortran_as_source_code_base AS fortran_as_antlr4_grammars_base 

WORKDIR /app
COPY antlr4/generated/ FortranAS/src/generated/antlr4_generated

FROM fortran_as_antlr4_grammars_base AS fortran_as_builder 

ARG PROJECT
ARG REQUIREMENTS_FILE

COPY antlr4/ antlr4 
COPY tools/ tools
RUN touch /.docker
COPY sql /app/sql
COPY sql /app/build/sql

RUN mvn -o compile -Dmaven.repo.local=/app/repo && \ 
    mvn -o package -Dmaven.repo.local=/app/repo

COPY tools/fortranas /app/build/fortranas 
WORKDIR /app


FROM fortran_as_builder AS fortran_as_runtime

ARG PROJECT
ARG USER
ARG UID
ARG GID


COPY --from=fortran_as_builder /app/build /app/build



WORKDIR /

COPY docker/entrypoint.sh . 
RUN chmod +x entrypoint.sh

RUN useradd -ms /bin/bash ${USER}
RUN usermod -u ${UID} ${USER} && groupmod -g ${GID} ${USER}

RUN mkdir -p /tmp/FortranAS
RUN chown -r ${UID}:${GID} /app | true
RUN chown -r ${UID}:${GID} /tmp/FortranAS | true
RUN chown ${UID}:${GID} /entrypoint.sh

USER ${USER}
WORKDIR /tmp/FortranAS 
CMD ["/entrypoint.sh"]

