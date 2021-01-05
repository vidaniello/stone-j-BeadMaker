package beadMaker;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import core.ConsoleHelper;

public class KeyboardListener {
	
	InterObjectCommunicator oComm;
	
	public KeyboardListener(InterObjectCommunicator myOComm) {
		
		oComm = myOComm;
		KeyboardFocusManager ckfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		ckfm.addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				
				if(e.getID() == KeyEvent.KEY_PRESSED) {
				
					//ConsoleHelper.PrintMessage("KeyboardListener class has detected a keyPressed event");
					
					oComm.communicate(e, "RENDER_LABEL");
					oComm.communicate(e, "MENU_BAR");
					oComm.communicate(e, "IMAGE_CONTROLLER");
					oComm.communicate(e, "PALETTE");
					oComm.communicate("Perler", e, "BM_CHECKBOX_PERLER");
					oComm.communicate("Artkal-S", e, "BM_CHECKBOX_ARTKAL");
					oComm.communicate("Show Grid", e, "BM_CHECKBOX_SHOW_GRID");
					oComm.communicate("Show Pixels as Beads", e, "BM_CHECKBOX_PIXELS_AS_BEADS");
					oComm.communicate("Flip Image", e, "BM_CHECKBOX_FLIP_IMAGE");
					oComm.communicate("toggle controls", e, "HIDE_CONTROLS_BUTTON");
				}

				return false;
			}
		});
	}
}
