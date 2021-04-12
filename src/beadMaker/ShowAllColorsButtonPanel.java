package beadMaker;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import core.helper.ColorHelper;

public class ShowAllColorsButtonPanel extends JPanel {
	
	InterObjectCommunicator oComm;
	
	ShowAllColorsButtonPanel(String label, InterObjectCommunicator myOComm) {
		super();
		init(label, myOComm);
	}
	

	public void init(String label, InterObjectCommunicator myOComm) {
		oComm = myOComm;
		this.setLayout(new FlowLayout(FlowLayout.CENTER));
		setOpaque(false);
		setPreferredSize(new Dimension(100, 36));
		setMaximumSize(new Dimension(360, 36));
		
		final JButton showAllColorsButton = new JButton(label);
		showAllColorsButton.setPreferredSize(new Dimension(260, 30));
		
		ActionListener showAllColorsActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				ShowAllColors();
			}
		};
		
		showAllColorsButton.addActionListener(showAllColorsActionListener);
		
		add(showAllColorsButton);
	}
	
	
	public void ShowAllColors() {
		oComm.communicate("show all colors", "IMAGE_CONTROLLER");
		oComm.communicate("show all colors", "PALETTE");
	}
}
