<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://java.sun.com/xml/ns/j2ee"
	 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	 xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
                             http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
	 version="2.5">
  <display-name>Thumbnail - $(title)</display-name>
  <description>
    This web application consists of ordinary HTML files, images,
    and javascript files for a web page entitled "$(title)".  The web
    archive was generated  by a program named "thumbnail", which (in
    addition to scaling images) can create a web page to browse through
    the images and present them in a slideshow.
  </description>
  <jsp-config>
    <jsp-property-group>
      <url-pattern>/controls/error.jsp</url-pattern>
      <page-encoding>UTF-8</page-encoding>
    </jsp-property-group>
  </jsp-config>
  <mime-mapping>
    <extension>js</extension>
    <mime-type>text/javascript</mime-type>
  </mime-mapping>
  <mime-mapping>
    <extension>html</extension>
    <mime-type>text/html</mime-type>
  </mime-mapping>
  <mime-mapping>
    <extension>png</extension>
    <mime-type>image/png</mime-type>
  </mime-mapping>
  <mime-mapping>
    <extension>gif</extension>
    <mime-type>image/gif</mime-type>
  </mime-mapping>
$(repeatMimeTypes:endMimeTypes)  <mime-mapping>
    <extension>$(extension)</extension>
    <mime-type>$(mimetype)</mime-type>
  </mime-mapping>
$(endMimeTypes)
  <welcome-file-list>
    <welcome-file>/index.html</welcome-file>
  </welcome-file-list>
  <error-page>
    <exception-code>100</exception-code>
    <location>/controls/error.jsp</location>
  </error-page>
  <error-page>
    <exception-code>404</exception-code>
    <location>/controls/error.jsp</location>
  </error-page>
  <error-page>
    <exception-code>405</exception-code>
    <location>/controls/error.jsp</location>
  </error-page>
  <error-page>
    <exception-code>406</exception-code>
    <location>/controls/error.jsp</location>
  </error-page>
  <error-page>
    <exception-code>417</exception-code>
    <location>/controls/error.jsp</location>
  </error-page>
  <error-page>
    <exception-code>500</exception-code>
    <location>/controls/error.jsp</location>
  </error-page>
</web-app>
