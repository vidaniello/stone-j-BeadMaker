package beadMaker;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

public class ControlPanelSuperPanel extends JPanel {
	
	boolean isVisible = true;
	
	ControlPanelSuperPanel() {
		super();	
	}
	
	public void toggleHide() {
		isVisible = !isVisible;
		if(isVisible) { 
			setPreferredSize(new Dimension(528,1));
		} else {
			setPreferredSize(new Dimension(21, 1));
		}
		this.repaint();
	}
	
	public void paintComponent(Graphics g){  
		super.paintComponent(g);
	}
}
