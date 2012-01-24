package webnail;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import org.bzdev.swing.*;
import org.bzdev.util.TemplateProcessor;
import org.bzdev.util.TemplateProcessor.KeyMap;
import org.bzdev.imageio.ImageMimeInfo;


public class EditImagesPane extends JComponent {

    static private final String resourceBundleName = "webnail/EditImagesPane";
    static ResourceBundle bundle = 
	ResourceBundle.getBundle(resourceBundleName);
    static String localeString(String name) {
	return bundle.getString(name);
    }

    static String[] mtarray =
	new String[ImageMimeInfo.getMimeTypes().size() + 1];
    static {
	int ind = 0;
	mtarray[ind++] = "Default";
	for (String mt: ImageMimeInfo.getMimeTypes()) {
	    mtarray[ind++] = mt;
	}
    }

    static int getMimeTypeIndex(String type) {
	if (type == null) return 0;
	for (int i = 0; i < mtarray.length; i++) {
	    if (type.equals(mtarray[i])) {
		return i;
	    }
	}
	return 0;
    }

    static String[] cbmtarray = mtarray.clone();
    static {
	for (int i = 0; i < mtarray.length; i++) {
	    cbmtarray[i] = localeMTString(mtarray[i]);
	}
    }


    static String localeMTString(String name) {
	String s;
	try {
	    s = bundle.getString(name);
	} catch (MissingResourceException e) {
	    s = null;
	}
	return (s == null)? name: s;
    }


    DefaultListModel model;
    LinkedList<TemplateProcessor.KeyMap> domMapList;

    JPanel panel = new JPanel();
    JPanel subpanel = new JPanel();

    InputPane inputPane;
    InputPane.SelectionMode oldMode = InputPane.SelectionMode.SINGLE;

    public void setSelectionMode(InputPane.SelectionMode mode) {
	if (oldMode != InputPane.SelectionMode.SINGLE && 
	    mode  == InputPane.SelectionMode.SINGLE) {
	    for (int i = model.size(); i > 1; i--) {
		model.remove(i);
	    }
	}
	inputPane.setSelectionMode(mode);
	// imgListTransferHandler.setSelectionMode(mode);
	imgListTransferHandler.setMultiSelectionMode
	    (mode == InputPane.SelectionMode.MULTI);
    }

    boolean limitedMode = false;
    public void setLimitedMode(boolean lmode) {
	limitedMode = lmode;
	setControls();
	// setLimited();
    }
    private void setLimited() {
	if (limitedMode) {
	    descrLabel.setEnabled(false);
	    descrTextArea.setEnabled(false);
	    propertyButton.setEnabled(false);
	    imageTimeLabel.setEnabled(false);
	    imageTimeTF.setEnabled(false);
	    minImageTimeLabel.setEnabled(false);
	    minImageTimeTF.setEnabled(false);
	} else {
	    descrLabel.setEnabled(true);
	    descrTextArea.setEnabled(true);
	    propertyButton.setEnabled(true);
	    imageTimeLabel.setEnabled(true);
	    imageTimeTF.setEnabled(true);
	    minImageTimeLabel.setEnabled(true);
	    minImageTimeTF.setEnabled(true);
	}
    }
   
    private boolean linkMode = true;
    public void setLinkMode (boolean lm) { 
	linkMode = lm;
	setControls();
    }

    private void setLinkModeControls() {
	if (linkMode) {
	    hrefTargetLabel.setEnabled(true);
	    hrefTargetComboBox.setEnabled(true);
	    hrefLabel.setEnabled(true);
	    hrefTextField.setEnabled(true);
	} else {
	    hrefTargetLabel.setEnabled(false);
	    hrefTargetComboBox.setEnabled(false);
	    hrefLabel.setEnabled(false);
	    hrefTextField.setEnabled(false);
	}
    }


    // ReorderableJList rlist;
    JList rlist;
    org.bzdev.swing.ImgListTransferHandler imgListTransferHandler;

