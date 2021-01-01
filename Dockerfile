FROM adoptopenjdk/openjdk8:debian-slim AS forgehax

ARG user=builder
ARG uid=1000
ARG gid=1000
ARG home=/home/$user
ARG cache=$home/.cache
ARG src=$home/src
ARG gradleHome=$cache/.gradle

ENV GRADLE_USER_HOME $gradleHome
ENV SRC_DIR $src
ENV BUILD_DIR $home/forgehax

# install git because jenkins for some reason doesn't store the commit id
RUN apt update -qqy
RUN apt install git -qqy

# create user and group with same user and group id as the host
RUN groupadd -g $gid $user
RUN useradd -ms /bin/bash -g $gid -u $uid $user

# make the working directory and make the new user its owner
RUN mkdir -p $home/forgehax \
    && chown -R $user:$user $home/forgehax

# create the gradle cache and set its owner
RUN mkdir -p $cache && mkdir -p $gradleHome \
    && chown -R $user:$user $cache

# src code volume
RUN mkdir -p $src \
    && chown -R $user:$user $src

# .minecraft/mods directory (forgehax will copy a jar to this folder if it exists)
RUN mkdir -p $home/.minecraft/mods \
    && chown -R $user:$user $home/.minecraft

# this is the directory gradle will cache jars
VOLUME $cache

# folder where minecraft a copy of the built jar will be copied
VOLUME $home/.minecraft/mods

# directory the src code is mounted
VOLUME $src

# init script location
VOLUME $home/init

USER $user
WORKDIR $home

ENTRYPOINT [ "./init" ]
