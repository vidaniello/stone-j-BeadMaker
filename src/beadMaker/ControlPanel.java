package beadMaker;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import processing.data.XML;
import beadMaker.ImageController.PegboardMode;
import core.BorderMaker;
import core.ConsoleHelper;
import core.InterObjectCommunicatorEventListener;
import core.JComboBoxMaker;
import core.JPanelPaintable;
import core.MathHelper;
import beadMaker.HelperClasses.XMLWorker;

public class ControlPanel extends JPanel implements InterObjectCommunicatorEventListener {

	//For InterObjectCommunicator identification
	private String objectName = "CONTROL_PANEL";
		
	public InterObjectCommunicator oComm;
	public ImageController imageController;
	public Palette pallette;
	public XMLWorker xMLHelper;
	
	public JPanel controlPanel;
	//public JPanel controlPanel2;
	
	public ControlLabel labelRed;
	public ControlLabel labelGreen;
	public ControlLabel labelBlue;
	public ControlLabel labelBrightness;
	public ControlLabel labelContrast;
	public ControlLabel labelSaturation;
	public ControlLabel labelDither;
	public ControlLabel labelSharpness;
	public ControlLabel labelScale;
	public ControlLabel labelZoom;

	public BMSlider sliderRed = null;
	public BMSlider sliderGreen = null;
	public BMSlider sliderBlue = null;
	public BMSlider sliderBrightness = null;
	public BMSlider sliderContrast = null;
	public BMSlider sliderSaturation = null;
	public BMSlider sliderDither = null;
	public BMSlider sliderSharpness = null;
	public BMSlider sliderScale = null;
	public BMSlider sliderZoom = null;
	
	public BMTextField redValue;
	public BMTextField greenValue;	
	public BMTextField blueValue;
	public BMTextField brightnessValue;
	public BMTextField contrastValue;
	public BMTextField saturationValue;
	public BMTextField ditherValue;
	public BMTextField sharpnessValue;
	public BMTextField scaleValue;
	public BMTextField zoomValue;
	
	public ControlPanelSubPanel redPanel;
	public ControlPanelSubPanel greenPanel;
	public ControlPanelSubPanel bluePanel;
	public ControlPanelSubPanel brightnessPanel;
	public ControlPanelSubPanel contrastPanel;
	public ControlPanelSubPanel saturationPanel;
	public ControlPanelSubPanel ditherPanel;
	public ControlPanelSubPanel sharpnessPanel;
	public ControlPanelSubPanel scalePanel;
	public ControlPanelSubPanel zoomPanel;
	public ControlPanelSubPanel lutPanel;
	
	public JLabel ditherMethodLabel;
	public JLabel customPalletteLabel;
	public JLabel pegboardSizeLabel;
	public JLabel lutLabel;
	
	public HoverLabel hoveredPixelColor;
	public JPanel hoverPanel;
	
	ControlPanelComboPanel ditherComboPanel;
	ControlPanelComboPanel scaleComboPanel;
	ControlPanelComboPanel palletteComboPanel;
	ControlPanelComboPanel pegboardComboPanel;
	ControlPanelComboPanel randomizeParametersComboPanel;
	
	//public ControlPanelSubPanel palletteModeControlPanel;
	
	public CheckBoxPanel perlerCheckboxPanel;
	public CheckBoxPanel artkalCheckboxPanel;
	public CheckBoxPanel hamaCheckboxPanel;
	
	public CheckBoxPanel showGridCheckboxPanel;
	public CheckBoxPanel renderPixelsAsBeadsCheckboxPanel;
	
	public CheckBoxPanel excludePearlsCheckboxPanel;
	public CheckBoxPanel excludeTranslucentsCheckboxPanel;

	public ColorChangerButtonPanel bgColorButtonPanel;
	public ColorChangerButtonPanel gridColorButtonPanel;
	
	public CheckBoxPanel flipImageCheckboxPanel;
	
	
	public JComboBox<String> ditherMethod;
	public JComboBox<String> customPallette;
	public JComboBox<String> pegboardSize;
	public JComboBox<String> lutSelector;
	
	public final String[][] customPalletteFiles;
	//public final String[] customPalletteNames;
	public final String[][] lutFiles;
	
	public final int FORWARD = 1;
	public final int BACKWARD = 0;
	
	public int currentRandomizerSeedIndex = 50;
	public ArrayList<Integer> randomizerSeeds = new ArrayList<>();
	
	public final JLabel randomizerSeedNumber;
	
	public boolean expertMode = true;
	
	ConsoleHelper consoleHelper = new ConsoleHelper();
	

