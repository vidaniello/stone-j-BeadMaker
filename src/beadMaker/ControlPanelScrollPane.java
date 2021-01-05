package beadMaker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import core.BorderMaker;

public class ControlPanelScrollPane extends JScrollPane {

	public ControlPanelScrollPane(ControlPanel controlPanel) {
		super(controlPanel);
		this.setBorder(new BorderMaker(BorderMaker.RAISEDBEVEL, 4, 8));
		this.getVerticalScrollBar().setUnitIncrement(16);
	}
}
