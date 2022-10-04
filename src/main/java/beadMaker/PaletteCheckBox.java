package beadMaker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import core.logging.ConsoleHelper;

public class PaletteCheckBox extends JCheckBox {
	
	Palette pallette;
	int colorIndex;
	
	InterObjectCommunicator oComm;
	
	ConsoleHelper consoleHelper = new ConsoleHelper();

	PaletteCheckBox(String label, boolean state, Palette myPallette, int myColorIndex, InterObjectCommunicator myOComm) {
		super(label, state);
		oComm = myOComm;
		this.pallette = myPallette;
		this.colorIndex = myColorIndex;
		init();
	}

	PaletteCheckBox(String label, Palette myPallette, int myColorIndex, InterObjectCommunicator myOComm) {
		this(label, false, myPallette, myColorIndex, myOComm);
	}

	public void init() {
		//this.setBorder(new BorderMaker(BorderMaker.NONE, 0, 0));
		setOpaque(false);
		
		//setPreferredSize(new Dimension(14, 20));
		//setMaximumSize(new Dimension(14, 20));
		
		ActionListener palletteCheckBoxActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				oComm.communicate(a, "PALETTE");
//				PaletteCheckBox palletteCheckBox = (PaletteCheckBox)a.getSource();
//				pallette.checkUncheck(palletteCheckBox.colorIndex, palletteCheckBox.isSelected());
				consoleHelper.PrintMessage("firing pallette check action");
			}
		};

		this.addActionListener(palletteCheckBoxActionListener);
	}
}
