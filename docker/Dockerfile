FROM openjdk:11.0.14-jre
#
# Add the repository for libbzev packages and webnail-server.
# sed is used because setup.sh uses sudo to get root access and
# sudo is not supported by openjdk:11.0.14-jre.
#
RUN curl https://billzaumen.github.io/bzdev/setup.sh | sed 's/sudo -k//' \
    | sed s/sudo// | sh
RUN apt update
RUN apt-get -y install apt-utils  zstd binutils
#
# We have to download the following Debian packages because, while
# actually system independent, the packages were written for a later
# Linux release whose version of apt uses zstd compression.  So, we
# have to install them manually.  Fortunately, this is easy as we just
# need some JAR files.
#
RUN apt-get download libbzdev-base-java libbzdev-ejws-java webnail-server
RUN ar p libbzdev-base-java_*_all.deb data.tar.zst | zstd -d | tar xf - \
  && ar p libbzdev-ejws-java_*_all.deb data.tar.zst | zstd -d | tar xf - \
  && ar p webnail-server_*_all.deb data.tar.zst | zstd -d | tar xf -
RUN mkdir -p app
EXPOSE 80/tcp
WORKDIR /app
CMD ["java", "-p", "/usr/share/java", "-m", "webnail/webnail.Server"]