	ControlPanel(ImageController myImageController, Palette myPallette, XMLWorker myXMLHelper, InterObjectCommunicator myOComm) {
				
		oComm = myOComm;
		oComm.setInterObjectCommunicatorEventListener(this);
		
		ActionListener ditherMethodActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				imageController.setDitherMethod(ImageController.DitherMethod.values()[ditherMethod.getSelectedIndex()]);
				pallette.CreateButtons("ditherMethodActionListener");
			}
		};
		
		ActionListener customPalletteActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				consoleHelper.PrintMessage(System.getProperty("user.dir") + "\\" + customPalletteFiles[0][customPallette.getSelectedIndex()]);
						
				pallette.GetPalletteFromXml(System.getProperty("user.dir") + "\\pallettes\\" + customPalletteFiles[0][customPallette.getSelectedIndex()], xMLHelper);
				imageController.selectedColorIndex = -1;
				pallette.GetPalletteWithFiltersApplied();
				imageController.updateImages();
				pallette.CreateButtons("customPalletteActionListener");
			}
		};
		
		ActionListener pegboardSizeActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				imageController.setPegboardMode(PegboardMode.values()[pegboardSize.getSelectedIndex()]);
			}
		};
		
		ActionListener lutActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				imageController.loadLUT(System.getProperty("user.dir") + "\\LUTs\\" + lutFiles[0][lutSelector.getSelectedIndex()]);
				imageController.updateImages();
			}
		};
		
		
		//BoxLayout.Y_AXIS displays objects in a vertical sequence
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		//this.setBorder(new BorderMaker(BorderMaker.RAISEDBEVEL, 4, 16));
		this.setBorder(new BorderMaker(BorderMaker.NONE, 16, 1));
		//this.setBorder(BorderFactory.createRaisedBevelBorder());

		this.imageController = myImageController;
		this.pallette = myPallette;
		this.xMLHelper = myXMLHelper;
		
		setExpertMode(xMLHelper.GetIntFromXml("expertMode", xMLHelper.configXML) == 1 ? true : false);
		
