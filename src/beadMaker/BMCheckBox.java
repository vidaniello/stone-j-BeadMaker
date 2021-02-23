package beadMaker;

import java.awt.event.KeyEvent;

import javax.swing.JCheckBox;

import core.BorderMaker;
import core.ConsoleHelper;
import core.InterObjectCommunicatorEventListener;
import core.StringHelper;

public class BMCheckBox extends JCheckBox implements InterObjectCommunicatorEventListener {

	//For InterObjectCommunicator identification
	private String objectName = "";
	
	private InterObjectCommunicator oComm;	
	
	ConsoleHelper consoleHelper = new ConsoleHelper();
	
	BMCheckBox(String myLabel, boolean checkedState, InterObjectCommunicator myOComm) {
		super(myLabel, checkedState);
		setBorder(new BorderMaker(BorderMaker.NONE, 0, 0));
		setOpaque(false);
		
		myLabel = StringHelper.removeXML(myLabel);
		
		if (myLabel.equals("Perler")) {objectName = "BM_CHECKBOX_PERLER";}
		if (myLabel.equals("Artkal-S")) {objectName = "BM_CHECKBOX_ARTKAL";}
		if (myLabel.equals("Show Grid")) {objectName = "BM_CHECKBOX_SHOW_GRID";}
		if (myLabel.equals("Show Pixels as Beads")) {objectName = "BM_CHECKBOX_PIXELS_AS_BEADS";}
		if (myLabel.equals("Flip Image")) {objectName = "BM_CHECKBOX_FLIP_IMAGE";}
		
		if (!objectName.equals("")) {
			oComm = myOComm;
			oComm.setInterObjectCommunicatorEventListener(this);
			consoleHelper.PrintMessage("Registering oComm with Checkbox " + myLabel);
		}
	}
	
	
	@Override
	public void onInterObjectCommunicator_CommunicateEvent(Object o) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInterObjectCommunicator_CommunicateEvent(String descriptor, Object o) {
		if (o instanceof KeyEvent) {
			KeyEvent e = ((KeyEvent) o);
			//ConsoleHelper.PrintMessage("fired a keyPressed event from oCommInterface in CHECKBOX" + e.getKeyCode());
			//if the descriptor matches the button name (text) then do the stuff
			//ConsoleHelper.PrintMessage("descriptor = " + descriptor + ", this.getText() = " + this.getText());
			if (descriptor.equals("Perler")) {
				if ((e.getKeyCode() == KeyEvent.VK_P) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
					//ConsoleHelper.PrintMessage("fired a keyPressed event from oCommInterface in CHECKBOX" + e.getKeyCode());
					//this.setSelected(!this.isSelected());
					this.setSelected((boolean)oComm.request("set bead brand", this, "PALETTE"));
					oComm.communicate("update images", "IMAGE_CONTROLLER");
					oComm.communicate("create buttons", "PALETTE");
		        }
			}
			if (descriptor.equals("Artkal-S")) {
				if ((e.getKeyCode() == KeyEvent.VK_A) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
					//ConsoleHelper.PrintMessage("fired a keyPressed event from oCommInterface in CHECKBOX" + e.getKeyCode());
					//this.setSelected(!this.isSelected());
					this.setSelected((boolean)oComm.request("set bead brand", this, "PALETTE"));
					oComm.communicate("update images", "IMAGE_CONTROLLER");
					oComm.communicate("create buttons", "PALETTE");
		        }
			}
			if (descriptor.equals("Show Grid")) {
				if ((e.getKeyCode() == KeyEvent.VK_G) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
					//ConsoleHelper.PrintMessage("fired a keyPressed event from oCommInterface in CHECKBOX" + e.getKeyCode());
					setSelected(!isSelected());
					oComm.communicate("show grid", isSelected(), "RENDER_LABEL");
					oComm.communicate("update images", "IMAGE_CONTROLLER");
		        }
			}
			if (descriptor.equals("Show Pixels as Beads")) {
				if ((e.getKeyCode() == KeyEvent.VK_B) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
					//ConsoleHelper.PrintMessage("fired a keyPressed event from oCommInterface in CHECKBOX" + e.getKeyCode());
					setSelected(!isSelected());
					//imageController.setRenderPixelsAsBeads(checkBox.isSelected());
					oComm.communicate("render pixels as beads", isSelected(), "IMAGE_CONTROLLER");
		        }
			}
			if (descriptor.equals("Flip Image")) {
				if ((e.getKeyCode() == KeyEvent.VK_F) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
					//ConsoleHelper.PrintMessage("fired a keyPressed event from oCommInterface in CHECKBOX" + e.getKeyCode());
					setSelected(!isSelected());
					//imageController.setRenderPixelsAsBeads(checkBox.isSelected());
					oComm.communicate("flip image", isSelected(), "IMAGE_CONTROLLER");
		        }
			}
		}
		
	}

	@Override
	public Object onInterObjectCommunicator_RequestEvent(Object o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getObjectName() {
		return objectName;
	}


	@Override
	public Object onInterObjectCommunicator_RequestEvent(String descriptor, Object o) {
		// TODO Auto-generated method stub
		return null;
	}

}
