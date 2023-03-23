FROM eclipse-temurin:11.0.18_10-jre-jammy

RUN apt update && apt upgrade -y
RUN apt-get -y install apt-utils binutils

#
# Add the repository for libbzev packages and webnail-server.
# sed is used because setup.sh uses sudo to get root access and
# sudo is not supported by eclipse-temurin:11.0.18_10-jre-jammy.
# Similary lsb_release is not supported but /etc/os-release exists.
#
RUN . /etc/os-release && \
    curl https://billzaumen.github.io/bzdev/setup.sh | \
    sed s/'sudo -k'// | sed s/sudo// \
    | sed s/'`lsb_release -c -s`'/$VERSION_CODENAME/ | sh

RUN apt-get -y install libbzdev-base-java libbzdev-ejws-java webnail-server

RUN mkdir -p app
EXPOSE 80/tcp
WORKDIR /app
CMD ["java", "-p", "/usr/share/bzdev", "-m", "webnail/webnail.Server"]