//		controlPanel = new JPanel();
//		//controlPanel2 = new JPanel();
//
//		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		//controlPanel1.setBackground(new Color(125, 188, 233));
		//controlPanel2.setLayout(new BoxLayout(controlPanel2, BoxLayout.Y_AXIS));
		//controlPanel2.setBackground(new Color(233, 188, 152));

		//controlPanel2.setPreferredSize(new Dimension(280, 800));

		labelRed 			= new ControlLabel("Red");
		labelGreen 			= new ControlLabel("Green");
		labelBlue 			= new ControlLabel("Blue");
		labelBrightness 	= new ControlLabel("Brightness");
		labelContrast 		= new ControlLabel("Contrast");
		labelSaturation 	= new ControlLabel("Saturation");
		labelDither 		= new ControlLabel("Dither");
		labelSharpness 		= new ControlLabel("Sharpness");
		labelScale 			= new ControlLabel("Scale %");
		labelZoom 			= new ControlLabel("Zoom");

		redValue 			= new BMTextField("0", 3, sliderRed);
		greenValue 			= new BMTextField("0", 3, sliderGreen);		
		blueValue 			= new BMTextField("0", 3, sliderBlue);
		brightnessValue 	= new BMTextField("0", 3, sliderBrightness);
		contrastValue 		= new BMTextField("0", 3, sliderContrast);
		saturationValue 	= new BMTextField("0", 3, sliderSaturation);
		ditherValue 		= new BMTextField("0", 3, sliderDither);
		sharpnessValue 		= new BMTextField("0", 3, sliderSharpness);
		scaleValue 			= new BMTextField("100", 3, sliderScale);
		zoomValue 			= new BMTextField("500", 3, sliderZoom);
		
		redValue			.setMaximumSize(new Dimension(30, 20));
		greenValue			.setMaximumSize(new Dimension(30, 20));
		blueValue			.setMaximumSize(new Dimension(30, 20));
		brightnessValue		.setMaximumSize(new Dimension(30, 20));
		contrastValue		.setMaximumSize(new Dimension(30, 20));
		saturationValue		.setMaximumSize(new Dimension(30, 20));
		ditherValue			.setMaximumSize(new Dimension(30, 20));
		sharpnessValue		.setMaximumSize(new Dimension(30, 20));
		scaleValue			.setMaximumSize(new Dimension(30, 20));
		zoomValue			.setMaximumSize(new Dimension(30, 20));

		sliderRed 			= new BMSlider ( -50,   50, "Red", 			imageController, redValue, oComm); 
		sliderGreen 		= new BMSlider ( -50,   50, "Green", 		imageController, greenValue, oComm);
		sliderBlue 			= new BMSlider ( -50,   50, "Blue", 		imageController, blueValue, oComm);
		sliderBrightness	= new BMSlider ( -50,   50, "Brightness", 	imageController, brightnessValue, oComm);
		sliderContrast		= new BMSlider ( -50,   50, "Contrast", 	imageController, contrastValue, oComm);
		sliderSaturation	= new BMSlider ( -50,   50, "Saturation", 	imageController, saturationValue, oComm);
		sliderDither		= new BMSlider (   0,  100, "Dither", 		imageController, ditherValue, 0, oComm);
		sliderSharpness		= new BMSlider (   0,  100, "Sharpness", 	imageController, sharpnessValue, 0, oComm);
		sliderScale			= new BMSlider (  10,  200, "Scale %", 		imageController, scaleValue, 100, oComm);
		sliderZoom			= new BMSlider ( 100, 3500, "Zoom %", 		imageController, zoomValue, 500, false, oComm);

		redPanel 			= new ControlPanelSubPanel(new Color(217,191,191));
		greenPanel 			= new ControlPanelSubPanel(new Color(191,217,191));
		bluePanel 			= new ControlPanelSubPanel(new Color(191,191,217));
		brightnessPanel 	= new ControlPanelSubPanel(new Color(204,204,204));
		contrastPanel 		= new ControlPanelSubPanel(new Color(204,204,204));
		saturationPanel 	= new ControlPanelSubPanel(new Color(204,204,204));
		ditherPanel 		= new ControlPanelSubPanel(new Color(204,204,204), false);
		sharpnessPanel 		= new ControlPanelSubPanel(new Color(204,204,204));
		scalePanel 			= new ControlPanelSubPanel(new Color(204,204,204), false);
		zoomPanel 			= new ControlPanelSubPanel(new Color(160,160,160));
		lutPanel 			= new ControlPanelSubPanel(new Color(204,204,204));
		
		
		//ControlPanelSubPanel palletteControlsPanel = new ControlPanelSubPanel(new Color(128,55,192), BoxLayout.Y_AXIS);

		String[] ditherMethods = {
				"Floyd-Steinberg",
				"Jarvis, Judice, Ninke",
				"Stucki",
				"Atkinson",
				"Burkes",
				"Sierra",
				"Two-Row Sierra",
				"Sierra Lite"
		};
		
		
		// create new filename filter
        FilenameFilter fileNameFilter = new FilenameFilter() {
  
           @Override
           public boolean accept(File dir, String name) {
              if(name.lastIndexOf('.')>0) {
              
                 // get last index for '.' char
                 int lastIndex = name.lastIndexOf('.');
                 
                 // get extension
                 String str = name.substring(lastIndex);
                 
                 // match path name extension
                 if(str.equals(".xml")) {
                    return true;
                 }
              }
              
              return false;
           }
        };

		File pallettePath = new File(System.getProperty("user.dir"), "pallettes");
		consoleHelper.PrintMessage(pallettePath.toString());
		File[] listOfPalletteFiles = pallettePath.listFiles(fileNameFilter);
		customPalletteFiles = new String[2][listOfPalletteFiles.length]; //index 0 = Filename, index 1 = common name
		//customPalletteNames = new String[listOfPalletteFiles.length];

		for (int i = 0; i < listOfPalletteFiles.length; i++) {			
			customPalletteFiles[0][i] = listOfPalletteFiles[i].getName();
			XML[] customPalletteXML = xMLHelper.GetXMLFromFile(listOfPalletteFiles[i].toString());
			customPalletteFiles[1][i] = xMLHelper.GetDataFromXml("palletteName", customPalletteXML);
		}
		
		
		FilenameFilter lutFileNameFilter = new FilenameFilter() {
			  
           @Override
           public boolean accept(File dir, String name) {
              if(name.lastIndexOf('.')>0) {
              
                 // get last index for '.' char
                 int lastIndex = name.lastIndexOf('.');
                 
                 // get extension
                 String str = name.substring(lastIndex);
                 
                 // match path name extension
                 if(str.equals(".png")) {
                    return true;
                 }
              }
              
              return false;
           }
        };
        
        File lutPath = new File(System.getProperty("user.dir"), "LUTs");
        consoleHelper.PrintMessage(lutPath.toString());
		File[] listOfLUTFiles = lutPath.listFiles(lutFileNameFilter);
		lutFiles = new String[2][listOfLUTFiles.length]; //index 0 = Filename, index 1 = common name

		for (int i = 0; i < listOfLUTFiles.length; i++) {			
			lutFiles[0][i] = listOfLUTFiles[i].getName();
			lutFiles[1][i] = listOfLUTFiles[i].getName();
		}
		
		
		String[] pegboardSizes = {
				"Perler Large Pegboard - 29w x 29h",
				"Perler Super Pegboard (Portrait) - 49w x 69h",
				"Perler Super Pegboard (Landscape) - 69w x 49h",
				"Perler Mini Pegboard - 28w x 28h",
				"Custom - 40w x 40h",
				"Custom - 41w x 49h",
				"Custom - 40w x 48h",
				"Lego Tile 8x8"
		};		
		

		ditherMethod 	= JComboBoxMaker.makeJComboBox(ditherMethods);
		customPallette 	= JComboBoxMaker.makeJComboBox(customPalletteFiles[1]);
		pegboardSize 	= JComboBoxMaker.makeJComboBox(pegboardSizes);
		lutSelector 	= JComboBoxMaker.makeJComboBox(lutFiles[1]);
		
		//this stuff is handled by LoadProject now
