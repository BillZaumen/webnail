package webnail;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import org.bzdev.util.TemplateProcessor;
import org.bzdev.util.TemplateProcessor.KeyMap;
import org.bzdev.swing.SwingErrorMessage;
import org.bzdev.swing.ReorderListTransferHandler;

public class DomMapPane extends JComponent {

    static private final String resourceBundleName = "webnail.DomMapPane";
    static ResourceBundle bundle = 
	ResourceBundle.getBundle(resourceBundleName);
    static String localeString(String name) {
	return bundle.getString(name);
    }

    static class OurMap extends TemplateProcessor.KeyMap {
	public OurMap(String key) {
	    super();
	    if (key == null) throw new NullPointerException();
	    put("domKey", key);
	    put("commaSeparator", ",");
	}
	public OurMap(TemplateProcessor.KeyMap keymap) {
	    this((String)keymap.get("domKey"));
	    String value = (String)keymap.get("domMode");
	    if (value != null) put("domMode", value);
	    else keymap.put("domMap", "property");
	    value = (String) keymap.get("domCondMode");
	    if (value != null) put("domCondMode", value);
	    else keymap.put("domCondMode", "onImageChange");
	    value = (String)keymap.get("domIDs");
	    if (value != null) put("domIDs", value);
	    value = (String)keymap.get("domFunction");
	    if (value != null) put("domFunction", value);
	    value = (String)keymap.get("domProp");
	    if (value != null) put("domProp", value);
	    value = (String)keymap.get("domName");
	    if (value != null) put("domName", value);
	    value = (String)keymap.get("domMethod");
	    if (value != null) put("domMethod", value);
	    value = (String)keymap.get("domDefaultValue");
	    if (value != null) put("domDefaultValue", value);
	    value = (String)keymap.get("domDefaultArgument");
	    if (value != null) put("domDefaultArgument", value);

	    value = (String)keymap.get("propertyValue");
	    if (value != null) put("propertyValue", value);
	}
	public String toString() {return (String)get("domKey");}
    }
    OurMap currentMap = null;


    int oldindex = -1;

