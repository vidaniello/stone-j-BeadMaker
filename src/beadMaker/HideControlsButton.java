package beadMaker;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;

import core.event.InterObjectCommunicatorEventListener;
import core.logging.ConsoleHelper;
import core.swingComponent.BorderMaker;

public class HideControlsButton extends JButton implements InterObjectCommunicatorEventListener {

	//For InterObjectCommunicator identification
	private String objectName = "HIDE_CONTROLS_BUTTON";
	
	public InterObjectCommunicator oComm;
		
	boolean controlPanelIsVisible = true;
	ControlPanelSuperPanel controlPanelSuperPanel;
	
	public HideControlsButton(ControlPanelSuperPanel myControlPanelSuperPanel, InterObjectCommunicator myOComm) {
		super("►");
		
		this.controlPanelSuperPanel = myControlPanelSuperPanel;
		oComm = myOComm;
		oComm.setInterObjectCommunicatorEventListener(this);
		
		this.setBorder(new BorderMaker(BorderMaker.RAISEDBEVEL, 0, 1));
		this.setPreferredSize(new Dimension(18,4000));
		
		ActionListener hideControlsActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Toggle();
			}
		};
		
		addActionListener(hideControlsActionListener);
	}
	
	
	public void Toggle() {
		controlPanelIsVisible = !controlPanelIsVisible;
		if(controlPanelIsVisible) {
			setText("►");
		} else {
			setText("◄");
		}
		controlPanelSuperPanel.toggleHide();
	}

	@Override
	public void onInterObjectCommunicator_CommunicateEvent(Object o) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInterObjectCommunicator_CommunicateEvent(String descriptor, Object o) {
		if (descriptor.equals("toggle controls")) {
			if (o instanceof KeyEvent) {
				KeyEvent e = ((KeyEvent) o);
				if (e.getKeyCode() == KeyEvent.VK_Q) {
					Toggle();
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
	public Object onInterObjectCommunicator_RequestEvent(String descriptor, Object o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getObjectName() {
		return objectName;
	}
}