//		for (int i = 0; i < customPalletteFiles.length; i++) {
//			if (customPalletteFiles[i].equals(XMLHelper.GetDataFromXml("defaultPallette", xmlHelper.configXML).replace("pallettes\\", ""))) {
//				customPallette.setSelectedIndex(i);
//			}			
//		}

		ditherMethod	.addActionListener(ditherMethodActionListener);
		customPallette	.addActionListener(customPalletteActionListener);
		pegboardSize	.addActionListener(pegboardSizeActionListener);
		lutSelector		.addActionListener(lutActionListener);


		//		palletteMode.addItemListener(new ItemChangeListener());
		//		ditherMethod.addItemListener(new ItemChangeListener());
		//		customPallette.addItemListener(new ItemChangeListener());
		//pegboardSize.addItemListener(itemListener);

		//ditherMethodLabel 	= new JLabel("Dithering");
		customPalletteLabel = new JLabel("Pallette");
		pegboardSizeLabel 	= new JLabel("Pegboard Size");
		lutLabel 			= new JLabel("LUT");
		
//		ditherMethodLabel	.setPreferredSize(new Dimension(90, 30));
		customPalletteLabel	.setPreferredSize(new Dimension(90, 30));
		pegboardSizeLabel	.setPreferredSize(new Dimension(90, 30));
		lutLabel			.setPreferredSize(new Dimension(90, 30));
		
		//ditherMethodLabel	.setMinimumSize(new Dimension(80, 30));
		//customPalletteLabel	.setMinimumSize(new Dimension(120, 30));
		//pegboardSizeLabel	.setMinimumSize(new Dimension(80, 30));
		
		//ditherMethodLabel	.setMaximumSize(new Dimension(80, 30));
		//customPalletteLabel	.setMaximumSize(new Dimension(120, 30));
		//pegboardSizeLabel	.setMaximumSize(new Dimension(80, 30));
				
		//hoverPanel = new JPanelPaintable(new Color(255,0,0));
		hoverPanel = new JPanelPaintable();
		hoverPanel.setLayout(new BoxLayout(hoverPanel, BoxLayout.X_AXIS));
		hoverPanel.setBorder(new BorderMaker(BorderMaker.NONE, 0, 2, 0, 0, 0));
		//hoverPanel.setOpaque(false);
		
		hoveredPixelColor	= new HoverLabel("");
		//hoveredPixelColor.setPreferredSize	(new Dimension(240, 60));
		//hoveredPixelColor.setMaximumSize	(new Dimension(240, 60));
	
		//hoverPanel.add(Box.createRigidArea(new Dimension(4,0)));
		//hoverPanel.add(Box.createHorizontalGlue());
		hoverPanel.add(hoveredPixelColor);
		//hoverPanel.add(Box.createHorizontalGlue());

		//ditherMethodLabel	.setAlignmentX(Component.CENTER_ALIGNMENT);
		customPalletteLabel	.setAlignmentX(Component.RIGHT_ALIGNMENT);
		customPalletteLabel	.setHorizontalTextPosition(JLabel.LEFT);
		pegboardSizeLabel	.setAlignmentX(Component.CENTER_ALIGNMENT);
		pegboardSizeLabel	.setHorizontalTextPosition(JLabel.LEFT);
		lutLabel			.setAlignmentX(Component.RIGHT_ALIGNMENT);
		lutLabel			.setHorizontalTextPosition(JLabel.LEFT);

		//palletteModeControlPanel = new ControlPanelSubPanel(new Color(204,204,204));

		//palletteModeControlPanel.setLayout(new BoxLayout(palletteModeControlPanel, BoxLayout.Y_AXIS));

		//palletteModeControlPanel.setPreferredSize(new Dimension(360, 800));
		//palletteModeControlPanel.setMaximumSize(new Dimension(360, 800));

		perlerCheckboxPanel 	= new CheckBoxPanel("<html><u>P</u>erler</html>", true, pallette, imageController, oComm);
		artkalCheckboxPanel 	= new CheckBoxPanel("<html><u>A</u>rtkal-S</html>", pallette, imageController, oComm);
		hamaCheckboxPanel 		= new CheckBoxPanel("Hama", pallette, imageController, oComm);
		
		showGridCheckboxPanel 				= new CheckBoxPanel("<html>Show <u>G</u>rid</html>", true, imageController, oComm);
		renderPixelsAsBeadsCheckboxPanel 	= new CheckBoxPanel("<html>Show Pixels as <u>B</u>eads</html>", false, imageController, oComm);
		
		excludePearlsCheckboxPanel			= new CheckBoxPanel("Exclude Pearls", true, pallette, imageController, oComm);
		excludeTranslucentsCheckboxPanel 	= new CheckBoxPanel("Exclude Translucents", true, pallette, imageController, oComm);

		
		bgColorButtonPanel 			= new ColorChangerButtonPanel("Background Color", 	imageController);
		gridColorButtonPanel 		= new ColorChangerButtonPanel("Grid Color", 		imageController, imageController.renderLabel);
		
		flipImageCheckboxPanel 	= new CheckBoxPanel("<html><u>F</u>lip Image</html>", false, pallette, imageController, oComm);
		//flipImageCheckboxPanel 	= new CheckBoxPanel("Flip Image", false, pallette, imageController, oComm);
		

		ComboBoxPanel ditherMethodPanel = new ComboBoxPanel();
		ComboBoxPanel customPallettePanel = new ComboBoxPanel();
		ComboBoxPanel pegboardSizePanel = new ComboBoxPanel();
		ComboBoxPanel scaleSliderPanel = new ComboBoxPanel();
		
		ditherComboPanel = new ControlPanelComboPanel(new Color(204,204,204), true);
		ditherComboPanel.add(Box.createRigidArea(new Dimension(0,4)));
		ditherComboPanel.add(ditherPanel);
		ditherComboPanel.add(Box.createRigidArea(new Dimension(0,16)));
		ditherComboPanel.add(ditherMethodPanel);
		
		scaleComboPanel = new ControlPanelComboPanel(new Color(204,204,204), true);
		scaleComboPanel.add(Box.createRigidArea(new Dimension(0,4)));
		scaleComboPanel.add(scalePanel);
		scaleComboPanel.add(Box.createRigidArea(new Dimension(0,16)));
		scaleComboPanel.add(scaleSliderPanel);
		
		JPanel flipImageCheckboxPanelBottom = new JPanel();
		flipImageCheckboxPanelBottom.setLayout(new BoxLayout(flipImageCheckboxPanelBottom, BoxLayout.X_AXIS));
		flipImageCheckboxPanelBottom.setOpaque(false);
		
		ControlPanelComboPanel flipImageCheckboxHorizontalPanelLeft = new ControlPanelComboPanel(new Color(160,160,160), false);
		ControlPanelComboPanel flipImageCheckboxHorizontalPanelRight = new ControlPanelComboPanel(new Color(160,160,160), false);
		
		flipImageCheckboxHorizontalPanelLeft.add(flipImageCheckboxPanel);
		flipImageCheckboxHorizontalPanelLeft.add(Box.createRigidArea(new Dimension(0,4)));
		//flipImageCheckboxHorizontalPanelLeft.add(xxxartkalCheckboxPanel);
		flipImageCheckboxHorizontalPanelLeft.setOpaque(false);

		