    void setCurrentMap(OurMap newMap) {
	// System.out.println("setCurrentMap called");

	if (currentMap != null) {
	    /*
	    System.out.println("saving map for key "
			       + (String)currentMap.get("domKey"));
	    */
	    int ind = modeComboBox.getSelectedIndex();
	    currentMap.put("domMode", modeValues[ind]);
	    int cind = condModeComboBox.getSelectedIndex();
	    currentMap.put("domCondMode", condModes[cind]);
	    switch (oldindex) {
	    case 0:
		currentMap.put("domIDs", idsTextField.getText());
		currentMap.put("domProp", propTextField.getText());
		currentMap.put("domDefaultValue", 
			       defaultValueTextArea.getText());
		break;
	    case 1:
		currentMap.put("domIDs", idsTextField.getText());
		currentMap.put("domName", propTextField.getText());
		currentMap.put("domDefaultValue", 
			       defaultValueTextArea.getText());
		break;
	    case 2:
		currentMap.put("domIDs", idsTextField.getText());
		currentMap.put("domMethod", propTextField.getText());
		/*
		currentMap.put("domCallAsDefault", "" +
			       callAsDefaultCheckBox.isSelected());
		*/
		break;
	    case 3:
		currentMap.put("domIDs", idsTextField.getText());
		currentMap.put("domMethod", propTextField.getText());
		currentMap.put("domDefaultArgument", 
			       defaultValueTextArea.getText());
		break;
	    case 4:
		currentMap.put("domFunction", propTextField.getText());
		currentMap.put("domDefaultArgument", 
			       defaultValueTextArea.getText());
		break;
	    case 5:
		currentMap.put("domFunction", propTextField.getText());
		break;
	    }
	}
	// if (currentMap == newMap) return;

	OurMap oldMap = currentMap;
	currentMap = newMap;
	if (newMap != null) {
	    modeLabel.setEnabled(true);
	    modeComboBox.setEnabled(true);
	    condModeLabel.setEnabled(true);
	    condModeComboBox.setEnabled(true);
	    String mode = (String)currentMap.get("domMode");
	    if (mode == null) {
		mode = "property";
		currentMap.put("domMode", mode);
	    }
	    String condMode = (String)currentMap.get("domCondMode");
	    if (condMode == null) {
		/*
		System.out.println("call mode was null for key "
				   + (String)currentMap.get("domKey")
				   + " "
				   + currentMap.hashCode());
		*/
		condMode = "onImageChange";
		currentMap.put("domCondMode", condMode);
	    }
	    condModeComboBox.setSelectedIndex(getCondModeIndex(condMode));
	    if (mode.equals("property")) {

		oldindex = 0;
		// callAsDefaultCheckBox.setSelected(false);
		// callAsDefaultCheckBox.setEnabled(false);
		idsLabel.setEnabled(true);
		idsTextField.setEnabled(true);
		defaultValueLabel.setEnabled(true);
		propTextField.setEnabled(true);
		propLabel.setEnabled(true);
		defaultValueTextArea.setEnabled(true);
		String text = (String)currentMap.get("domIDs");
		idsTextField.setText((text==null)?"":text);
		text = (String)currentMap.get("domProp");
		propTextField.setText((text==null)?"":text);
		text = (String)currentMap.get("domDefaultValue");
		defaultValueTextArea.setText((text==null)?"":text);
		// Need to do this last because the combo box's action
		// listener can call setCurrentMap
		modeComboBox.setSelectedIndex(0);
	    } else if (mode.equals("attribute")) {
		oldindex = 1;
		// callAsDefaultCheckBox.setSelected(false);
		// callAsDefaultCheckBox.setEnabled(false);
		idsLabel.setEnabled(true);
		idsTextField.setEnabled(true);
		defaultValueLabel.setEnabled(true);
		propTextField.setEnabled(true);
		propLabel.setEnabled(true);
		defaultValueTextArea.setEnabled(true);
		String text = (String)currentMap.get("domIDs");
		idsTextField.setText((text==null)?"":text);
		text = (String)currentMap.get("domName");
		propTextField.setText((text==null)?"":text);
		text = (String)currentMap.get("domDefaultValue");
		defaultValueTextArea.setText((text==null)?"":text);
		// Need to do this last because the combo box's action
		// listener can call setCurrentMap
		modeComboBox.setSelectedIndex(1);
	    } else if (mode.equals("method0")) {
		oldindex = 2;
		// callAsDefaultCheckBox.setEnabled(true);
		idsLabel.setEnabled(true);
		idsTextField.setEnabled(true);
		propTextField.setEnabled(true);
		propLabel.setEnabled(true);
		defaultValueLabel.setEnabled(false);
		defaultValueTextArea.setEnabled(false);
		/*
		String x = (String)currentMap.get("domCallAsDefault");
		if (x == null) x = "false";
		callAsDefaultCheckBox.setSelected(x.equals("true"));
		*/
		String text = (String)currentMap.get("domIDs");
		idsTextField.setText((text==null)?"":text);
		text = (String)currentMap.get("domMethod");
		propTextField.setText((text==null)?"":text);
		defaultValueTextArea.setText("");
		modeComboBox.setSelectedIndex(2);
	    } else if (mode.equals("method1")) {
		oldindex = 3;
		// callAsDefaultCheckBox.setSelected(false);
		// callAsDefaultCheckBox.setEnabled(false);
		idsLabel.setEnabled(true);
		idsTextField.setEnabled(true);
		propTextField.setEnabled(true);
		propLabel.setEnabled(true);
		defaultValueLabel.setEnabled(true);
		defaultValueTextArea.setEnabled(true);
		String text = (String)currentMap.get("domIDs");
		idsTextField.setText((text==null)?"":text);
		text = (String)currentMap.get("domMethod");
		propTextField.setText((text==null)?"":text);
		text = (String)currentMap.get("domDefaultArgument");
		defaultValueTextArea.setText((text==null)?"":text);
		modeComboBox.setSelectedIndex(3);
	    } else if (mode.equals("function")) {
		oldindex = 4;
		// callAsDefaultCheckBox.setEnabled(false);
		// callAsDefaultCheckBox.setSelected(false);
		idsLabel.setEnabled(false);
		idsTextField.setText("");
		idsTextField.setEnabled(false);
		propTextField.setEnabled(true);
		propLabel.setEnabled(true);
		defaultValueLabel.setEnabled(true);
		defaultValueTextArea.setEnabled(true);
		String text = (String)currentMap.get("domFunction");
		propTextField.setText((text==null)?"":text);
		text = (String)currentMap.get("domDefaultArgument");
		defaultValueTextArea.setText((text==null)?"":text);
		modeComboBox.setSelectedIndex(4);
	    } else if (mode.equals("test")) {
		oldindex = 5;
		// callAsDefaultCheckBox.setEnabled(false);
		// callAsDefaultCheckBox.setSelected(false);
		idsLabel.setEnabled(false);
		idsTextField.setText("");
		idsTextField.setEnabled(false);
		propTextField.setEnabled(true);
		propLabel.setEnabled(true);
		defaultValueLabel.setEnabled(false);
		defaultValueTextArea.setEnabled(false);
		String text = (String)currentMap.get("domFunction");
		propTextField.setText((text==null)?"":text);
		text = (String)currentMap.get("domDefaultArgument");
		defaultValueTextArea.setText((text==null)?"":text);
		modeComboBox.setSelectedIndex(5);
	    } else {
		System.err.println("unknown mode"); System.exit(1);
	    }
	} else {
	    idsTextField.setText("");
	    propTextField.setText("");
	    defaultValueTextArea.setText("");
	    modeComboBox.setSelectedIndex(0);
	    //oldindex = 0;
	    oldindex = -1;
	    modeLabel.setEnabled(false);
	    modeComboBox.setEnabled(false);
	    condModeLabel.setEnabled(false);
	    condModeComboBox.setEnabled(false);
	    // callAsDefaultCheckBox.setEnabled(false);
	    // callAsDefaultCheckBox.setSelected(false);
	    idsLabel.setEnabled(false);
	    propLabel.setEnabled(false);
	    defaultValueLabel.setEnabled(false);
	    idsTextField.setEnabled(false);
	    propTextField.setEnabled(false);
	    defaultValueTextArea.setEnabled(false);
	}
    }