    JList getImageList() {return rlist;}

    JScrollPane scrollPane;


    ArrayList<Object> cutElements = new ArrayList<Object>(512);

    JButton deleteCutButton = 
	new JButton(localeString("deleteCutButton"));
    JButton cutImagesButton = new JButton(localeString("cutImagesButton"));
    JButton pasteImagesBeforeButton = 
	new JButton(localeString("pasteImagesBeforeButton"));
    JButton pasteImagesAfterButton = 
	new JButton(localeString("pasteImagesAfterButton"));
    JButton doneButton = new JButton(localeString("doneButton"));

    JLabel titleLabel = new JLabel(localeString("titleLabel") + ":");

    URLTextAreaPane titleTextArea = 
	new URLTextAreaPane(4, 50, Gui.localeString("titleErrorTitle"));
    JScrollPane titleScrollPane = new
	JScrollPane(titleTextArea,
		    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    JLabel descrLabel = new JLabel(localeString("descrLabel") + ":");

    URLTextAreaPane descrTextArea = 
	new URLTextAreaPane(4, 50, Gui.localeString("descrErrorTitle"));
    JScrollPane descrScrollPane = new
	JScrollPane(descrTextArea,
		    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    PropertyButton propertyButton = 
	new PropertyButton(localeString("propertyButton"), this,
			   localeString("editProperties")) {
	    protected void doOutput(LinkedList<TemplateProcessor.KeyMap> list) {
		MapElement element = 
		    (MapElement)EditImagesPane.this.model.get(currentIndex);
		String ns =(String)element.get("nProps");
		int n = (ns == null)? 0: Integer.parseInt(ns);
		int i;
		for (i = 0; i < n; i++) {
		    element.remove("propKey" + i);
		    element.remove("propValue" + i);
		}
		element.remove("nProps");
		i = 0;
		for (TemplateProcessor.KeyMap map: list) {
		    String key = (String) map.get("domKey");
		    String value = (String) map.get("propertyValue");
		    element.put("propKey" + i, key);
		    element.put("propValue" + i, value);
		    i++;
		}
		element.put("nProps", "" + i);
		
	    }
	    protected TemplateProcessor.KeyMap[] getPropertyInput() 
	    {
		MapElement element = 
		    (MapElement)EditImagesPane.this.model.get(currentIndex);
		String ns =(String)element.get("nProps");
		int n = (ns == null)? 0: Integer.parseInt(ns);
		TemplateProcessor.KeyMap[] array =
		    new TemplateProcessor.KeyMap[n];
		for (int i = 0; i < n; i++) {
		    String key = (String) element.get("propKey" + i);
		    String value = (String) element.get("propValue" + i);
		    TemplateProcessor.KeyMap map =
			new TemplateProcessor.KeyMap();
		    map.put("domKey", key);
		    map.put("propertyValue", value);
		    array[i] = map;
		}
		return array;
	    }
	    protected LinkedList<TemplateProcessor.KeyMap> getDomListInput() {
		return domMapList;
	    }
	};
    String[] linkOptions = {
	localeString("linkComboBox0"), // default
	localeString("linkComboBox1"), // no link
	localeString("linkComboBox2"),  // use link
    };
    JComboBox linkComboBox = new JComboBox(linkOptions);
    JLabel mimeTypeLabel = 
	new JLabel(localeString("outputImageMIMEtype") + ":");
    JComboBox mimeTypeComboBox;

    JLabel hrefTargetLabel = new JLabel(localeString("hrefTargetLabel") + ":");
    Object[] targetStrings = {"_blank", "_top"};
    JComboBox hrefTargetComboBox = new JComboBox(targetStrings);
    JLabel hrefLabel = new JLabel(localeString("hrefLabel") + ":");
    JTextField hrefTextField = new JTextField(50);

    JLabel imageTimeLabel = new JLabel(localeString("imageTime") + ":");
    TimeTextField imageTimeTF = new TimeTextField(15) {
	    protected boolean handleError() {
		JOptionPane.showMessageDialog (this,
					       localeString("timeFormatError"),
					       localeString("Error"), 
					       JOptionPane.ERROR_MESSAGE);
		return false;
	    }
	};

    JLabel minImageTimeLabel = new JLabel(localeString("minImageTime") + ":");
    TimeTextField minImageTimeTF = new TimeTextField(15) {
	    protected boolean handleError() {
		JOptionPane.showMessageDialog(this,
					      localeString("timeFormatError"),
					      localeString("Error"),
					      JOptionPane.ERROR_MESSAGE);
		return false;
	    }
	};
    JLabel urlLabel = new JLabel(localeString("URL") + ":");
    JLabel currentURL = new JLabel("");


    private void loadElement(MapElement element) {
	String text = (String)element.get("title");
	String url = (String)element.get("titleURL");
	// System.out.println("text = " +text);
	boolean isInUse = (url != null);
	titleTextArea.init(url, isInUse, ((text == null)? "":text));
	text = (String)element.get("descr");
	url = (String) element.get("descrURL");
	isInUse = (url != null);
	descrTextArea.init(url, isInUse, ((text == null)? "":text));
	String linkMode = (String)element.get("linkMode");
	if (linkMode == null) {
	    linkComboBox.setSelectedIndex(0);
	} else if (linkMode.trim().equals("true")) {
	    linkComboBox.setSelectedIndex(2);
	} else if (linkMode.trim().equals("false")) {
	    linkComboBox.setSelectedIndex(1);
	}
	String mt = (String)element.get("mimeType");
	mimeTypeComboBox.setSelectedIndex(getMimeTypeIndex(mt));
	String target = (String)element.get("hrefTarget");
	if (target == null || target.equals("_blank")) {
	    hrefTargetComboBox.setSelectedIndex(0);
	} else if (target.equals("_top")) {
	    hrefTargetComboBox.setSelectedIndex(1);
	}
	text = (String) element.get("hrefURL");
	hrefTextField.setText((text == null)? "": text);
	text = (String) element.get("imageTime");
	imageTimeTF.setText((text == null)? "": text);
	text = (String) element.get("minImageTime");
	minImageTimeTF.setText((text == null)? "": text);
	text = (String)element.get("url");
	currentURL.setText((text == null)? "": text);
    }

    private void saveElement(MapElement element) {
	String text = titleTextArea.getText();
	// System.out.println("(save) text = " + text);
	String url = null;
	if (titleTextArea.urlInUse()) {
	    url = titleTextArea.getURL().trim();
	    if (url != null || url.length() > 0) {
		element.put("titleURL", url);
	    } else {
		element.remove("titleURL");
	    }
	} else {
	    element.remove("titleURL");
	}
	if (text.length() == 0) {
	    element.remove("title");
	} else {
	    // System.out.println("set title to \"" + text + "\"");
	    element.put("title", text);
	}
	text = descrTextArea.getText();
	url = null;
	if (descrTextArea.urlInUse()) {
	    url = descrTextArea.getURL().trim();
	    if (url != null || url.length() > 0) {
		element.put("descrURL", url);
	    } else {
		element.remove("descrURL");
	    }
	} else {
	    element.remove("descrURL");
	}
	if (text.length() == 0) {
	    element.remove("descr");
	} else {
	    element.put("descr", text);
	}
	int ind = linkComboBox.getSelectedIndex();
	if (ind == 0) {
	    element.remove("linkMode");
	} else if (ind == 1) {
	    element.put("linkMode", "false");
	} else if (ind == 2) {
	    element.put("linkMode", "true");
	}
	ind = mimeTypeComboBox.getSelectedIndex();
	if (ind == 0) {
	    element.remove("mimeType");
	} else {
	    element.put("mimeType", mtarray[ind]);
	}
	ind = hrefTargetComboBox.getSelectedIndex();
	element.put("hrefTarget", ((ind == 0)? "_blank": "_top"));
	text = hrefTextField.getText().trim();
	if (text.length() == 0) {
	    element.remove("hrefURL");
	} else {
	    element.put("hrefURL", text);
	}
	text = imageTimeTF.getText();
	if (text.length() == 0) {
	    element.remove("imageTime");
	} else {
	    element.put("imageTime", text);
	}
	text = minImageTimeTF.getText();
	if (text.length() == 0) {
	    element.remove("minImageTime");
	} else {
	    element.put("minImageTime", text);
	}
    }

    int currentIndex = -1;
    MapElement currentElement = null;

    boolean webpageMode = false;

    public void setWebpageMode(boolean mode) {
	webpageMode = mode;
	setControls();
    }

    void blankControls() {
		titleLabel.setEnabled(false);
		titleTextArea.init(null, false, "");
		titleTextArea.setEnabled(false);
		descrLabel.setEnabled(false);
		descrTextArea.init(null, false, "");
		descrTextArea.setEnabled(false);
		linkComboBox.setSelectedItem(linkOptions[0]);
		linkComboBox.setEnabled(false);
		mimeTypeLabel.setEnabled(false);
		mimeTypeComboBox.setSelectedIndex(0);
		mimeTypeComboBox.setEnabled(false);
		hrefTargetLabel.setEnabled(false);
		hrefTargetComboBox.setSelectedIndex(0);
		hrefTargetComboBox.setEnabled(false);
		hrefLabel.setEnabled(false);
		hrefTextField.setText("");
		hrefTextField.setEnabled(false);
		imageTimeLabel.setEnabled(false);
		imageTimeTF.setEnabled(false);
		imageTimeTF.setText("");
		minImageTimeLabel.setEnabled(false);
		minImageTimeTF.setEnabled(false);
		minImageTimeTF.setText("");
		urlLabel.setEnabled(false);
		currentURL.setEnabled(false);
		currentURL.setText("");
		propertyButton.setEnabled(false);
    }
    
    void setControls() {
	if (rlist.isSelectionEmpty()) {
	    if (currentIndex == -1) {
		blankControls();
	    } else {
		saveElement(currentElement);
		currentIndex = -1;
		currentElement = null;
		blankControls();
	    }
	} else {
	    int newIndex = rlist.getMinSelectionIndex();
	    if (newIndex != rlist.getMaxSelectionIndex()) {
		if (currentIndex != -1) {
		    saveElement(currentElement);
		    currentIndex = -1;
		    currentElement = null;
		    blankControls();
		}
	    } else if (currentIndex == -1) {
		if (webpageMode) {
		    currentElement = (MapElement)model.get(newIndex);
		    currentIndex = newIndex;
		    loadElement(currentElement);
		    titleLabel.setEnabled(true);
		    titleTextArea.setEnabled(true);
		    // descrLabel.setEnabled(true); - done in setLimited()
		    // descrTextArea.setEnabled(true); - done in setLimited()
		    linkComboBox.setEnabled(true);
		    mimeTypeLabel.setEnabled(true);
		    mimeTypeComboBox.setEnabled(true);
		    hrefTargetLabel.setEnabled(true);
		    hrefLabel.setEnabled(true);
		    hrefTargetComboBox.setEnabled(true);
		    hrefTextField.setEnabled(true);
		    imageTimeLabel.setEnabled(true);
		    imageTimeTF.setEnabled(true);
		    minImageTimeLabel.setEnabled(true);
		    minImageTimeTF.setEnabled(true);
		    urlLabel.setEnabled(true);
		    currentURL.setEnabled(true);
		    // propertyButton.setEnabled(true); - done in setLimited()
		    setLimited();
		    setLinkModeControls();
		} else {
		    blankControls();
		}
	    } else if (currentIndex == newIndex) {
		if (webpageMode) {
		    setLimited();
		    setLinkModeControls();
		} else {
		    blankControls();
		}
	    } else {
		saveElement(currentElement);
		currentElement = (MapElement)model.get(newIndex);
		currentIndex = newIndex;
		loadElement(currentElement);
	    }
	}
    }

    private void onClosing() {
	rlist.clearSelection();
    }

    ActionListener doneActionListener = new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		onClosing();
	    }
	};

