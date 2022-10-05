package beadMaker;

import java.awt.event.KeyEvent;

import beadMaker.BMImage.DitherMethod;
import core.event.InterObjectCommunicatorEventListener;

public class InterObjectCommunicator {
	
	private InterObjectCommunicatorEventListener[] oCommListener;
		
	public enum Subscriber {
		RENDER_LABEL,
		IMAGE_CONTROLLER,
		PALETTE,
		CONTROL_PANEL,
		BEAD_MAKER,
		MENU_BAR,
		BM_CHECKBOX_PERLER,
		BM_CHECKBOX_ARTKAL,
		BM_CHECKBOX_SHOW_GRID,
		BM_CHECKBOX_PIXELS_AS_BEADS,
		BM_CHECKBOX_FLIP_IMAGE,
		HIDE_CONTROLS_BUTTON;
	}
	
	public InterObjectCommunicator() {
		oCommListener = new InterObjectCommunicatorEventListener[Subscriber.values().length];
	}
	
	public void setInterObjectCommunicatorEventListener(InterObjectCommunicatorEventListener myOCommListener) {
	    this.oCommListener[Subscriber.valueOf(myOCommListener.getObjectName()).ordinal()] = myOCommListener;
	}
	
	public void communicate(Object o, String subscriberName) {
		if (oCommListener != null) oCommListener[Subscriber.valueOf(subscriberName).ordinal()].onInterObjectCommunicator_CommunicateEvent(o);
	}
	
	public void communicate(String descriptor, Object o, String subscriberName) {
		try {
			if (oCommListener != null) oCommListener[Subscriber.valueOf(subscriberName).ordinal()].onInterObjectCommunicator_CommunicateEvent(descriptor, o);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	public Object request(Object o, String subscriberName) {	
		if (oCommListener != null) {
			return oCommListener[Subscriber.valueOf(subscriberName).ordinal()].onInterObjectCommunicator_RequestEvent(o);
		} else {
			return null;
		}
	}
	
	public Object request(String descriptor, Object o, String subscriberName) {	
		if (oCommListener != null) {
			return oCommListener[Subscriber.valueOf(subscriberName).ordinal()].onInterObjectCommunicator_RequestEvent(descriptor, o);
		} else {
			return null;
		}
	}
}