    // LinkedList<OurMap> domlist = new LinkedList<OurMap>();

    JPanel panel = new JPanel();

    DefaultListModel<OurMap> listModel = new DefaultListModel<OurMap>();
    JList<OurMap> keyList = new JList<OurMap>(listModel);
    JScrollPane scrollPane = new JScrollPane(keyList);

    public int listSize() {return listModel.size();}

    TransferHandler th = new ReorderListTransferHandler(keyList);

    public void initModel(TemplateProcessor.KeyMap [] entries) {
	listModel.clear();
	if (entries != null) {
	    for (TemplateProcessor.KeyMap entry: entries) {
		OurMap element = new OurMap(entry);
		/*
		System.out.println((String)element.get("domKey")
				   + " " + (String)element.get("domMode")
				   + " " + (String)element.get("domCondMode")
				   +" " + element.hashCode()
				   +"  -- initModel");
		*/
		listModel.addElement(element);
	    }
	}
    }

    public String getKey(int i) {
	setCurrentMap(currentMap);
	return (String)((OurMap)listModel.get(i)).get("domKey");
    }
    public String getID(int i) {
	setCurrentMap(currentMap);
	return (String)((OurMap)listModel.get(i)).get("domID");
    }
    public String getProp(int i) {
	setCurrentMap(currentMap);
	return (String)((OurMap)listModel.get(i)).get("domProp");
    }
    public String getName(int i) {
	setCurrentMap(currentMap);
	return (String)((OurMap)listModel.get(i)).get("domName");
    }
    public String getDefaultValue(int i) {
	setCurrentMap(currentMap);
	return (String)((OurMap)listModel.get(i)).get("domDefaultValue");
    }

    public LinkedList<TemplateProcessor.KeyMap> getList() {
	LinkedList<TemplateProcessor.KeyMap> list = 
	    new LinkedList<TemplateProcessor.KeyMap>();
	return getList(list);
    }

    public LinkedList<TemplateProcessor.KeyMap> 
	getList(LinkedList<TemplateProcessor.KeyMap> oldList) 
    {
	oldList.clear();
	int n = listSize();
	Enumeration<?> elements = listModel.elements();
	while (elements.hasMoreElements()) {
	    oldList.add(new TemplateProcessor.KeyMap
			((OurMap)elements.nextElement()));
	}
	if (oldList.size() > 0) {
	    oldList.getLast().remove("commaSeparator");
	}

	return oldList;
    }

    JButton addButton = new JButton(localeString("AddButton"));
    JButton removeButton = new JButton(localeString("RemoveButton"));
    JLabel modeLabel = new JLabel(localeString("modeLabel") + ": ");
    JCheckBox callAsDefaultCheckBox = 
	new JCheckBox(localeString("callAsDefault"));

