package webnail;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import java.util.*;
import java.util.prefs.*;
import java.awt.*;
import java.awt.event.*;

import java.io.*;
import java.net.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import org.bzdev.swing.*;
import org.bzdev.swing.URLListTransferHandler;

public class LayoutPane extends JComponent {

    protected void onClosing(Map<String,LayoutParms> map) {
    }

    boolean modified = false;

    static private final String resourceBundleName = "webnail.LayoutPane";
    static ResourceBundle bundle = 
	ResourceBundle.getBundle(resourceBundleName);
    static String localeString(String name) {
	return bundle.getString(name);
    }

    static final String prefname = "webnail/layouts";
    static Preferences userPrefs = Preferences.userRoot().node(prefname);
    static LayoutParser lp;
    static LayoutParser lpDelayed;
    static {
	try {
	    lp = new LayoutParser();
	    lpDelayed = new LayoutParser();
	} catch (Exception e) {}
    }

    final static Object syncObject = new Object();
    static int processCount = 0;

    void processURL(final URL url, final String name) {
	if (url == null) return;
	final String urlString = url.toString();
	if (!map.containsKey(urlString)) {
	    if (urlString.startsWith("file:")) {
		try {
		    LayoutParms parms = lp.parse(url);
		    // String value = parms.getName();
		    map.put(urlString, parms);
		} catch (Exception e) {
		    ErrorMessage.display(e);
		}
	    } else {
		final LayoutParms parms = (name == null)?
		    new LayoutParms(urlString): 
		    new LayoutParms(urlString, name);
		map.put(urlString, parms);
		parms.thread = new Thread (new Runnable() {
			public void run() {
			    synchronized(syncObject) {processCount++;}
			    try {
				synchronized(syncObject) {
				    final LayoutParms vparms = 
					lpDelayed.parse(url);
				    SwingUtilities.invokeLater(new Runnable() {
					    public void run() {
						parms.set(vparms);
						/*
						String value = parms.getName();
						map.put(urlString, 
							value);
						*/
					    }
					});
				}
			    } catch (Exception e) {
				ErrorMessage.display(e);
			    } finally {
				synchronized(syncObject) {processCount--;}
			    }
			}
		    });
		parms.thread.start();
	    }
	}
    }

    // change as appropriate
    File icurrentDir = new File(".");

    DefaultListModel<Object> model = new DefaultListModel<>();
    JList<Object> jlist;
    JScrollPane scrollPane;
    HashMap<String,LayoutParms> map = new LinkedHashMap<String,LayoutParms>();


    // must be initialized after jlist is initialized.
    TransferHandler th = null; 

    JLabel urlLabel = new JLabel(localeString("urlLabel") + ":");
    JTextField addURLTF = new JTextField(56);
    int oldlength = 0;
    JButton chooseAddButton = new JButton(localeString("chooseAddButtonNoURL"));
    JButton cutSelectionButton = 
	new JButton(localeString("cutSelectionButton"));
    JButton deleteCutButton = new JButton(localeString("deleteCutButton"));
    JButton pasteBeforeSelectionButton = 
	new JButton(localeString("pasteBeforeSelectionButton"));
    JButton pasteAfterSelectionButton = 
	new JButton(localeString("pasteAfterSelectionButton"));
    JLabel urlsLabel = new JLabel(localeString("urlsLabel") + ":");
    JButton saveButton = new JButton(localeString("saveButton"));
    JButton closeButton = new JButton(localeString("closeButton"));

    public void init() {
	modified = false;
	model.clear();
	cutElements.clear();
	try {
	    for (String nm: userPrefs.childrenNames()) {
		Preferences pref = userPrefs.node(nm);
		String url = pref.get("url", null);
		String name = pref.get("name", url);
		// System.out.println("loading " + url +" " + name);
		if (url != null) {
		    try {
			URL u = new URL(url);
			processURL(u, name);
		    } catch (Exception e) {
			ErrorMessage.display(e);
		    }
		    model.addElement(url);
		}
	    }
	} catch (BackingStoreException e) {
	    ErrorMessage.display(e);
	}
    }

    public void init(boolean callOnClosing) {
	init();
	if (callOnClosing) {
	    onClosing(map);
	}
    }

