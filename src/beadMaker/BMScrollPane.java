package beadMaker;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JScrollPane;

import core.BorderMaker;
import core.ConsoleHelper;

public class BMScrollPane extends JScrollPane implements MouseWheelListener {
	
	public BeadMaker beadMaker;
	
	ConsoleHelper consoleHelper = new ConsoleHelper();
	
	BMScrollPane(BeadMaker myBeadMaker) {
		super();
		beadMaker = myBeadMaker;
		this.setBorder(new BorderMaker(BorderMaker.RAISEDBEVEL, 4, 0));
		this.getVerticalScrollBar().setUnitIncrement(16);
		this.addMouseWheelListener(this);
	}

	public BMScrollPane(BeadMaker myBeadMaker, RenderLabel renderLabel) {
		super(renderLabel);
		beadMaker = myBeadMaker;
		this.setBorder(new BorderMaker(BorderMaker.RAISEDBEVEL, 4, 0));
		this.getVerticalScrollBar().setUnitIncrement(16);
		this.addMouseWheelListener(this);
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		consoleHelper.PrintMessage("fired a mouseWheelMoved event in BMScrollPane");
		if(e.isControlDown()) {
			setWheelScrollingEnabled(false);
			consoleHelper.PrintMessage("fired a CTRL + mouseScrolled event: " + e.getWheelRotation());
			
			if (e.getWheelRotation() == -1) { //wheel up
				beadMaker.controlPanel.sliderZoom.setValue(beadMaker.controlPanel.sliderZoom.getValue() + 50);
			} else { //wheel down
				beadMaker.controlPanel.sliderZoom.setValue(beadMaker.controlPanel.sliderZoom.getValue() - 50);
			}
			
			beadMaker.controlPanel.sliderZoom.jTextField.setText(Integer.toString(beadMaker.controlPanel.sliderZoom.getValue()));
			beadMaker.imageController.setColorMatchingWeight("Zoom %", beadMaker.controlPanel.sliderZoom.getValue());
			
		} else {
			setWheelScrollingEnabled(true);
		}
		
	}
	
	
}
