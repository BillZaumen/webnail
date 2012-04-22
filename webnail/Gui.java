package webnail;

import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.LinkedList;
import java.util.HashMap;
import javax.swing.*;
import java.io.*;
import java.io.IOException;
import javax.swing.filechooser.*;
import java.util.zip.*;
import java.util.jar.*;
import java.net.*;

import javax.xml.parsers.*;
import org.xml.sax.helpers.*;
import org.xml.sax.*;

import org.bzdev.swing.*;
import org.bzdev.util.TemplateProcessor;
import org.bzdev.util.TemplateProcessor.KeyMap;
import org.bzdev.imageio.ImageScaler;
import org.bzdev.imageio.ImageMimeInfo;
import org.bzdev.net.WebEncoder;

public class Gui {

    static {
	try {
	    org.bzdev.protocols.Handlers.enable();
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	}
    }

    static final Color COLOR1 = new Color(220, 220, 255);
    static final Color COLOR2 = new Color(220, 220, 220);

    // Extends ImageIcon so we'll have an image to display in
    // a JList.

    static private final String resourceBundleName = "webnail/Gui";
    static ResourceBundle bundle = 
	ResourceBundle.getBundle(resourceBundleName);
    static String localeString(String name) {
	return bundle.getString(name);
    }
    
    static File currentDir = new File(System.getProperty("user.dir"));
    static File icurrentDir = currentDir;
    static File ocurrentDir = currentDir;

    static String localeMTString(String name) {
	String s;
	try {
	    s = bundle.getString(name);
	} catch (MissingResourceException e) {
	    s = null;
	}
	return (s == null)? name: s;
    }

    static JFrame frame = null;

    static private JFrame helpframe = null;
    static private JFrame editFrame = null;
    static private JFrame consoleFrame = null;
    static private SimpleConsole console;

    static DefaultListModel imageListModel = new DefaultListModel();
    static LinkedList<TemplateProcessor.KeyMap>domMapList = 
	new LinkedList<TemplateProcessor.KeyMap>();

    static EditImagesPane editImagesPane = 
	new EditImagesPane(imageListModel, domMapList);

    static void setOfntfToolTipText() {
	if (oftrbFile.isSelected()) {
	    ofntf.setToolTipText(localeString("ofntfIFileToolTip"));
	} else if (oftrbDir.isSelected()|| oftrbWebDir.isSelected()
		   || oftrbWarDir.isSelected()) {
	    ofntf.setToolTipText(localeString("ofntfDirToolTip"));
	} else if (oftrbZip.isSelected() || oftrbWebZip.isSelected()) {
	    ofntf.setToolTipText(localeString("ofntfZipToolTip"));
	} else if (oftrbWar.isSelected()) {
	    ofntf.setToolTipText(localeString("ofntfWarToolTip"));
	} else {
	    throw new IllegalStateException("ofnrb not working");
	}
    }

    static void setEnableds() {
	if (oftrbFile.isSelected()) {
	    inputPane.setSelectionMode(InputPane.SelectionMode.SINGLE);
	    editImagesPane.setSelectionMode(InputPane.SelectionMode.SINGLE);
	} else {
	    inputPane.setSelectionMode(InputPane.SelectionMode.MULTI);
	    editImagesPane.setSelectionMode(InputPane.SelectionMode.MULTI);
	}
	if (oftrbWebDir.isSelected()|| oftrbWebZip.isSelected()
	    || oftrbWarDir.isSelected() || oftrbWar.isSelected()) {
	    windowTitleButton.setEnabled(true);
	    linkCheckBox.setEnabled(true);
	    flatCheckBox.setEnabled(true);
	    hrCheckBox.setEnabled(true);
	    syncCheckBox.setEnabled(true);
	    waitOnErrCheckBox.setEnabled(true);
	    hrefToOrigCheckBox.setEnabled(true);
	    layoutComboBox.setEnabled(true);
	    boolean selected = hrCheckBox.isSelected() 
		//		|| layoutComboBox.getSelectedIndex() != 0;
		|| layoutParms.isSingle();
	    mtnwl.setEnabled(selected);
	    mtnhl.setEnabled(selected);
	    mtnhtf.setEnabled(selected);
	    mtnhtf.setEnabled(selected);

	    colorButton.setEnabled(hrCheckBox.isSelected());
	    imageTimeLabel.setEnabled(true);
	    imageTimeTF.setEnabled(true);
	    minImageTimeLabel.setEnabled(true);
	    minImageTimeTF.setEnabled(true);
	    editLabel.setEnabled(true);
	    domMapButton.setEnabled(true);
	    titleButton.setEnabled(true);
	    descrButton.setEnabled(true);
	    headButton.setEnabled(true);
	    headerButton.setEnabled(true);
	    trailerButton.setEnabled(true);
	    finalHtmlButton.setEnabled(true);
	    editImagesPane.setWebpageMode(true);
	} else {
	    mtnwl.setEnabled(true);
	    mtnhl.setEnabled(true);
	    mtnhtf.setEnabled(true);
	    mtnhtf.setEnabled(true);
	    windowTitleButton.setEnabled(false);
	    linkCheckBox.setEnabled(false);
	    flatCheckBox.setEnabled(false);
	    hrCheckBox.setEnabled(false);
	    syncCheckBox.setEnabled(false);
	    waitOnErrCheckBox.setEnabled(false);
	    hrefToOrigCheckBox.setEnabled(false);
	    layoutComboBox.setEnabled(false);
	    imageTimeLabel.setEnabled(false);
	    imageTimeTF.setEnabled(false);
	    minImageTimeLabel.setEnabled(false);
	    minImageTimeTF.setEnabled(false);
	    colorButton.setEnabled(false);
	    editLabel.setEnabled(false);
	    domMapButton.setEnabled(false);
	    titleButton.setEnabled(false);
	    descrButton.setEnabled(false);
	    headButton.setEnabled(false);
	    headerButton.setEnabled(false);
	    trailerButton.setEnabled(false);
	    finalHtmlButton.setEnabled(false);
	    editImagesPane.setWebpageMode(false);
	}
	if (layoutComboBox.isEnabled() 
	    && layoutParms.isSingle()
	    /*&& layoutComboBox.getSelectedIndex() != 0*/) {
	    linkCheckBox.setEnabled(false);
	    flatCheckBox.setEnabled(false);
	    hrCheckBox.setEnabled(false);
	    colorButton.setEnabled(false);
	    syncCheckBox.setEnabled(false);
	    waitOnErrCheckBox.setEnabled(false);
	    descrButton.setEnabled(false);
	    domMapButton.setEnabled(false);
	    imageTimeLabel.setEnabled(false);
	    imageTimeTF.setEnabled(false);
	    minImageTimeLabel.setEnabled(false);
	    minImageTimeTF.setEnabled(false);
	    editImagesPane.setLimitedMode(true);
	    editImagesPane.setLinkMode(hrefToOrigCheckBox.isEnabled() &&
				       hrefToOrigCheckBox.isSelected());
	} else {
	    editImagesPane.setLimitedMode(false);
	    editImagesPane.setLinkMode(true);
	}
    }

    static File fileToSave = null;

    static void load(final URL url) {
	final String urlstr = url.toString();
	worker = new Thread(new Runnable() {
		public void run() {
		    try {
			URLConnection c = url.openConnection();
			if (c instanceof HttpURLConnection) {
			    c.setRequestProperty("accept", 
						 Webnail.ACCEPT_VALUE);
			    ((HttpURLConnection) c).setRequestMethod("GET");
			    c.connect();
			    if (((HttpURLConnection) c).getResponseCode() 
				!= HttpURLConnection.HTTP_OK) {
				throw new IOException(String.format
						      (localeString
						       ("couldNotConnect"), 
						       urlstr)); 
			    }
			}
			String ct = c.getContentType();
			if (ct != null) {
			    if ((!ct.equals(Webnail.XML_MIME_TYPE)) &&
				(!ct.equals(Webnail
					    .WEBNAIL_XML_MIME_TYPE)) &&
				(!ct.equals(Webnail.ALT_XML_MIME_TYPE))) {
				if (!(ct.equals(Webnail.GENERIC_MIME_TYPE)
				      ||
				      ct.equals(Webnail.BOGUS_MIME_TYPE))
				    || 0 == JOptionPane.showConfirmDialog
				    (frame, 
				     String.format(localeString("acceptInput"),
						   ct),
				     localeString("unrecognizedMIMETypeTitle"),
				     JOptionPane.OK_CANCEL_OPTION,
				     JOptionPane.QUESTION_MESSAGE)) {
				    throw new Exception
					(String.format
					 (localeString("notWebnailFile"),
					  url.toString(), ct));
				}
			    }
			} else {
			    InputStream is = c.getInputStream();
			    ct = c.guessContentTypeFromStream(is);
			    is.close();
			    if (ct == null) {
				ct = "application/octet-stream";
			    }
			    if ((!ct.equals(Webnail.XML_MIME_TYPE)) &&
				(!ct.equals(Webnail
					    .WEBNAIL_XML_MIME_TYPE)) &&
				(!ct.equals(Webnail.ALT_XML_MIME_TYPE))) {
				if (!(ct.equals(Webnail.GENERIC_MIME_TYPE)
				      ||
				      ct.equals(Webnail.BOGUS_MIME_TYPE))
				    || 0 == JOptionPane.showConfirmDialog
				    (frame, String.format
				     (localeString("acceptInput"), ct),
				     localeString("unrecognizedMIMETypeTitle"),
				     JOptionPane.OK_CANCEL_OPTION,
				     JOptionPane.QUESTION_MESSAGE)) {
				    throw new Exception
					(String.format
					 (localeString("notWebnailFile"),
					  url.toString(), ct));
				}
			    }
			}
			InputStream is = url.openStream();
			load(null, is);
		    } catch(Exception e) {
			ErrorMessage.display(e);
		    }
		    worker = null;
		}
	    });
	worker.start();
    }