    String[] modeValues = {
	"property",
	"attribute",
	"method0",
	"method1",
	"function",
	"test"
    };

    static String[] condModes = {
	"onImageChange",
	"onOverridden",
	"onNotOverridden",
	"onSlideshowStart",
	"onSlideshowEnd",
	"asSlideshowTest",
	"asSlideshowEnabledTest"
    };

    static int getCondModeIndex(String condMode) {
	for (int i = 0; i < condModes.length; i++) {
	    if (condModes[i].equals(condMode)) return i;
	}
	return -1;
    }

    static String[] localeCondModes = new String[condModes.length];
    static {
	for (int i = 0; i < condModes.length; i++) {
	    localeCondModes[i] = localeString(condModes[i]);
	}
    }

    JLabel  condModeLabel = new JLabel(localeString("condModeLabel"));
    JComboBox<String> condModeComboBox = new JComboBox<>(localeCondModes);


    void setVaryingToolTipsAndLabels(int modeIndex) {
	switch(modeIndex) {
	case 0:
	    // callAsDefaultCheckBox.setText(localeString("callAsDefault"));
	    idsLabel.setText(localeString("IDsLabel"));
	    propLabel.setText(localeString("PropLabel"));
	    defaultValueLabel.setText(localeString("defaultValueLabel"));
	    idsTextField.setToolTipText(localeString("idsTextFieldToolTip"));
	    propTextField.setToolTipText(localeString("propTextFieldToolTip"));
	    defaultValueTextArea.setToolTipText
		(localeString("defaultValueTextAreaToolTip"));
	    // callAsDefaultCheckBox.setEnabled(false);
	    defaultValueLabel.setEnabled(true);
	    propTextField.setEnabled(true);
	    propLabel.setEnabled(true);
	    defaultValueTextArea.setEnabled(true);
	    break;
	case 1:
	    // callAsDefaultCheckBox.setText(localeString("callAsDefault"));
	    idsLabel.setText(localeString("IDsLabel"));
	    propLabel.setText(localeString("PropLabel1"));
	    defaultValueLabel.setText(localeString("defaultValueLabel1"));
	    idsTextField.setToolTipText(localeString("idsTextFieldToolTip"));
	    propTextField.setToolTipText(localeString("propTextFieldToolTip1"));
	    defaultValueTextArea.setToolTipText
		(localeString("defaultValueTextAreaToolTip1"));
	    // callAsDefaultCheckBox.setEnabled(false);
	    defaultValueLabel.setEnabled(true);
	    propTextField.setEnabled(true);
	    propLabel.setEnabled(true);
	    defaultValueTextArea.setEnabled(true);
	    break;
	case 2:
	    // callAsDefaultCheckBox.setText(localeString("callAsDefault1"));
	    idsLabel.setText(localeString("IDsLabel"));
	    propLabel.setText(localeString("PropLabel2"));
	    defaultValueLabel.setText(localeString("defaultValueLabel2"));
	    idsTextField.setToolTipText(localeString("idsTextFieldToolTip"));
	    propTextField.setToolTipText(localeString("propTextFieldToolTip2"));
	    defaultValueTextArea.setToolTipText
		(localeString("defaultValueTextAreaToolTip2"));
	    // callAsDefaultCheckBox.setEnabled(true);
	    propTextField.setEnabled(true);
	    propLabel.setEnabled(true);
	    defaultValueLabel.setEnabled(false);
	    defaultValueTextArea.setEnabled(false);
	    break;
	case 3:
	    // callAsDefaultCheckBox.setText(localeString("callAsDefault1"));
	    idsLabel.setText(localeString("IDsLabel"));
	    propLabel.setText(localeString("PropLabel2"));
	    defaultValueLabel.setText(localeString("defaultValueLabel3"));
	    idsTextField.setToolTipText(localeString("idsTextFieldToolTip"));
	    propTextField.setToolTipText(localeString("propTextFieldToolTip2"));
	    defaultValueTextArea.setToolTipText
		(localeString("defaultValueTextAreaToolTip3"));
	    // callAsDefaultCheckBox.setEnabled(false);
	    propTextField.setEnabled(true);
	    propLabel.setEnabled(true);
	    defaultValueLabel.setEnabled(true);
	    defaultValueTextArea.setEnabled(true);
	    break;
	case 4:
	    // callAsDefaultCheckBox.setText(localeString("callAsDefault2"));
	    idsLabel.setText(localeString("IDsLabel"));
	    propLabel.setText(localeString("PropLabel3"));
	    defaultValueLabel.setText(localeString("defaultValueLabel4"));
	    idsTextField.setToolTipText(localeString("idsTextFieldToolTip"));
	    propTextField.setToolTipText(localeString("propTextFieldToolTip3"));
	    defaultValueTextArea.setToolTipText
		(localeString("defaultValueTextAreaToolTip4"));
	    // callAsDefaultCheckBox.setEnabled(false);
	    propTextField.setEnabled(true);
	    propLabel.setEnabled(true);
	    defaultValueLabel.setEnabled(true);
	    defaultValueTextArea.setEnabled(true);
	    break;
	case 5:
	    idsLabel.setText(localeString("IDsLabel"));
	    propLabel.setText(localeString("PropLabel3"));
	    defaultValueLabel.setText(localeString("defaultValueLabel3"));
	    idsTextField.setToolTipText(localeString("idsTextFieldToolTip"));
	    propTextField.setToolTipText(localeString("propTextFieldToolTip3"));
	    defaultValueTextArea.setToolTipText
		(localeString("defaultValueTextAreaToolTip4"));
	    propTextField.setEnabled(true);
	    propLabel.setEnabled(true);
	    defaultValueLabel.setEnabled(false);
	    defaultValueTextArea.setEnabled(false);
	    break;
	}
    }

