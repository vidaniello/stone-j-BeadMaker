package beadMaker;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import core.ConsoleHelper;

public class WindowController extends JFrame {

	public InterObjectCommunicator oComm;
	
	private boolean useAppData;
	private String appDataFolderName;
		
	public WindowController (
		String frameTitle,
		InterObjectCommunicator myOComm,
		boolean myUseAppData,
		String myAppDataFolderName
	) {
		
		super(frameTitle);
		
		this.useAppData = myUseAppData;
		this.appDataFolderName = myAppDataFolderName;
		
		oComm = myOComm;
		//this.setPreferredSize (new Dimension(900, 600));
		//this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(1600, 1000));
		//this.setState(Frame.NORMAL);
		//this.setVisible(true);
		try {
			if (useAppData) {
				this.setIconImage(ImageIO.read(new File(System.getenv("APPDATA") + File.separator + appDataFolderName + File.separator + "icon" + File.separator + "BeadMakerIcon.png")));
			} else {
				this.setIconImage(ImageIO.read(new File(System.getProperty("user.dir") + File.separator + "icon" + File.separator + "BeadMakerIcon.png")));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.pack();
		
		KeyboardListener kl = new KeyboardListener(oComm);
	}
	
	
	//---------------------------------------------------------------------------
	// add
	//---------------------------------------------------------------------------
	public void add(JLabel jLabel, String borderLayout) {
		this.getContentPane().add(jLabel, borderLayout);
	}
	
	
	//---------------------------------------------------------------------------
	// SetCursorState
	//---------------------------------------------------------------------------
	void SetCursorState(int myCursorMode) {
		this.setCursor(Cursor.getPredefinedCursor(myCursorMode));
	}
	
	
	//-------------------------------------------------------
	//SetFrameTitle
	//-------------------------------------------------------
	public void SetFrameTitle(String frameTitle) {
		this.setTitle(frameTitle);
	}
}
