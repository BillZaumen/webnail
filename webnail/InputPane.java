package webnail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;

import java.awt.dnd.*;
import java.awt.datatransfer.*;

import org.bzdev.swing.ErrorMessage;
import org.bzdev.imageio.ImageMimeInfo;

public class InputPane extends JComponent {
    
    static private final String resourceBundleName = "webnail/InputPane";
    static ResourceBundle bundle = 
	ResourceBundle.getBundle(resourceBundleName);
    static String localeString(String name) {
	return bundle.getString(name);
    }

    JRadioButton dndInputRB = new JRadioButton(localeString("dndInputRB"));
    JRadioButton fileInputRB = 
	new JRadioButton(localeString("fileInputRB"));
    JRadioButton urlInputRB = new JRadioButton(localeString("urlInputRB"));

    ButtonGroup inputbg = new ButtonGroup();

    JTextField inputTextField1 = new JTextField(40);
    JButton inputChooseButton1 = new JButton(localeString("choose"));

    JLabel dndLabel = new 
	JLabel(localeString("dndInputLabelText"),
	       new ImageIcon(ClassLoader.getSystemClassLoader().getResource
			     (localeString("dndInputLabelIcon"))),
	       SwingConstants.CENTER);
    JButton inputPasteButton = 
	new JButton(localeString("inputPasteButton"));

    JTextField inputTextField2 = new JTextField(40);
    JButton inputChooseButton2 = new JButton(localeString("choose"));
    JButton addButton = new JButton(localeString("addImages"));
    JLabel scalingStatus =  null; 

    boolean disabledByAddButton = false;
    // want to disable addButton if there is nothing to add.
    CaretListener caretListener = new CaretListener() {
	    public void caretUpdate(CaretEvent e) {
		switch (smode) {
		case SINGLE:
		    addButton.setEnabled(!dndInput && 
					 inputTextField1.getText().length()!=0);
		    break;
		case MULTI:
		    addButton.setEnabled(!dndInput && 
					 inputTextField2.getText().length()!=0);
		    break;
		}
		disabledByAddButton = false;
		
	    }
	};

    public void setBackground(Color c) {
	topPanel.setBackground(c);
	cardPanel.setBackground(c);
	inputConfPanel.setBackground(c);
	singleSelectionPanel.setBackground(c);
	dndPanel.setBackground(c);
	addButtonPanel.setBackground(c);
	multiSelectionPanel.setBackground(c);
	dndInputRB.setBackground(c);
	fileInputRB.setBackground(c);
	urlInputRB.setBackground(c);
}

    JPanel topPanel = new JPanel();

    JPanel cardPanel = new JPanel();
    CardLayout cl = new CardLayout();
    
    JPanel inputConfPanel = new JPanel();
    JPanel singleSelectionPanel = new JPanel();
    JPanel dndPanel = new JPanel();
    JPanel multiSelectionPanel = new JPanel();
    JPanel addButtonPanel = new JPanel();


    public enum SelectionMode {SINGLE, MULTI};

    boolean fileInput = false;
    boolean dndInput = true;
    SelectionMode smode = SelectionMode.SINGLE;

    public void setSelectionMode(SelectionMode mode) {
	smode = mode;
	switch (mode) {
	case SINGLE:
	    addButton.setEnabled(!dndInput &&  !disabledByAddButton
				 && inputTextField1.getText().length() != 0);
	    cl.first(cardPanel);
	    if (dndInput) cl.next(cardPanel);
	    inputChooser.setMultiSelectionEnabled(false);
	    inputChooser.setApproveButtonText(localeString("selectImage"));
	    inputChooser.setDialogTitle(localeString("inputFileName"));
	    inputChooseButton1.setEnabled(fileInput);
	    addButton.setText(localeString("selectImage"));
	    addButton.setToolTipText(localeString("selectImageToolTip"));
	    if (fileInput) {
		inputTextField1.setToolTipText
		    (localeString("inputTextField1ToolTip"));
	    } else {
		inputTextField1.setToolTipText
		    (localeString("inputTextField1URLToolTip"));
	    }
	    break;
	case MULTI:
	    // inputFile = null;
	    // url = null;
	    inputChooser.setMultiSelectionEnabled(true);
	    addButton.setEnabled(!dndInput &&  !disabledByAddButton
				 && inputTextField2.getText().length() != 0);
	    cl.last(cardPanel);
	    if (dndInput) { 
		cl.previous(cardPanel);
	    }
	    if (fileInput) {
		inputChooser.setApproveButtonText(localeString("addImage"));
		addButton.setText(localeString("addImage"));
		addButton.setToolTipText(localeString("addImageToolTip"));
		inputTextField2.setToolTipText
		    (localeString("inputTextField2ToolTip"));
	    } else {
		inputChooser.setApproveButtonText(localeString("addImages"));
		addButton.setText(localeString("addImages"));
		addButton.setToolTipText(localeString("addImagesToolTip"));
		inputTextField2.setToolTipText
		    (localeString("inputTextField2URLToolTip"));
	    }
	    inputChooser.setDialogTitle(localeString("inputFileNames"));
	    inputChooseButton2.setEnabled(fileInput);
	    break;
	}
    }

