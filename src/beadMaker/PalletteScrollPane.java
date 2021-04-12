package beadMaker;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JScrollPane;

import core.logging.ConsoleHelper;
import core.swingComponent.BorderMaker;

public class PalletteScrollPane extends JScrollPane {
	
	public PalletteScrollPane(PallettePanel pallettePanel) {
		super(pallettePanel);
		this.setBorder(new BorderMaker(BorderMaker.RAISEDBEVEL, 4, 0));
		this.getVerticalScrollBar().setUnitIncrement(16);
	}
}
