FROM eclipse-temurin:11.0.18_10-jre-jammy
#
# Add the repository for libbzev packages and webnail-server.
# sed is used because setup.sh uses sudo to get root access and
# sudo is not supported by openjdk:11.0.14-jre.
#
RUN wget -q -O /etc/apt/trusted.gpg.d/org.bzdev.BZDev.gpg \
    https://billzaumen.github.io/bzdev/BZDev.gpg \
    && echo deb https://billzaumen.github.io/bzdev/archive/ jammy contrib \
    > /etc/apt/sources.list.d/org.bzdev.BZDev.list \
    && apt update && apt upgrade -y
RUN apt-get -y install apt-utils  zstd binutils
#
# NOTE: leaving the following just in case - should not be an issue
#       with jammy.
# We have to download the following Debian packages because, while
# actually system independent, the packages were written for a later
# Linux release whose version of apt uses zstd compression.  So, we
# have to install them manually.  Fortunately, this is easy as we just
# need some JAR files.
#
# RUN apt-get download libbzdev-base-java libbzdev-ejws-java webnail-server
# RUN ar p libbzdev-base-java_*_all.deb data.tar.zst | zstd -d | tar xf - \
#  && ar p libbzdev-ejws-java_*_all.deb data.tar.zst | zstd -d | tar xf - \
#   && ar p webnail-server_*_all.deb data.tar.zst | zstd -d | tar xf -

RUN apt-get -y install libbzdev-base-java libbzdev-ejws-java webnail-server

RUN mkdir -p app
EXPOSE 80/tcp
WORKDIR /app
CMD ["java", "-p", "/usr/share/bzdev", "-m", "webnail/webnail.Server"]