//		flipImageCheckboxHorizontalPanelRight.add(excludePearlsCheckboxPanel);
//		flipImageCheckboxHorizontalPanelRight.add(Box.createRigidArea(new Dimension(0,4)));
//		flipImageCheckboxHorizontalPanelRight.add(excludeTranslucentsCheckboxPanel);
		
		flipImageCheckboxPanelBottom.add(flipImageCheckboxHorizontalPanelLeft);
		flipImageCheckboxPanelBottom.add(flipImageCheckboxHorizontalPanelRight);
		
		scaleComboPanel.add(flipImageCheckboxPanelBottom);
		
		//-------------------------------------------------------------------------------
		//-------------------------------------------------------------------------------
		//-------------------------------------------------------------------------------
		palletteComboPanel = new ControlPanelComboPanel(new Color(160,160,160), true);
		palletteComboPanel.setPreferredSize(new Dimension(300,110));
		palletteComboPanel.add(Box.createRigidArea(new Dimension(0,8)));
		palletteComboPanel.add(customPallettePanel);
		palletteComboPanel.add(Box.createRigidArea(new Dimension(0,8)));
		
		JPanel palletteCheckboxPanelBottom = new JPanel();
		palletteCheckboxPanelBottom.setLayout(new BoxLayout(palletteCheckboxPanelBottom, BoxLayout.X_AXIS));
		palletteCheckboxPanelBottom.setOpaque(false);
		
		ControlPanelComboPanel palletteCheckboxHorizontalPanelLeft = new ControlPanelComboPanel(new Color(160,160,160), false);
		ControlPanelComboPanel palletteCheckboxHorizontalPanelRight = new ControlPanelComboPanel(new Color(160,160,160), false);
		
		palletteCheckboxHorizontalPanelLeft.add(perlerCheckboxPanel);
		palletteCheckboxHorizontalPanelLeft.add(Box.createRigidArea(new Dimension(0,4)));
		palletteCheckboxHorizontalPanelLeft.add(artkalCheckboxPanel);
		
		palletteCheckboxHorizontalPanelRight.add(excludePearlsCheckboxPanel);
		palletteCheckboxHorizontalPanelRight.add(Box.createRigidArea(new Dimension(0,4)));
		palletteCheckboxHorizontalPanelRight.add(excludeTranslucentsCheckboxPanel);
		
		palletteCheckboxPanelBottom.add(palletteCheckboxHorizontalPanelLeft);
		palletteCheckboxPanelBottom.add(palletteCheckboxHorizontalPanelRight);
		
		
		palletteComboPanel.add(palletteCheckboxPanelBottom);
		//-------------------------------------------------------------------------------
		//-------------------------------------------------------------------------------
		//-------------------------------------------------------------------------------
		
		pegboardComboPanel = new ControlPanelComboPanel(new Color(160,160,160), true);
		pegboardComboPanel.setPreferredSize(new Dimension(300,110));
		pegboardComboPanel.add(Box.createRigidArea(new Dimension(0,8)));
		pegboardComboPanel.add(pegboardSizePanel);
		pegboardComboPanel.add(Box.createRigidArea(new Dimension(0,8)));
		
		JPanel pegboardCheckboxPanelBottom = new JPanel();
		pegboardCheckboxPanelBottom.setLayout(new BoxLayout(pegboardCheckboxPanelBottom, BoxLayout.X_AXIS));
		pegboardCheckboxPanelBottom.setOpaque(false);
		
		ControlPanelComboPanel pegboardCheckboxHorizontalPanelLeft = new ControlPanelComboPanel(new Color(160,160,160), false);
		ControlPanelComboPanel pegboardCheckboxHorizontalPanelRight = new ControlPanelComboPanel(new Color(160,160,160), false);
		
		pegboardCheckboxHorizontalPanelLeft.add(showGridCheckboxPanel);
		pegboardCheckboxHorizontalPanelLeft.add(Box.createRigidArea(new Dimension(0,8)));		
		pegboardCheckboxHorizontalPanelLeft.add(gridColorButtonPanel);
		
		pegboardCheckboxHorizontalPanelRight.add(renderPixelsAsBeadsCheckboxPanel);
		pegboardCheckboxHorizontalPanelRight.add(Box.createRigidArea(new Dimension(0,8)));
		pegboardCheckboxHorizontalPanelRight.add(bgColorButtonPanel);
		
		pegboardCheckboxPanelBottom.add(pegboardCheckboxHorizontalPanelLeft);
		pegboardCheckboxPanelBottom.add(pegboardCheckboxHorizontalPanelRight);
		
		pegboardComboPanel.add(pegboardCheckboxPanelBottom);
		
		pegboardComboPanel.setPreferredSize(new Dimension(300,125));
		//-------------------------------------------------------------------------------
		//-------------------------------------------------------------------------------
		//-------------------------------------------------------------------------------
		
		
		//ditherMethodPanel.add(ditherMethodLabel);
		//ditherMethodPanel.add(Box.createRigidArea(new Dimension(8,0)));
		ditherMethodPanel.add(ditherMethod);
		
		customPallettePanel.add(Box.createRigidArea(new Dimension(8,0)));
		customPallettePanel.add(customPalletteLabel);
		customPallettePanel.add(Box.createRigidArea(new Dimension(8,0)));
		customPallettePanel.add(customPallette);
		customPallettePanel.add(Box.createHorizontalGlue());
		//----------------------------------------------------------------------------
		//NEW RANDOMIZE BUTTON (TEMPORARY LOCATION)
		//----------------------------------------------------------------------------
		for (int i = 0; i <= GlobalConstants.randomizerSeedCount; i++) {
			randomizerSeeds.add((int)(Math.random() * 10000));
		}
		
		RandomizeParametersButton randomizeParametersButton_Backward = new RandomizeParametersButton("Backward", new Color(204,204,204), this, BACKWARD, oComm);
		RandomizeParametersButton randomizeParametersButton_Forward = new RandomizeParametersButton("Forward", new Color(204,204,204), this, FORWARD, oComm);
		
		randomizerSeedNumber = new JLabel("Seed: [none]");
		