    public void addActionListener(ActionListener al) {
	doneButton.addActionListener(al);
    }
    public void removeActionListener(ActionListener al) {
	doneButton.removeActionListener(al);
    }

    private WindowAdapter onClosingWindowAdapter = new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		onClosing();
	    }
	};

    public WindowListener getOnClosingWindowListener() {
	return onClosingWindowAdapter;
    }


    public EditImagesPane(DefaultListModel theModel, 
			  LinkedList<TemplateProcessor.KeyMap> dml) {
	model = theModel;
	domMapList = dml;
	//rlist = new ReorderableJList(model);
	rlist = new JList(model);

	inputPane = new InputPane(rlist) {
		protected void addFile (File f) {
		    try {
			URL url = f.toURI().toURL();
			MapElement map = new ImageMapElement(url, model);
		    } catch (MalformedURLException e) {
			// should not happen - the standard Java
			// class library methods are creating the URL.
		    }

		}
		protected void addURL(URL url) {
		    MapElement map = new ImageMapElement(url, model);
		}
		protected void setFile (File f) {
		    model.clear();
		    addFile(f);
			    
		}
		protected void setURL(URL url) {
		    model.clear();
		    addURL(url);
		}
	    };

	/*
	imgListTransferHandler = new 
	    ImgListTransferHandler(this, inputPane, rlist);
	*/
	imgListTransferHandler = new 
	    org.bzdev.swing.ImgListTransferHandler(rlist, 
						   inputPane.icurrentDir) 
	    {
		protected void insertByURL(URL url, 
					   DefaultListModel model,
					   int index)
		    throws Exception
		{
		    new ImageMapElement(url, model, index);
		}
	    };

	rlist.setTransferHandler(imgListTransferHandler);
	rlist.setDragEnabled(true);
	rlist.setDropMode(DropMode.INSERT);

	setLayout(new FlowLayout());
	DefaultListCellRenderer lcr = (DefaultListCellRenderer)
	    rlist.getCellRenderer();
	lcr.setHorizontalAlignment(SwingConstants.CENTER);

	scrollPane  = new JScrollPane(rlist);
	scrollPane.setViewportBorder(null);
	scrollPane.setHorizontalScrollBarPolicy
	    (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	scrollPane.setVerticalScrollBarPolicy
	    (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	Dimension d = scrollPane.getHorizontalScrollBar().getPreferredSize();
	int h = d.height;
	d = scrollPane.getVerticalScrollBar().getPreferredSize();
	int w = d.width;
	rlist.setFixedCellHeight(ImageMapElement.blankImageIcon.getIconHeight()
				 + h);
	rlist.setFixedCellWidth(ImageMapElement.blankImageIcon.getIconWidth()
				+ w);
	rlist.setLayoutOrientation(JList.VERTICAL);
	rlist.setVisibleRowCount(5);

	
	mimeTypeComboBox = new JComboBox(cbmtarray);

	mimeTypeComboBox.setSelectedItem(cbmtarray[0]);
	mimeTypeComboBox.setToolTipText
	    (localeString("mimeTypeComboBoxToolTip"));
	rlist.setToolTipText(localeString("rlistToolTip"));
	rlist.addListSelectionListener(new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
		    boolean value = !rlist.isSelectionEmpty();
		    cutImagesButton.setEnabled(value);
		    value = !cutElements.isEmpty();
		    pasteImagesBeforeButton.setEnabled(value);
		    pasteImagesAfterButton.setEnabled(value);
		    setControls();
		}
	    });

	deleteCutButton.setEnabled(false);
	deleteCutButton.setToolTipText(localeString("deleteCutButtonToolTip"));
	deleteCutButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    cutElements.clear();
		    pasteImagesBeforeButton.setEnabled(false);
		    pasteImagesAfterButton.setEnabled(false);
		    cutImagesButton.setText(localeString("cutImagesButton"));
		    deleteCutButton.setEnabled(false);
		}
	    });

	cutImagesButton.setToolTipText(localeString("cutImagesButtonToolTip"));
	cutImagesButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (!rlist.isSelectionEmpty()) {
			int[] indices = rlist.getSelectedIndices();
			Object[] elements = rlist.getSelectedValues();
			for (Object obj: elements) {
			    cutElements.add(obj);
			}
			rlist.clearSelection();
			for (int i = indices.length - 1; i >= 0; i--) {
			    model.remove(indices[i]);
			}
			cutImagesButton.setText(localeString
						("cutImagesButton1"));
			pasteImagesBeforeButton.setEnabled(false);
			pasteImagesAfterButton.setEnabled(false);
			deleteCutButton.setEnabled(true);
		    }
		}
	    });

	pasteImagesBeforeButton.setToolTipText
	    (localeString("pasteImagesBeforeButtonToolTip"));

	pasteImagesBeforeButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    int index = rlist.getSelectedIndex();
		    rlist.clearSelection();
		    if (index != -1) {
			int sz = cutElements.size();
			while (sz > 0) {
			    sz--;
			    Object object = cutElements.remove(sz);
			    model.add(index, object);
			}
			cutElements.clear();
			pasteImagesBeforeButton.setEnabled(false);
			pasteImagesAfterButton.setEnabled(false);
			cutImagesButton.setText
			    (localeString("cutImagesButton"));
			deleteCutButton.setEnabled(false);
		    }
		}
	    });

	pasteImagesAfterButton.setToolTipText
	    (localeString("pasteImagesAfterButtonToolTip"));
	pasteImagesAfterButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    int index = rlist.getSelectedIndex();
		    if (cutElements != null && index != -1) {
			rlist.clearSelection();
			index++;
			if (index == model.getSize()) {
			    for (Object obj: cutElements) {
				model.addElement(obj);
			    }
			} else {
			    int sz = cutElements.size();
			    while (sz > 0) {
				sz--;
				Object object = cutElements.remove(sz);
				model.add(index, object);
			    }
			}
			cutElements.clear();
			pasteImagesBeforeButton.setEnabled(false);
			pasteImagesAfterButton.setEnabled(false);
			cutImagesButton.setText
			    (localeString("cutImagesButton"));
			deleteCutButton.setEnabled(false);
		    }
		}
	    });

	doneButton.setToolTipText(localeString("doneButtonToolTip"));
	doneButton.addActionListener(doneActionListener);

	pasteImagesBeforeButton.setEnabled(false);
	pasteImagesAfterButton.setEnabled(false);
	cutImagesButton.setEnabled(false);

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

	subpanel.setLayout(gridbag);
	gridbag.setConstraints(inputPane, c);
	subpanel.add(inputPane);

	JPanel controlPanel1 = new JPanel();
	controlPanel1.setLayout(new FlowLayout(FlowLayout.LEADING));
	controlPanel1.add(cutImagesButton);
	controlPanel1.add(deleteCutButton);

	gridbag.setConstraints(controlPanel1, c);
	subpanel.add(controlPanel1);

	JPanel controlPanel2 = new JPanel();
	controlPanel2.setLayout(new FlowLayout(FlowLayout.LEADING));
	controlPanel2.add(pasteImagesBeforeButton);
	controlPanel2.add(pasteImagesAfterButton);

	gridbag.setConstraints(controlPanel2, c);
	subpanel.add(controlPanel2);

	JPanel urlPanel = new JPanel();
	urlPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
	urlPanel.add(urlLabel);
	urlPanel.add(currentURL);
	gridbag.setConstraints(urlPanel, cc);
	subpanel.add(urlPanel);

	JPanel controlPanel3 = new JPanel();
	controlPanel3.setLayout(new FlowLayout(FlowLayout.LEADING));
	controlPanel3.add(linkComboBox);
	controlPanel3.add(mimeTypeLabel);
	controlPanel3.add(mimeTypeComboBox);
	controlPanel3.add(propertyButton);

	gridbag.setConstraints(controlPanel3, c);
	subpanel.add(controlPanel3);


	JPanel timePanel = new JPanel();
	timePanel.setLayout(new FlowLayout(FlowLayout.LEADING));
	timePanel.add(imageTimeLabel);
	timePanel.add(imageTimeTF);
	timePanel.add(minImageTimeLabel);
	timePanel.add(minImageTimeTF);
	gridbag.setConstraints(timePanel,cc);
	subpanel.add(timePanel);

	gridbag.setConstraints(hrefTargetLabel, ccc);
	subpanel.add(hrefTargetLabel);
	
	gridbag.setConstraints(hrefTargetComboBox, cc);
	subpanel.add(hrefTargetComboBox);

	gridbag.setConstraints(hrefLabel, cc);
	subpanel.add(hrefLabel);

	gridbag.setConstraints(hrefTextField, cc);
	subpanel.add(hrefTextField);

	JPanel tdpanel = new JPanel();
	GridBagLayout tdgridbag = new GridBagLayout();
	tdpanel.setLayout(tdgridbag);

	tdgridbag.setConstraints(titleLabel, cccc);
	tdpanel.add(titleLabel);
	tdgridbag.setConstraints(titleScrollPane, cccc);
	tdpanel.add(titleScrollPane);
	JLabel lab1 = new JLabel(" ");
	tdgridbag.setConstraints(lab1, c);
	tdpanel.add(lab1);
	tdgridbag.setConstraints(descrLabel, cccc);
	tdpanel.add(descrLabel);
	tdgridbag.setConstraints(descrScrollPane, cccc);
	tdpanel.add(descrScrollPane);
	JLabel lab2 = new JLabel(" ");
	tdgridbag.setConstraints(lab2, c);
	tdpanel.add(lab2);
	gridbag.setConstraints(tdpanel, cc);
	subpanel.add(tdpanel);
	gridbag.setConstraints(doneButton, c);
	subpanel.add(doneButton);

	descrTextArea.setToolTipText(localeString("descrTextAreaToolTip"));
	titleTextArea.setToolTipText(localeString("titleTextAreaToolTip"));
	imageTimeTF.setToolTipText(localeString("imageTimeTFToolTip"));
	minImageTimeTF.setToolTipText(localeString("minImageTimeTFToolTip"));
	hrefTargetComboBox.setToolTipText
	    (localeString("hrefTargetComboBoxToolTip"));
	hrefTextField.setToolTipText(localeString("hrefTextFieldToolTip"));
	propertyButton.setToolTipText(localeString("propertyButtonToolTip"));
	linkComboBox.setToolTipText(localeString("linkComboBoxToolTip"));
	panel.setLayout(new BorderLayout());
	panel.add(scrollPane, "West");
	panel.add(subpanel, "Center");
	add(panel);
	setControls();
    }


    static public void main(String argv[]) {
	try {
	    DefaultListModel model = new DefaultListModel();
	    new ImageMapElement("file:///home/wtz/Misc/thumb/fleft.gif", model);
	    new ImageMapElement("file:///home/wtz/Misc/thumb/left.gif", model);
	    new ImageMapElement ("file:///home/wtz/Misc/thumb/redo.gif", model);
	    new ImageMapElement("file:///home/wtz/Misc/thumb/right.gif", model);
	    new ImageMapElement("file:///home/wtz/Misc/thumb/fright.gif",model);
	    // System.out.println("model.size() = " + model.size());

	    EditImagesPane pane = new EditImagesPane(model, null);
	    pane.setSelectionMode(InputPane.SelectionMode.MULTI);
	    final JFrame frame = new JFrame("EditImagesPane Test");
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
			// need to delay this so that the EditImagesPane's
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
