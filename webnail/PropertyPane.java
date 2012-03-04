package webnail;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import org.bzdev.util.TemplateProcessor;
import org.bzdev.util.TemplateProcessor.KeyMap;


public class PropertyPane extends JComponent {

    static private final String resourceBundleName = "webnail/PropertyPane";
    static ResourceBundle bundle = 
	ResourceBundle.getBundle(resourceBundleName);
    static String localeString(String name) {
	return bundle.getString(name);
    }


    DefaultListModel listModel = new DefaultListModel();
    JList keyList = new JList(listModel);
    JScrollPane scrollPane = new JScrollPane(keyList);

    JLabel addLabel = new JLabel(localeString("propertyAddLabel")+ ":");

    JComboBox addComboBox = new JComboBox();
    JButton removeButton = 
	new JButton(localeString("propertyRemoveButton"));
    JLabel propertyValueLabel = 
	new JLabel(localeString("propertyValueLabel"));
    JTextArea propertyValueTextArea = new JTextArea(10, 50);
    JScrollPane propertyValueScrollPane = 
	new JScrollPane(propertyValueTextArea,
		    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    public int listSize() {return listModel.size();}

    public String getKey(int i) {
	setCurrentMap(currentMap);
	return (String)((DomMapPane.OurMap)listModel.get(i)).get("domKey");
    }

    public String getValue(int i) {
	setCurrentMap(currentMap);
	return (String)((DomMapPane.OurMap)listModel.get(i)).get
	    ("propertyValue");
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
	    oldList.add(new TemplateProcessor.KeyMap((DomMapPane.OurMap)
					      elements.nextElement()));
	}
	if (oldList.size() > 0) {
	    oldList.getLast().remove("commaSeparator");
	}

	return oldList;
    }

    public void initModel(TemplateProcessor.KeyMap [] entries, 
			  LinkedList<TemplateProcessor.KeyMap>domMapList) 
    {
	HashMap<String,String> condModeMap = new HashMap<String,String>();
	addComboBox.removeAllItems();
	addComboBox.addItem(localeString("propertyAddComboBox"));
	for (TemplateProcessor.KeyMap map: domMapList) {
	    String condMode = (String) map.get("domCondMode");
	    if (!(condMode.startsWith("as") || condMode.startsWith("onSl"))) {
		String key = (String) map.get("domKey");
		addComboBox.addItem(key);
		condModeMap.put(key, condMode);
	    }
	}

	listModel.clear();
	if (entries != null) {
	    for (TemplateProcessor.KeyMap entry: entries) {
		DomMapPane.OurMap element = new DomMapPane.OurMap(entry);
		String key = (String) element.get("domKey");
		String condMode = (key == null)? null:
		    (String)condModeMap.get(key);
		if (condMode != null &&
		    !(condMode.startsWith("as") ||
		      condMode.startsWith("onSl"))) {
		    listModel.addElement(element);
		}
	    }
	}
    }


    DomMapPane.OurMap currentMap = null;

    void setCurrentMap(DomMapPane.OurMap newMap) {
	// System.out.println("setCurrentMap called");
	if (currentMap != null) {
	    currentMap.put("propertyValue", propertyValueTextArea.getText());
	}
	if (currentMap == newMap) return;
	currentMap = newMap;
	if (newMap != null) {
	    propertyValueLabel.setEnabled(true);
	    propertyValueTextArea.setEnabled(true);
	    String text = (String)currentMap.get("propertyValue");
	    propertyValueTextArea.setText((text==null)?"":text);
	} else {
	    propertyValueTextArea.setText("");
	    propertyValueLabel.setEnabled(false);
	    propertyValueTextArea.setEnabled(false);
	}
    }

    public void clearSelection() {
	keyList.clearSelection();
    }



    public PropertyPane() {
	this(null);
    }

    public PropertyPane(LinkedList<TemplateProcessor.KeyMap>domMapList) {
	super();
	keyList.setPrototypeCellValue("01234567890123456789");

	addComboBox.addItem(localeString("propertyAddComboBox"));
	if (domMapList != null) {
	    for (TemplateProcessor.KeyMap map: domMapList) {
		addComboBox.addItem(map.get("domKey"));
	    }
	}


	addComboBox.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (addComboBox.getSelectedIndex() == 0) return;
		    String key = (String)addComboBox.getSelectedItem();
		    int n = listModel.getSize();
		    if (key != null) {
			for (int i = 0; i < n; i++) {
			    if (key.equals((String)((TemplateProcessor.KeyMap)
						    listModel.elementAt(i))
					   .get("domKey"))) {
				keyList.setSelectedIndex(i);
				propertyValueTextArea.requestFocusInWindow();
				addComboBox.setSelectedIndex(0);
				return;
			    }
			}
			listModel.addElement(new DomMapPane.OurMap(key));
			keyList.setSelectedIndex(listModel.size() - 1);
			addComboBox.setSelectedIndex(0);
			propertyValueTextArea.requestFocusInWindow();
		    }
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
			DomMapPane.OurMap map = 
			    (DomMapPane.OurMap)listModel.get(minSelection);
			setCurrentMap(map);
		    }

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

	setLayout(new BorderLayout());
	JPanel scrPanel = new JPanel();
	scrPanel.setLayout(new BorderLayout());
	JLabel scrollLabel = new JLabel(localeString("domMapKeys"));
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
	buttonPanel.add(addLabel);
	buttonPanel.add(addComboBox);
	buttonPanel.add(removeButton);
	sbl.setConstraints(buttonPanel, c);
	subPanel.add(buttonPanel);
	c.anchor = GridBagConstraints.LINE_START;
	sbl.setConstraints(propertyValueLabel, c);
	subPanel.add(propertyValueLabel);
	sbl.setConstraints(propertyValueScrollPane, c);
	subPanel.add(propertyValueScrollPane);
	add(subPanel, BorderLayout.CENTER);

	keyList.setToolTipText(localeString("keyListToolTip"));
	addComboBox.setToolTipText(localeString("addComboBoxToolTip"));
	removeButton.setToolTipText(localeString("removeButtonToolTip"));
	propertyValueTextArea.setToolTipText
	    (localeString("propertyValueTextAreaToolTip"));
    }

    static public void main(String argv[]) {
	
	LinkedList<TemplateProcessor.KeyMap> domMapList = new
	    LinkedList<TemplateProcessor.KeyMap>();

	int i;
	DomMapPane.OurMap map1 = new DomMapPane.OurMap("foo");
	DomMapPane.OurMap map2 = new DomMapPane.OurMap("bar");
	DomMapPane.OurMap map3 = new DomMapPane.OurMap("foobar");
	domMapList.add(map1);
	domMapList.add(map2);
	domMapList.add(map3);

	final PropertyPane pp = new PropertyPane(domMapList);

	JFrame frame = new JFrame("PropertyPane Test");
        Container fpane = frame.getContentPane();
        frame.addWindowListener(new WindowAdapter () {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
 
        fpane.setLayout(new FlowLayout());

	fpane.add(pp);
	frame.setSize(750, 400);
        frame.setVisible(true);

    }
}