//		customPallettePanel.add(Box.createRigidArea(new Dimension(8,0)));
//		customPallettePanel.add(randomizeParametersButton_Backward);
//		customPallettePanel.add(randomizeParametersButton_Forward);
		
		randomizeParametersComboPanel = new ControlPanelComboPanel(new Color(160,160,160), true);
		randomizeParametersComboPanel.setLayout(new BoxLayout(randomizeParametersComboPanel, BoxLayout.X_AXIS));
		randomizeParametersComboPanel.setPreferredSize(new Dimension(300,110));
		randomizeParametersComboPanel.add(Box.createRigidArea(new Dimension(0,8)));
		randomizeParametersComboPanel.add(randomizeParametersButton_Backward);
		randomizeParametersComboPanel.add(randomizerSeedNumber);
		randomizeParametersComboPanel.add(randomizeParametersButton_Forward);
		randomizeParametersComboPanel.add(Box.createRigidArea(new Dimension(0,8)));
		//----------------------------------------------------------------------------
		//----------------------------------------------------------------------------
		
		customPallettePanel.setOpaque(false);
		
		pegboardSizePanel.add(pegboardSizeLabel);
		pegboardSizePanel.add(Box.createRigidArea(new Dimension(8,0)));
		pegboardSizePanel.add(pegboardSize);
		
		pegboardSizePanel.setOpaque(false);
		
		//palletteModeControlPanel.add(Box.createRigidArea(new Dimension(0,16)));
		////palletteModeControlPanel.add(ditherMethodLabel);
		////palletteModeControlPanel.add(Box.createRigidArea(new Dimension(0,4)));
		////palletteModeControlPanel.add(ditherMethod);
		//palletteModeControlPanel.add(ditherMethodPanel);
		//palletteModeControlPanel.add(Box.createRigidArea(new Dimension(0,16)));
		//palletteModeControlPanel.add(customPalletteLabel);
		//palletteModeControlPanel.add(Box.createRigidArea(new Dimension(0,4)));
		//palletteModeControlPanel.add(customPallette);
		//palletteModeControlPanel.add(Box.createRigidArea(new Dimension(0,16)));
		//palletteModeControlPanel.add(pegboardSizeLabel);
		//palletteModeControlPanel.add(Box.createRigidArea(new Dimension(0,4)));
		//palletteModeControlPanel.add(pegboardSize);
