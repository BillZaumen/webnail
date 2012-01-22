package webnail;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.bzdev.util.TemplateProcessor;
import org.bzdev.util.TemplateProcessor.KeyMap;

public class PropertyButton extends JButton {

    protected void doOutput(LinkedList<TemplateProcessor.KeyMap> list) {}

    protected TemplateProcessor.KeyMap[] getPropertyInput() {return null;}

    protected LinkedList<TemplateProcessor.KeyMap> getDomListInput() {
	return null;
    }

    PropertyPane ppane = new PropertyPane();

    public PropertyButton(String label, final Component frame, 
			  final String title)
    {
	super(label);
	addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    ppane.initModel(getPropertyInput(), getDomListInput());
		    int choice = JOptionPane.showOptionDialog
			(frame, ppane, title, JOptionPane.OK_CANCEL_OPTION,
			 JOptionPane.PLAIN_MESSAGE, null, null, null);
		    if (choice == 0) {
			ppane.clearSelection();
			doOutput(ppane.getList());
		    }
		}
	    });
    }
}