    String[] modeVector = {
	localeString("property"),
	localeString("attribute"),
	localeString("method0"),
	localeString("method1"),
	localeString("function"),
	localeString("test")
	
    };
    JComboBox<String> modeComboBox = new JComboBox<>(modeVector);
    JLabel idsLabel = new JLabel(localeString("IDsLabel"));
    JTextField idsTextField = new JTextField(32);
    JLabel propLabel = new JLabel(localeString("PropLabel"));
    JTextField propTextField = new JTextField(32);
    JLabel defaultValueLabel = 
	new JLabel(localeString("defaultValueLabel"));
    JTextArea defaultValueTextArea = new JTextArea(10, 50);
    JScrollPane dvtaScrollPane = new 
	JScrollPane(defaultValueTextArea,
		    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    public DomMapPane() {
	super();
	keyList.setPrototypeCellValue(new OurMap("01234567890123456789"));
	keyList.setTransferHandler(th);
	keyList.setDragEnabled(true);
	keyList.setDropMode(DropMode.INSERT);

	setLayout(new BorderLayout());
	JPanel scrPanel = new JPanel();
	scrPanel.setLayout(new BorderLayout());
	JLabel scrollLabel = new JLabel(localeString("Keys"));
	scrPanel.add(scrollLabel, BorderLayout.PAGE_START);
	scrPanel.add(scrollPane, BorderLayout.CENTER);
	add(scrPanel, BorderLayout.LINE_START);

	GridBagConstraints c = new GridBagConstraints();
	c.insets = new Insets(5, 5, 5, 5);
	c.gridwidth = GridBagConstraints.REMAINDER;
	JPanel subPanel = new JPanel();
	GridBagLayout sbl = new GridBagLayout();
	subPanel.setLayout(sbl);
	JPanel buttonPanel = new JPanel();
	FlowLayout bpfl = new FlowLayout(FlowLayout.LEADING);
	bpfl.setHgap(10);
	buttonPanel.setLayout(bpfl);
	buttonPanel.add(addButton);
	buttonPanel.add(removeButton);
	sbl.setConstraints(buttonPanel, c);
	subPanel.add(buttonPanel);

	c.anchor = GridBagConstraints.LINE_START;

	JPanel modePanel = new JPanel();
	FlowLayout mpfl = new FlowLayout(FlowLayout.LEADING);
	mpfl.setHgap(10);
	modePanel.setLayout(mpfl);
	modePanel.add(modeLabel);
	modePanel.add(modeComboBox);
	modePanel.add(condModeLabel);
	modePanel.add(condModeComboBox);
	sbl.setConstraints(modePanel, c);
	subPanel.add(modePanel);

	modeComboBox.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    int ind = modeComboBox.getSelectedIndex();
		    if (ind < 0) ind = 0;
		    setVaryingToolTipsAndLabels(ind);
		    setCurrentMap(currentMap);
		}
	    });

	// sbl.setConstraints(callAsDefaultCheckBox, c);
	//subPanel.add(callAsDefaultCheckBox);

	sbl.setConstraints(idsLabel, c);
	subPanel.add(idsLabel);
	sbl.setConstraints(idsTextField, c);
	subPanel.add(idsTextField);
	sbl.setConstraints(propLabel, c);
	subPanel.add(propLabel);
	sbl.setConstraints(propTextField, c);
	subPanel.add(propTextField);
	sbl.setConstraints(defaultValueLabel, c);
	subPanel.add(defaultValueLabel);
	sbl.setConstraints(dvtaScrollPane, c);
	subPanel.add(dvtaScrollPane);
	add(subPanel, BorderLayout.CENTER);

	addButton.setToolTipText(localeString("addButtonToolTip"));
	condModeComboBox.setToolTipText
	    (localeString("condModeComboBoxToolTip"));
	removeButton.setToolTipText(localeString("removeButtonToolTip"));
	idsTextField.setToolTipText(localeString("idsTextFieldToolTip"));
	propTextField.setToolTipText(localeString("propTextFieldToolTip"));
	defaultValueTextArea.setToolTipText
	    (localeString("defaultValueTextAreaToolTip"));
	keyList.setToolTipText(localeString("keyListToolTip"));

	addButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    String key = JOptionPane.showInputDialog
			(DomMapPane.this,
			 localeString("AddButtonMessage"),
			 localeString("AddButtonTitle"),
			 JOptionPane.PLAIN_MESSAGE);
		    if (key == null) return;
		    key = key.trim();
		    if (key.length() == 0) {
			JOptionPane.showMessageDialog
			    (DomMapPane.this,
			     localeString("KeyMissing"),
			     localeString("KeyMissingTitle"),
			     JOptionPane.ERROR_MESSAGE);
			return;
		    }
		    if (!key.matches("[\\p{L}_$][\\p{L}_$0-9]*")) {
			JOptionPane.showMessageDialog
			    (DomMapPane.this,
			     localeString("KeyInvalid"),
			     localeString("KeyInvalidTitle"),
			     JOptionPane.ERROR_MESSAGE);
			return;
			
		    }
		    int n = listModel.size();
		    for (int i = 0; i < n; i++) {
			if (key.equals((String)
				       ((OurMap)listModel.elementAt(i))
				       .get("domKey"))) {
			    keyList.setSelectedIndex(i);
			    idsTextField.requestFocusInWindow();
			    return;
			}
		    }
		    OurMap map = new OurMap(key);
		    listModel.addElement(map);
		    keyList.setSelectedIndex(listModel.size() - 1);
		    idsTextField.requestFocusInWindow();
		    setCurrentMap(map);
		}
	    });
	removeButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    int i = keyList.getMinSelectionIndex();
		    if (i == -1) return;
		    int n = keyList.getMaxSelectionIndex();
		    while (n >= i) {
			if (keyList.isSelectedIndex(n)) {
			    listModel.remove(n);
			}
			n--;
		    }
		    keyList.clearSelection();
		}
	    });
	keyList.addListSelectionListener(new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
		    int minSelection = keyList.getMinSelectionIndex();
		    int maxSelection = keyList.getMaxSelectionIndex();
		    /*
		    System.out.println("valueChanged called "
				       + minSelection + " "
				       + maxSelection);
		    */
		    if (minSelection == -1 || minSelection != maxSelection) {
			setCurrentMap(null);
		    } else {
			OurMap map = (OurMap)listModel.get(minSelection);
			setCurrentMap(map);
		    }

		}
	    });
	modeLabel.setEnabled(false);
	condModeLabel.setEnabled(false);
	condModeComboBox.setEnabled(false);
	// callAsDefaultCheckBox.setEnabled(false);
	modeComboBox.setEnabled(false);
	idsLabel.setEnabled(false);
	propLabel.setEnabled(false);
	defaultValueLabel.setEnabled(false);
	idsTextField.setEnabled(false);
	propTextField.setEnabled(false);
	defaultValueTextArea.setEnabled(false);
    }

    public void clearSelection() {
	keyList.clearSelection();
    }

    static public void main(String argv[]) {
	final DomMapPane dm = new DomMapPane();

	JFrame frame = new JFrame("DomMapPane Test");
        Container fpane = frame.getContentPane();
        frame.addWindowListener(new WindowAdapter () {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
 
        fpane.setLayout(new FlowLayout());

	fpane.add(dm);
	frame.pack();
        frame.setVisible(true);

    }
}
