package beadMaker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import beadMaker.HelperClasses.XMLWorker;
import core.ConsoleHelper;
import core.ExceptionLogger;
import core.FileHelper;
import core.InterObjectCommunicatorEventListener;
import core.StringHelper;

public class BeadMaker implements InterObjectCommunicatorEventListener
{	
	/*
	 TODO:
	 ------------------------------------------------------------------------------------------------------
	 ------------- 
	 BUGS:
	 ------------- 
	 FIXED 2019-06-03 When project file is saved, the image file used is not correct
	 FIXED Transparent beads are being shown as opaque beads (load FF1 Frost dragon, see yellow bg pixels)
	 When pallette gets reloaded/recreated (change color dials, etc.) the selected color bolding does not display
	 FIXED When exporting PDF with only Artkal beads in pallette, the PDF gets messed up (multiple pages with same beads highlighted, omitted pages, etc)
	 2021-03-13 open project --> change color values --> "save project" brings up "SAVE AS" dialog-- should just save with no dialog
	 ------------------------------------------------------------------------------------------------------
	 ------------- 
	 FEATURES:
	 ------------- 
	 Mouse over beads should show color somewhere
	 project stats window somewhere
	 reorganize/clean up controls (watch out for regression)	 
	 */
	
	//For InterObjectCommunicator identification
	private String objectName = "BEAD_MAKER";
	
	private final boolean useAppData = true;
	private final String appDataFolderName = "Nostalgic Pixels Pixel Perfect";
	
	public ImageController imageController;
	public Palette pallette;
	public WindowController windowController;
	public XMLWorker xmlWorker;
	public InterObjectCommunicator oComm;
	 
	public ControlPanel controlPanel = null;
	public BMenuBar bMenuBar;
	
	public ShowAllColorsButtonPanel showAllColorsButtonPanel;
	
	public HoverLabel totalBeadsUsedLabel;
	
	ExceptionLogger exceptionLogger;
	
	public ControlPanelScrollPane controlPanelScrollPane;
	public ControlPanelSuperPanel controlPanelSuperPanel;
	
	private FileHelper fileHelper;
			
	public static void main(String[] args) throws Exception
	{
		new BeadMaker(args);
	}

