// Used only for webnail server - the webnail-server Debian package
// conflicts with the webnail Debian package because both contain a
// JAR file for the Java package webnail. The main use of the package
// webnail-server is inside a Linux container.  For example a Docker
// image uses webnail-server because it only needs a couple of classes
// from the full webnail package.
module webnail {
    exports webnail;
    requires org.bzdev.base;
    requires org.bzdev.ejws;
    requires java.base;
}
