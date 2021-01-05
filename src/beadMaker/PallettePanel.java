package beadMaker;

import javax.swing.BoxLayout;
import javax.swing.JPanel;


public class PallettePanel extends JPanel {
	
	PallettePanel() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setAlignmentX(LEFT_ALIGNMENT);		
	}
}
