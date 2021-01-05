package beadMaker;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JLabel;

public class PalletteLabel extends JLabel {
	
	Color bgColor = new Color(204,204,204);
	
	public PalletteLabel(String label, Color myBGColor) {
		super(label);
		this.setOpaque(true);
		this.bgColor = myBGColor;
	}
	
	public void paintComponent(Graphics g){  
        super.paintComponent(g);
        this.setBackground(bgColor);
    }
	
}
