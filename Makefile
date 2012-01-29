#
# GNU Make file.
#

DATE = $(shell date -R)

#
# Set this if  'make install' should install its files into a
# user directory - useful for package systems that will grab
# all the files they see.  Setting this will allow a package
# to be built without requiring root permissions.
#
DESTDIR :=

JROOT := $(shell while [ ! -d webnail -a `pwd` != / ] ; do cd .. ; done ; pwd)

include VersionVars.mk


CLASSES = $(JROOT)/classes

#
# System directories (that contains JAR files, etc.)
#
SYS_BIN = /usr/bin
SYS_MANDIR = /usr/share/man
SYS_DOCDIR = /usr/share/doc/webnail
SYS_CONFDIR = /etc
SYS_MIMEDIR = /usr/share/mime
SYS_APPDIR = /usr/share/applications
SYS_APP_ICON_DIR = /usr/share/icons/hicolor/scalable/apps
SYS_MIME_ICON_DIR = /usr/share/icons/hicolor/scalable/mimetypes
SYS_JARDIRECTORY = /usr/share/java

# Target JARDIRECTORY - where 'make install' actually puts the jar
# file (DESTDIR is not null when creating packages)
#
JARDIRECTORY = $(DESTDIR)$(SYS_JARDIRECTORY)
#
# JARDIRECTORY modified so that it can appear in a sed command
#
JARDIR=$(shell echo $(SYS_JARDIRECTORY) | sed  s/\\//\\\\\\\\\\//g)

EXTLIBS=$(SYS_JARDIRECTORY)/bzdevlib.jar

# Other target directories

BIN = $(DESTDIR)$(SYS_BIN)
MANDIR = $(DESTDIR)$(SYS_MANDIR)
DOCDIR = $(DESTDIR)$(SYS_DOCDIR)
CONFDIR = $(DESTDIR)$(SYS_CONFDIR)
MIMEDIR = $(DESTDIR)$(SYS_MIMEDIR)
APPDIR = $(DESTDIR)$(SYS_APPDIR)
MIME_ICON_DIR = $(DESTDIR)$(SYS_MIME_ICON_DIR)
# Icon directory for applications
#
APP_ICON_DIR = $(DESTDIR)$(SYS_APP_ICON_DIR)

# Full path name of for where webnail.desktop goes
#
# APPDIR = $(DESTDIR)/usr/share/applications

# Installed name of the icon to use for the WEBNAIL application
#
SOURCEICON = Icons/webnailicon.svg
TARGETICON = webnail.svg


# Installed names of icons to use for webnail document types
# (originals in Icons subdirectory)
#
SOURCE_DOC_ICON = webnaildocicon.svg
TARGET_DOC_ICON = application-prs.wtz.webnail+xml.svg

SOURCE_LDOC_ICON = webnaillayouticon.svg
TARGET_LDOC_ICON = application-prs.wtz.webnail-layout+xml.svg

SOURCE_TDOC_ICON = webnailtmplicon.svg
TARGET_TDOC_ICON = application-prs.wtz.webnail-template.svg

JROOT_DOCDIR = $(JROOT)$(SYS_DOCDIR)
JROOT_JARDIR = $(JROOT)/jar
JROOT_MANDIR = $(JROOT)/man
JROOT_BIN = $(JROOT)/bin
JROOT_CONFDIR = $(JROOT)/etc
# Icon directory for applications
#
APP_ICON_DIR = $(DESTDIR)$(SYS_APP_ICON_DIR)

MANS = $(JROOT_MANDIR)/man1/webnail.1.gz $(JROOT_MANDIR)/man5/webnail.conf.5.gz

HELPICONS = WebFiles/fleft.gif  WebFiles/fright.gif  \
	WebFiles/left.gif  WebFiles/redo.gif \
	WebFiles/right.gif  WebFiles/rlredo.gif
ICONS = $(SOURCEICON) $(HELPICONS) Icons/dndTarget.png Icons/blank.png \
	WebFiles/expand.png

HELPFILES = Manual/manual.xml Manual/manual.html \
	Manual/gui-main.png Manual/gui-edit.png Manual/gui-proxy.png \
	Manual/gui-dom.png Manual/gui-properties.png Manual/gui-title.png
WEBFILES = WebFiles/strut.gif WebFiles/initImage.png WebFiles/initial.html \
	WebFiles/slideshow1.js WebFiles/slideshow2.js \
	WebFiles/medium.html WebFiles/error.jsp

LAYOUTFILES = Layouts/normalLayout.xml Layouts/html5Layout.xml \
	Layouts/single1.xml Layouts/single2.xml Layouts/single3.xml \
	Layouts/single4.xml Layouts/single5.xml

TEMPLATES = Templates/indexHTML.wnt Templates/indexHTML5.wnt \
	Templates/mediumHTML.wnt Templates/params.wnt  Templates/web.wnt \
	Templates/slideshowHTML.wnt Templates/tindexHTML.wnt \
	Templates/singleHTML1.wnt Templates/singleHTML2.wnt \
	Templates/singleHTML3.wnt Templates/singleHTML4.wnt \
	Templates/singleHTML5.wnt Templates/singleHTML1link.wnt \
	Templates/singleHTML2link.wnt Templates/singleHTML3link.wnt \
	Templates/singleHTML4link.wnt Templates/singleHTML5link.wnt


JFILES = $(wildcard webnail/*.java)
PROPERTIES = $(wildcard Properties/*.properties)

FILES = $(JFILES) $(PROPERTIES) webnail.mf $(ICONS) \
	$(HELPFILES) $(WEBFILES) $(LAYOUTFILES) $(TEMPLATES) webnail-1.0.dtd \
	webnail-layout-info-1.0.dtd webnail/helpers.txt

PROGRAM = $(JROOT_BIN)/webnail $(JROOT_JARDIR)/webnail-$(VERSION).jar 
ALL = $(PROGRAM) webnail.desktop webnail.conf \
	 $(MANS) $(JROOT_BIN)/webnail

# program: $(JROOT_BIN)/webnail $(JROOT_JARDIR)/webnail-$(VERSION).jar 

program: $(PROGRAM)

# all: program webnail.desktop webnail.conf $(MANS) $(DOCS)

all: $(ALL)

include MajorMinor.mk

$(CLASSES):
	(cd $(JROOT); mkdir classes)

#
# The action for this rule removes all the webnail-*.jar files
# because the old ones would otherwise still be there and end up
# being installed.
#
$(JROOT_JARDIR)/webnail-$(VERSION).jar: $(FILES)
	mkdir -p $(CLASSES)
	mkdir -p $(CLASSES)/webnail
	javac -Xlint:unchecked -Xlint:deprecation \
		-d $(CLASSES) -classpath $(CLASSES):$(EXTLIBS) -sourcepath . \
		$(JFILES)
	cp $(PROPERTIES) $(ICONS) $(HELPFILES) $(LAYOUTFILES) \
		webnail/helpers.txt \
		$(WEBFILES) $(TEMPLATES) $(CLASSES)/webnail
	cp webnail-1.0.dtd webnail-layout-info-1.0.dtd $(CLASSES)
	mkdir -p $(JROOT_JARDIR)
	rm -f $(JROOT_JARDIR)/webnail-*.jar
	jar cfm $(JROOT_JARDIR)/webnail-$(VERSION).jar webnail.mf \
		-C $(CLASSES) . 

$(JROOT_BIN)/webnail: webnail.sh MAJOR MINOR \
		$(JROOT_JARDIR)/webnail-$(VERSION).jar
	(cd $(JROOT); mkdir -p $(JROOT_BIN))
	sed s/VERSION/$(VERSION)/g webnail.sh | \
	sed s/JARDIRECTORY/$(JARDIR)/g > $(JROOT_BIN)/webnail
	chmod u+x $(JROOT_BIN)/webnail

$(JROOT_MANDIR)/man1/webnail.1.gz: webnail.1
	mkdir -p $(JROOT_MANDIR)/man1
	sed s/VERSION/$(VERSION)/g webnail.1 | \
	gzip -9 > $(JROOT_MANDIR)/man1/webnail.1.gz

$(JROOT_MANDIR)/man5/webnail.conf.5.gz: webnail.conf.5
	mkdir -p $(JROOT_MANDIR)/man5
	sed s/VERSION/$(VERSION)/g webnail.conf.5 | \
	gzip -9 > $(JROOT_MANDIR)/man5/webnail.conf.5.gz

clean:
	rm $(CLASSES)/*.class $(JROOT_JARDIR)/webnail-$(VERSION).jar \
	$(JROOT_MANDIR)/man1/webnail.1.gz \
	$(JROOT_MANDIR)/man5/webnail.conf.5.gz \
	$(JROOT_BIN)/webnail \

install: all 
	install -d $(CONFDIR)
	install -d $(APP_ICON_DIR)
	install -d $(MIME_ICON_DIR)
	install -d $(MIMEDIR)
	install -d $(MIMEDIR)/packages
	install -d $(APPDIR)
	install -d $(BIN)
	install -d $(MANDIR)
	install -d $(MANDIR)/man1
	install -d $(MANDIR)/man5
	install -d $(JARDIRECTORY)
	install -m 0644 webnail.conf  $(CONFDIR)
	install -m 0644 $(SOURCEICON) $(APP_ICON_DIR)/$(TARGETICON)
	(cd MIME ; \
	 install -m 0644 webnail.xml $(MIMEDIR)/packages/webnail.xml)
	(cd MIME ; \
	 install -m 0644 webnail-layout.xml \
		$(MIMEDIR)/packages/webnail-layout.xml)
	(cd MIME ; \
	 install -m 0644 webnail-template.xml \
		$(MIMEDIR)/packages/webnail-template.xml)
	(cd Icons ; \
	 install -m 0644 $(SOURCE_DOC_ICON) \
		$(MIME_ICON_DIR)/$(TARGET_DOC_ICON))
	(cd Icons ; \
	 install -m 0644 $(SOURCE_LDOC_ICON) \
		$(MIME_ICON_DIR)/$(TARGET_LDOC_ICON))
	(cd Icons ; \
	 install -m 0644 $(SOURCE_TDOC_ICON) \
		$(MIME_ICON_DIR)/$(TARGET_TDOC_ICON))
	install -m 0644 $(JROOT_JARDIR)/webnail-$(VERSION).jar \
		$(JARDIRECTORY)
	install -m 0755 $(JROOT_BIN)/webnail $(BIN)
	install -m 0644 webnail.desktop $(APPDIR)
	install -m 0644 $(JROOT_MANDIR)/man1/webnail.1.gz $(MANDIR)/man1
	install -m 0644 $(JROOT_MANDIR)/man5/webnail.conf.5.gz $(MANDIR)/man5

uninstall:
	@rm $(MANDIR)/man1/webnail.1.gz || echo ... rm webnail.1.gz  FAILED
	@rm $(MANDIR)/man5/webnail.conf.5.gz || \
		echo ... rm webnail.conf.5.gz  FAILED
	@rm $(APPDIR)/webnail.desktop || echo ... rm webnail.desktop FAILED
	@rm $(BIN)/webnail   || echo ... rm $(BIN)/webnail FAILED
	@rm $(APP_ICON_DIR)/$(TARGETICON)  || echo ... rm $(TARGETICON) FAILED
	@rm $(MIME_ICON_DIR)/$(TARGET_DOC_ICON)  || \
		echo ... rm $(TARGET_DOC_ICON) FAILED
	@rm $(MIME_ICON_DIR)/$(TARGET_LDOC_ICON)  || \
		echo ... rm $(TARGET_LDOC_ICON) FAILED
	@rm $(MIME_ICON_DIR)/$(TARGET_TDOC_ICON)  || \
		echo ... rm $(TARGET_TDOC_ICON) FAILED
	(cd $(MIMEDIR)/packages ; \
	 rm webnail-template.xml webnail-layout.xml  webnail.xml)
	@rm $(CONFDIR)/webnail.conf || echo \
		... rm $(CONFDIR)/webnail.conf FAILED
	@rm $(JARDIRECTORY)/webnail-$(VERSION).jar \
		|| echo ... rm webnail-$(VERSION).jar FAILED

