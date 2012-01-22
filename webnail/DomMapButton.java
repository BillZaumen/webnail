package webnail;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.bzdev.util.TemplateProcessor;
import org.bzdev.util.TemplateProcessor.KeyMap;


public class DomMapButton extends JButton {

    protected void doOutput(LinkedList<TemplateProcessor.KeyMap> list) {}
    protected TemplateProcessor.KeyMap[] getInput() {return null;}

    DomMapPane dmpane = new DomMapPane();

    public DomMapButton(String label, final Component frame, final String title)
    {
	super(label);
	addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    dmpane.initModel(getInput());
		    int choice = JOptionPane.showOptionDialog
			(frame, dmpane, title, JOptionPane.OK_CANCEL_OPTION,
			 JOptionPane.PLAIN_MESSAGE, null, null, null);
		    if (choice == 0) {
			dmpane.clearSelection();
			doOutput(dmpane.getList());
		    }
		}
	    });
    }
}
