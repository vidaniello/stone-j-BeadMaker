package beadMaker;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import core.logging.ConsoleHelper;

public class BMKeyListener implements KeyListener {
	
	ConsoleHelper consoleHelper = new ConsoleHelper();
	
	@Override
    public void keyTyped(KeyEvent e) {
		consoleHelper.PrintMessage("fired a key event " + e.getKeyCode());
    }

    @Override
    public void keyPressed(KeyEvent e) {
        consoleHelper.PrintMessage("fired a key event " + e.getKeyCode());
//        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
//        	dragPoint = null;
//    		repaint();
//        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