    static void load(String fileName, InputStream is) {
	try {
	    Parser p = new Parser();
	    if (fileName != null) p.setXMLFilename(fileName);
	    console.addSeparatorIfNeeded();
	    p.parse(is);
	    boolean webmode = p.getWebMode();
	    String wt = p.getValue("windowTitle");
	    String mtype = p.getMimeType();
	    boolean linkmode = p.getLinkMode();
	    boolean flatmode = p.getFlatMode();
	    boolean hrmode = p.getHighResMode();
	    boolean warmode = p.getWebArchiveMode();
	    String bgColor = p.getValue("bgcolor");
	    boolean syncmode = p.getSyncMode();
	    boolean waitOnError = p.getWaitOnError();
	    String imageTime = p.getImageTime();
	    String minImageTime = p.getMinImageTime();
	    boolean hrefToOrig = p.getHrefToOrig();

	    windowTitle = (wt == null)? "": wt;
	    linkCheckBox.setSelected(linkmode);
	    flatCheckBox.setSelected(flatmode);
	    hrCheckBox.setSelected(hrmode);
	    syncCheckBox.setSelected(syncmode);
	    waitOnErrCheckBox.setSelected(waitOnError);
	    hrefToOrigCheckBox.setSelected(hrefToOrig);
	    layoutComboBox.setSelectedIndex(p.getLayoutIndex());
	    layoutParms = p.getLayoutParms();
	    customParms = p.getCustomParms();
	    
	    bgcolor = (bgColor == null)?
		Webnail.DEFAULT_BGCOLOR : bgColor;
	    imageTimeTF.setText((imageTime == null)? "": 
				imageTime);
	    minImageTimeTF.setText((minImageTime == null)? "":
				   minImageTime);

	    if (webmode && !warmode) {
		oftrbWebZip.setSelected(true);
	    } else if (warmode) {
		oftrbWar.setSelected(true);
	    } else {
		oftrbZip.setSelected(true);
	    }
	    setEnableds();
			    
	    int height = p.getHeight();
	    int width = p.getWidth();

	    if (height == 0) {
		mtnhtf.setValue(0, "");
	    } else {
		mtnhtf.setValue(height);
	    }
	    if (width == 0) {
		mtnwtf.setValue(0, "");
	    } else {
		mtnwtf.setValue(width);
	    }

	    domMapList.clear();
	    domMapList.addAll(p.getDomList());
	    titleURL = p.getValue("titleURL");
	    titleURLInUse = (titleURL != null);
	    title = p.getValue("title");
	    // System.out.println("titleURL = " + titleURL);
	    // System.out.println("title = " + title);
	    descrURL = p.getValue("descrURL");
	    descrURLInUse = (descrURL != null);
	    descr = p.getValue("descr");
	    headURL = p.getValue("headURL");
	    headURLInUse = (headURL != null);
	    head = p.getValue("head");
	    headerURL = p.getValue("headerURL");
	    headerURLInUse = (headerURL != null);
	    header = p.getValue("header");
	    trailerURL = p.getValue("trailerURL");
	    trailerURLInUse = (trailerURL != null);
	    trailer = p.getValue("trailer");
	    finalHtmlURL = p.getValue("finalHtmlURL");
	    finalHtmlURLInUse = (finalHtmlURL != null);
	    finalHtml = p.getValue("finalHtml");
	    for (TemplateProcessor.KeyMap map: p.getImageArray()) {
		// add images by creating new MapElement 
		// instances
		String url = (String)map.get("url");
		if (url == null) {
		    String filename = (String)
			map.get("filename");
		    if (filename != null) {
			url = (new File(filename)).toURI()
			    .toURL().toString();
			map.remove("filename");
			map.put("url", url);
		    } else {
			// error - no url, no filename
		    }
		}
		new ImageMapElement(map, imageListModel);
	    }
	} catch (ParserConfigurationException epc) {
	    ErrorMessage.display(epc);
	} catch (SAXException es) {
	    ErrorMessage.
		display(es /*, 
					 localeString("parseErrorTitle"),
					 frame*/);
	} catch (IOException eio) {
	    ErrorMessage.
		display(eio /*,
					  localeString("parseErrorTitle"),
					  frame*/);
	} finally {
	    if (console.hasNewTextToDisplay()) {
		showConsole();
	    }
	}
	return;
    }

