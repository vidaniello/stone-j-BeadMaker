package beadMaker;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import core.ConsoleHelper;

public class BMKeyListener implements KeyListener {
	@Override
    public void keyTyped(KeyEvent e) {
		ConsoleHelper.PrintMessage("fired a key event " + e.getKeyCode());
    }

    @Override
    public void keyPressed(KeyEvent e) {
        ConsoleHelper.PrintMessage("fired a key event " + e.getKeyCode());
//        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
//        	dragPoint = null;
//    		repaint();
//        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
