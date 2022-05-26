#
# GNU Makefile.
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

APPS_DIR = apps
MIMETYPES_DIR = mimetypes

#
# System directories (that contains JAR files, etc.)
#
SYS_BIN = /usr/bin
SYS_MANDIR = /usr/share/man
SYS_DOCDIR = /usr/share/doc/webnail
SYS_MIMEDIR = /usr/share/mime
SYS_APPDIR = /usr/share/applications
SYS_ICON_DIR = /usr/share/icons/hicolor
SYS_POPICON_DIR = /usr/share/icons/Pop
SYS_APP_ICON_DIR = $(SYS_ICON_DIR)/scalable/$(APPS_DIR)
SYS_APP_POPICON_DIR = $(SYS_POPICON_DIR)/scalable/$(APPS_DIR)
SYS_MIME_ICON_DIR =$(SYS_ICON_DIR)/scalable/$(MIMETYPES_DIR)
SYS_MIME_POPICON_DIR =$(SYS_POPICON_DIR)/scalable/$(MIMETYPES_DIR)
SYS_JARDIRECTORY = /usr/share/java
SYS_BZDEV_DIR = /usr/share/bzdev

# ICON_WIDTHS = 16 20 22 24 32 36 48 64 72 96 128 192 256

ICON_WIDTHS = 8 16 20 22 24 32 36 48 64 72 96 128 192 256 512
ICON_WIDTHS2x = 16 24 32 48 64 128 256

POPICON_WIDTHS = 8 16 24 32 48 64 128 256
POPICON_WIDTHS2x = 8 16 24 32 48 64 128 256

