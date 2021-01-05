package beadMaker;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JPanel;

public class TotalBeadsUsedPanel extends JPanel {
	
	TotalBeadsUsedPanel(String label) {
		super();
		init(label);
	}

	public void init(String label) {
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		setOpaque(false);
		setPreferredSize(new Dimension(100, 60));
		setMaximumSize(new Dimension(360, 60));
		//this.setBorder(new BorderMaker(BorderMaker.NONE, 16, 1));
	}
}
