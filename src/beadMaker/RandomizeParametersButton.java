package beadMaker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.SwingConstants;

import core.MathHelper;

public class RandomizeParametersButton extends JButton {

	Color bgColor = new Color(204,204,204);
	public ControlPanel controlPanel;
	public int seedDirection;
	
	InterObjectCommunicator oComm;	

	public RandomizeParametersButton(
		String label,
		Color myBGColor,
		ControlPanel mycontrolPanel,
		int mySeedDirection,
		InterObjectCommunicator myOComm
	) {
		super(label);
		oComm = myOComm;
		this.controlPanel = mycontrolPanel;
		//this.bgColor = myBGColor;
		this.seedDirection = mySeedDirection;
		init();
	}
	
	
	public void init() {
		
		//this.setBorderPainted( false );
		this.setHorizontalAlignment(SwingConstants.LEFT);
		setPreferredSize(new Dimension(100, 30));
		setMaximumSize(new Dimension(360, 30));
		
		//this.setForeground(ColorHelper.GetTextColorForBGColor(bgColor));
				
		ActionListener randomizeParametersButtonClickActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//https://stackoverflow.com/questions/1816458/getting-hold-of-the-outer-class-object-from-the-inner-class-object
				oComm.communicate(RandomizeParametersButton.this, "CONTROL_PANEL");
			}
		};
		
		this.addActionListener(randomizeParametersButtonClickActionListener);
	}
	

//	public void paintComponent(Graphics g){  
//		super.paintComponent(g);
//		this.setBackground(bgColor);
//	}

}