//		palletteModeControlPanel.add(Box.createRigidArea(new Dimension(0,16)));
//		palletteModeControlPanel.add(perlerCheckboxPanel);
//		palletteModeControlPanel.add(artkalCheckboxPanel);
		
		
		//palletteModeControlPanel.add(hamaCheckboxPanel);
		//palletteModeControlPanel.add(showGridCheckboxPanel);
		//palletteModeControlPanel.add(Box.createRigidArea(new Dimension(0,16)));
		//palletteModeControlPanel.add(renderPixelsAsBeadsCheckboxPanel);
		//palletteModeControlPanel.add(Box.createRigidArea(new Dimension(0,16)));

//		palletteModeControlPanel.add(bgColorButtonPanel);
//		palletteModeControlPanel.add(gridColorButtonPanel);
		//palletteModeControlPanel.add(showAllColorsButtonPanel);
		//palletteModeControlPanel.add(Box.createRigidArea(new Dimension(0,16)));
		
//		palletteModeControlPanel.add(excludePearlsCheckboxPanel);
//		palletteModeControlPanel.add(excludeTranslucentsCheckboxPanel);
//		palletteModeControlPanel.add(Box.createRigidArea(new Dimension(0,16)));
		
		//palletteModeControlPanel.add(hoverPanel);
		//palletteModeControlPanel.add(Box.createVerticalGlue());
		
		//if (expertMode == false) {
			redPanel.add(labelRed);
			redPanel.add(Box.createRigidArea(new Dimension(16,0)));
			redPanel.add(sliderRed);
			redPanel.add(Box.createRigidArea(new Dimension(16,0)));
			redPanel.add(redValue);
			greenPanel.add(labelGreen);
			greenPanel.add(Box.createRigidArea(new Dimension(16,0)));
			greenPanel.add(sliderGreen);
			greenPanel.add(Box.createRigidArea(new Dimension(16,0)));
			greenPanel.add(greenValue);
			bluePanel.add(labelBlue);
			bluePanel.add(Box.createRigidArea(new Dimension(16,0)));
			bluePanel.add(sliderBlue);
			bluePanel.add(Box.createRigidArea(new Dimension(16,0)));
			bluePanel.add(blueValue);
			brightnessPanel.add(labelBrightness);
			brightnessPanel.add(Box.createRigidArea(new Dimension(16,0)));
			brightnessPanel.add(sliderBrightness);
			brightnessPanel.add(Box.createRigidArea(new Dimension(16,0)));
			brightnessPanel.add(brightnessValue);
			contrastPanel.add(labelContrast);
			contrastPanel.add(Box.createRigidArea(new Dimension(16,0)));
			contrastPanel.add(sliderContrast);
			contrastPanel.add(Box.createRigidArea(new Dimension(16,0)));
			contrastPanel.add(contrastValue);
			saturationPanel.add(labelSaturation);
			saturationPanel.add(Box.createRigidArea(new Dimension(16,0)));
			saturationPanel.add(sliderSaturation);
			saturationPanel.add(Box.createRigidArea(new Dimension(16,0)));
			saturationPanel.add(saturationValue);
			ditherPanel.add(labelDither);
			ditherPanel.add(Box.createRigidArea(new Dimension(16,0)));
			ditherPanel.add(sliderDither);
			ditherPanel.add(Box.createRigidArea(new Dimension(16,0)));
			ditherPanel.add(ditherValue);
			sharpnessPanel.add(labelSharpness);
			sharpnessPanel.add(Box.createRigidArea(new Dimension(16,0)));
			sharpnessPanel.add(sliderSharpness);
			sharpnessPanel.add(Box.createRigidArea(new Dimension(16,0)));
			sharpnessPanel.add(sharpnessValue);
		//}
		scalePanel.add(labelScale);
		scalePanel.add(Box.createRigidArea(new Dimension(16,0)));
		scalePanel.add(sliderScale);
		scalePanel.add(Box.createRigidArea(new Dimension(16,0)));
		scalePanel.add(scaleValue);
		zoomPanel.add(labelZoom);
		zoomPanel.add(Box.createRigidArea(new Dimension(16,0)));
		zoomPanel.add(sliderZoom);
		zoomPanel.add(Box.createRigidArea(new Dimension(16,0)));
		zoomPanel.add(zoomValue);
		
		lutPanel.add(Box.createRigidArea(new Dimension(0,38)));
		lutPanel.add(Box.createRigidArea(new Dimension(16,0)));
		lutPanel.add(lutLabel);
		lutPanel.add(Box.createRigidArea(new Dimension(16,0)));
		lutPanel.add(lutSelector);
		lutPanel.add(Box.createRigidArea(new Dimension(0,38)));
		lutPanel.add(Box.createHorizontalGlue());
		
		buildControls();

		//add(controlPanel);
		//add(Box.createRigidArea(new Dimension(32,0)));
		//add(controlPanel2);
	}
	
	
	void setExpertMode(boolean myExpertMode) {
		expertMode = myExpertMode;
		if(!(controlPanel == null)) {
			buildControls();
		}
	}
	
	
	void buildControls() {
		
		if (!(controlPanel == null)) {
			remove(controlPanel);
		}
		
		controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		controlPanel.removeAll();
		
		controlPanel.add(hoverPanel);
		controlPanel.add(Box.createRigidArea(new Dimension(0,16)));
		controlPanel.add(zoomPanel);
		controlPanel.add(Box.createRigidArea(new Dimension(0,16)));
		if (expertMode == true) {
			controlPanel.add(redPanel);
			controlPanel.add(greenPanel);
			controlPanel.add(bluePanel);
			controlPanel.add(Box.createRigidArea(new Dimension(0,16)));
			controlPanel.add(brightnessPanel);
			controlPanel.add(contrastPanel);
			controlPanel.add(saturationPanel);
			controlPanel.add(Box.createRigidArea(new Dimension(0,16)));
			//controlPanel1.add(ditherPanel);
			controlPanel.add(ditherComboPanel);
			controlPanel.add(sharpnessPanel);
			controlPanel.add(Box.createRigidArea(new Dimension(0,16)));
		}
		//controlPanel1.add(scalePanel);
		controlPanel.add(scaleComboPanel);
		//controlPanel1.add(Box.createRigidArea(new Dimension(0,16)));
		//controlPanel1.add(ditherMethodPanel);
		controlPanel.add(Box.createRigidArea(new Dimension(0,16)));		
		//controlPanel1.add(customPallettePanel);
		controlPanel.add(palletteComboPanel);
		controlPanel.add(Box.createRigidArea(new Dimension(0,16)));
		//controlPanel1.add(pegboardSizePanel);
		controlPanel.add(pegboardComboPanel);
		//-------------------------------------
		if (expertMode == false) {
			controlPanel.add(Box.createRigidArea(new Dimension(0,16)));
			controlPanel.add(randomizeParametersComboPanel);
		}
		//-------------------------------------
		if (GlobalConstants.applyLUT == 1) {
			controlPanel.add(Box.createRigidArea(new Dimension(0,16)));
			//controlPanel1.add(pegboardSizePanel);
			controlPanel.add(lutPanel);
		}
		
		controlPanel.add(Box.createVerticalGlue());
		
		add(controlPanel);
		
		revalidate();
		repaint();
	}
	
	
	public void randomizeParameters(RandomizeParametersButton b) {
		if (b.seedDirection == 1) {
			currentRandomizerSeedIndex++;
			if (currentRandomizerSeedIndex > GlobalConstants.randomizerSeedCount) currentRandomizerSeedIndex = 0;
		} else {
			currentRandomizerSeedIndex--;
			if (currentRandomizerSeedIndex < 0) currentRandomizerSeedIndex = GlobalConstants.randomizerSeedCount;
		}
		
		randomizerSeedNumber.setText("Seed: " + Integer.toString(randomizerSeeds.get(currentRandomizerSeedIndex)));
		
		sliderRed		.setValue(MathHelper.getStandardDistributionRandomNumber(-50,  50, 0, randomizerSeeds.get(currentRandomizerSeedIndex) +  0));
		sliderGreen		.setValue(MathHelper.getStandardDistributionRandomNumber(-50,  50, 0, randomizerSeeds.get(currentRandomizerSeedIndex) + 100000));
		sliderBlue		.setValue(MathHelper.getStandardDistributionRandomNumber(-50,  50, 0, randomizerSeeds.get(currentRandomizerSeedIndex) + 200000));
		sliderBrightness.setValue(MathHelper.getStandardDistributionRandomNumber(-50,  50, 0, randomizerSeeds.get(currentRandomizerSeedIndex) + 300000));
		sliderContrast	.setValue(MathHelper.getStandardDistributionRandomNumber(-50,  50, 0, randomizerSeeds.get(currentRandomizerSeedIndex) + 300200));
		sliderSaturation.setValue(MathHelper.getStandardDistributionRandomNumber(-50,  50, 0, randomizerSeeds.get(currentRandomizerSeedIndex) + 500000));
		sliderDither	.setValue(MathHelper.getStandardDistributionRandomNumber(  0, 100, 0, randomizerSeeds.get(currentRandomizerSeedIndex) + 600000));
		sliderSharpness	.setValue(MathHelper.getStandardDistributionRandomNumber(  0, 100, 0, randomizerSeeds.get(currentRandomizerSeedIndex) + 700000));
		//not sure if this is needed or not
		//imageController.pallette.CreateButtons("ControlPanel");
	}
	
	
	@Override
	public void onInterObjectCommunicator_CommunicateEvent(Object o) {
		consoleHelper.PrintMessage("Detected a oComm event in ControlPanel");
		if (o instanceof RandomizeParametersButton) {
			consoleHelper.PrintMessage("o instanceof RandomizeParametersButton TRUE");
			randomizeParameters(((RandomizeParametersButton) o));						
		}
	}
	
	
	@Override
	public void onInterObjectCommunicator_CommunicateEvent(String descriptor, Object o) {
		// TODO Auto-generated method stub
		
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