    File currentDir = new File(System.getProperty("user.dir"));
    File icurrentDir = currentDir;
    JFileChooser inputChooser = new JFileChooser(icurrentDir);

    // File inputFile = null;
    // URL inputURL = null;

    protected void setFile(File file) {}
    protected void setURL (URL url){}

    protected void addFile(File file) {};
    protected void addURL(URL url) {}

    void chooseFile() {
	String label;
	switch (smode) {
	case SINGLE:
	    label = localeString("selectImage");
	    break;
	case MULTI:
	    label = localeString("addImages");
	    break;
	default:
	    label = null;	// never happens (just suppresses a warning)
	}

	if (inputChooser.showDialog(this,label)==JFileChooser.APPROVE_OPTION) {
	    switch(smode) {
	    case SINGLE:
		File inputFile = inputChooser.getSelectedFile();
		setFile(inputFile);
		inputTextField1.setText(inputFile.getAbsolutePath());
		break;
	    case MULTI:
		for (File f: inputChooser.getSelectedFiles()) {
		    addFile(f);
		}
		break;
	    }
	}
    }

    static String[] extensions = ImageMimeInfo.getAllExt();
    static HashSet<String> extSet = new HashSet<String>(2 * extensions.length);
    
    boolean checkExtension(String ext) {
	return extSet.contains(ext);
    }