	//------------------------------------------------------------
	//CONSTRUCTOR
	//------------------------------------------------------------
	public BeadMaker(String[] args) throws Exception
	{
		exceptionLogger = new ExceptionLogger(useAppData, appDataFolderName);
		
		exceptionLogger.logMessage("LIST OF ARGS:");
		for (int i = 0; i < args.length; i++) 
		{ 
			exceptionLogger.logMessage("Argument " + Integer.toString(i) + " = " + args[i]);
		}
		exceptionLogger.logMessage("--END LIST OF ARGS--");		
		
		
		oComm = new InterObjectCommunicator();
		oComm.setInterObjectCommunicatorEventListener(this);
		
		windowController = new WindowController("Pixel Perfect", oComm, useAppData, appDataFolderName);	
		
		xmlWorker = new XMLWorker(useAppData, appDataFolderName);
		
		fileHelper = new FileHelper(useAppData, appDataFolderName);
				
		pallette = new Palette(xmlWorker.GetAbsoluteFilePathStringFromXml("defaultPallette", xmlWorker.configXML, useAppData, appDataFolderName), xmlWorker, oComm);
		
		imageController = new ImageController(this, pallette, windowController, oComm, useAppData, appDataFolderName);
		
		showAllColorsButtonPanel = new ShowAllColorsButtonPanel("<html>Show All <u>C</u>olors</html>", oComm);
		
		TotalBeadsUsedPanel totalBeadsUsedPanel = new TotalBeadsUsedPanel("Total Beads Used:");
		
		totalBeadsUsedLabel = new HoverLabel("Total Beads Used: ");
		totalBeadsUsedLabel.bgColor = new Color(254,254,254);
		totalBeadsUsedLabel.setOpaque(false);
		
		totalBeadsUsedPanel.add(totalBeadsUsedLabel);
		
		
		//this key listener currently does nothing.
		//In order to support key commands,
		//I believe code will need to be added to the BMKeyListener
		//windowController.addKeyListener(new BMKeyListener());
		
		JPanel palletteSuperPanel = new JPanel();
		palletteSuperPanel.setPreferredSize(new Dimension(300,1));
		palletteSuperPanel.setLayout(new BoxLayout(palletteSuperPanel, BoxLayout.Y_AXIS));
		palletteSuperPanel.setOpaque(false);
		
		palletteSuperPanel.add(Box.createRigidArea(new Dimension(0,12)));
		
		//try {
		palletteSuperPanel.add(showAllColorsButtonPanel);
		//} catch(NullPointerException e) {
		//	ConsoleHelper.PrintMessage("NULL POINTER EXCEPTION: controlPanel.showAllColorsButtonPanel");
		//}
			
		palletteSuperPanel.add(totalBeadsUsedPanel);		
				
		//ConsoleHelper.PrintMessage("renderPanel width = " + Integer.toString(imageController.renderJPanel.getBounds().width));
		//ConsoleHelper.PrintMessage("renderPanel height = " + Integer.toString(imageController.renderJPanel.getBounds().height));
		PalletteScrollPane palletteScrollPane = new PalletteScrollPane(pallette.pallettePanel);
		palletteSuperPanel.add(palletteScrollPane);
			
		controlPanel = new ControlPanel(imageController, pallette, xmlWorker, oComm, useAppData, appDataFolderName);
		if (GlobalConstants.applyLUT == 1) {
			controlPanel.setPreferredSize(new Dimension(260,1038));
		} else {
			controlPanel.setPreferredSize(new Dimension(260,974));
		}
		
		controlPanelScrollPane = new ControlPanelScrollPane(controlPanel);
		controlPanelScrollPane.setPreferredSize(new Dimension(510,1));
		
		controlPanelSuperPanel = new ControlPanelSuperPanel();
		controlPanelSuperPanel.setPreferredSize(new Dimension(528,1));
		controlPanelSuperPanel.setLayout(new BoxLayout(controlPanelSuperPanel, BoxLayout.X_AXIS));
		
		JPanel hideControlsButtonPanel = new JPanel();
		hideControlsButtonPanel.setPreferredSize(new Dimension(18,4000));
		hideControlsButtonPanel.setLayout(new BoxLayout(hideControlsButtonPanel, BoxLayout.Y_AXIS));
		
		HideControlsButton hideControlsButton = new HideControlsButton(controlPanelSuperPanel, oComm);
				
		hideControlsButtonPanel.add(Box.createRigidArea(new Dimension(1,4)));
		hideControlsButtonPanel.add(Box.createVerticalGlue());
		hideControlsButtonPanel.add(hideControlsButton);
		hideControlsButtonPanel.add(Box.createVerticalGlue());
		hideControlsButtonPanel.add(Box.createRigidArea(new Dimension(1,4)));
		
		controlPanelSuperPanel.add(hideControlsButtonPanel);
		controlPanelSuperPanel.add(controlPanelScrollPane);
		
		windowController.add(palletteSuperPanel, BorderLayout.LINE_START);		
		windowController.add(imageController.renderScrollPanel, BorderLayout.CENTER);		
		//windowController.add(controlPanel, BorderLayout.LINE_END);
		//windowController.add(controlPanelScrollPane, BorderLayout.LINE_END);
		windowController.add(controlPanelSuperPanel, BorderLayout.LINE_END);
		
		bMenuBar = new BMenuBar(xmlWorker.configXML, xmlWorker, imageController, controlPanel, this, oComm, useAppData, appDataFolderName);		
		
		//if a PBP file was opened, try to load it
		boolean loadedInputFile = false;
		if(args.length > 0) {
			String inputFileExtension = fileHelper.getExtension(args[0]);
			if(inputFileExtension.equals("pbp")) {
				bMenuBar.LoadProject(args[0], true);
				loadedInputFile = true;
			}
		}
		//otherwise, load the default project
		if (!loadedInputFile) {
			bMenuBar.LoadProject(xmlWorker.GetAbsoluteFilePathStringFromXml("defaultProjectFilePath", xmlWorker.configXML, useAppData, appDataFolderName), false);
		}
		
		windowController.setMenuBar(bMenuBar);
		
		//------------------------------------------------------------------------
		//Turn this chunk on to set the zoom so that the image fills the panel
		//------------------------------------------------------------------------
		/*
		windowController.pack();
		ConsoleHelper.PrintMessage("renderScrollPanel width = " + imageController.renderScrollPanel.getWidth() + ";  renderScrollPanel height = " + imageController.renderScrollPanel.getHeight());
		int bestFitZoomValue = MathHelper.getMinimumValue(
			imageController.renderScrollPanel.getWidth()  * 100 / imageController.originalCleanedImage.width,
			imageController.renderScrollPanel.getHeight() * 100 / imageController.originalCleanedImage.height
		);
		imageController.setColorMatchingWeight(
			"Zoom %",
			bestFitZoomValue
		);
		controlPanel.sliderZoom.setValue(bestFitZoomValue);
		*/
		//------------------------------------------------------------------------
		//------------------------------------------------------------------------
		
		windowController.setLocationRelativeTo(null);
		windowController.setVisible(true);
		//https://stackoverflow.com/questions/13202593/jframe-is-very-tiny-when-restore-down-is-pressed
		windowController.setExtendedState(windowController.getExtendedState() | JFrame.MAXIMIZED_BOTH);
				
		//need to call this after the window has been packed, otherwise the render panel reports its size as 0,0
		imageController.updateImages();
	}
	
	
	@Override
	public void onInterObjectCommunicator_CommunicateEvent(Object o) {
//		if (o instanceof String) {
//			String s = ((String) o);
//			ConsoleHelper.PrintMessage("InterObjectCommunicatorEvent in BeadMaker with object type String: <" + StringHelper.getLeftString(s, 16) + ">");
//			if (StringHelper.getLeftString(s, 16).equals("Total Beads Used")) {
//	        	totalBeadsUsedLabel.setText(s);
//	        }
//		}		
	}
	
	
	@Override
	public void onInterObjectCommunicator_CommunicateEvent(String descriptor, Object o) {
		if (descriptor.equals("totals")) {
			if (o instanceof String) {
				String s = ((String) o);
				//ConsoleHelper.PrintMessage("InterObjectCommunicatorEvent in BeadMaker with object type String: <" + StringHelper.getLeftString(s, 16) + ">");
				totalBeadsUsedLabel.setText(s);
			}
		}		
	}
	

	@Override
	public String getObjectName() {
		return objectName;
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

	
}



