FROM adoptopenjdk/openjdk8:debian-slim AS buildenv

ARG userId
ARG groupId
ARG gradleHome

# install git because jenkins for some reason doesn't store the commit id
RUN apt update -yqq && apt install git -yqq

# create user
RUN useradd -ms /bin/bash -u $userId builder

# make the working directory and make the new user its owner
RUN mkdir -p /opt/forgehax/build \
    && chown -R builder:builder /opt/forgehax
# create the gradle cache and set its owner
RUN mkdir -p $gradleHome \
    && chown -R builder:builder $gradleHome

FROM buildenv AS forgehax

ARG gradleHome
ENV GRADLE_USER_HOME $gradleHome

# this is the directory the built jar will be in
VOLUME /opt/forgehax/build/libs
# this is the directory gradle will cache jars
VOLUME $gradleHome

USER builder
WORKDIR /opt/forgehax

# copy project files into image
# anything that shouldn't be copied should be filtered in .dockerignore
COPY --chown=builder:builder . /opt/forgehax

# allow gradlew to be executed
RUN chmod +x gradlew

ENTRYPOINT [ "./gradlew" ]