# Target JARDIRECTORY - where 'make install' actually puts the jar
# file (DESTDIR is not null when creating packages)
#
JARDIRECTORY = $(DESTDIR)$(SYS_JARDIRECTORY)
BZDEV_DIR = $(DESTDIR)$(SYS_BZDEV_DIR)
#
# JARDIRECTORY modified so that it can appear in a sed command
#
JARDIR=$(shell echo $(SYS_JARDIRECTORY) | sed  s/\\//\\\\\\\\\\//g)

BZDIR =$(shell echo $(SYS_BZDEV_DIR) | sed  s/\\//\\\\\\\\\\//g)

# Other target directories

BIN = $(DESTDIR)$(SYS_BIN)
MANDIR = $(DESTDIR)$(SYS_MANDIR)
DOCDIR = $(DESTDIR)$(SYS_DOCDIR)
MIMEDIR = $(DESTDIR)$(SYS_MIMEDIR)
APPDIR = $(DESTDIR)$(SYS_APPDIR)
MIME_ICON_DIR = $(DESTDIR)$(SYS_MIME_ICON_DIR)
MIME_POPICON_DIR = $(DESTDIR)$(SYS_MIME_POPICON_DIR)
# Icon directory for applications
#
APP_ICON_DIR = $(DESTDIR)$(SYS_APP_ICON_DIR)
APP_POPICON_DIR = $(DESTDIR)$(SYS_APP_POPICON_DIR)

ICON_DIR = $(DESTDIR)$(SYS_ICON_DIR)
POPICON_DIR = $(DESTDIR)$(SYS_POPICON_DIR)

# Full path name of for where webnail.desktop goes
#
# APPDIR = $(DESTDIR)/usr/share/applications

# Installed name of the icon to use for the WEBNAIL application
#
SOURCEICON = Icons/webnailicon.svg
TARGETICON = webnail.svg
TARGETICON_PNG = webnail.png


# Installed names of icons to use for webnail document types
# (originals in Icons subdirectory)
#
SOURCE_DOC_ICON = Icons/webnaildocicon.svg
TARGET_DOC_ICON = application-x.webnail+xml.svg
TARGET_DOC_ICON_PNG = application-x.webnail+xml.png

SOURCE_LDOC_ICON = Icons/webnaillayouticon.svg
TARGET_LDOC_ICON = application-x.webnail-layout+xml.svg
TARGET_LDOC_ICON_PNG = application-x.webnail-layout+xml.png

SOURCE_TDOC_ICON = Icons/webnailtmplicon.svg
TARGET_TDOC_ICON = application-x.webnail-template.svg
TARGET_TDOC_ICON_PNG = application-x.webnail-template.png

JROOT_DOCDIR = $(JROOT)$(SYS_DOCDIR)
JROOT_JARDIR = $(JROOT)/jar
JROOT_MANDIR = $(JROOT)/man
JROOT_BIN = $(JROOT)/bin

EXTDIR = $(SYS_JARDIRECTORY)

EXTLIBS=$(EXTDIR)/libbzdev.jar:$(EXTDIR)/libbzdev-servlets.jar:$(EXTDIR)/servlet-api.jar


MANS = $(JROOT_MANDIR)/man1/webnail.1.gz \
	$(JROOT_MANDIR)/man5/webnail.5.gz

HELPICONS = WebFiles/fleft.gif  WebFiles/fright.gif  \
	WebFiles/left.gif  WebFiles/redo.gif \
	WebFiles/right.gif  WebFiles/rlredo.gif
ICONS = $(SOURCEICON) $(HELPICONS) Icons/dndTarget.png Icons/blank.png \
	WebFiles/expand.png WebFiles/expandRV.png \
	WebFiles/fleftRV.gif WebFiles/frightRV.gif  \
	WebFiles/leftRV.gif WebFiles/rightRV.gif \
	Icons/webnailicon16.png	Icons/webnailicon24.png \
	Icons/webnailicon32.png	Icons/webnailicon48.png \
	Icons/webnailicon64.png	Icons/webnailicon96.png \
	Icons/webnailicon128.png Icons/webnailicon256.png \
	Icons/webnailicon512.png

HELPFILES = Manual/manual.xml Manual/manual.html docs/manual/browser.png \
	docs/manual/gui-main.png docs/manual/gui-edit.png \
	docs/manual/gui-proxy.png docs/manual/gui-dom.png \
	docs/manual/gui-properties.png docs/manual/gui-title.png \
	docs/manual/editFields.png docs/manual/editImages.png \
	docs/manual/input.png docs/manual/maxhw.png \
	docs/manual/output.png docs/manual/run.png \
	Manual/manualDM.xml

WEBFILES = WebFiles/strut.gif WebFiles/initImage.png WebFiles/initial.html \
	WebFiles/slideshow1.js WebFiles/slideshow2.js \
	WebFiles/medium.html WebFiles/error.jsp WebFiles/initial.png

LAYOUTFILES = Layouts/html5Layout.xml Layouts/html590Layout.xml \
	Layouts/html5NSLayout.xml Layouts/html5NTLayout.xml \
	Layouts/html590NSLayout.xml \
	Layouts/single1.xml Layouts/single2.xml Layouts/single3.xml \
	Layouts/single4.xml Layouts/single5.xml

TEMPLATES = Templates/indexHTML5.wnt Templates/indexHTML590.wnt \
	Templates/indexHTML5NS.wnt Templates/indexHTML590NS.wnt \
	Templates/indexHTML590NT.wnt \
	Templates/mediumHTML.wnt Templates/params.wnt \
	Templates/web.wnt Templates/tindexHTML.wnt \
	Templates/tindex90HTML.wnt \
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
SERVER = $(JROOT_JARDIR)/webnail-server-$(VERSION).jar
ALL = $(PROGRAM) webnail.desktop $(MANS) $(JROOT_BIN)/webnail $(SERVER) \
	docs/manual/manual.html

# program: $(JROOT_BIN)/webnail $(JROOT_JARDIR)/webnail-$(VERSION).jar 

program: clean $(PROGRAM) docs/manual/manual.html

#
# Before using, set up a symbolic link for bzdevlib.jar in the ./jar directory.
# This is useful for testing that requires modifying files in bzdev-jlib.
#
testversion:
	make program EXTDIR=$(JROOT_JARDIR)


# all: program webnail.desktop $(MANS) $(DOCS)

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
	sed -e 's/<!-- line1 -->/A {color: rgb(65,225,128);}/' \
	    -e 's/<!-- line2 -->/A:link {color: rgb(65,225,128);}/' \
	    -e 's/<!-- line3 -->/A:visited {color: rgb(65,164,128);}/' \
	    Manual/manual.html > $(CLASSES)/webnail/manualDM.html
	mkdir -p $(JROOT_JARDIR)
	rm -f $(JROOT_JARDIR)/webnail-[0123456789]*.jar
	jar cfm $(JROOT_JARDIR)/webnail-$(VERSION).jar webnail.mf \
		-C $(CLASSES) .

$(JROOT_JARDIR)/webnail-server-$(VERSION).jar: modinfo/module-info.java \
		$(JROOT_JARDIR)/webnail-$(VERSION).jar \
		Properties/Server.properties
	mkdir -p mods/webnail/webnail
	cp $(CLASSES)/webnail/WebnailServletAdapter.class mods/webnail/webnail
	cp $(CLASSES)/webnail/WebnailAuthenticator.class mods/webnail/webnail
	cp $(CLASSES)/webnail/Server.class mods/webnail/webnail
	cp Properties/Server.properties mods/webnail/webnail
	javac -d mods/webnail -p /usr/share/bzdev  modinfo/module-info.java
	rm -f $(JROOT_JARDIR)/webnail-server-*.jar
	jar --create --file $(SERVER) --main-class=webnail.Server \
		-C mods/webnail .

$(JROOT_BIN)/webnail: webnail.sh MAJOR MINOR \
		$(JROOT_JARDIR)/webnail-$(VERSION).jar
	(cd $(JROOT); mkdir -p $(JROOT_BIN))
	sed s/VERSION/$(VERSION)/g webnail.sh | \
	sed s/BZDEV_DIR/$(BZDIR)/g | \
	sed s/JARDIRECTORY/$(JARDIR)/g > $(JROOT_BIN)/webnail
	chmod u+x $(JROOT_BIN)/webnail
	if [ "$(DESTDIR)" = "" ] ; \
	then ln -sf $(EXTDIR)/libbzdev.jar $(JROOT_JARDIR)/libbzdev.jar ; \
	fi

$(JROOT_MANDIR)/man1/webnail.1.gz: webnail.1
	mkdir -p $(JROOT_MANDIR)/man1
	sed s/VERSION/$(VERSION)/g webnail.1 | \
	gzip -n -9 > $(JROOT_MANDIR)/man1/webnail.1.gz

$(JROOT_MANDIR)/man5/webnail.5.gz: webnail.5
	mkdir -p $(JROOT_MANDIR)/man5
	sed s/VERSION/$(VERSION)/g webnail.5 | \
	gzip -n -9 > $(JROOT_MANDIR)/man5/webnail.5.gz


#
# Genrated and saved in the git repository because the 'docs'
# subdirectory is used by GitHub to create project documentation.
#
docs/manual/manual.html: Manual/manual.html
	sed -e 's!/[*] START!!' \
	    -e 's!END [*]/!!' \
	    -e 's/<!-- line1 -->/A {color: rgb(65,225,128);}/' \
	    -e 's/<!-- line2 -->/A:link {color: rgb(65,225,128);}/' \
	    -e 's/<!-- line3 -->/A:visited {color: rgb(65,164,128);}/' \
	    Manual/manual.html > docs/manual/manual.html

clean:
	rm -f $(CLASSES)/webnail/* $(JROOT_JARDIR)/webnail-$(VERSION).jar \
	$(JROOT_MANDIR)/man1/* \
	$(JROOT_MANDIR)/man5/* \
	$(JROOT_BIN)/webnail \
	$(CLASSES)/*.dtd
	[ -d $(CLASSES)/webnail ] && rmdir $(CLASSES)/webnail || true
	[ -d man/man1 ] && rmdir man/man1 || true
	[ -d man/man5 ] && rmdir man/man5 || true
	[ -d man ] && rmdir man || true

install: all 
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
	install -m 0644 -T $(SOURCEICON) $(APP_ICON_DIR)/$(TARGETICON)
	for i in $(ICON_WIDTHS) ; do \
		install -d $(ICON_DIR)/$${i}x$${i}/$(APPS_DIR) ; \
		inkscape -w $$i --export-filename=tmp.png $(SOURCEICON) ; \
		install -m 0644 -T tmp.png \
			$(ICON_DIR)/$${i}x$${i}/$(APPS_DIR)/$(TARGETICON_PNG); \
		rm tmp.png ; \
	done
	for i in $(ICON_WIDTHS2x) ; do \
		ii=`expr 2 '*' $$i` ; \
		install -d $(ICON_DIR)/$${i}x$${i}@2x/$(APPS_DIR) ; \
		dir=$(ICON_DIR)/$${i}x$${i}@2x/$(APPS_DIR) ; \
		inkscape -w $$i --export-filename=tmp.png $(SOURCEICON) ; \
		install -m 0644 -T tmp.png $$dir/$(TARGETICON_PNG); \
		rm tmp.png ; \
	done
	install -m 0644 -T MIME/webnail.xml $(MIMEDIR)/packages/webnail.xml
	install -m 0644 -T $(SOURCE_DOC_ICON) \
		$(MIME_ICON_DIR)/$(TARGET_DOC_ICON)
	install -m 0644 -T $(SOURCE_LDOC_ICON) \
		$(MIME_ICON_DIR)/$(TARGET_LDOC_ICON)
	install -m 0644 -T $(SOURCE_TDOC_ICON) \
		$(MIME_ICON_DIR)/$(TARGET_TDOC_ICON)
	for i in $(ICON_WIDTHS) ; do \
	    install -d $(ICON_DIR)/$${i}x$${i}/$(MIMETYPES_DIR) ; \
	    inkscape -w $$i --export-filename=tmp.png $(SOURCE_DOC_ICON) ; \
	    dir=$(ICON_DIR)/$${i}x$${i}/$(MIMETYPES_DIR) ; \
	    install -m 0644 -T tmp.png $$dir/$(TARGET_DOC_ICON_PNG); \
	    inkscape -w $$i --export-filename=tmp.png $(SOURCE_LDOC_ICON) ; \
	    install -m 0644 -T tmp.png $$dir/$(TARGET_LDOC_ICON_PNG); \
	    inkscape -w $$i --export-filename=tmp.png $(SOURCE_TDOC_ICON) ; \
	    install -m 0644 -T tmp.png $$dir/$(TARGET_TDOC_ICON_PNG); \
	    rm tmp.png ; \
	done
	for i in $(ICON_WIDTHS2x) ; do \
	    install -d $(ICON_DIR)/$${i}x$${i}@2x/$(MIMETYPES_DIR) ; \
	    ii=`expr 2 '*' $$i` ; \
	    dir=$(ICON_DIR)/$${i}x$${i}@2x/$(MIMETYPES_DIR) ; \
	    inkscape -w $$ii --export-filename=tmp.png $(SOURCE_DOC_ICON) ; \
	    install -m 0644 -T tmp.png $$dir/$(TARGET_DOC_ICON_PNG); \
	    inkscape -w $$ii --export-filename=tmp.png $(SOURCE_LDOC_ICON) ; \
	    install -m 0644 -T tmp.png $$dir/$(TARGET_LDOC_ICON_PNG); \
	    inkscape -w $$ii --export-filename=tmp.png $(SOURCE_TDOC_ICON) ; \
	    install -m 0644 -T tmp.png $$dir/$(TARGET_TDOC_ICON_PNG); \
	    rm tmp.png ; \
	done
	install -m 0644 $(JROOT_JARDIR)/webnail-$(VERSION).jar \
		$(JARDIRECTORY)
	install -m 0755 $(JROOT_BIN)/webnail $(BIN)
	install -m 0644 webnail.desktop $(APPDIR)
	install -m 0644 $(JROOT_MANDIR)/man1/webnail.1.gz $(MANDIR)/man1
	install -m 0644 $(JROOT_MANDIR)/man5/webnail.5.gz $(MANDIR)/man5

uninstall:
	@rm $(MANDIR)/man1/webnail.1.gz || echo ... rm webnail.1.gz  FAILED
	@rm $(APPDIR)/webnail.desktop || echo ... rm webnail.desktop FAILED
	@rm $(BIN)/webnail   || echo ... rm $(BIN)/webnail FAILED
	@rm $(APP_ICON_DIR)/$(TARGETICON)  || echo ... rm $(TARGETICON) FAILED
	@for i in $(ICON_WIDTHS) ; do \
	   rm $(ICON_DIR)/$${i}x$${i}/$(APPS_DIR)/$(TARGETICON_PNG) \
		|| echo .. rm $(TARGETICON_PNG) from $${i}x$${i} FAILED; \
	done
	@rm $(MIME_ICON_DIR)/$(TARGET_DOC_ICON)  || \
		echo ... rm $(TARGET_DOC_ICON) FAILED
	@rm $(MIME_ICON_DIR)/$(TARGET_LDOC_ICON)  || \
		echo ... rm $(TARGET_LDOC_ICON) FAILED
	@rm $(MIME_ICON_DIR)/$(TARGET_TDOC_ICON)  || \
		echo ... rm $(TARGET_TDOC_ICON) FAILED
	@for i in $(ICON_WIDTHS) ; do \
	  rm $(ICON_DIR)/$${i}x$${i}/$(MIMETYPES_DIR)/$(TARGET_DOC_ICON_PNG) \
		|| echo rm $(TARGET_DOC_ICON_PNG) from $${i}x$${i} FAILED; \
	  rm $(ICON_DIR)/$${i}x$${i}/$(MIMETYPES_DIR)/$(TARGET_LDOC_ICON_PNG) \
		|| echo rm $(TARGET_LDOC_ICON_PNG) from $${i}x$${i} FAILED; \
	  rm $(ICON_DIR)/$${i}x$${i}/$(MIMETYPES_DIR)/$(TARGET_TDOC_ICON_PNG) \
		|| echo rm $(TARGET_TDOC_ICON_PNG) from $${i}x$${i} FAILED; \
	done
	@for i in $(ICON_WIDTHS2x) ; do \
	 rm $(ICON_DIR)/$${i}x$${i}@2x/$(MIMETYPES_DIR)/$(TARGET_DOC_ICON_PNG)\
		|| echo rm $(TARGET_DOC_ICON_PNG) from $${i}x$${i} FAILED; \
	 rm $(ICON_DIR)/$${i}x$${i}@2x/$(MIMETYPES_DIR)/$(TARGET_LDOC_ICON_PNG)\
		|| echo rm $(TARGET_LDOC_ICON_PNG) from $${i}x$${i} FAILED; \
	 rm $(ICON_DIR)/$${i}x$${i}@2x/$(MIMETYPES_DIR)/$(TARGET_TDOC_ICON_PNG)\
		|| echo rm $(TARGET_TDOC_ICON_PNG) from $${i}x$${i} FAILED; \
	done
	@(cd $(MIMEDIR)/packages ; \
	 rm webnail.xml || echo rm .../webail.xml FAILED)
	@rm $(JARDIRECTORY)/webnail-$(VERSION).jar \
		|| echo ... rm webnail-$(VERSION).jar FAILED


install-pop:
	install -d $(APP_POPICON_DIR)
	install -d $(MIME_POPICON_DIR)
	install -m 0644 -T $(SOURCEICON) $(APP_POPICON_DIR)/$(TARGETICON)
	install -m 0644 -T $(SOURCE_DOC_ICON) \
		$(MIME_POPICON_DIR)/$(TARGET_DOC_ICON)
	install -m 0644 -T $(SOURCE_LDOC_ICON) \
		$(MIME_POPICON_DIR)/$(TARGET_LDOC_ICON)
	install -m 0644 -T $(SOURCE_TDOC_ICON) \
		$(MIME_POPICON_DIR)/$(TARGET_TDOC_ICON)

saved-install-pop-actions:
	for i in $(POPICON_WIDTHS) ; do \
		install -d $(POPICON_DIR)/$${i}x$${i}/$(APPS_DIR) ; \
		inkscape -w $$i --export-filename=tmp.png $(SOURCEICON) ; \
		install -m 0644 -T tmp.png \
			$(POPICON_DIR)/$${i}x$${i}/$(APPS_DIR)/$(TARGETICON_PNG); \
		rm tmp.png ; \
	done
	for i in $(POPICON_WIDTHS2x) ; do \
		ii=`expr 2 '*' $$i` ; \
		install -d $(POPICON_DIR)/$${i}x$${i}@2x/$(APPS_DIR) ; \
		dir=$(POPICON_DIR)/$${i}x$${i}@2x/$(APPS_DIR) ; \
		inkscape -w $$i --export-filename=tmp.png $(SOURCEICON) ; \
		install -m 0644 -T tmp.png $$dir/$(TARGETICON_PNG); \
		rm tmp.png ; \
	done
	for i in $(POPICON_WIDTHS) ; do \
	    install -d $(POPICON_DIR)/$${i}x$${i}/$(MIMETYPES_DIR) ; \
	    inkscape -w $$i --export-filename=tmp.png $(SOURCE_DOC_ICON) ; \
	    dir=$(POPICON_DIR)/$${i}x$${i}/$(MIMETYPES_DIR) ; \
	    install -m 0644 -T tmp.png $$dir/$(TARGET_DOC_ICON_PNG); \
	    inkscape -w $$i --export-filename=tmp.png $(SOURCE_LDOC_ICON) ; \
	    install -m 0644 -T tmp.png $$dir/$(TARGET_LDOC_ICON_PNG); \
	    inkscape -w $$i --export-filename=tmp.png $(SOURCE_TDOC_ICON) ; \
	    install -m 0644 -T tmp.png $$dir/$(TARGET_TDOC_ICON_PNG); \
	    rm tmp.png ; \
	done
	for i in $(POPICON_WIDTHS2x) ; do \
	    install -d $(POPICON_DIR)/$${i}x$${i}@2x/$(MIMETYPES_DIR) ; \
	    ii=`expr 2 '*' $$i` ; \
	    dir=$(POPICON_DIR)/$${i}x$${i}@2x/$(MIMETYPES_DIR) ; \
	    inkscape -w $$ii --export-filename=tmp.png $(SOURCE_DOC_ICON) ; \
	    install -m 0644 -T tmp.png $$dir/$(TARGET_DOC_ICON_PNG); \
	    inkscape -w $$ii --export-filename=tmp.png $(SOURCE_LDOC_ICON) ; \
	    install -m 0644 -T tmp.png $$dir/$(TARGET_LDOC_ICON_PNG); \
	    inkscape -w $$ii --export-filename=tmp.png $(SOURCE_TDOC_ICON) ; \
	    install -m 0644 -T tmp.png $$dir/$(TARGET_TDOC_ICON_PNG); \
	    rm tmp.png ; \
	done


install-server:
	install -d $(JARDIRECTORY)
	install -m 0644 $(JROOT_JARDIR)/webnail-server-$(VERSION).jar \
		$(JARDIRECTORY)

uninstall-server:
	@rm $(JARDIRECTORY)/webnail-server-$(VERSION).jar \
		|| echo ... rm webnail-server-$(VERSION).jar FAILED