    void save() {
	try {
	    int count = 0;
	    int sz = model.getSize();
	    // userPrefs.sync();
	    for (String nm: userPrefs.childrenNames()) {
		int inm = Integer.parseInt(nm.substring(4));
		if (inm >= sz) {
		    Preferences prefs = userPrefs.node(nm);
		    prefs.removeNode();
		    // System.out.println("removed " +nm);
		}
	    }
	    for (Object obj: model.toArray()) {
		String url = (String) obj;
		String name = map.get(url).getName();
		String nm = "node" + (count++);
		Preferences prefs = 
		    userPrefs.node(nm);
		prefs.put("url", url);
		if (name != null) {
		    prefs.put("name", name);
		} else {
		    if (prefs.get("name", null) != null) {
			prefs.remove("name");
		    }
		}
		//System.out.println("saved " + nm + ": " + url + " - " + name);
	    }
	    userPrefs.flush();
	    modified = false;
	} catch (BackingStoreException e) {
	}
    }

    ArrayList<Object> cutElements = new ArrayList<Object>(512);

    static File currentDir = new File(System.getProperty("user.dir"));

    static String proto = 
	"http://CompanyOrOrganization.com/" + 
	"theSampleLayoutNamexxxxxxxxyyyyyyyy.xml";

    ActionListener closeActionListener = new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if (modified) {
		    // System.out.println("modified == true");
		    /*
		    int status = JOptionPane.showConfirmDialog
			(LayoutPane.this, localeString("closeQuestion"),
			 localeString("closeQuestionTitle"),
			 JOptionPane.YES_NO_OPTION,
			 JOptionPane.QUESTION_MESSAGE);
		    */
		    int status = JOptionPane.showConfirmDialog
			(frame, localeString("closeQuestion"),
			 localeString("closeQuestionTitle"),
			 JOptionPane.YES_NO_OPTION,
			 JOptionPane.QUESTION_MESSAGE);

		    // System.out.println("status = " + status);
		    if (status == 0) {
			save();
		    }
		} else {
		    // System.out.println("modified == false");
		}
		deleteCutActionListener.actionPerformed(null);
		onClosing(map);
	    }
	};

    public void addActionListener(ActionListener al) {
	closeButton.addActionListener(al);
    }
    public void removeActionListener(ActionListener al) {
	closeButton.removeActionListener(al);
    }


    private ActionListener deleteCutActionListener = new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		for (Object obj: cutElements) {
		    String url = (String) obj;
		    map.remove(url);
		}
		cutElements.clear();
		pasteBeforeSelectionButton.setEnabled(false);
		pasteAfterSelectionButton.setEnabled(false);
		cutSelectionButton.setToolTipText
		    (localeString("cutSelectionButtonToolTip1"));
		deleteCutButton.setEnabled(false);
	    }
	};

    Component frame;

    public void setFrame(JFrame f) {
	frame = f;
    }

    public LayoutPane() {
	jlist = new JList<Object>(model) {
		public String getToolTipText(MouseEvent event) {
		    int ind = locationToIndex(event.getPoint());
		    if (ind != -1) {
			/*
			System.out.println("getting tooltip for \"" 
					   + (String)model.getElementAt(ind)
					   + "\"" );
			System.out.println("... tip should be "
					   +map.get((String)
						    model.getElementAt(ind)));
			*/
			return map.get
			    ((String)model.getElementAt(ind)).toString();
		    } else {
			return super.getToolTipText();
		    }
		}
	    };
	jlist.setPrototypeCellValue(proto);
	jlist.setVisibleRowCount(15);

	th = new URLListTransferHandler(jlist, icurrentDir) {
		public void processURL(URL url) {
		    LayoutPane.this.processURL(url, null);
		}
		public void assertModified() {
		    modified = true;
		}
	    };

	jlist.setTransferHandler(th);
	jlist.setDragEnabled(true);
	jlist.setDropMode(DropMode.INSERT);

	jlist.setToolTipText(localeString("jlistToolTip"));

	setTransferHandler(th);

	addURLTF.addCaretListener(new CaretListener() {
		public void caretUpdate(CaretEvent e) {
		    int length = addURLTF.getText().trim().length();
		    if (length != oldlength) {
			if (length == 0) {
			    chooseAddButton.setText
				(localeString("chooseAddButtonNoURL"));
			} else {
			    chooseAddButton.setText
				(localeString("chooseAddButtonURL"));
			}
			oldlength = length;
		    }
		}
	    });

	jlist.addListSelectionListener(new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
		    boolean value = !jlist.isSelectionEmpty();
		    cutSelectionButton.setEnabled(value);
		    value = !cutElements.isEmpty() && value;
		    pasteBeforeSelectionButton.setEnabled(value);
		    pasteAfterSelectionButton.setEnabled(value);
		}
	    });

	chooseAddButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (addURLTF.getText().trim().length() == 0) {
			JFileChooser fc = new JFileChooser(currentDir);
			for (javax.swing.filechooser.FileFilter f: 
				 fc.getChoosableFileFilters()) {
			    fc.removeChoosableFileFilter(f);
			}
			FileNameExtensionFilter xmlFilter =
			    new FileNameExtensionFilter("webnail layout xml", 
							"xml", "wlo");
			fc.addChoosableFileFilter(xmlFilter);
			fc.setFileFilter(xmlFilter);
			fc.setMultiSelectionEnabled(true);
			fc.setApproveButtonText(localeString("addFiles"));
			int status = fc.showOpenDialog(chooseAddButton);
			if (status == JFileChooser.APPROVE_OPTION) {
			    for (File ofile: fc.getSelectedFiles()) {
				try {
				    URL url = ofile.toURI().toURL();
				    URLConnection c = url.openConnection();
				    String ct = c.getContentType();
				    boolean ok = true;
				    if (ct != null) {
					if ((!ct.equals
					     (Webnail.XML_MIME_TYPE)) 
					    &&(!ct.equals
					       (Webnail
						.WEBNAIL_LAYOUT_XML_MIME_TYPE))
					    && (!ct.equals
						(Webnail
						 .ALT_XML_MIME_TYPE))) {
					    if (!(ct.equals
						  (Webnail.GENERIC_MIME_TYPE)
						  ||
						  ct.equals
						  (Webnail.BOGUS_MIME_TYPE))) {
						throw new
						    Exception
						    (String.format
						     (localeString
						      ("notWebnailFile"),
						      url.toString(), ct));
					    } else {
						ok =
						    (0 == JOptionPane
						     .showConfirmDialog
						     (chooseAddButton,
						      String.format
						      (localeString
						       ("acceptInput"),
						       ct),
						      localeString
						      ("unrecognized"
						       +"MIMETypeTitle"),
						      JOptionPane.
						      OK_CANCEL_OPTION,
						      JOptionPane
						      .QUESTION_MESSAGE));
					    }
					}
				    } else {
					InputStream is = c.getInputStream();
					ct = c.guessContentTypeFromStream(is);
					is.close();
					if (ct == null) {
					    ct = "application/octet-stream";
					}
					if ((!ct.equals(Webnail.XML_MIME_TYPE))
					    &&
					    (!ct.equals
					     (Webnail.
					      WEBNAIL_LAYOUT_XML_MIME_TYPE))
					    &&
					    (!ct.equals
					     (Webnail.ALT_XML_MIME_TYPE))) {
					    if (!(ct.equals
						  (Webnail.GENERIC_MIME_TYPE)
						  ||
						  ct.equals
						  (Webnail.BOGUS_MIME_TYPE))
						) {
						throw new Exception
						    (String.format
						     (localeString
						      ("notWebnailFile"),
						      url.toString(), ct));
					    } else {
						ok =
						    (0 == JOptionPane
						     .showConfirmDialog
						     (chooseAddButton,
						      String.format
						      (localeString
						       ("acceptInput"), ct),
						      localeString
						      ("unrecognized"
						       +"MIMETypeTitle"),
						      JOptionPane.
						      OK_CANCEL_OPTION,
						      JOptionPane.
						      QUESTION_MESSAGE));
					    }
					}
				    }
				    if (ok) {
					LayoutParms parms = lp.parse(url);
					modified = true;
					model.addElement(url.toString());
					map.put(url.toString(), parms);
				    }
				} catch (Exception e2) {
				    ErrorMessage.display(e2);
				} finally {
				}
			    }
			    ErrorMessage.displayConsoleIfNeeded();
			    return;
			} else if (status == JFileChooser.CANCEL_OPTION) {
			    return;
			} else {
			    // error - should not happen.
			    return;
			}
		    } else {
			String urlstr = addURLTF.getText().trim();
			try {
			    URL url = new URL(urlstr);
			    URLConnection c = url.openConnection();
			    String ct = c.getContentType();
			    boolean ok = true;
			    if (ct != null) {
				if ((!ct.equals(Webnail.XML_MIME_TYPE)) &&
				    (!ct.equals(Webnail
						.WEBNAIL_XML_MIME_TYPE)) &&
				    (!ct.equals(Webnail.ALT_XML_MIME_TYPE))) {
				    if (!(ct.equals(Webnail.GENERIC_MIME_TYPE)
					  ||
					  ct.equals(Webnail.BOGUS_MIME_TYPE))) {
					throw new 
					    Exception(String.format
						      (localeString
						       ("notWebnailFile"),
						       url.toString(), ct));
				    } else {
					ok =
					    (0 == JOptionPane.showConfirmDialog
					     (chooseAddButton, String.format
					      (localeString("acceptInput"), ct),
					      localeString
					      ("unrecognizedMIMETypeTitle"),
					      JOptionPane.OK_CANCEL_OPTION,
					      JOptionPane.QUESTION_MESSAGE));
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
					  ct.equals(Webnail.BOGUS_MIME_TYPE))) {
					throw new Exception
					    (String.format
					     (localeString("notWebnailFile"),
					      url.toString(), ct));
				    } else {
					ok =
					    (0 == JOptionPane.showConfirmDialog
					     (chooseAddButton, String.format
					      (localeString("acceptInput"), ct),
					      localeString
					      ("unrecognizedMIMETypeTitle"),
					      JOptionPane.OK_CANCEL_OPTION,
					      JOptionPane.QUESTION_MESSAGE));
				    }
				}
			    }
			    if (ok) {
				modified = true;
				model.addElement(url.toString());
				LayoutParms parms = lp.parse(url);
				map.put(url.toString(), parms);
				// InputStream is = url.openStream();
				// load(null, is);
			    }
			} catch (Exception e1) {
			    ErrorMessage.display(e1.getClass().toString()
						 + ": " 
						 + e1.getMessage());
			} finally {
			    ErrorMessage.displayConsoleIfNeeded();
			    /*
			    if (console.hasNewTextToDisplay()) {
				showConsole();
			    }
			    */
			}
		    }
		}
	    });

	cutSelectionButton.setEnabled(false);
	cutSelectionButton.setToolTipText
	    (localeString("cutSelectionButtonToolTip1"));
	cutSelectionButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (!jlist.isSelectionEmpty()) {
			int[] indices = jlist.getSelectedIndices();
			Object[] elements =
			    jlist.getSelectedValuesList().toArray();
			for (Object obj: elements) {
			    cutElements.add(obj);
			}
			jlist.clearSelection();
			DefaultListModel model = (DefaultListModel)
			    (jlist.getModel());
			for (int i = indices.length - 1; i >= 0; i--) {
			    modified = true;
			    model.remove(indices[i]);
			}
			cutSelectionButton.setToolTipText
			    (localeString ("cutSelectionButtonToolTip2"));
			boolean value = (model.getSize() == 0);
			pasteBeforeSelectionButton.setEnabled(value);
			pasteAfterSelectionButton.setEnabled(value);
			deleteCutButton.setEnabled(true);
		    }
		}
	    });


	deleteCutButton.setEnabled(false);
	deleteCutButton.setToolTipText(localeString("deleteCutButtonToolTip"));
	deleteCutButton.addActionListener(deleteCutActionListener);

	
	pasteBeforeSelectionButton.setEnabled(false);
	pasteBeforeSelectionButton.setToolTipText
	    (localeString("pasteBeforeSelectionButtonToolTip"));

	pasteBeforeSelectionButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    int index = jlist.getSelectedIndex();
		    jlist.clearSelection();
		    if (index != -1) {
			int sz = cutElements.size();
			while (sz > 0) {
			    sz--;
			    Object object = cutElements.remove(sz);
			    modified = true;
			    model.add(index, object);
			}
			cutElements.clear();
			pasteBeforeSelectionButton.setEnabled(false);
			pasteAfterSelectionButton.setEnabled(false);
			cutSelectionButton.setToolTipText
			    (localeString("cutSelectionButtonToolTip1"));
			deleteCutButton.setEnabled(false);
		    } else if (model.getSize() == 0) {
			int sz = cutElements.size();
			while (sz > 0) {
			    sz--;
			    Object object = cutElements.remove(0);
			    modified = true;
			    model.addElement(object);
			}
			cutElements.clear();
			pasteBeforeSelectionButton.setEnabled(false);
			pasteAfterSelectionButton.setEnabled(false);
			cutSelectionButton.setToolTipText
			    (localeString("cutSelectionButtonToolTip1"));
			deleteCutButton.setEnabled(false);
			
		    }
		}
	    });

	pasteAfterSelectionButton.setEnabled(false);
	pasteAfterSelectionButton.setToolTipText
	    (localeString("pasteAfterSelectionButtonToolTip"));
	pasteAfterSelectionButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    int index = jlist.getSelectedIndex();
		    if (!cutElements.isEmpty() && index != -1) {
			jlist.clearSelection();
			index++;
			if (index == model.getSize()) {
			    for (Object obj: cutElements) {
				modified = true;
				model.addElement(obj);
			    }
			} else {
			    int sz = cutElements.size();
			    while (sz > 0) {
				sz--;
				Object object = cutElements.remove(sz);
				modified = true;
				model.add(index, object);
			    }
			}
			cutElements.clear();
			pasteBeforeSelectionButton.setEnabled(false);
			pasteAfterSelectionButton.setEnabled(false);
			cutSelectionButton.setToolTipText
			    (localeString("cutSelectionButtonToolTip1"));
			deleteCutButton.setEnabled(false);
		    } else if (!cutElements.isEmpty() && model.getSize() == 0) {
			for (Object obj: cutElements) {
			    modified = true;
			    model.addElement(obj);
			}
			cutElements.clear();
			pasteBeforeSelectionButton.setEnabled(false);
			pasteAfterSelectionButton.setEnabled(false);
			cutSelectionButton.setToolTipText
			    (localeString("cutSelectionButtonToolTip1"));
		    }
		}
	    });

	

	scrollPane  = new JScrollPane(jlist);
	// scrollPane.setViewportBorder(null);
	scrollPane.setHorizontalScrollBarPolicy
	    (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
	scrollPane.setVerticalScrollBarPolicy
	    (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();

	c.gridwidth = GridBagConstraints.REMAINDER;
	c.insets = new Insets(2, 2, 2, 2);

	GridBagConstraints cc = new GridBagConstraints();
	cc.gridwidth = GridBagConstraints.REMAINDER;
	cc.anchor = GridBagConstraints.LINE_START;
	cc.insets = new Insets(3, 3, 3, 3);

	GridBagConstraints ccc = new GridBagConstraints();
	ccc.gridwidth = 1;
	ccc.anchor = GridBagConstraints.LINE_START;
	ccc.insets = new Insets(3, 3, 3, 3);

	GridBagConstraints cccc = new GridBagConstraints();
	cccc.gridwidth = 1;
	cccc.anchor = GridBagConstraints.FIRST_LINE_START;
	cccc.insets = new Insets(3, 3, 3, 3);
	ccc.weightx = 0;
	setLayout(gridbag);

	
	gridbag.setConstraints(urlLabel, cc);
	add(urlLabel);

	gridbag.setConstraints(addURLTF, ccc);
	add(addURLTF);
	gridbag.setConstraints(chooseAddButton, cc);
	add(chooseAddButton);
	
	JPanel topPanel = new JPanel();
	FlowLayout tfl =new FlowLayout(FlowLayout.LEADING);
	topPanel.setLayout(tfl);

	topPanel.add(cutSelectionButton, ccc);
	topPanel.add(pasteBeforeSelectionButton, ccc);
	topPanel.add(pasteAfterSelectionButton, ccc);
	topPanel.add(deleteCutButton, cc);
	add(topPanel, c);
	gridbag.setConstraints(scrollPane, c);
	add(scrollPane);



	saveButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    save();
		}
	    });

	closeButton.addActionListener(closeActionListener);

	JPanel bottomPanel = new JPanel();
	FlowLayout bfl =new FlowLayout(FlowLayout.LEADING);
	bottomPanel.setLayout(bfl);
	bottomPanel.add(saveButton);
	bottomPanel.add(closeButton);
	add(bottomPanel, c);
    }

    private WindowAdapter onClosingWindowAdapter = new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		deleteCutActionListener.actionPerformed(null);
		onClosing(map);
	    }
	};

    public WindowListener getOnClosingWindowListener() {
	return onClosingWindowAdapter;
    }


    static public void main(String argv[]) {
	try {
	    LayoutPane pane = new LayoutPane() {
		    protected void onClosing(Map<String,LayoutParms> map) {
			for (Map.Entry<String,LayoutParms> entry: 
				 map.entrySet()) {
			    System.out.println(entry.getKey() +" - "
					       +entry.getValue());
			}
		    }
		};
	    pane.init();
	    final JFrame frame = new JFrame("LayoutPane Test");
	    Authenticator.setDefault
		(AuthenticationPane.getAuthenticator(frame));

	    Container fpane = frame.getContentPane();
	    frame.addWindowListener(pane.getOnClosingWindowListener());

	    pane.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			// System.out.println("main action listener");
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				    frame.dispose();
				}
			    });
			}
		});

	    frame.addWindowListener(new WindowAdapter () {
		    public void windowClosing(WindowEvent e) {
			// System.out.println("closing");
			// need to delay this so that the LayoutPane's
			// listener will have a chance to do something.
			// once frame.displose is called, an event
			// closing the window will be posted, so we want
			// to be safe about ordering the events.
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				    frame.dispose();
				}
			    });
		    }
		    public void windowClosed(WindowEvent e) {
			// System.out.println("closed");
			System.exit(0);
		    }
		});
	    fpane.setLayout(new FlowLayout());
	    fpane.add(pane);
	    frame.pack();
	    frame.setVisible(true);
	
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