    ActionListener pasteActionListener = new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		try {
		    Clipboard clipboard = 
			Toolkit.getDefaultToolkit().getSystemClipboard();
		    Transferable t = clipboard.getContents(InputPane.this);
		    // System.out.println(t.toString());
		    /*
		    for (DataFlavor df: t.getTransferDataFlavors()) {
			if (df.equals(plainTextStringDataFlavor)) {
			    System.out.println(t.getTransferData
					       (plainTextStringDataFlavor));
			}
		    }
		    */
		    TransferHandler h = getTransferHandler();
		    TransferHandler.TransferSupport support = new
			TransferHandler.TransferSupport(InputPane.this, t);
		    if (h.canImport(support)) {
			// System.out.println("trying to import");
			h.importData(support);
		    }
		} catch (Exception ex) {
		    ErrorMessage.display(ex);
		}
	    }

	};

    public ActionListener getPasteActionListener() {
	return pasteActionListener;
    }

    public InputPane(final JList list) {

	inputbg.add(dndInputRB);
	inputbg.add(fileInputRB);
	inputbg.add(urlInputRB);
	
	dndInputRB.setToolTipText(localeString("dndInputRBToolTip"));
	fileInputRB.setToolTipText(localeString("fileInputRBToolTip"));
	urlInputRB.setToolTipText(localeString("urlInputRBToolTip"));

	ActionListener fileUrlActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    fileInput = fileInputRB.isSelected();
		    dndInput = dndInputRB.isSelected();
		    // System.out.println(fileInput +" " + dndInput);
		    setSelectionMode(smode);
		}
	    };
	dndInputRB.addActionListener(fileUrlActionListener);
	fileInputRB.addActionListener(fileUrlActionListener);
	urlInputRB.addActionListener(fileUrlActionListener);
	dndInputRB.setSelected(true);

	if (list != null) {
	    scalingStatus = new JLabel(localeString("noImagesToScale"));
	    scalingStatus.setEnabled(false);
	    list.getModel().addListDataListener(new ListDataListener() {
		    public void handleEnabled() {
			if (ImageMapElement.getOutstandingCount() == 0) {
			    list.setEnabled(true);
			    scalingStatus.setText
				(localeString("scalingComplete"));
			    scalingStatus.setEnabled(false);
			    if (ImageMapElement.getErrCount() > 0)
				ErrorMessage.displayConsoleIfNeeded();
			} else {
			    list.setEnabled(false);
			    scalingStatus.setEnabled(true);
			    scalingStatus.setText
				(ImageMapElement.getProgressString());
			    // System.out.println("busy");
			}
		    }
		    public void contentsChanged(ListDataEvent e) {
			handleEnabled();
		    }
		    public void intervalAdded(ListDataEvent e) {
			handleEnabled();
		    }
		    public void intervalRemoved(ListDataEvent e) {
			handleEnabled();
		    }
		});
	}


	addButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (fileInput) {
			String name;
			File f;
			switch(smode) {
			case SINGLE:
			    name = inputTextField1.getText().trim();
			    if (name.equals("")) break;
			    f = new File(name);
			    if (f.isAbsolute()) {
				setFile(f);
			    } else {
				setFile(new File(icurrentDir, name));
			    }
			    break;
			case MULTI:
			    name = inputTextField2.getText().trim();
			    if (name.equals("")) break;
			    f = new File(name);
			    if (f.isAbsolute()) {
				addFile(f);
			    } else {
				addFile (new File(icurrentDir, name));
			    }
			    break;
			}
		    } else {
			String name;
			String[] names;
			try {
			    switch(smode) {
			    case SINGLE:
				name = inputTextField1.getText().trim();
				if (name.length() > 0) {
				    names = name.split("\\s+");
				    if (names.length == 1) {
					URL inputURL = new URL(name);
					setURL(inputURL);
				    }
				}
				break;
			    case MULTI:
				name = inputTextField2.getText().trim();
				if (name.length() > 0) {
				    names = name.split("\\s+");
				    for (String n: names) {
					addURL(new URL(n));
				    }
				}
				break;
			    }
			} catch(MalformedURLException em) {
			    ErrorMessage.display(em);
			}
		    }
 		    addButton.setEnabled(false);
		    disabledByAddButton = true;
		}
	    });

	for (FileFilter f: inputChooser.getChoosableFileFilters()) {
	    inputChooser.removeChoosableFileFilter(f);
	}

	FileNameExtensionFilter imageFilter =
	    new FileNameExtensionFilter("Image Formats",
					ImageMimeInfo.getAllExt());
	// "jpg", "jpeg", "png");

	inputChooser.addChoosableFileFilter(imageFilter);
	for (Map.Entry<String,String[]> entry:
		 ImageMimeInfo.getMimeToSuffixesEntrySet()
		 /*Thumbnail.suffixmap.entrySet()*/) {
	    String mt = entry.getKey();
	    String[] suffixes = entry.getValue();
	    inputChooser.addChoosableFileFilter
		(new FileNameExtensionFilter(mt, suffixes));
	}
	inputChooser.setFileFilter(imageFilter);

	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	c.gridwidth = GridBagConstraints.REMAINDER;
	GridBagConstraints cc = new GridBagConstraints();
	cc.gridwidth = GridBagConstraints.RELATIVE;

	topPanel.setLayout(gridbag);
	
	FlowLayout icpfl = new FlowLayout(FlowLayout.LEADING);
	icpfl.setHgap(10);
	inputConfPanel.setLayout(icpfl);
	inputConfPanel.add(dndInputRB);
	inputConfPanel.add(fileInputRB);
	inputConfPanel.add(urlInputRB);
	
	gridbag.setConstraints(inputConfPanel, c);
	topPanel.add(inputConfPanel);


	FlowLayout sfl = new FlowLayout(FlowLayout.LEADING, 10, 10);
	FlowLayout dfl = new FlowLayout(FlowLayout.CENTER, 10, 10);
	FlowLayout mfl = new FlowLayout(FlowLayout.LEADING, 10, 10);
	singleSelectionPanel.setLayout(sfl);
	dndPanel.setLayout(dfl);
	multiSelectionPanel.setLayout(mfl);

	inputTextField1.setToolTipText(localeString("inputTextField1ToolTip"));
	inputChooseButton1.setToolTipText
	    (localeString("inputChooseButton1ToolTip"));
	singleSelectionPanel.add(inputTextField1);
	singleSelectionPanel.add(inputChooseButton1);

	inputPasteButton.setToolTipText
	    (localeString("inputPasteButtonToolTip"));
	dndPanel.add(dndLabel);
	dndPanel.add(inputPasteButton);

	inputTextField2.setToolTipText(localeString("inputTextField2ToolTip"));
	inputChooseButton2.setToolTipText
	    (localeString("inputChooseButton2ToolTip"));
	multiSelectionPanel.add(inputTextField2);
	multiSelectionPanel.add(inputChooseButton2);

	inputTextField1.addCaretListener(caretListener);
	inputTextField2.addCaretListener(caretListener);

	
	cardPanel.setLayout(cl);
	cardPanel.add(singleSelectionPanel, "first");
	cardPanel.add(dndPanel, "second");
	cardPanel.add(multiSelectionPanel, "third");
	

	gridbag.setConstraints(cardPanel, c);
	topPanel.add(cardPanel);
	addButtonPanel.setLayout(new FlowLayout(FlowLayout.LEADING));

	if (list != null) {
	    addButtonPanel.add(addButton);
	    addButtonPanel.add(scalingStatus);
	} else {
	    addButtonPanel.add(addButton);
	}
	gridbag.setConstraints(addButtonPanel, c);
	topPanel.add(addButtonPanel);

	gridbag = new GridBagLayout();
	setLayout(gridbag);
	gridbag.setConstraints(topPanel,c);
	add(topPanel);

	inputChooseButton1.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    chooseFile();
		}
	    });

	inputChooseButton2.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    chooseFile();
		}
	    });

	inputPasteButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    pasteActionListener.actionPerformed(e);
		}
	    });

	inputChooser.setMultiSelectionEnabled(false);
	inputChooser.setApproveButtonText(localeString("selectImage"));
	inputChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	inputChooser.setDialogTitle(localeString("inputFileName"));
	setSelectionMode(smode);

	setTransferHandler(new FilenameTransfer());
    }

    // drag and drop from a Gnome Nautilus file-browser window provides
    // the following flavor instead of DataFlavor.javaFileListFlavor, so
    // we'll try to support both.

    static DataFlavor uriListDataFlavor;
    static {
	try {
	    uriListDataFlavor = 
		new DataFlavor("text/uri-list; class=java.lang.String");
	} catch (Exception e) {
	    System.err.println
		("cannot create MIME type " +
		 "\"text/uri-list; class=java.lang.String\"");
	    System.exit(1);
	}
    }
    static DataFlavor plainTextStringDataFlavor;
    static {
	try {
	    plainTextStringDataFlavor =
		new DataFlavor("text/plain; class=java.lang.String");
	} catch (Exception e) {
	    System.err.println
		("cannot create MIME type " +
		 "\"text/uri-list; class=java.lang.String\"");
	    System.exit(1);
	}
    }
	

    public class FilenameTransfer extends TransferHandler {
	public boolean importData(JComponent comp, Transferable t) {
	    return importData(new TransferHandler.TransferSupport(comp, t));
	}

	public boolean importData(TransferHandler.TransferSupport support) {
	    Component comp = support.getComponent();
	    Transferable t = support.getTransferable();
	    if (!(comp instanceof InputPane) &&
		!(comp.getParent() instanceof InputPane)) {
		return false;
	    }
	    int mode = -1;
	    if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
		mode = 0;
	    } else if (t.isDataFlavorSupported(uriListDataFlavor)) {
		mode = 1;
	    } else if (t.isDataFlavorSupported(plainTextStringDataFlavor)) {
		mode = 2;
	    } else if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
		mode = 3;
	    } else {
		return false;
	    }
	    // System.out.println("mode = " + mode);
	    try {
		if (mode == 0) {
		    java.util.List flist = (java.util.List)
			t.getTransferData(DataFlavor.javaFileListFlavor);
		    if (smode == SelectionMode.SINGLE) {
			if (flist.size() > 1) {
			    return false;
			} else if (flist.size() == 1) {
			    setFile((File)flist.get(0));
			} else {
			    return false;
			}
		    } else {
			for (Object obj: flist) {
			    File f = (File)obj;
			    addFile(f);
			}
		    }
		} else if (mode == 1) {
		    String[] strings = 
			((String) 
			 t.getTransferData(uriListDataFlavor)).split("\\s+");
		    if (smode == SelectionMode.SINGLE) {
			if (strings.length > 1) {
			    return false;
			} else if (strings.length == 1) {
			    setURL(new URL(strings[0]));
			}
		    } else {
			for (String s: strings) {
			    addURL(new URL(s));
			}
		    }
		} else /* if (mode == 2 || mode == 3) */ {
		    String[] strings = 
			((String) 
			 t.getTransferData(plainTextStringDataFlavor))
			.split("\\n+");
		    if (smode == SelectionMode.SINGLE) {
			if (strings.length > 1) {
			    return false;
			} else if (strings.length == 0) {
			    return false;
			}
		    }
		    for (String s: strings) {
			boolean isURL = false;
			if (s.startsWith("file:") || s.startsWith("ftp:")
			    || s.startsWith("http:")
			    || s.startsWith("https:")) {
			    isURL = true;
			}
			if (isURL) {
			    if (smode == SelectionMode.SINGLE) {
				setURL(new URL(s));
			    } else {
				addURL(new URL(s));
			    }
			} else {
			    if (smode == SelectionMode.SINGLE) {
				File f = new File(s);
				if (f.isAbsolute()) {
				    setFile(f);
				} else {
				    setFile(new File(icurrentDir, s));
				}
			    } else {
				File f = new File(s);
				if (f.isAbsolute()) {
				    addFile(f);
				} else {
				    addFile(new File(icurrentDir, s));
				}
			    }
			}
		    }
		}
		return true;
	    } catch (UnsupportedFlavorException ufe) {
		ErrorMessage.display(ufe);
		// ufe.printStackTrace();
	    } catch (IOException ioe) {
		ErrorMessage.display(ioe);
		// ioe.printStackTrace();
	    }
	    return false;
	}

	public boolean canImport(TransferHandler.TransferSupport support) {
	    DataFlavor[] transferFlavors = support.getDataFlavors();
	    Component comp = support.getComponent();
	    if ((comp instanceof InputPane) ||
		(comp.getParent() instanceof InputPane)) {
		if (!dndInput) return false;
		for (int i = 0; i < transferFlavors.length; i++) {
		    /*
		    System.out.println(i + " " + transferFlavors[i].toString());
		    System.out.println("   ..." +
				       transferFlavors[i].getPrimaryType()
				       +" " +
				       transferFlavors[i].getSubType());
		    */
		    if (transferFlavors[i].
			equals(DataFlavor.javaFileListFlavor) ||
			transferFlavors[i].equals(uriListDataFlavor) ||
			transferFlavors[i].equals(plainTextStringDataFlavor) ||
			transferFlavors[i].equals(DataFlavor.stringFlavor)) {
			return true;
		    } 
		}
		return false;
	    }
	    return false;
	}
    }

    static public void main(String argv[]) {
	InputPane.SelectionMode sm = InputPane.SelectionMode.SINGLE;
	if (argv.length > 0) {
	    if (argv[0].equals("-s")) {
		sm = InputPane.SelectionMode.SINGLE;
	    } else if (argv[0].equals("-m")) {
		sm = InputPane.SelectionMode.MULTI;
	    }
	}
	InputPane ip = new InputPane(null) {
		protected void addFile (File f) {
		    System.out.println(f.toString());
		}
		protected void addURL(URL url) {
		    System.out.println(url.toString());
		}
		protected void setFile (File f) {
		    System.out.println(f.toString());
		}
		protected void setURL(URL url) {
		    System.out.println(url.toString());
		}
	    };

	ip.setSelectionMode(sm);
	JFrame frame = new JFrame("InputPane Test");
        Container fpane = frame.getContentPane();

        frame.addWindowListener(new WindowAdapter () {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
 
        // frame.setSize(500,500);
        fpane.setLayout(new FlowLayout());

	fpane.add(ip);
	JMenuBar menubar = new JMenuBar();
	JMenu editMenu = new JMenu("Edit");
	JMenuItem pasteMenuItem = new JMenuItem("Paste");
	pasteMenuItem.addActionListener(ip.getPasteActionListener());
	editMenu.add(pasteMenuItem);
	menubar.add(editMenu);
	frame.setJMenuBar(menubar);

        // fpane.setVisible(true);
	frame.setSize(600, 400);
        frame.setVisible(true);
    }
}