    static void configureMenus() {
	JMenuBar menubar = new JMenuBar();
	JMenu fileMenu = new JMenu(localeString("fileMenu"));
	JMenu editMenu = new JMenu(localeString("editMenu"));
	JMenu toolsMenu = new JMenu(localeString("toolsMenu"));
	JMenu help = new JMenu(localeString("helpMenu"));
	JMenuItem quit = new JMenuItem(localeString("quitMenuItem"));
	JMenuItem load = new JMenuItem(localeString("loadMenuItem"));
	JMenuItem open = new JMenuItem(localeString("openMenuItem"));
	JMenuItem save = new JMenuItem(localeString("saveMenuItem"));
	JMenuItem saveAs = new JMenuItem(localeString("saveAsMenuItem"));

	JMenuItem proxies = new ProxyMenuItem(localeString("proxyMenuItem"),
					      frame,
					      localeString("proxyTitle"));

	JMenuItem layouts = new JMenuItem(localeString("layoutMenuItem"));

	JMenuItem openConsole = new JMenuItem(localeString("consoleMenuItem"));
	JMenuItem manual = new JMenuItem(localeString("manualMenuItem"));
	JMenuItem about = new JMenuItem(localeString("aboutMenuItem"));

	final JCheckBoxMenuItem stackTraceButtonMenuItem =
	    new JCheckBoxMenuItem(localeString("showStackTraceMenuItem"),
				     false);
	fileMenu.add(load);
	fileMenu.add(open);
	fileMenu.add(save);
	fileMenu.add(saveAs);
	fileMenu.add(quit);
	editMenu.add(proxies);
	editMenu.add(layouts);
	toolsMenu.add(stackTraceButtonMenuItem);
	toolsMenu.add(openConsole);
        help.add(manual);
	help.add(about);
	menubar.add(fileMenu);
	menubar.add(editMenu);
	menubar.add(toolsMenu);
        menubar.add(help);
        quit.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
	quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
						   InputEvent.CTRL_MASK));

	final AbstractAction saveAsActionListener =  new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    JFileChooser fc = new JFileChooser(currentDir);
		    for (javax.swing.filechooser.FileFilter f: 
			     fc.getChoosableFileFilters()) {
			fc.removeChoosableFileFilter(f);
		    }
		    FileNameExtensionFilter xmlFilter =
			new FileNameExtensionFilter("webnail xml", 
						    "xml", "wnl");
		    fc.addChoosableFileFilter(xmlFilter);
		    fc.setFileFilter(xmlFilter);
		    int status = fc.showSaveDialog(frame);
		    if (status == JFileChooser.APPROVE_OPTION ) {
			try {
			    savedFileName = 
				fc.getSelectedFile().getCanonicalPath();
			    processFiles(imageListModel, savedFileName,
					 domMapList);
			    Thread w = worker;
			    if (w != null) {
				try {
				    w.join();
				} catch (Exception we){}
			    }
			    checkConsole();
			} catch (IOException eio) {
			    ErrorMessage.display(eio);
			}
			return;
		    } else if (status == JFileChooser.CANCEL_OPTION) {
			return;
		    } else {
			System.err.println("unknown status");
		    }
		}
	    };

	stackTraceButtonMenuItem.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    ErrorMessage.setStackTrace
			(stackTraceButtonMenuItem.isSelected());
		}
	    });

	save.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    if (fileToSave == null) {
			saveAsActionListener.actionPerformed(null);
			fileToSave = new File(savedFileName);
		    } else {
			try {
			    String savedFileName = 
				fileToSave.getCanonicalPath();
			    processFiles(imageListModel, savedFileName,
					 domMapList);
			    Thread w = worker;
			    if (w != null) {
				try {
				    w.join();
				} catch (Exception we) {}
			    }
			    checkConsole();
			} catch (IOException eio) {
			    ErrorMessage.display(eio);
			}
		    }
		    return;
		}
	    });

	saveAs.addActionListener(saveAsActionListener);
	
	load.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    JTextField urltf = new JTextField(50);
		    boolean retry;
		    do {
			retry = false;
			try {
			    int status = JOptionPane.showConfirmDialog
				(frame, urltf,
				 localeString("inputConfigurationURL"),
				 JOptionPane.OK_CANCEL_OPTION,
				 JOptionPane.QUESTION_MESSAGE);
			    if (status == 0) {
				String urlstr = urltf.getText().trim();
				URL url = new URL(urlstr);
				fileToSave = null;
				load(url);
				final Thread w = worker;
				if (w != null) {
				    runButton.setEnabled(false);
				    (new Thread() {
					    public void run() {
						cancelButton.setEnabled(true);
						try {
						    w.join();
						} catch(Exception e) {}
						SwingUtilities.invokeLater
						    (new Runnable() {
							    public void run() {
								cancelButton
								    .setEnabled
								    (false);
								runButton
								    .setEnabled
								    (true);
								checkConsole();
							    }
							});
					    }
					}).start();
				}
			    }
			} catch (MalformedURLException murle) {
			    JOptionPane.showMessageDialog
				(frame, localeString("malformedURL"),
				 localeString("malformedURLTitle"),
				 JOptionPane.ERROR_MESSAGE);
			    retry = true;
			} /* catch (Exception e1) {
			    ErrorMessage.display(e1);
			    }*/
		    } while (retry);
		    return;
		}
	    });

	open.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    JFileChooser fc = new JFileChooser(currentDir);
		    for (javax.swing.filechooser.FileFilter f: 
			     fc.getChoosableFileFilters()) {
			fc.removeChoosableFileFilter(f);
		    }
		    FileNameExtensionFilter xmlFilter =
			new FileNameExtensionFilter("webnail xml", 
						    "xml", "wnl");
		    fc.addChoosableFileFilter(xmlFilter);
		    fc.setFileFilter(xmlFilter);
		    int status = fc.showOpenDialog(frame);
		    if (status == JFileChooser.APPROVE_OPTION ) {
			try {
			    File ofile= 
				fc.getSelectedFile();
			    URL url = ofile.toURI().toURL();
			    URLConnection c = url.openConnection();
			    String ct = c.getContentType();
			    if (ct != null) {
				if ((!ct.equals(Webnail.XML_MIME_TYPE)) &&
				    (!ct.equals(Webnail
						.WEBNAIL_XML_MIME_TYPE)) &&
				    (!ct.equals(Webnail.ALT_XML_MIME_TYPE))) {
				    if (!(ct.equals(Webnail.GENERIC_MIME_TYPE)
					  ||
					  ct.equals(Webnail.BOGUS_MIME_TYPE))
					|| 0 == JOptionPane.showConfirmDialog
					(frame, String.format
					 (localeString("acceptInput"), ct),
					 localeString
					 ("unrecognizedMIMETypeTitle"),
					 JOptionPane.OK_CANCEL_OPTION,
					 JOptionPane.QUESTION_MESSAGE)) {
					throw new
					    Exception(String.format
						      (localeString
						       ("notWebnailFile"),
						       url.toString(), ct));
				    }
				}
			    } else {
				InputStream is = c.getInputStream();
				ct = c.guessContentTypeFromStream(is);
				is.close();
				if (ct == null) {
				    ct = "application/octet-stream";
				}
				if ((!ct.equals(Webnail.XML_MIME_TYPE)) &&
				    (!ct.equals(Webnail
						.WEBNAIL_XML_MIME_TYPE)) &&
				    (!ct.equals(Webnail.ALT_XML_MIME_TYPE))) {
				    if (!(ct.equals(Webnail.GENERIC_MIME_TYPE)
					  ||
					  ct.equals(Webnail.BOGUS_MIME_TYPE))
					|| 0 == JOptionPane.showConfirmDialog
					(frame, String.format
					 (localeString("acceptInput"), ct),
					 localeString
					 ("unrecognizedMIMETypeTitle"),
					 JOptionPane.OK_CANCEL_OPTION,
					 JOptionPane.QUESTION_MESSAGE)) {
					throw new Exception
					    (String.format
					     (localeString("notWebnailFile"),
					      url.toString(), ct));
				    }
				}
			    }
			    InputStream is = new FileInputStream(ofile);
			    fileToSave = ofile;
			    load(ofile.getCanonicalPath(), is);
			    /*
			      Parser p = new Parser();
			      p.setXMLFilename(ofile.getCanonicalPath());
			      console.addSeparatorIfNeeded();
			      p.parse(is);
			      boolean webmode = p.getWebMode();
			      String wt = p.getValue("windowTitle");
			      String mtype = p.getMimeType();
			      boolean linkmode = p.getLinkMode();
			      boolean flatmode = p.getFlatMode();
			      boolean hrmode = p.getHighResMode();
			      boolean warmode = p.getWebArchiveMode();
			      String bgColor = p.getValue("bgcolor");
			      boolean syncmode = p.getSyncMode();
			      boolean waitOnError = p.getWaitOnError();
			      String imageTime = p.getImageTime();
			      String minImageTime = p.getMinImageTime();
			      boolean hrefToOrig = p.getHrefToOrig();

			      windowTitle = (wt == null)? "": wt;
			      linkCheckBox.setSelected(linkmode);
			      flatCheckBox.setSelected(flatmode);
			      hrCheckBox.setSelected(hrmode);
			      syncCheckBox.setSelected(syncmode);
			      waitOnErrCheckBox.setSelected(waitOnError);
			      hrefToOrigCheckBox.setSelected(hrefToOrig);
			      layoutComboBox.setSelectedIndex
			      (p.getLayoutIndex());
			      bgcolor = (bgColor == null)?
			      Webnail.DEFAULT_BGCOLOR : bgColor;
			      imageTimeTF.setText((imageTime == null)? "": 
			      imageTime);
			      minImageTimeTF.setText((minImageTime == null)? "":
			      minImageTime);

			      if (webmode && !warmode) {
			      oftrbWebZip.setSelected(true);
			      } else if (warmode) {
			      oftrbWar.setSelected(true);
			      } else {
			      oftrbZip.setSelected(true);
			      }
			      setEnableds();
			    
			      int height = p.getHeight();
			      int width = p.getWidth();

			      if (height == 0) {
			      mtnhtf.setValue(0, "");
			      } else {
			      mtnhtf.setValue(height);
			      }
			      if (width == 0) {
			      mtnwtf.setValue(0, "");
			      } else {
			      mtnwtf.setValue(width);
			      }

			      domMapList.clear();
			      domMapList.addAll(p.getDomList());
			      titleURL = p.getValue("titleURL");
			      titleURLInUse = (titleURL != null);
			      title = p.getValue("title");
			      // System.out.println("titleURL = " + titleURL);
			      // System.out.println("title = " + title);
			      descrURL = p.getValue("descrURL");
			      descrURLInUse = (descrURL != null);
			      descr = p.getValue("descr");
			      headURL = p.getValue("headURL");
			      headURLInUse = (headURL != null);
			      head = p.getValue("head");
			      headerURL = p.getValue("headerURL");
			      headerURLInUse = (headerURL != null);
			      header = p.getValue("header");
			      trailerURL = p.getValue("trailerURL");
			      trailerURLInUse = (trailerURL != null);
			      trailer = p.getValue("trailer");
			      finalHtmlURL = p.getValue("finalHtmlURL");
			      finalHtmlURLInUse = (finalHtmlURL != null);
			      finalHtml = p.getValue("finalHtml");
			      for (TemplateProcessor.KeyMap map: 
			           p.getImageArray()) {
			      // add images by creating new MapElement 
			      // instances
			      String url = (String)map.get("url");
			      if (url == null) {
			      String filename = (String)
			      map.get("filename");
			      if (filename != null) {
			      url = (new File(filename)).toURI()
			      .toURL().toString();
			      map.remove("filename");
			      map.put("url", url);
			      } else {
			      // error - no url, no filename
			      }
			      }
			      new ImageMapElement(map, imageListModel);
			      }
			      } catch (ParserConfigurationException epc) {
			      } catch (SAXException es) {
			      ErrorMessage.
			      display(es.getMessage());
			    */
			} catch (Exception e2) {
			    ErrorMessage.display(e2);
			} finally {
			    if (console.hasNewTextToDisplay()) {
				showConsole();
			    }
			}
			return;
		    } else if (status == JFileChooser.CANCEL_OPTION) {
			return;
		    } else {
			// error
		    }
		}
	    });

	
	save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
						   InputEvent.CTRL_MASK));
	

	about.addActionListener(new AbstractAction() {
		private String createAboutText() {
		    StringBuilder builder = new StringBuilder(256);
		    builder.append(String.format(localeString("aboutText1"),
						 "Bill Zaumen"));
		    builder.append('\n');
		    builder.append(localeString("aboutText2"));
		    InputStream is = 
			ClassLoader.getSystemClassLoader().getResourceAsStream
			("webnail/helpers.txt");
		    LineNumberReader lr = new LineNumberReader
			(new BufferedReader(new InputStreamReader(is)));
		    String line = null;
		    String next = null;
		    int count = 0;
		    try {
			while ((line = lr.readLine()) != null) {
			    line = line.trim();
			    if (line.isEmpty()) continue;
			    if (next != null) {
				builder.append(next);
				if (count++ % 4 == 0) {
				    builder.append(",\n");
				} else {
				    builder.append(", ");
				}

			    } else {
				builder.append('\n');
				builder.append(localeString("aboutText3"));
				builder.append(' ');
			    }
			    next = line;
			}
		    } catch (IOException e) {
			// should not occur because we are loading from
			// our own jar file.
		    }
		    if (next != null) {
			builder.append(next);
			builder.append(".");
		    }
		    return builder.toString();
		}
		public void actionPerformed(ActionEvent e) {
		    JOptionPane.showMessageDialog
			(frame,
			 createAboutText(),
			 localeString("aboutTitle"),
			 0);
						  
		}
	    });

	openConsole.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    showConsole();
		}
	    });
        manual.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    showHelp();
                }
            });

	layouts.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    showLayoutFrame();
		}
	    });

	frame.setJMenuBar(menubar);
    }

    static private void showConsole() {
	consoleFrame.setVisible(true);
    }

    static private void showHelp () {
        if (helpframe == null) {
            helpframe = new JFrame (localeString("helpFrameTitle"));
            Container hpane = helpframe.getContentPane();
	    HtmlWithTocPane helpPane = new HtmlWithTocPane();

            helpframe.setSize(920, 700);
	    helpframe.setIconImages(iconList);
            helpframe.addWindowListener(new WindowAdapter () {
                    public void windowClosing(WindowEvent e) {
                        helpframe.setVisible(false);
                    }
                });
            URL url = 
                ClassLoader.getSystemClassLoader()
		.getResource(localeString("manualToc"));
            if (url != null) {
                try {
                    helpPane.setToc(url, true, false);
                    helpPane.setSelectionWithAction(0);
                } catch (IOException e) {
		    ErrorMessage.display(e);
                    helpframe = null;
                    return;
                }
                catch (org.xml.sax.SAXException ee) {
		    ErrorMessage.display(ee);
		    helpframe.dispose();
                    helpframe = null;
                    return;
                }
                catch (javax.xml.parsers.ParserConfigurationException  eee) {
		    ErrorMessage.display(eee);
		    helpframe.dispose();
                    helpframe = null;
                    return;
                }
            } else {
		ErrorMessage.display(String.format
				     (localeString("manLoadError"),
				      url.toString()));
		helpframe.dispose();
                helpframe = null;
                return;
            }

            hpane.setLayout(new BorderLayout());
            hpane.add(helpPane, "Center");
            helpframe.setVisible(true);
        } else {
            helpframe.setVisible(true);
        }
    }

    static private void showEditFrame() {
	if (editFrame == null) {
	    editFrame = new JFrame(localeString("editFrame"));
	    editFrame.setIconImages(iconList);
	    Container cpane = editFrame.getContentPane();
	    editFrame.addWindowListener
		(editImagesPane.getOnClosingWindowListener());
	    editImagesPane.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			editFrame.setVisible(false);
		    }
		});
	    editFrame.addWindowListener (new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
			editFrame.setVisible(false);
		    } 
		});
            cpane.setLayout(new BorderLayout());
	    cpane.add(editImagesPane, "Center");
	    editFrame.pack();
	}
	editFrame.setLocation(frame.getLocation());

	editFrame.setVisible(true);
    }

    static String savedFileName = null;

    static Thread worker = null;

    static  void 
	processFiles(/*LinkedList<TemplateProcessor.KeyMap> mapList */
		     DefaultListModel imageListModel, 
		     final String savedFileName,
		     LinkedList<TemplateProcessor.KeyMap>domMapList)
    {
	// System.out.println("calling processFiles");
	console.addSeparatorIfNeeded();
	final int mtw = mtnwtf.getValue();
	final int mth = mtnhtf.getValue();
	// String ifn = ifntf.getText().trim();
	String ofn = savedFileName != null? savedFileName: 
	    ofntf.getText().trim();
	File ofile = savedFileName != null? new File(savedFileName):
	    new File(ofn);
	if (!ofile.isDirectory()) {
	    if (ofile.getName().lastIndexOf(".") == -1) {
		ofn = ofn + "." + extension;
		ofile = new File(ofn);
	    }
	}
	if (ofile.exists() && !ofile.canWrite()) {
	    JOptionPane.showMessageDialog
		(frame,
		 localeString("ofileNotWritable"),
		 localeString("Error"),
		 JOptionPane.ERROR_MESSAGE,
		 null);
	}
	if (mtw < 0 || mth < 0) {
	    JOptionPane.showMessageDialog
		(frame,
		 localeString("mustBePositiveNumber"),
		 localeString("Error"),
		 JOptionPane.ERROR_MESSAGE,
		 null);
	} else {
	    if (!ofile.isDirectory() &&
		!ofile.getName().endsWith(".zip") &&
		!ofile.getName().endsWith(".war") && 
		savedFileName == null) {
		if (ofile.canWrite() ||
		    (!ofile.exists() 
		     && ofile.getParentFile().canWrite())) {
		    URL xurl = null;
		    try {
			xurl = new URL((String)
				       ((/*Image*/MapElement)
					imageListModel.get(0)).
				       get("url"));
		    } catch(MalformedURLException mue) {
			ErrorMessage.display("Malformed URL");
		    }
		    final URL url = xurl;
				    
		    final File xofile = ofile;
		    (worker = new Thread(new Runnable() {
			    public void run() {
				ImageScaler scaler = new ImageScaler();
				try {
				    pm.startProgress(1);
				    scaler.scaleImage(mtw, mth, url, xofile,
						      type);
				    pm.incrProgressCount();
				} catch (Exception e) {
				    ErrorMessage.display(e);
				}
				worker = null;
				pm.stopProgress();
			    }
			})).start();

		} else {
		    // not writable.
		}
	    } else {
		Parser p = null;
		try {
		    p = new Parser();
		    boolean webmode = oftrbWebDir.isSelected()
			|| oftrbWebZip.isSelected()
			|| oftrbWarDir.isSelected()
			|| oftrbWar.isSelected();
		    boolean warmode = oftrbWarDir.isSelected()
			|| oftrbWar.isSelected();
		    boolean linkmode = linkCheckBox.isSelected()
			&& linkCheckBox.isEnabled();
		    boolean flatmode = flatCheckBox.isSelected()
			&& flatCheckBox.isEnabled();
		    boolean hrmode = hrCheckBox.isSelected()
			&& hrCheckBox.isEnabled();
		    boolean syncMode = syncCheckBox.isSelected()
			&& syncCheckBox.isEnabled();
		    boolean waitOnError = 
			waitOnErrCheckBox.isSelected()
			&& waitOnErrCheckBox.isEnabled();

		    boolean hrefToOrig = 
			hrefToOrigCheckBox.isSelected() &&
			hrefToOrigCheckBox.isEnabled();


		    long imageTime = imageTimeTF.getValue();
		    long minImageTime = minImageTimeTF.getValue();

		    p.setAttributes(mtype, hrmode,
				    webmode, warmode,
				    linkmode, flatmode,
				    syncMode, waitOnError,
				    imageTime, minImageTime,
				    bgcolor, mtw, mth, hrefToOrig);
		    p.setCustomParms(customParms);

		    int layoutIndex = layoutComboBox.isEnabled()?
			layoutComboBox.getSelectedIndex(): 0;
		    p.setLayoutByIndex(layoutIndex);

		    String wt = (windowTitle.length() == 0)?
			localeString("windowTitle"): windowTitle;
		    p.setWindowTitle(WebEncoder.htmlEncode(wt));
		    if (titleURLInUse && titleURL != null) {
			p.setTitleURL(titleURL);
		    } else {
			p.setTitle(title);
		    }
		    if (descrURLInUse && descrURL != null) {
			p.setDescrURL(descr);
		    } else {
			p.setDescr(descr);
		    }
		    if (headURLInUse && headURL != null) {
			p.setHeadURL(headURL);
		    } else {
			p.setHead(head);
		    }
		    if (headerURLInUse && headerURL != null) {
			p.setHeaderURL(headerURL);
		    } else {
			p.setHeader(header);
		    }
		    if (trailerURLInUse && trailerURL != null) {
			p.setTrailerURL(trailerURL);
		    } else {
			p.setTrailer(trailer);
		    }
		    if (finalHtmlURLInUse && finalHtmlURL != null) {
			p.setAfterScriptURL(finalHtmlURL);
		    } else {
			p.setAfterScript(finalHtml);
		    }
		    p.startDomMappings();
		    for (TemplateProcessor.KeyMap map: domMapList) {
			p.addMapping(map);
		    }
		    p.endDomMappings();
		    for (Object obj: imageListModel.toArray()) {
			MapElement entry = (MapElement) obj;
			// System.out.println(entry.get("url"));
			p.addImageMap(entry.getKeyMap());
		    }
		    p.imagesComplete();
		} catch (ParserConfigurationException pce) {
		} catch (SAXException se) {
		    ErrorMessage.display(se);
		}
		final Parser parser = p;
		final File xofile = ofile;
		if (savedFileName != null) {
		    // cannot call pm because we'll deadlock
		    try {
			File tmp1 = File.createTempFile("thumbnail",
							".xml");
			PrintStream out = new 
			    PrintStream(tmp1, 
					"UTF-8");
			parser.write(out);
			out.flush();
			out.close();
			if (xofile.exists()) {
			    File backup = 
				new File(xofile.getCanonicalPath()
					 +"~");
			    if (backup.exists()) {
				backup.delete();
			    }
			    xofile.renameTo(backup);
			}
			if(!tmp1.renameTo(xofile)) {
			    throw new 
				IOException("could not rename");
			}
		    } catch(FileNotFoundException enf) {
			ErrorMessage.display("file not found");
		    } catch (IOException eio) {
			ErrorMessage.display("IO Exception");
		    }
		} else {
		    (worker = new Thread (new Runnable() {
			    public void run() {
				ZipOutputStream zos = null;
				if (xofile.isDirectory()) {
				    try {
					// System.out.println("gen dir");
					Webnail.generate(parser, 
							   xofile, 
							   zos,
							   pm);
				    } catch (Exception e) {
					ErrorMessage.display(e);
					// e.printStackTrace();
				    }
				} else if (savedFileName != null) {
				} else if (xofile.getName()
					   .endsWith(".zip")) {
				    try {
					zos = new ZipOutputStream
					    (new FileOutputStream
					     (xofile));
					Webnail.generate(parser, 
							   null, 
							   zos,
							   pm);
				    } catch (Exception e) {
					ErrorMessage.display(e);
					// e.printStackTrace();
				    }
				} else if (xofile.getName()
					   .endsWith(".war")) {
				    try {
					zos = new JarOutputStream
					    (new FileOutputStream
					     (xofile));
					Webnail.generate(parser, 
							   null, 
							   zos,
							   pm);
				    } catch (Exception e) {
					ErrorMessage.display(e);
					// e.printStackTrace();
				    }
				}
				worker = null;
				pm.stopProgress();
			    }
			})).start();
		}
	    }
	}
    }

    static JLabel mtnwl = new JLabel(localeString("maxImageWidth") + ":");
    static JLabel mtnhl = new JLabel(localeString("maxImageHeight") + ":");

    static WholeNumbTextField mtnhtf;
    static WholeNumbTextField mtnwtf;
    static String type = "jpeg";
    static String mtype = "image/jpeg";
    static String extension = "jpg";
    static JRadioButton oftrbFile;
    static JRadioButton oftrbDir;
    static JRadioButton oftrbZip;
    static JRadioButton oftrbWebDir;
    static JRadioButton oftrbWebZip;
    static JRadioButton oftrbWarDir;
    static JRadioButton oftrbWar;
    static ButtonGroup oftbg;

    // JTextField ifntf = new JTextField(40);
    static JTextField ofntf;
    static String old = null; // old value in ofntf field
    static JButton ofnb = new JButton(localeString("choose"));


    static String bgcolor = Webnail.DEFAULT_BGCOLOR;
    static JButton colorButton;

    static JLabel imageTimeLabel;

    static TimeTextField imageTimeTF;

    static JLabel minImageTimeLabel;

    static TimeTextField minImageTimeTF;
		

    static String windowTitle = "";
    static JButton windowTitleButton;

    static JCheckBox linkCheckBox;
    static JCheckBox flatCheckBox;
    static JCheckBox hrCheckBox;

    static JCheckBox syncCheckBox;

    static JCheckBox waitOnErrCheckBox;

    static JCheckBox hrefToOrigCheckBox;

    static String layout = Parser.defaultLayout;
    static LayoutParms layoutParms;
    static LayoutParms customParms = null;
    static int lastLayoutIndex = 0;
    static Object[] layoutChoices = {
	/*
	  localeString(Parser.getLayoutName(0)),
	  localeString(Parser.getLayoutName(1)),
	  localeString(Parser.getLayoutName(2)),
	  localeString(Parser.getLayoutName(3)),
	  localeString(Parser.getLayoutName(4)),
	  localeString(Parser.getLayoutName(5))
	*/
	Parser.getLayoutParms(0),
	Parser.getLayoutParms(1),
	Parser.getLayoutParms(2),
	Parser.getLayoutParms(3),
	Parser.getLayoutParms(4),
	Parser.getLayoutParms(5),
	//	Parser.getLayoutParms(6),
	localeString("custom"),
	localeString("setCustomLayout")
    };
	
    static DefaultComboBoxModel lcbmodel = 
	new DefaultComboBoxModel(layoutChoices);
    static JComboBox layoutComboBox = new JComboBox(lcbmodel);
    static boolean layoutComboBoxBeingModified = false;

    static LayoutPane layoutPane = new LayoutPane() {
	    protected void onClosing(Map<String,LayoutParms> map) {
		layoutComboBoxBeingModified = true;
		Object prev = layoutComboBox.getSelectedItem();
		int lind = layoutComboBox.getSelectedIndex();
		int m = lcbmodel.getSize();
		boolean useCustom = false;
		if (m > 0 && lind > 0 && lind > m-3) {
		    useCustom = true;
		}

		Parser.setLayouts(map);

		lcbmodel.removeAllElements();
		int n = Parser.getNumberOfLayouts();
		for (int i = 0; i < n; i++) {
		    lcbmodel.addElement(Parser.getLayoutParms(i));
		}
		/*
		for (Object value: map.values()) {
		    lcbmodel.addElement(value);
		}
		*/
		lcbmodel.addElement(Gui.localeString("custom"));
		lcbmodel.addElement(Gui.localeString("setCustomLayout"));
		int mm = lcbmodel.getSize();
		
		if (useCustom) layoutComboBox.setSelectedIndex(mm - 2);
		else if (lind < m-2 && prev == layoutComboBox.getItemAt(lind)) {
		    layoutComboBox.setSelectedIndex(lind);
		} else {
		    layoutComboBox.setSelectedIndex(0);
		}
		layoutComboBoxBeingModified = false;
		/*
		for (Map.Entry<String,LayoutParms> entry: 
			 map.entrySet()) {
		    System.out.println(entry.getKey() +" - "
				       +entry.getValue());
		}
		*/
	    }
	};

    static JFrame layoutFrame = null;

    static private void showLayoutFrame() {
	if (layoutFrame == null) {
	    layoutFrame = new JFrame(localeString("layoutFrame"));
	    layoutFrame.setIconImages(iconList);
	    Container cpane = layoutFrame.getContentPane();
	    layoutFrame.addWindowListener
		(layoutPane.getOnClosingWindowListener());
	    layoutPane.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			layoutFrame.setVisible(false);
		    }
		});
	    layoutFrame.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
			layoutFrame.setVisible(false);
		    }
		});
	    cpane.setLayout(new BorderLayout());
	    cpane.add(layoutPane, "Center");
	    layoutFrame.pack();
	    layoutPane.setFrame(frame);
	}
	layoutFrame.setLocation(frame.getLocation());
	layoutFrame.setVisible(true);
    }


    static JLabel editLabel;

    static JButton domMapButton = null;

    static String titleURL = null;
    static boolean titleURLInUse = false;
    static String title = "";
    static JButton titleButton = null;
    static String descrURL = null;
    static boolean descrURLInUse = false;
    static String descr = "";
    static JButton descrButton = null;
    static String user = "";
    static char[] password = new char[0];

    // JButton authButton = null;
		
    static boolean headURLInUse = false;
    static String headURL = null;
    static String head = "";
    static JButton headButton = null;
    static boolean headerURLInUse = false;
    static String headerURL = null;
    static String header = "";
    static JButton headerButton = null;
    static boolean trailerURLInUse = false;
    static String trailerURL = null;
    static String trailer="";
    static JButton trailerButton = null;
    static boolean finalHtmlURLInUse = false;
    static String finalHtmlURL = null;
    static String finalHtml = "";
    static JButton finalHtmlButton = null;

    static InputPane inputPane;

    static JButton editButton;
    static JButton runButton;
    static JButton cancelButton;

    /*
    static TimeTextField iimageTimeTF;
    static TimeTextField iminImageTimeTF;
    */

    // JFileChooser inputChooser = null;

    static boolean canceled = false;

    static JProgressBar pbar;
    static ProgMonitor pm;

    static JLabel customParmsLabel = new 
	JLabel(localeString("customParmsURL") + ": ");
    static JTextField customParmsTF = new JTextField(50);
    static JPanel customParmsPanel = null;
    static JButton customParmsButton = 
	new JButton(localeString("customParmsButton"));

    static boolean askForCustomParms(Component comp) {
	if (customParmsPanel == null) {
	    customParmsPanel = new JPanel();
	    customParmsPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
	    customParmsPanel.add(customParmsLabel);
	    customParmsPanel.add(customParmsTF);
	    customParmsPanel.add(customParmsButton);
	    customParmsButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser(currentDir);
			for (javax.swing.filechooser.FileFilter f: 
				 fc.getChoosableFileFilters()) {
			    fc.removeChoosableFileFilter(f);
			}
			FileNameExtensionFilter xmlFilter =
			    new FileNameExtensionFilter("webnail-layout xml", 
							"xml", "wlo");
			fc.addChoosableFileFilter(xmlFilter);
			fc.setFileFilter(xmlFilter);
			fc.setApproveButtonText
			    (localeString("customParmsButtonApproveText"));
			boolean retry;
			do {
			    retry = false;
			    int status = fc.showOpenDialog(frame);
			    if (status == JFileChooser.APPROVE_OPTION ) {
				try {
				    File f = fc.getSelectedFile();
				    if (!f.isFile() && !f.canRead()) {
					retry = true;
					continue;
				    }
				    String url =f.toURI().toURL().toString();
				    customParmsTF.setText(url);
				} catch(Exception ee) {}
				return;
			    } else if (status == JFileChooser.CANCEL_OPTION) {
				return;
			    } else {
				System.err.println("unknown status");
				return;
			    }
			} while (retry);
		    }
		});
	}
	customParmsTF.setText((customParms == null)? "":
			      customParms.getCanonicalName());
	boolean retry;
	LayoutParser lp;
	try {
	    lp = new LayoutParser();
	} catch (Exception e) {
	    lp = null;
	}
	do {
	    retry = false;
	    int status =
		JOptionPane.showConfirmDialog (comp, customParmsPanel, 
					       localeString("customParmsTitle"),
					       JOptionPane.OK_CANCEL_OPTION);
	    switch (status) {
	    case JOptionPane.CLOSED_OPTION:
	    case JOptionPane.OK_OPTION:
		try {
		    String result = customParmsTF.getText().trim();
		    URL url = new URL(result);
		    LayoutParms parms = lp.parse(url);
		    customParms = parms;
		} catch (MalformedURLException murle) {
		    JOptionPane.showMessageDialog
			(comp, localeString("malformedURL"),
			 localeString("malformedURLTitle"),
			 JOptionPane.ERROR_MESSAGE);
		    retry = true;
		} catch (IOException ioe) {
		    JOptionPane.showMessageDialog
			(comp, ioe.getMessage(),
			 localeString("customParmsIOTitle"),
			 JOptionPane.ERROR_MESSAGE);
		    retry = true;
		} catch (SAXException se) {
		    JOptionPane.showMessageDialog
			(comp, se.getMessage(),
			 localeString("customParmsSAXTitle"),
			 JOptionPane.ERROR_MESSAGE);
		    retry = true;
		}
		break;
	    case JOptionPane.CANCEL_OPTION:
		return false;
	    }
	} while (retry);
	return true;
    }

    static void checkConsole() {
	if (console.hasNewTextToDisplay()) {
	    showConsole();
	}
    }

    static java.util.List<Image> iconList = new LinkedList<Image>();

    public static java.util.List<Image> getIconList() {
	return iconList;
    }

    static {
	try {
	    iconList.add((new 
			  ImageIcon((ClassLoader.getSystemClassLoader()
				     .getResource("webnail/webnailicon16.png")
				     ))).getImage());
	    iconList.add((new 
			  ImageIcon((ClassLoader.getSystemClassLoader()
				     .getResource("webnail/webnailicon24.png")
				     ))).getImage());
	    iconList.add((new 
			  ImageIcon((ClassLoader.getSystemClassLoader()
				     .getResource("webnail/webnailicon32.png")
				     ))).getImage());
	    iconList.add((new 
			  ImageIcon((ClassLoader.getSystemClassLoader()
				     .getResource("webnail/webnailicon48.png")
				     ))).getImage());
	    iconList.add((new 
			  ImageIcon((ClassLoader.getSystemClassLoader()
				     .getResource("webnail/webnailicon64.png")
				     ))).getImage());
	    iconList.add((new 
			  ImageIcon((ClassLoader.getSystemClassLoader()
				     .getResource("webnail/webnailicon96.png")
				     ))).getImage());
	    iconList.add((new 
			  ImageIcon((ClassLoader.getSystemClassLoader()
				     .getResource("webnail/webnailicon128.png")
				     ))).getImage());
	    iconList.add((new 
			  ImageIcon((ClassLoader.getSystemClassLoader()
				     .getResource("webnail/webnailicon256.png")
				     ))).getImage());
	    iconList.add((new 
			  ImageIcon((ClassLoader.getSystemClassLoader()
				     .getResource("webnail/webnailicon512.png")
				     ))).getImage());
	} catch (Exception e) {
	    System.err.println("initialization failed - missing icon for iconList");
	}
    }


    /* Called in Webnail's main program */
    static void configureGui() {
	SwingUtilities.invokeLater(new Runnable() {

		private void configureFields() {
		    mtnhtf = new WholeNumbTextField(5) {
			    protected boolean acceptText(String text) {
				if (text.equals("")) return true;
				return (Integer.parseInt(text) > 0);
			    }
			    protected boolean handleError() {
				JOptionPane.showMessageDialog
				    (this,
				     localeString("mustBePositiveNumber"),
				     localeString("Error"),
				     JOptionPane.ERROR_MESSAGE);
				return false;
			    }
			};
		    mtnwtf = new WholeNumbTextField(5) {
			    protected boolean acceptText(String text) {
				if (text.equals("")) return true;
				return (Integer.parseInt(text) > 0);
			    }
			    protected boolean handleError() {
				JOptionPane.showMessageDialog
				    (this,
				     localeString("mustBePositiveNumber"),
				     localeString("Error"),
				     JOptionPane.ERROR_MESSAGE);
				return false;
			    }
			};
		    ofntf = new JTextField(60);
		    imageTimeLabel = 
			new JLabel(localeString("imageTime") + ":");
		    imageTimeTF = new TimeTextField(15) {
			    protected boolean handleError() {
				JOptionPane.showMessageDialog
				    (this,
				     localeString("timeFormatError"),
				     localeString("Error"),
				     JOptionPane.ERROR_MESSAGE);
				return false;
			    }
			};
		    minImageTimeLabel = 
			new JLabel(localeString("minImageTime") + ":");
		    minImageTimeTF = new TimeTextField(15) {
			    protected boolean handleError() {
				JOptionPane.showMessageDialog
				    (this,
				     localeString("timeFormatError"),
				     localeString("Error"),
				     JOptionPane.ERROR_MESSAGE);
				return false;
			    }
			};
		    /*
		    iimageTimeTF = new TimeTextField(15) {
			    protected boolean handleError() {
				JOptionPane.showMessageDialog
				    (this,
				     localeString("timeFormatError"),
				     localeString("Error"),
				     JOptionPane.ERROR_MESSAGE);
				return false;
			    }
			};
		    iminImageTimeTF = new TimeTextField(15) {
			    protected boolean handleError() {
				JOptionPane.showMessageDialog
				    (this,
				     localeString("timeFormatError"),
				     localeString("Error"),
				     JOptionPane.ERROR_MESSAGE);
				return false;
			    }
			};
		    */
		}

		private void configurePanes() {
		    editLabel = new JLabel(localeString("editLabel")  +":");
		    inputPane = 
			new InputPane(editImagesPane.getImageList()) {
			    protected void clear(boolean all) {
				if (all) {
				    imageListModel.clear();
				} else {
				    int len = imageListModel.getSize();
				    while ((--len) > 0) {
					imageListModel.remove(len);
				    }
				}
			    }
			    protected void addFile (File f) {
				try {
				    URL url = f.toURI().toURL();
				    MapElement map = 
					new ImageMapElement(url, 
							    imageListModel);
				} catch (MalformedURLException e) {
				    // should not happen - 
				    // standard java methods are
				    // used to create the URL.
				}
			    }
			    protected void addURL(URL url) {
				MapElement map = 
				    new ImageMapElement(url, imageListModel);
			    }
			    protected void setFile (File f) {
				imageListModel.clear();
				addFile(f);
			    
			    }
			    protected void setURL(URL url) {
				// mapList.clear();
				imageListModel.clear();
				addURL(url);
			    }
			};
		    console = new SimpleConsole();
		    consoleFrame = 
			new JFrame(localeString("consoleFrameTitle"));
		    consoleFrame.setIconImages(iconList);
		    Container cpane = consoleFrame.getContentPane();
		    consoleFrame.setSize(800,600);
		    consoleFrame.addWindowListener(new WindowAdapter() {
			    public void windowClosing(WindowEvent e) {
				consoleFrame.setVisible(false);
			    }
			});
		    cpane.setLayout(new BorderLayout());
		    cpane.add(console, "Center");
		    pbar = new JProgressBar();
		    pm = new ProgMonitor() {
			    int count = 0;
			    Color pbarFG = pbar.getForeground();
			    public void startProgress(final int nfiles) {
				try {
				    SwingUtilities.invokeAndWait
					(new Runnable() {
						public void run() {
						    count = 0;
						    pbar.setMaximum(nfiles);
						    pbar.setForeground
							(Color.GREEN);
						    pbar.setEnabled(true);
						}
					    });
				} catch (Exception e) {
				    // ErrorMessage.display(e.getMessage());
				}
			    }
			    public void incrProgressCount() {
				count++;
				try {
				    SwingUtilities.invokeLater
					(new Runnable() {
						int thecount = count;
						public void run() {
						    pbar.setValue(thecount);
						}
					    });
				} catch (Exception e) {
				    // ErrorMessage.display(e.getMessage());
				}
			    }

			    public void stopProgress() {
				try {
				    if (SwingUtilities.isEventDispatchThread()){
					pbar.setEnabled(false);
					pbar.setForeground(pbarFG);
				    } else {
					SwingUtilities.invokeAndWait
					    (new Runnable() {
						    public void run() {
							pbar.setEnabled(false);
							pbar.setForeground
							    (pbarFG);
						    }
						});
				    }
				} catch (Exception e) {
				    // ErrorMessage.display(e.getMessage());
				}
			    }

			};
		}

		private void configureButtons() {
		    oftrbFile = 
			new JRadioButton(localeString("outToImageFile"), true);
		    oftrbDir =
			new JRadioButton(localeString("outToDirectory"));
		    oftrbZip =
			new JRadioButton(localeString("outToZipfile"));
		    oftrbWebDir =
			new JRadioButton(localeString("outToWebDirectory"));
		    oftrbWebZip =
			new JRadioButton(localeString("outToWebZipfile"));
		    oftrbWarDir =
			new JRadioButton(localeString("outToWarDirectory"));
		    oftrbWar =
			new JRadioButton(localeString("outToWarFile"));
		    oftbg = new ButtonGroup();
		    colorButton = 
			new JButton (localeString("colorButton"));
		    windowTitleButton =
			new JButton(localeString("windowTitleButton"));
		    linkCheckBox = 
			new JCheckBox(localeString("linkCheckBox"));
		    flatCheckBox =
			new JCheckBox(localeString("flatCheckBox"));
		    hrCheckBox =
			new JCheckBox(localeString("hrCheckBox"));
		    syncCheckBox = 
			new JCheckBox(localeString("syncCheckBox"));
		    waitOnErrCheckBox = 
			new JCheckBox(localeString("waitOnErrCheckBox"));
		    hrefToOrigCheckBox = 
			new JCheckBox(localeString("hrefToOrigCheckBox"));
		     
		    /*
		      for (int li = 0; li < 6; li++) {
		      LayoutParms parms = Parser.getLayoutParms(li);
		      System.out.println(parms);
		      }
		    */
		     
		    // layoutComboBox = new JComboBox(lcbmodel);
		    layoutPane.init(true);
		    layoutParms = Parser.getLayoutParms(0);
		    customParms = null;
			 
		    editButton = new JButton(localeString("editImages"));
		    runButton = new JButton(localeString("run"));
		    cancelButton = new JButton(localeString("cancel"));
		    cancelButton.setEnabled(false);

		    domMapButton = 
			new DomMapButton (localeString("domMapButton"), 
					  frame,
					  localeString
					  ("domMapButtonDialogTitle")) {
			    protected TemplateProcessor.KeyMap[] getInput() {
				int n = domMapList.size();
				TemplateProcessor.KeyMap[] array = 
				    new TemplateProcessor.KeyMap[n];
				array = domMapList.toArray(array);
				return array;
			    }
			    protected void doOutput
				(LinkedList<TemplateProcessor.KeyMap> list)
			    {
				domMapList.clear();
				domMapList.addAll(0, list);
			    }
			};
		    titleButton = new 
			URLTextAreaButton(localeString("titleButton"), 10, 50,
					  frame,
					  localeString("titleTitle"),
					  localeString("titleErrorTitle")) {
			    protected String inputText() {
				// System.out.println("adding title " + title);
				return title;
			    }
			    protected String inputURL() {
				return titleURL;
			    }
			    protected boolean inputURLInUse() {
				return titleURLInUse;
			    }
			    protected void outputText(String value) {
				title = value;
			    }
			    protected void outputURL(String url) {
				titleURL = url;
			    }
			    protected void outputURLInUse(boolean inUse) {
				titleURLInUse = inUse;
			    }
			};

		    descrButton = new 
			URLTextAreaButton(localeString("descrButton"), 10, 50,
					  frame,
					  localeString("descrTitle"),
					  localeString("descrErrorTitle")) {
			    protected String inputText() {
				return descr;
			    }
			    protected String inputURL() {
				return descrURL;
			    }
			    protected boolean inputURLInUse() {
				return descrURLInUse;
			    }
			    protected void outputText(String value) {
				descr = value;
			    }
			    protected void outputURL(String url) {
				descrURL = url;
			    }
			    protected void outputURLInUse(boolean inUse) {
				descrURLInUse = inUse;
			    }
			};

		    headButton = new 
			URLTextAreaButton(localeString("headButton"), 10, 50,
					  frame,
					  localeString("headTitle"),
					  localeString("headErrorTitle")) {
			    protected String inputText() {
				return head;
			    }
			    protected String inputURL() {
				return headURL;
			    }
			    protected boolean inputURLInUse() {
				return headURLInUse;
			    }
			    protected void outputText(String value) {
				head = value;
			    }
			    protected void outputURL(String url) {
				headURL = url;
			    }
			    protected void outputURLInUse(boolean inUse) {
				headURLInUse = inUse;
			    }
			};
		    headerButton = new 
			URLTextAreaButton(localeString("headerButton"), 10, 50,
					  frame,
					  localeString("headerTitle"),
					  localeString("headerErrorTitle")) {
			    protected String inputText() {
				return header;
			    }
			    protected String inputURL() {
				return headerURL;
			    }
			    protected boolean inputURLInUse() {
				return headerURLInUse;
			    }
			    protected void outputText(String value) {
				header = value;
			    }
			    protected void outputURL(String url) {
				headerURL = url;
			    }
			    protected void outputURLInUse(boolean inUse) {
				headerURLInUse = inUse;
			    }
			};
		    trailerButton = new 
			URLTextAreaButton(localeString("trailerButton"), 10, 50,
					  frame,
					  localeString("trailerTitle"),
					  localeString("trailerErrorTitle")) {
			    protected String inputText() {
				return trailer;
			    }
			    protected String inputURL() {
				return trailerURL;
			    }
			    protected boolean inputURLInUse() {
				return trailerURLInUse;
			    }
			    protected void outputText(String value) {
				trailer = value;
			    }
			    protected void outputURL(String url) {
				trailerURL = url;
			    }
			    protected void outputURLInUse(boolean inUse) {
				trailerURLInUse = inUse;
			    }
			};

		    finalHtmlButton = new 
			URLTextAreaButton(localeString("finalHtmlButton"),
					  10, 50,
					  frame,
					  localeString("finalHtmlTitle"),
					  localeString("finalHTMLErrorTitle")) {
			    protected String inputText() {
				return finalHtml;
			    }
			    protected String inputURL() {
				return finalHtmlURL;
			    }
			    protected boolean inputURLInUse() {
				return finalHtmlURLInUse;
			    }
			    protected void outputText(String value) {
				finalHtml = value;
			    }
			    protected void outputURL(String url) {
				finalHtmlURL = url;
			    }
			    protected void outputURLInUse(boolean inUse) {
				finalHtmlURLInUse = inUse;
			    }
			};
		}

		private Component searchContainer(Container ct, Class target) {
		    int n = ct.getComponentCount();
		    for (Component c: ct.getComponents()) {
			if (target.isInstance(c)) {
			    return c;
			}
			if (c instanceof Container) {
			    Component c1 = 
				searchContainer((Container)c, target);
			    if (c1 != null) {
				return c1;
			    }
			}
		    }
		    return null;
		}
		
		boolean focusComponentOK(Component c) {
		    if (c == null) return false;
		    return (c == oftrbFile || c == oftrbDir || c == oftrbZip 
			    || c == oftrbWebDir || c == oftrbWebZip
			    || c == oftrbWarDir ||c == oftrbWar);
		}

		boolean handleOutputFileTextFieldAux(String path, 
						     FocusEvent fe) {
		    Component c = (fe == null)? null: fe.getOppositeComponent();
		    File target = new File(path);
		    if (!target.isAbsolute()) {
			target = new File(currentDir, path);
		    }
		    if (oftrbFile.isSelected()) {
			// single image
			String name = target.getName();
			int ind = name.lastIndexOf('.');
			if (ind < 0) {
			    // suffix missing.
			    if (!focusComponentOK(c)) {
				JOptionPane.showMessageDialog
				    (frame,
				     localeString("ofSuffixMissing"));
				SwingUtilities.invokeLater
				    (new Runnable() {
					    public void run() {
						ofntf
						    .requestFocusInWindow();
					    }});
			    }
			    return false;
			} else {
			    String suffix = name.substring(ind+1);
			    String mt =
				ImageMimeInfo.getMIMETypeForSuffix(suffix);
			    if (mt == null) {
				// not an image
				if (!focusComponentOK(c)) {
				    JOptionPane.showMessageDialog
					(frame,
					 String.format
					 (localeString("ofNotImage"),
					  "." + suffix));
				    SwingUtilities.invokeLater
					(new Runnable() {
						public void run() {
						    ofntf
							.requestFocusInWindow();
						}});
				}
				return false;
			    } else if (!mt.equals(mtype)) {
				// wrong suffix
				String mtsuffix =
				    ImageMimeInfo.getExtensionForMT(mtype);
				if (!focusComponentOK(c)) {
				    JOptionPane.showMessageDialog
					(frame,
					 String.format
					 (localeString("ofWrongSuffix"),
					  "." + mtsuffix,
					  "." + suffix));
				    SwingUtilities.invokeLater
					(new Runnable() {
						public void run() {
						    ofntf
							.requestFocusInWindow();
						}});
				}
				return false;
			    }
			}
		    } else if (oftrbDir.isSelected()
			       || oftrbWebDir.isSelected()
			       || oftrbWarDir.isSelected()) {
			// directory
			if (target.exists()) {
			    if (!target.isDirectory()) {
				if (!focusComponentOK(c)) {
				    JOptionPane.showMessageDialog
					(frame,
					 String.format
					 (localeString("ofNotADirectory"),
					  target.toString()));
				    SwingUtilities.invokeLater
					(new Runnable() {
						public void run() {
						    ofntf
							.requestFocusInWindow();
						}});
				}
				return false;
			    }
			} else {
			    if (target.mkdirs() == false) {
				if (!focusComponentOK(c)) {
				    JOptionPane.showMessageDialog
					(frame,
					 String.format
					 (localeString("dirCreateFailed"),
					  target.toString()));
				    SwingUtilities.invokeLater
					(new Runnable() {
						public void run() {
						    ofntf
							.requestFocusInWindow();
						}});
				}
				return false;
			    }
			}
		    } else if (oftrbZip.isSelected() || 
			       oftrbWebZip.isSelected()) {
			// ZIP file
			String name = target.getName();
			int ind = name.lastIndexOf('.');
			if (ind < 0) {
			    // suffix missing.
			    if (!focusComponentOK(c)) {
				JOptionPane.showMessageDialog
				    (frame,
				     localeString("ofSuffixMissing"));
				SwingUtilities.invokeLater
				    (new Runnable() {
					    public void run() {
						ofntf
						    .requestFocusInWindow();
					    }});
			    }
			    return false;
			} else {
			    String suffix = name.substring(ind+1);
			    if (!suffix.toLowerCase().equals("zip")) {
				if (!focusComponentOK(c)) {
				    JOptionPane.showMessageDialog
					(frame,
					 String.format
					 (localeString("ofWrongSuffix"),
					  ".zip",
					  "." + suffix));
				    SwingUtilities.invokeLater
					(new Runnable() {
						public void run() {
						    ofntf
							.requestFocusInWindow();
						}});
				}
				return false;
			    }
			}
		    } else if (oftrbWar.isSelected()) {
			// WAR file
			String name = target.getName();
			int ind = name.lastIndexOf('.');
			if (ind < 0) {
			    // suffix missing.
			    if (!focusComponentOK(c)) {
				JOptionPane.showMessageDialog
				    (frame,
				     localeString("ofSuffixMissing"));
				SwingUtilities.invokeLater
				    (new Runnable() {
					    public void run() {
						ofntf
						    .requestFocusInWindow();
					    }});
			    }
			    return false;
			} else {
			    String suffix = name.substring(ind+1);
			    if (!suffix.toLowerCase().equals("war")) {
				if (!focusComponentOK(c)) {
				    JOptionPane.showMessageDialog
					(frame,
					 String.format
					 (localeString("ofWrongSuffix"),
					  ".war",
					  "." + suffix));
				    SwingUtilities.invokeLater
					(new Runnable() {
						public void run() {
						    ofntf
							.requestFocusInWindow();
						}});
				}
				return false;
			    }
			}
		    }
		    ofntf.setText(target.toString());
			    
		    if (c != ofnb) return true;
		    final boolean wm = oftrbWar.isSelected()
			|| oftrbZip.isSelected() 
			|| oftrbDir.isSelected()
			|| oftrbWebDir.isSelected();
		    SwingUtilities.invokeLater
			(new Runnable() {
				public void run() {
				    if (wm) {
					layoutComboBox.requestFocusInWindow();
				    } else {
					mtnwtf.requestFocusInWindow();
				    }
				}
			    });
		    return true;
		}

		void handleOutputFileTextField(FocusEvent fe) {
		    String path = ofntf.getText();
		    if (old != path) {
			if (path == null || 
			    path.trim().length() == 0) {
			    //inputChooser.setSelectedFile(null);
			    // inputPane.setSelectionMode
			    //    (InputPane.SelectionMode.SINGLE);
			    // inputChooser.setMultiSelectionEnabled(false);
			    // webmodeBox.setEnabled(false);
			} else {
			    handleOutputFileTextFieldAux(path, fe);
			}
			old = null;
		    }
		}

		public void run() {
		    frame = new JFrame(localeString("title"));

		    frame.setIconImages(iconList);

		    // set up an authenticator for web sites, etc.
		    Authenticator.setDefault
			(AuthenticationPane.getAuthenticator(frame));

		    configureFields();
		    configurePanes();
		    configureButtons();
		    ErrorMessage.setComponent(frame);
		    ErrorMessage.setAppendable(console);

		    Container pane = frame.getContentPane();
		    GridBagLayout gridbag = new GridBagLayout();
		    GridBagConstraints c = new GridBagConstraints();
		    c.gridwidth = GridBagConstraints.REMAINDER;
		    pane.setLayout(gridbag);

		    JLabel spacer0 = new JLabel(" ");
		    gridbag.setConstraints(spacer0 ,c);
		    pane.add(spacer0);

		    JPanel outPanel = new JPanel();
		    GridBagLayout gridbag1 = new GridBagLayout();
		    outPanel.setLayout(gridbag1);
		    outPanel.setBackground(COLOR1);

		    JPanel outTypePanel1 = new JPanel();
		    FlowLayout outtfl1 = new FlowLayout(FlowLayout.LEADING);
		    outTypePanel1.setBackground(COLOR1);
		    outTypePanel1.setLayout(outtfl1);
		    oftbg.add(oftrbFile);
		    oftrbFile.setBackground(COLOR1);
		    oftbg.add(oftrbDir);
		    oftrbDir.setBackground(COLOR1);
		    oftbg.add(oftrbZip);
		    oftrbZip.setBackground(COLOR1);
		    oftbg.add(oftrbWebDir);
		    oftrbWebDir.setBackground(COLOR1);
		    oftbg.add(oftrbWebZip);
		    oftrbWebZip.setBackground(COLOR1);
		    oftbg.add(oftrbWarDir);
		    oftrbWarDir.setBackground(COLOR1);
		    oftbg.add(oftrbWar);
		    oftrbWar.setBackground(COLOR1);
		    outTypePanel1.add(new JLabel(localeString("outType") +":"));
		    outTypePanel1.add(oftrbFile);
		    outTypePanel1.add(oftrbDir);
		    outTypePanel1.add(oftrbZip);
		    outTypePanel1.add(oftrbWebDir);
		    outTypePanel1.add(oftrbWebZip);
		    outTypePanel1.add(oftrbWarDir);
		    outTypePanel1.add(oftrbWar);
		    oftrbFile.setToolTipText(localeString("oftrbFileToolTip"));
		    oftrbDir.setToolTipText(localeString("oftrbDirToolTip"));
		    oftrbZip.setToolTipText(localeString("oftrbZipToolTip"));
		    oftrbWebDir.setToolTipText
			(localeString("oftrbWebDirToolTip"));
		    oftrbWebZip.setToolTipText
			(localeString("oftrbWebZipToolTip"));
		    oftrbWarDir.setToolTipText
			(localeString("oftrbWarDirToolTip"));
		    oftrbWar.setToolTipText(localeString("oftrbWarToolTip"));

		    gridbag1.setConstraints(outTypePanel1, c);
		    outPanel.add(outTypePanel1);

		    JPanel mtPanel = new JPanel();
		    FlowLayout npfl = new FlowLayout(FlowLayout.LEADING);
		    npfl.setHgap(10);
		    mtPanel.setLayout(npfl);
		    mtPanel.setBackground(COLOR1);
		    final String[] mtarray = 
			ImageMimeInfo.getMimeTypes().toArray
			(new String[ImageMimeInfo.getMimeTypes().size()]);
		    String[] cbmtarray = mtarray.clone();
		    for (int i = 0; i < mtarray.length; i++) {
			cbmtarray[i] = localeMTString(mtarray[i]);
		    }
		    JLabel cbLabel = 
			new JLabel(localeString("outputImageMIMEtype"));
		    final JComboBox comboBox = new JComboBox(cbmtarray);
		    for (int i = 0; i < mtarray.length; i++) {
			if (mtarray[i].equals("image/jpeg")) {
			    comboBox.setSelectedItem(cbmtarray[i]);
			}
		    }

		    ActionListener cbal = new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
				int ind = comboBox.getSelectedIndex();
				mtype = mtarray[ind];
				//extension = Webnail.extmap.get(mtype);
				//type = Webnail.fmtmap.get(mtype);
				extension =
				    ImageMimeInfo.getExtensionForMT(mtype);
				type =
				    ImageMimeInfo.getFormatNameFromMimeType
				    (mtype);
				//System.out.println(type +" " + extension);
			    }
			};
		    comboBox.addActionListener(cbal);
		    layoutComboBox.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
				if (layoutComboBoxBeingModified) return;
				int ind = layoutComboBox.getSelectedIndex();
				if (ind == -1) return;
				// int n = layoutChoices.length;
				int n = lcbmodel.getSize();
				if (ind < n-2) {
				    layoutParms = Parser.getLayoutParms(ind);
				} else if (ind == (n - 2)) {
				    if (customParms == null) {
					if (askForCustomParms(layoutComboBox)) {
					    layoutParms = customParms;
					} else {
					    layoutComboBox.setSelectedIndex
						(lastLayoutIndex);
					}
				    } else {
					layoutParms = customParms;
				    }
				} else if (ind == (n - 1)) {
				    if (askForCustomParms(layoutComboBox)) {
					layoutComboBox.setSelectedIndex(n-2);
					layoutParms = customParms;
				    } else {
					layoutComboBox.setSelectedIndex
					    (lastLayoutIndex);
				    }
				}
				lastLayoutIndex = 
				    layoutComboBox.getSelectedIndex();
				setEnableds();
			    }
			});
		    mtPanel.add(cbLabel);
		    mtPanel.add(comboBox);
		    comboBox.setToolTipText(localeString("MTcomboBoxToolTip"));


		    gridbag1.setConstraints(mtPanel,c);
		    outPanel.add(mtPanel);


		    JLabel ofnl = 
			new JLabel(localeString("outputFileName"));
		    gridbag1.setConstraints(ofnl, c);
		    outPanel.add(ofnl);
		    JPanel outPanel1 = new JPanel();
		    FlowLayout outfl = new FlowLayout(FlowLayout.LEADING);
		    outfl.setHgap(10);
		    outPanel1.setLayout(outfl);
		    outPanel1.setBackground(COLOR1);
		    outPanel1.add(ofntf);
		    outPanel1.add(ofnb);
		    ofnb.setToolTipText(localeString("ofnbToolTip"));
		    gridbag1.setConstraints(outPanel1, c);
		    outPanel.add(outPanel1);
		    
		    gridbag.setConstraints(outPanel, c);
		    pane.add(outPanel);
		    
		    JLabel spacer2 = new JLabel(" ");
		    gridbag.setConstraints(spacer2, c);
		    pane.add(spacer2);

		    JPanel webPanel3 = new JPanel();
		    FlowLayout webfl3 = new FlowLayout(FlowLayout.LEADING);
		    webPanel3.add(layoutComboBox);
		    layoutComboBox.setToolTipText
			(localeString("layoutComboBoxToolTip"));
		    webPanel3.add(mtnwl);
		    webPanel3.add(mtnwtf);
		    webPanel3.add(mtnhl);
		    webPanel3.add(mtnhtf);
		    webPanel3.add(domMapButton);
		    domMapButton.setToolTipText
			(localeString("domMapButtonToolTip"));
		    gridbag.setConstraints(webPanel3, c);
		    pane.add(webPanel3);

		    JPanel webPanel4 = new JPanel();
		    FlowLayout webfl4 = new FlowLayout(FlowLayout.LEADING);
		    webPanel4.setLayout(webfl4);
		    webPanel4.add(editLabel);
		    webPanel4.add(windowTitleButton);
		    windowTitleButton.setToolTipText
			(localeString("windowTitleButtonToolTip"));
		    webPanel4.add(headButton);
		    headButton.setToolTipText
			(localeString("headButtonToolTip"));
		    webPanel4.add(headerButton);
		    headerButton.setToolTipText
			(localeString("headerButtonToolTip"));
		    webPanel4.add(titleButton);
		    titleButton.setToolTipText
			(localeString("titleButtonToolTip"));
		    webPanel4.add(descrButton);
		    descrButton.setToolTipText
			(localeString("descrButtonToolTip"));
		    webPanel4.add(trailerButton);
		    trailerButton.setToolTipText
			(localeString("trailerButtonToolTip"));
		    webPanel4.add(finalHtmlButton);
		    finalHtmlButton.setToolTipText
			(localeString("finalHtmlButtonToolTip"));
		    gridbag.setConstraints(webPanel4, c);
		    pane.add(webPanel4);

		    JLabel spacer1 = new JLabel(" ");
		    gridbag.setConstraints(spacer1, c);
		    pane.add(spacer1);

		    JPanel webOptPanel = new JPanel();
		    GridBagLayout gridbag2 = new GridBagLayout();
		    webOptPanel.setLayout(gridbag2);
		    webOptPanel.setBackground(COLOR2);

		    JPanel webPanel1 = new JPanel();
		    FlowLayout webfl1 = new FlowLayout(FlowLayout.LEADING);
		    webPanel1.setLayout(webfl1);
		    webPanel1.setBackground(COLOR2);
		    webPanel1.add(colorButton);
		    colorButton.setToolTipText
			(localeString("colorButtonToolTip"));
		    webPanel1.add(imageTimeLabel);
		    webPanel1.add(imageTimeTF);
		    imageTimeTF.setToolTipText
			(localeString("imageTimeTFToolTip"));
		    webPanel1.add(minImageTimeLabel);
		    imageTimeTF.setDefaultValue(10000);
		    webPanel1.add(minImageTimeTF);
		    minImageTimeTF.setDefaultValue(4000);
		    minImageTimeTF.setToolTipText
			(localeString("minImageTimeTFToolTip"));
		    gridbag2.setConstraints(webPanel1, c);
		    webOptPanel.add(webPanel1);

		    JPanel webPanel2 = new JPanel();
		    FlowLayout webfl2 = new FlowLayout(FlowLayout.LEADING);
		    webPanel2.setLayout(webfl2);
		    webPanel2.setBackground(COLOR2);
		    webPanel2.add(hrCheckBox);
		    hrCheckBox.setSelected(true);
		    hrCheckBox.setBackground(COLOR2);
		    hrCheckBox.setToolTipText
			(localeString("hrCheckBoxToolTip"));
		    webPanel2.add(syncCheckBox);
		    syncCheckBox.setBackground(COLOR2);
		    syncCheckBox.setToolTipText
			(localeString("syncCheckBoxToolTip"));
		    webPanel2.add(waitOnErrCheckBox);
		    waitOnErrCheckBox.setBackground(COLOR2);
		    waitOnErrCheckBox.setToolTipText
			(localeString("waitOnErrCheckBoxToolTip"));
		    webPanel2.add(linkCheckBox);
		    linkCheckBox.setBackground(COLOR2);
		    webPanel2.add(flatCheckBox);
		    flatCheckBox.setBackground(COLOR2);
		    webPanel2.add(hrefToOrigCheckBox);
		    hrefToOrigCheckBox.setBackground(COLOR2);
		    gridbag2.setConstraints(webPanel2, c);
		    webOptPanel.add(webPanel2);

		    gridbag.setConstraints(webOptPanel, c);
		    pane.add(webOptPanel);

		    mtnwtf.setToolTipText(localeString("mtnwtfToolTip"));
		    mtnhtf.setToolTipText(localeString("mtnhtfToolTip"));
		    linkCheckBox.setToolTipText
			(localeString("linkCheckBoxToolTip"));
		    flatCheckBox.setToolTipText
			(localeString("flatCheckBoxToolTip"));
		    hrefToOrigCheckBox.setToolTipText
			(localeString("hrefToOrigCheckBoxToolTip"));
		    windowTitleButton.addActionListener (new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
				String oldTitle = windowTitle;
				windowTitle = (String)
				    JOptionPane.showInputDialog
				    (frame,
				     localeString("wtbMessage"),
				     localeString("wtbTitle"),
				     JOptionPane.PLAIN_MESSAGE,
				     null,
				     null,
				     windowTitle);
				if (windowTitle == null) windowTitle = oldTitle;
			    }
			});

		    ActionListener warRbListener = new ActionListener() {
			    JRadioButton last = oftrbFile;
			    public void actionPerformed(ActionEvent e) {
				if (e != null) {
				    if (oftrbFile.isSelected() &&
					imageListModel.size() > 1) {
					// have too many images; need to confirm
					// if we can remove some.
					int status = 
					    JOptionPane.showConfirmDialog
					    (frame,
					     "deleteExtraImagesOK",
					     "deleteExtraImagesTitle",
					     JOptionPane.OK_CANCEL_OPTION);
					if (status != 0) {
					    last.setSelected(true);
					    return;
					}

				    }
				    last = (JRadioButton)e.getSource();
				}
				setEnableds();
				setOfntfToolTipText();
				if (ofntf.getText().trim().length() > 0) {
				    handleOutputFileTextField(null);
				}
			    }
			};

		    oftrbFile.addActionListener(warRbListener);
		    oftrbDir.addActionListener(warRbListener);
		    oftrbZip.addActionListener(warRbListener);
		    oftrbWebDir.addActionListener(warRbListener);
		    oftrbWebZip.addActionListener(warRbListener);
		    oftrbWebZip.addActionListener(warRbListener);
		    oftrbWarDir.addActionListener(warRbListener);
		    oftrbWar.addActionListener(warRbListener);

		    ActionListener seActionListener = new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
				setEnableds();
			    }
			};

		    hrefToOrigCheckBox.addActionListener(seActionListener);
		    hrCheckBox.addActionListener(seActionListener);

		    // we don't use the ActionEvent, so do this to initialize.
		    warRbListener.actionPerformed(null);


		    ofntf.addFocusListener(new FocusAdapter() {
			    public void focusGained(FocusEvent fe) {
				old = ofntf.getText();
			    }
			    public void focusLost(FocusEvent fe) {
				handleOutputFileTextField(fe);
			    }
			});
		    ofntf.addKeyListener(new KeyAdapter() {
			    public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == 10) {
				    SwingUtilities.invokeLater
					(new Runnable() {
						public void run() {
						    ofnb.requestFocusInWindow();
						}
					    });      
				}
			    }
			});

		    JLabel spacer3 = new JLabel(" ");
		    gridbag.setConstraints(spacer3, c);
		    pane.add(spacer3);

		    inputPane.setBackground(COLOR1);
		    gridbag.setConstraints(inputPane, c);
		    pane.add(inputPane);

		    JLabel spacer4 = new JLabel(" ");
		    gridbag.setConstraints(spacer4, c);
		    pane.add(spacer4);

		    JPanel controlPanel = new JPanel();
		    FlowLayout cntlfl = new FlowLayout(FlowLayout.LEADING);
		    cntlfl.setHgap(10);
		    controlPanel.setLayout(cntlfl);
		    controlPanel.add(editButton);
		    editButton.setToolTipText
			(localeString("editButtonToolTip"));
		    controlPanel.add(runButton);
		    runButton.setToolTipText(localeString("runButtonToolTip"));
		    controlPanel.add(cancelButton);
		    cancelButton.setToolTipText
			(localeString("cancelButtonToolTip"));
		    gridbag.setConstraints(controlPanel, c);
		    pane.add(controlPanel);
		    

		    editButton.addActionListener (new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
				showEditFrame();
				return; 
			    }
			});
		    runButton.addActionListener (new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
				String path = ofntf.getText();
				if (path == null || path.trim().length() == 0) {
				    JOptionPane.showMessageDialog
					(frame,
					 localeString("ofMissing"));
				    return;
				}
				if (!handleOutputFileTextFieldAux(path, null)) {
				    ErrorMessage.display
					("bad output file name");
				    return;
				}
				canceled = false;
				/*
				  System.out.println("runButton Action Listener");
				*/
				processFiles(imageListModel, (String)null,
					     domMapList);
				final Thread w = worker;
				if (w != null) {
				    runButton.setEnabled(false);
				    (new Thread() {
					    public void run() {
						cancelButton.setEnabled(true);
						try {
						    w.join();
						} catch(Exception e) {}
						SwingUtilities.invokeLater
						    (new Runnable() {
							    public void run() {
								cancelButton
								    .setEnabled
								    (false);
								runButton
								    .setEnabled
								    (true);
								checkConsole();
							    }
							});
					    }
					}).start();
				}
			    }
			});
		    cancelButton.addActionListener (new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
				Thread w = worker;
				if (!canceled && w != null) {
				    w.interrupt();
				    pm.stopProgress();
				}
				canceled = true;
			    }
			});

		    JPanel pbarPanel = new JPanel();
		    FlowLayout pbfl = new FlowLayout(FlowLayout.LEADING);
		    pbfl.setHgap(10);
		    pbarPanel.setLayout(pbfl);

		    pbar.setMinimum(0);
		    pbar.setMaximum(1);
		    pbar.setValue(0);
		    pbar.setEnabled(false);
		    
		    pbarPanel.add(new JLabel(localeString("pbarLabel")
					     +":"));
		    pbarPanel.add(pbar);

		    gridbag.setConstraints(pbarPanel, c);
		    pane.add(pbarPanel);

		    JLabel spacer5 = new JLabel(" ");
		    gridbag.setConstraints(spacer5, c);
		    pane.add(spacer5);


		    configureMenus();
		    frame.pack();

		    frame.addWindowListener(new WindowAdapter() {
			    public void 
				windowClosing(WindowEvent e) {
				System.exit(0);
			    }
			    public void windowOpened(WindowEvent e) {
				oftrbDir.requestFocus();
			    }
			});

		    colorButton.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
				Color color = JColorChooser.showDialog
				    (frame, localeString("colorDialogTitle"),
				     Color.decode(bgcolor));
				if (color != null) {
				    bgcolor = 
					String.format("#%06X", 
						      color.getRGB()
						      & 0xFFFFFF)
					.toLowerCase();
				}
				// System.out.println(bgcolor);
			    }
			});

		    ofnb.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = 
				    new JFileChooser(ocurrentDir);
				for (javax.swing.filechooser.FileFilter f: 
					 chooser
					 .getChoosableFileFilters()) {
				    chooser.removeChoosableFileFilter(f);
				}
				boolean multi = true;
				if (oftrbFile.isSelected()) {
				    chooser.setFileSelectionMode
					(JFileChooser.FILES_ONLY);
				    FileNameExtensionFilter imageFilter =
					new FileNameExtensionFilter
					("Image Formats",
					 ImageMimeInfo.getAllExt());
				    chooser.addChoosableFileFilter(imageFilter);
				    multi = false;
				} else if (oftrbDir.isSelected()
					   || oftrbWebDir.isSelected()
					   || oftrbWarDir.isSelected()) {
				    chooser.setFileSelectionMode
					(JFileChooser.DIRECTORIES_ONLY);
				} else if (oftrbZip.isSelected()
					   || oftrbWebZip.isSelected()) {
				    chooser.setFileSelectionMode
					(JFileChooser.FILES_ONLY);
				    chooser.addChoosableFileFilter
					(new FileNameExtensionFilter
					 ("Zip File", "zip"));
				} else if (oftrbWar.isSelected()) {
				    chooser.setFileSelectionMode
					(JFileChooser.FILES_ONLY);
				    chooser.addChoosableFileFilter
					(new FileNameExtensionFilter
					 ("War File", "war"));
				}

				if (chooser.showDialog
				    (frame, localeString("choose")) ==
				    JFileChooser.APPROVE_OPTION) {
				    ofntf.setText(chooser.getSelectedFile()
						  .getPath());
				    ocurrentDir = 
					chooser.getCurrentDirectory();
				    // inputChooser.setSelectedFile(null);
				    File target = chooser.getSelectedFile();
				    if (multi) {
					inputPane.setSelectionMode
					    (InputPane.SelectionMode.MULTI);
					editImagesPane.setSelectionMode
					    (InputPane.SelectionMode.MULTI);
				    } else {
					inputPane.setSelectionMode
					    (InputPane.SelectionMode.SINGLE);
					editImagesPane.setSelectionMode
					    (InputPane.SelectionMode.SINGLE);
				    }
				    if (oftrbFile.isSelected() 
					|| oftrbDir.isSelected()
					|| oftrbZip.isSelected()) {
					Component c = searchContainer
					    (inputPane, JRadioButton.class);
					if (c != null) {
					    c.requestFocusInWindow();
					} else {
					    inputPane.requestFocusInWindow();
					}
				    } else {
					colorButton.requestFocusInWindow();
				    }
				}
			    }
			});
		    frame.setVisible(true);
		}
	    });
    }

    /*
    static public void main(String argv[]) {
	if (argv.length > 0) {
	    Webnail.main(argv);
	} else {
	    configureGui();
	}
    }
    */
}
