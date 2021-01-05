package beadMaker;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class ControlLabel extends JLabel {
	ControlLabel(String labelName) {
		super(labelName);
		//this.setPreferredSize(new Dimension(80, 30));
		this.setPreferredSize(new Dimension(68, 2));
		
		this.setHorizontalAlignment(SwingConstants.RIGHT);
		//this.setBorder(new BorderMaker(BorderMaker.NONE, 8, 0));
	}
}
