package beadMaker;

import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import processing.data.XML;
import beadMaker.Palette.ExcludePearls;
import beadMaker.Palette.ExcludeTranslucents;
import beadMaker.helpers.PDFHelper;
import beadMaker.helpers.XMLWorker;
import core.jfxComponent.SynchronousJFXFileChooser;
import core.logging.ConsoleHelper;
import core.logging.ConsoleHelper.MessageLevel;
import core.variable.ArrayHelper;
import core.event.InterObjectCommunicatorEventListener;
import core.helper.FileHelper;

import java.io.IOException;

public class BMenuBar extends MenuBar implements InterObjectCommunicatorEventListener {

	// For InterObjectCommunicator identification
	private final String objectName = "MENU_BAR";

	ConsoleHelper consoleHelper;
	FileHelper fileHelper;

	// private int customPalletteIndex = 0;

	final String imageFileDescription = "All Supported Image Types (*.png, *.jpg, *.tga, *.gif)";
	final String[] imageFileExtensions = { "png", "jpg", "tga", "gif" };

	final String
		youtubeURL = "https://youtu.be/x_SNjAIZV1c", // "https://youtu.be/0gvja6lzhSw",
		perlerProjectFileDescription = "Perler Bead Project (*.pbp)", perlerProjectFileExtension = "pbp",
		configFilePath = "config\\_default_config.xml";

	public String imageFile = "";
	// public String customPalletteFile = "";
	String currentProjectName = "Untitled";
	String defaultProjectFilePath;

	// initialize menu options
	private Menu fileMenu = new Menu("File");
	private MenuItem openProject = new MenuItem("Open Project...                      Ctrl+O");
	private MenuItem selectImage = new MenuItem("Select Image...                     Ctrl+I");
	private Menu menu_image = new Menu("Images");
	private MenuItem savePNG = new MenuItem("Export PNG...                         Ctrl+E");
	private MenuItem savePattern = new MenuItem("Export B&W PDF Pattern...  Ctrl+D");
	private MenuItem saveColorPattern = new MenuItem("Export Color PDF Pattern... Ctrl+Shift+D");
	private MenuItem saveSCAD = new MenuItem("Export SCAD...                          ");
	private MenuItem saveProject = new MenuItem("Save Project                          Ctrl+S");
	private MenuItem saveProjectAs = new MenuItem("Save Project As...                 Ctrl+Shift+S");
	private MenuItem exit = new MenuItem("Exit                                           Ctrl+Shift+X");

	private Menu settingsMenu = new Menu("Settings");
	private CheckboxMenuItem expertMode = new CheckboxMenuItem("  Expert Mode          Ctrl+M"); // leave extra

	private Menu helpMenu = new Menu("Help");
	private MenuItem tutorialVideo = new MenuItem("Tutorial Video (YouTube)");

	public ImageController imageController;
	public BeadMaker beadMaker;

	private XMLWorker xmlHelper;
	private XML[] configXML;

	public ControlPanel controlPanel;
	InterObjectCommunicator oComm;
	
	private boolean useAppData;
	private String appDataFolderName;

	// CONSTRUCTOR
	public BMenuBar(
		XML[] myConfigXML,
		XMLWorker myXMLHelper,
		ImageController myImageController,
		ControlPanel myControlPanel,
		BeadMaker myBeadMaker,
		InterObjectCommunicator myOComm,
		boolean myUseAppData,
		String myAppDataFolderName
	) throws Exception {
		super();
		oComm = myOComm;
		this.beadMaker = myBeadMaker;
		this.imageController = myImageController;
		this.xmlHelper = myXMLHelper;
		this.controlPanel = myControlPanel;
		this.configXML = myConfigXML;
		this.useAppData = myUseAppData;
		this.appDataFolderName = myAppDataFolderName;
		init();
	}
	
	private void init() {
		consoleHelper = new ConsoleHelper();
		fileHelper = new FileHelper(useAppData, appDataFolderName);
		
		oComm.setInterObjectCommunicatorEventListener(this);
		// ((MenuComponent)this).getAccessibleContext().firePropertyChange(arg0, arg1,
		// arg2);
		// awtMenu = new AccessibleAWTMenuBar();
		// this.AccessibleAWTMenu.
		// setForeground
		
		this.defaultProjectFilePath = xmlHelper.GetAbsoluteFilePathStringFromXml("defaultProjectFilePath", xmlHelper.configXML, useAppData, appDataFolderName);

		expertMode.setState(xmlHelper.GetIntFromXml("expertMode", xmlHelper.configXML) == 1 ? true : false);

		consoleHelper.PrintMessage("$$$$$$$$$$$$$$$$HOLY SCHNIKES$$$$$$$$$$$$$$$$$$", MessageLevel.MESSAGE);

		// populate images submenu
		File myImagePath = new File(xmlHelper.GetAbsoluteFilePathStringFromXml("currentImagePath", configXML, useAppData, appDataFolderName));
		if (myImagePath.isDirectory()) {
			consoleHelper.PrintMessage(myImagePath.toString());
			PopulateImageMenu(xmlHelper.GetAbsoluteFilePathStringFromXml("currentImagePath", configXML, useAppData, appDataFolderName));
		} else {
			if (useAppData) {
				consoleHelper.PrintMessage(System.getenv("APPDATA") + File.separator + appDataFolderName + File.separator + "images");
				PopulateImageMenu(System.getenv("APPDATA") + File.separator + appDataFolderName + File.separator + "images");
			} else {
				consoleHelper.PrintMessage(System.getProperty("user.dir") + File.separator + "images");
				PopulateImageMenu(System.getProperty("user.dir") + File.separator + "images");
			}
		}

		// construct the menu
		this.add(fileMenu);
		fileMenu.add(openProject);
		fileMenu.add(saveProject);
		fileMenu.add(saveProjectAs);
		fileMenu.addSeparator();
		fileMenu.add(selectImage);
		fileMenu.add(menu_image);
		fileMenu.addSeparator();
		fileMenu.add(savePNG);
		fileMenu.add(savePattern);
		fileMenu.add(saveColorPattern);
		fileMenu.add(saveSCAD);
		fileMenu.addSeparator();
		fileMenu.add(exit);

		this.add(settingsMenu);
		settingsMenu.add(expertMode);

		this.add(helpMenu);
		helpMenu.add(tutorialVideo);

		// add event listeners
		openProject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OpenProject();
			}
		});
		selectImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SelectImage();
			}
		});
		savePNG.addActionListener(new ActionListener() {
			synchronized public void actionPerformed(ActionEvent e) {
				oComm.communicate("save PNG", "IMAGE_CONTROLLER");
			}
		});
		saveSCAD.addActionListener(new ActionListener() {
			synchronized public void actionPerformed(ActionEvent e) {
				oComm.communicate("save SCAD", "IMAGE_CONTROLLER");
			}
		});
		savePattern.addActionListener(new ActionListener() {
			synchronized public void actionPerformed(ActionEvent e) {
				SavePattern(false);
			}
		});

		saveColorPattern.addActionListener(new ActionListener() {
			synchronized public void actionPerformed(ActionEvent e) {
				SavePattern(true);
			}
		});

		saveProject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SaveProject(currentProjectName);
			}
		});

		saveProjectAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SaveProject();
			}
		});

		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		// stats.addActionListener(new ActionListener() {
		// synchronized public void actionPerformed(ActionEvent e) {
		// displayNumericData();
		// }
		// });

		expertMode.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				SetExpertMode();
			}
		});

		tutorialVideo.addActionListener(new ActionListener() {
			synchronized public void actionPerformed(ActionEvent e) {
				OpenYouTubeVideo();
			}
		});
	}

	// ---------------------------------------------------------------------------
	// SavePattern
	// ---------------------------------------------------------------------------
	void SavePattern(boolean fullColorPDFPrinting) {
		PDFHelper pdfHelper = new PDFHelper(beadMaker.windowController, imageController, useAppData, appDataFolderName);
		pdfHelper.SavePatternPDF(fullColorPDFPrinting);
	}

	// ---------------------------------------------------------------------------
	// SelectImage
	// ---------------------------------------------------------------------------
	void SelectImage() {

		String imageToLoad;
		File myImagePath = new File(xmlHelper.GetAbsoluteFilePathStringFromXml("currentImagePath", xmlHelper.configXML, useAppData, appDataFolderName));
		
		if (myImagePath.isDirectory()) {
			consoleHelper.PrintMessage(myImagePath.toString());
			imageToLoad = fileHelper.GetFilenameFromFileChooser(imageFileExtensions, imageFileDescription, xmlHelper.GetAbsoluteFilePathStringFromXml("currentImagePath", xmlHelper.configXML, useAppData, appDataFolderName));
			if (imageToLoad != null) {
				LoadImage(imageToLoad, true);
			}
		} else {
			if (useAppData) {
				consoleHelper.PrintMessage(System.getenv("APPDATA") + File.separator + appDataFolderName + File.separator + "images");
				imageToLoad = fileHelper.GetFilenameFromFileChooser(imageFileExtensions, imageFileDescription, System.getenv("APPDATA") + File.separator + appDataFolderName + File.separator + "images");
			} else {
				consoleHelper.PrintMessage(System.getProperty("user.dir") + File.separator + "images");
				imageToLoad = fileHelper.GetFilenameFromFileChooser(imageFileExtensions, imageFileDescription, System.getProperty("user.dir") + File.separator + "images");
			}
			
			if (imageToLoad != null) {
				LoadImage(imageToLoad, true);
			}
		}
	}

	// ---------------------------------------------------------------------------
	// SetExpertMode
	// ---------------------------------------------------------------------------
	void SetExpertMode() {
		controlPanel.setExpertMode(expertMode.getState());
		xmlHelper.AlterXML("expertMode", Integer.toString(expertMode.getState() ? 1 : 0), configFilePath, useAppData, appDataFolderName);
	}

	// ---------------------------------------------------------------------------
	// LoadImage
	// ---------------------------------------------------------------------------
	void LoadImage(String myImageFilename, boolean updateImagePath) {
		consoleHelper.PrintMessage("LoadImage");

		this.imageFile = myImageFilename;

		oComm.communicate("set image file", myImageFilename, "IMAGE_CONTROLLER");
		oComm.communicate(-1, "IMAGE_CONTROLLER");
		oComm.communicate(-1, "PALETTE");

		if (updateImagePath) {
			String myImagePath = myImageFilename.substring(0, myImageFilename.lastIndexOf(File.separator));

			xmlHelper.AlterXML("currentImagePath", myImagePath, configFilePath, useAppData, appDataFolderName);
			// reload the XML into the variable
			xmlHelper.configXML = xmlHelper.GetXMLFromFile(configFilePath);

			PopulateImageMenu(myImagePath);
		}
	}

	// ---------------------------------------------------------------------------
	// PopulateImageMenu
	// ---------------------------------------------------------------------------
	private void PopulateImageMenu(String path) {
		int imageIndex = 0;

		menu_image.removeAll();

		File filePath = new File(path);
		File[] listOfFiles = filePath.listFiles();
		MenuItem[] menuItem_images = new MenuItem[listOfFiles.length];

		for (final File file : listOfFiles) {
			String extension = fileHelper.getExtension(file.getName());
			if (file.isFile() && extension.equals("png")) {
				menuItem_images[imageIndex] = new MenuItem(file.getName());
				menu_image.add(menuItem_images[imageIndex]);
				// add event listeners
				menuItem_images[imageIndex].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						imageFile = file.getAbsoluteFile().toString();
						consoleHelper.PrintMessage(imageFile);
						// SetFrameTitle(renderPApplet, currentProjectName);
						LoadImage(imageFile, false);
					}
				});
				imageIndex++;
			}
		}
	}

	// ---------------------------------------------------------------------------
	// OpenYouTubeVideo
	// ---------------------------------------------------------------------------
	synchronized void OpenYouTubeVideo() {
		consoleHelper.PrintMessage("OpenYouTubeVideo");

		URI uri = null;

		try {
			uri = new URI(youtubeURL);
		} catch (URISyntaxException e) {
		}

		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(uri);
			} catch (IOException e) {
				/* TODO: error handling */ }
		} else {
			/* TODO: error handling */ }
	}

	// ---------------------------------------------------------------------------
	// OpenProject
	// ---------------------------------------------------------------------------
	synchronized void OpenProject() {
		GetProjectFromFileChooser();
	}

	// ---------------------------------------------------------------------------
	// GetProjectNameFromFileChooser
	// ---------------------------------------------------------------------------
	public void GetProjectFromFileChooser() {
		consoleHelper.PrintMessage("GetProjectNameFromFileChooser");

		String myChosenProjectFile = fileHelper.GetFilenameFromFileChooser(new String[] { perlerProjectFileExtension }, perlerProjectFileDescription, xmlHelper.GetAbsoluteFilePathStringFromXml("currentProjectFilePath", xmlHelper.configXML, useAppData, appDataFolderName));

		if (myChosenProjectFile != null && !myChosenProjectFile.isEmpty()) {

			String myChosenProjectPath = myChosenProjectFile.substring(0, myChosenProjectFile.lastIndexOf(File.separator));

			xmlHelper.AlterXML("currentProjectFilePath", myChosenProjectPath, configFilePath, useAppData, appDataFolderName);

			LoadProject(myChosenProjectFile);
		}
	}

	// ---------------------------------------------------------------------------
	// LoadProject
	// ---------------------------------------------------------------------------
	// TODO: load project info and use it to set all controls, load image, etc.
	public void LoadProject(String myProjectFile) {
		LoadProject(myProjectFile, true);
	}

	// ---------------------------------------------------------------------------
	// LoadProject
	// ---------------------------------------------------------------------------
	// TODO: load project info and use it to set all controls, load image, etc.
	public void LoadProject(String myProjectFile, boolean updateCurrentProjectFilePath) {
		consoleHelper.PrintMessage("LoadProject");
		consoleHelper.PrintMessage("Loading Project: " + myProjectFile);

		xmlHelper.projectXML = xmlHelper.GetXMLFromFile(myProjectFile);

		imageFile = xmlHelper.GetAbsoluteFilePathStringFromXml("imageFile", xmlHelper.projectXML, useAppData, appDataFolderName);

		// set control dials to values in project xml
		controlPanel.sliderRed.setValue(xmlHelper.GetIntFromXml("dialValues.red", xmlHelper.projectXML));
		controlPanel.sliderGreen.setValue(xmlHelper.GetIntFromXml("dialValues.green", xmlHelper.projectXML));
		controlPanel.sliderBlue.setValue(xmlHelper.GetIntFromXml("dialValues.blue", xmlHelper.projectXML));
		controlPanel.sliderBrightness.setValue(xmlHelper.GetIntFromXml("dialValues.brightness", xmlHelper.projectXML));
		controlPanel.sliderContrast.setValue(xmlHelper.GetIntFromXml("dialValues.contrast", xmlHelper.projectXML));
		controlPanel.sliderSaturation.setValue(xmlHelper.GetIntFromXml("dialValues.saturation", xmlHelper.projectXML));
		controlPanel.sliderDither.setValue(xmlHelper.GetIntFromXml("dialValues.ditherLevel", xmlHelper.projectXML));
		controlPanel.sliderSharpness.setValue(xmlHelper.GetIntFromXml("dialValues.sharpness", xmlHelper.projectXML));
		controlPanel.sliderScale.setValue(xmlHelper.GetIntFromXml("dialValues.imageScale", xmlHelper.projectXML));
		controlPanel.sliderZoom.setValue(xmlHelper.GetIntFromXml("dialValues.zoom", xmlHelper.projectXML));
		controlPanel.ditherMethod.setSelectedIndex(xmlHelper.GetIntFromXml("displaySettings.ditherMethod", xmlHelper.projectXML));
		// controlPanel.customPallette .setSelectedIndex
		// (xmlHelper.GetIntFromXml("displaySettings.selectedPallette" ,
		// xmlHelper.projectXML));
		controlPanel.pegboardSize.setSelectedIndex(xmlHelper.GetIntFromXml("displaySettings.pegboardMode", xmlHelper.projectXML));

		String selectedPalette = xmlHelper.GetDataFromXml("displaySettings.selectedPallette", xmlHelper.projectXML);

		// if the pbp stored palette filename matches the palette filename, set it as
		// the selected palette in the UI
		for (int i = 0; i < controlPanel.customPalletteFiles.length; i++) {
			if (selectedPalette.equals(controlPanel.customPalletteFiles[0][i])) {
				controlPanel.customPallette.setSelectedIndex(i);
			}
		}

		// This is for backward compatibility for projects that did not have
		// "displaySettings.flipImage"
		// If we add more new project settings,
		// this try catch might not be smart enough to know which XML field is missing,
		// but for now it works, since there's only one new field.
		try {
			imageController.flipImage = xmlHelper.GetIntFromXml("displaySettings.flipImage", xmlHelper.projectXML) == 1 ? true : false;
			controlPanel.flipImageCheckboxPanel.checkbox.setSelected(xmlHelper.GetIntFromXml("displaySettings.flipImage", xmlHelper.projectXML) == 1 ? true : false);
		} catch (ArrayIndexOutOfBoundsException e) {
			controlPanel.flipImageCheckboxPanel.checkbox.setSelected(false);
		}
		
		// This is for backward compatibility for projects that did not have
		// "displaySettings.minimumBeads" (see comment block above for possible regression issues)
//		try {
//			//beadMaker.minBeadsComboBox = xmlHelper.GetIntFromXml("displaySettings.minimumBeads", xmlHelper.projectXML);
//			beadMaker.minBeadsComboBox.setSelectedIndex(xmlHelper.GetIntFromXml("displaySettings.minimumBeads", xmlHelper.projectXML));
//		} catch (ArrayIndexOutOfBoundsException e) {
//			beadMaker.minBeadsComboBox.setSelectedIndex(0);
//		}
		try {
			//beadMaker.minBeadsComboBox = xmlHelper.GetIntFromXml("displaySettings.minimumBeads", xmlHelper.projectXML);
			beadMaker.minBeadsComboBox.setSelectedItem(xmlHelper.GetDataFromXml("displaySettings.minimumBeads", xmlHelper.projectXML));
		} catch (ArrayIndexOutOfBoundsException e) {
			beadMaker.minBeadsComboBox.setSelectedIndex(0);
		}

		boolean perlerCheckBoxState = xmlHelper.GetIntFromXml("brandsToUse.perler", xmlHelper.projectXML) == 1 ? true : false;
		boolean artkalCheckBoxState = xmlHelper.GetIntFromXml("brandsToUse.artkalS", xmlHelper.projectXML) == 1 ? true : false;

		consoleHelper.PrintMessage("brandsToUse.perler = " + xmlHelper.GetIntFromXml("brandsToUse.perler", xmlHelper.projectXML));
		consoleHelper.PrintMessage("brandsToUse.artkalS = " + xmlHelper.GetIntFromXml("brandsToUse.artkalS", xmlHelper.projectXML));

		// if both brands are unchecked (which is an invalid state, but *could* happen,
		// check Perler by default
		if (!artkalCheckBoxState) {
			this.controlPanel.perlerCheckboxPanel.checkbox.setSelected(imageController.pallette.setBeadBrand("Perler", true));
			this.controlPanel.artkalCheckboxPanel.checkbox.setSelected(imageController.pallette.setBeadBrand("Artkal-S", false));
		} else if (!perlerCheckBoxState) {
			this.controlPanel.artkalCheckboxPanel.checkbox.setSelected(imageController.pallette.setBeadBrand("Artkal-S", true));
			this.controlPanel.perlerCheckboxPanel.checkbox.setSelected(imageController.pallette.setBeadBrand("Perler", false));
		} else {
			this.controlPanel.perlerCheckboxPanel.checkbox.setSelected(imageController.pallette.setBeadBrand("Perler", true));
			this.controlPanel.artkalCheckboxPanel.checkbox.setSelected(imageController.pallette.setBeadBrand("Artkal-S", true));
		}

		consoleHelper.PrintMessage("controlPanel.perlerCheckboxPanel.checkbox = " + controlPanel.perlerCheckboxPanel.checkbox.isSelected());
		consoleHelper.PrintMessage("controlPanel.artkalCheckboxPanel.checkbox = " + controlPanel.artkalCheckboxPanel.checkbox.isSelected());

		this.controlPanel.showGridCheckboxPanel.checkbox.setSelected(xmlHelper.GetIntFromXml("displaySettings.showGrid", xmlHelper.projectXML) == 1 ? true : false);
		imageController.renderLabel.showGrid = xmlHelper.GetIntFromXml("displaySettings.showGrid", xmlHelper.projectXML) == 1 ? true : false;

		this.controlPanel.renderPixelsAsBeadsCheckboxPanel.checkbox.setSelected(xmlHelper.GetIntFromXml("displaySettings.showPixelsAsBeads", xmlHelper.projectXML) == 1 ? true : false);
		imageController.setRenderPixelsAsBeads(xmlHelper.GetIntFromXml("displaySettings.showPixelsAsBeads", xmlHelper.projectXML) == 1 ? true : false);

		Color bgColor = new Color(
			xmlHelper.GetIntFromXml("displaySettings.backgroundColor.red", xmlHelper.projectXML),
			xmlHelper.GetIntFromXml("displaySettings.backgroundColor.green", xmlHelper.projectXML),
			xmlHelper.GetIntFromXml("displaySettings.backgroundColor.blue", xmlHelper.projectXML)
		);
		imageController.renderScrollPanel.getViewport().setBackground(bgColor);

		Color gridColor = new Color(
			xmlHelper.GetIntFromXml("displaySettings.gridColor.red", xmlHelper.projectXML),
			xmlHelper.GetIntFromXml("displaySettings.gridColor.green", xmlHelper.projectXML),
			xmlHelper.GetIntFromXml("displaySettings.gridColor.blue", xmlHelper.projectXML)
		);
		imageController.renderLabel.gridColor = gridColor;

		this.controlPanel.showGridCheckboxPanel.checkbox.setSelected(xmlHelper.GetIntFromXml("displaySettings.showGrid", xmlHelper.projectXML) == 1 ? true : false);
		imageController.renderLabel.showGrid = xmlHelper.GetIntFromXml("displaySettings.showGrid", xmlHelper.projectXML) == 1 ? true : false;

		imageController.pallette.excludePearls = xmlHelper.GetIntFromXml("beadsToExclude.pearls", xmlHelper.projectXML) == 1 ? ExcludePearls.TRUE : ExcludePearls.FALSE;
		controlPanel.excludePearlsCheckboxPanel.checkbox.setSelected(xmlHelper.GetIntFromXml("beadsToExclude.pearls", xmlHelper.projectXML) == 1 ? true : false);

		imageController.pallette.excludeTranslucents = xmlHelper.GetIntFromXml("beadsToExclude.translucents", xmlHelper.projectXML) == 1 ? ExcludeTranslucents.TRUE : ExcludeTranslucents.FALSE;
		controlPanel.excludeTranslucentsCheckboxPanel.checkbox.setSelected(xmlHelper.GetIntFromXml("beadsToExclude.translucents", xmlHelper.projectXML) == 1 ? true : false);

		if (useAppData) {
			imageController.pallette.GetPalletteFromXml(System.getenv("APPDATA") + File.separator + appDataFolderName + File.separator + "pallettes" + File.separator + controlPanel.customPalletteFiles[0][controlPanel.customPallette.getSelectedIndex()], xmlHelper);
		} else {
			imageController.pallette.GetPalletteFromXml(System.getProperty("user.dir") + File.separator + "pallettes" + File.separator + controlPanel.customPalletteFiles[0][controlPanel.customPallette.getSelectedIndex()], xmlHelper);
		}
		
		imageController.pallette.uncheckedColorIndices = ArrayHelper.SplitStringToIntegerArray(xmlHelper.GetDataFromXml("beadsToExclude.uncheckedColorIndices", xmlHelper.projectXML));

		// uncheck all colors that are listed in the "uncheckedColorIndices" project XML
		for (int i = 0; i < imageController.pallette.perlerColorsRGB.length; i++) {
			for (int j = 0; j < imageController.pallette.uncheckedColorIndices.length; j++) {
				if (imageController.pallette.uncheckedColorIndices[j] == imageController.pallette.perlerColorsRGB[i][imageController.pallette.arrayIndex04_ColorIndex]) {
					consoleHelper.PrintMessage("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ UNCHECKED MATCH $$$$$$$$$$$$");
					imageController.pallette.perlerColorsRGB[i][imageController.pallette.arrayIndex16_IsChecked] = 0;
				}
			}
		}

		imageController.pallette.GetPalletteWithFiltersApplied();

		if (updateCurrentProjectFilePath) {
			xmlHelper.AlterXML("currentProjectFilePath", myProjectFile.substring(0, myProjectFile.lastIndexOf(File.separator)), configFilePath, useAppData, appDataFolderName);

			beadMaker.windowController.setTitle("--Bead Maker-- " + myProjectFile + "  --" + imageFile.substring(imageFile.lastIndexOf(File.separator) + 1) + "--");
		}
		// reload the XML into the variable
		xmlHelper.configXML = xmlHelper.GetXMLFromFile(configFilePath);

		// set image to image value in xml
		imageController.selectedColorIndex = -1;
		imageController.pallette.selectedColorIndex = -1;
		imageController.setOriginalCleanedImage(xmlHelper.GetAbsoluteFilePathStringFromXml("imageFile", xmlHelper.projectXML, useAppData, appDataFolderName));
		if (GlobalConstants.pixelArtMultiPaletteMode == 1) {
			if (useAppData) {
				imageController.loadColorMap(System.getenv("APPDATA") + File.separator + appDataFolderName + File.separator + "ColorMaps" + File.separator + "default.png");
			} else {
				imageController.loadColorMap(System.getProperty("user.dir") + File.separator + "ColorMaps" + File.separator + "default.png");
			}
		}
		imageController.pallette.CreateButtons("beadBrandActionListener");

	}

	// ---------------------------------------------------------------------------
	// SaveProject: see http://www.mkyong.com/java/java-properties-file-examples/
	// ---------------------------------------------------------------------------
	public void SaveProject() {
		SaveProject("");
	}

	public void SaveProject(String myProjectName) {
		consoleHelper.PrintMessage("SaveProject");

		// if the current project is the default project, do not oeverwirte the file
		if (myProjectName.equals(defaultProjectFilePath) || myProjectName.equals("Untitled")) {
			myProjectName = "";
		}

		File projectFile = new File(myProjectName);

		if (!projectFile.exists()) {

			File dataDir = new File(
				xmlHelper.GetAbsoluteFilePathStringFromXml("currentProjectFilePath", xmlHelper.configXML, useAppData, appDataFolderName), File.separator
			);

			File selectedFile;

			SynchronousJFXFileChooser chooser = new SynchronousJFXFileChooser(
				dataDir,
				perlerProjectFileDescription,
				//new String[] {"*." + perlerProjectFileExtension}
				new String[] {perlerProjectFileExtension}
			);
			selectedFile = chooser.showSaveDialog();

			if (selectedFile != null) {
				myProjectName = selectedFile.toString();

				if (!fileHelper.getExtension(myProjectName).equals(perlerProjectFileExtension)) {
					myProjectName += "." + perlerProjectFileExtension;
				}
				consoleHelper.PrintMessage("myProjectName = " + myProjectName);
				selectedFile = new File(myProjectName);
				if (!selectedFile.exists()) {
					try {
						FileHelper.CopyFileUsingStream(new File(defaultProjectFilePath), selectedFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				consoleHelper.PrintMessage("myProjectName EXTENSION = ***" + fileHelper.getExtension(myProjectName) + "***");

				if (!fileHelper.getExtension(myProjectName).equals(perlerProjectFileExtension)) {
					myProjectName += "." + perlerProjectFileExtension;
					consoleHelper.PrintMessage("After extension check, myProjectName = " + myProjectName);
				}

				currentProjectName = myProjectName;
				consoleHelper.PrintMessage("You chose to save this file: " + myProjectName);

			} else {
				return; // bail out because the user did not select a file
			}
		}

		String[][] xmlData = new String[31][2];

		xmlData[0] = new String[] { "imageFile", imageFile };
		xmlData[1] = new String[] { "dialValues.red", Integer.toString(controlPanel.sliderRed.getValue()) };
		xmlData[2] = new String[] { "dialValues.green", Integer.toString(controlPanel.sliderGreen.getValue()) };
		xmlData[3] = new String[] { "dialValues.blue", Integer.toString(controlPanel.sliderBlue.getValue()) };
		xmlData[4] = new String[] { "dialValues.brightness",Integer.toString(controlPanel.sliderBrightness.getValue()) };
		xmlData[5] = new String[] { "dialValues.contrast",Integer.toString(controlPanel.sliderContrast.getValue()) };
		xmlData[6] = new String[] { "dialValues.saturation",Integer.toString(controlPanel.sliderSaturation.getValue()) };
		xmlData[7] = new String[] { "dialValues.ditherLevel",Integer.toString(controlPanel.sliderDither.getValue()) };
		xmlData[8] = new String[] { "dialValues.sharpness",Integer.toString(controlPanel.sliderSharpness.getValue()) };
		xmlData[9] = new String[] { "dialValues.imageScale",Integer.toString(controlPanel.sliderScale.getValue()) };
		xmlData[10] = new String[] { "dialValues.zoom", Integer.toString(controlPanel.sliderZoom.getValue()) };
		xmlData[11] = new String[] { "brandsToUse.perler",Integer.toString(controlPanel.perlerCheckboxPanel.checkbox.isSelected() ? 1 : 0) };
		xmlData[12] = new String[] { "brandsToUse.hama", Integer.toString(0) };
		xmlData[13] = new String[] { "brandsToUse.artkalS",Integer.toString(controlPanel.artkalCheckboxPanel.checkbox.isSelected() ? 1 : 0) };

		xmlData[14] = new String[] { "beadsToExclude.translucents",Integer.toString(controlPanel.excludeTranslucentsCheckboxPanel.checkbox.isSelected() ? 1 : 0) };
		xmlData[15] = new String[] { "beadsToExclude.pearls",Integer.toString(controlPanel.excludePearlsCheckboxPanel.checkbox.isSelected() ? 1 : 0) };
		xmlData[16] = new String[] { "beadsToExclude.uncheckedColorIndices",imageController.pallette.getUncheckedIndices() };

		// xmlData[17] = new String[] {"displaySettings.selectedPallette",Integer.toString(controlPanel.customPallette.getSelectedIndex())};
		xmlData[17] = new String[] { "displaySettings.selectedPallette",controlPanel.customPalletteFiles[0][controlPanel.customPallette.getSelectedIndex()] };
		xmlData[18] = new String[] { "displaySettings.showColorCodes", Integer.toString(0) };
		xmlData[19] = new String[] { "displaySettings.showPixelsAsBeads",Integer.toString(controlPanel.renderPixelsAsBeadsCheckboxPanel.checkbox.isSelected() ? 1 : 0) };
		xmlData[20] = new String[] { "displaySettings.pegboardMode",Integer.toString(controlPanel.pegboardSize.getSelectedIndex()) };
		xmlData[21] = new String[] { "displaySettings.showGrid",Integer.toString(controlPanel.showGridCheckboxPanel.checkbox.isSelected() ? 1 : 0) };
		xmlData[22] = new String[] { "displaySettings.ditherMethod",Integer.toString(controlPanel.ditherMethod.getSelectedIndex()) };
		xmlData[23] = new String[] { "displaySettings.backgroundColor.red",Integer.toString(imageController.renderScrollPanel.getViewport().getBackground().getRed()) };
		xmlData[24] = new String[] { "displaySettings.backgroundColor.green",Integer.toString(imageController.renderScrollPanel.getViewport().getBackground().getGreen()) };
		xmlData[25] = new String[] { "displaySettings.backgroundColor.blue",Integer.toString(imageController.renderScrollPanel.getViewport().getBackground().getBlue()) };
		xmlData[26] = new String[] { "displaySettings.gridColor.red",Integer.toString(imageController.renderLabel.gridColor.getRed()) };
		xmlData[27] = new String[] { "displaySettings.gridColor.green",Integer.toString(imageController.renderLabel.gridColor.getGreen()) };
		xmlData[28] = new String[] { "displaySettings.gridColor.blue",Integer.toString(imageController.renderLabel.gridColor.getBlue()) };
		xmlData[29] = new String[] { "displaySettings.flipImage",Integer.toString(controlPanel.flipImageCheckboxPanel.checkbox.isSelected() ? 1 : 0) };
		//xmlData[30] = new String[] { "displaySettings.minimumBeads",Integer.toString(beadMaker.minBeadsComboBox.getSelectedIndex()) };
		xmlData[30] = new String[] { "displaySettings.minimumBeads",beadMaker.minBeadsComboBox.getSelectedItem().toString()};

		xmlHelper.AlterXML(xmlData, myProjectName, useAppData, appDataFolderName);

		xmlHelper.AlterXML("currentProjectFilePath", myProjectName.substring(0, myProjectName.lastIndexOf(File.separator)), configFilePath, useAppData, appDataFolderName);
		// reload the XML into the variable
		xmlHelper.configXML = xmlHelper.GetXMLFromFile(configFilePath);

		beadMaker.windowController.setTitle("--Pixel Perfect-- " + myProjectName + "  --" + xmlData[0][1].substring(xmlData[0][1].lastIndexOf(File.separator) + 1) + "--");
	}

	@Override
	public void onInterObjectCommunicator_CommunicateEvent(Object o) {
		if (o instanceof KeyEvent) {
			KeyEvent e = ((KeyEvent) o);
			// https://stackoverflow.com/questions/5970765/java-detect-ctrlx-key-combination-on-a-jtree
			if ((e.getKeyCode() == KeyEvent.VK_O) && ((e.getModifiers() ^ KeyEvent.CTRL_MASK) == 0)) {
				// ConsoleHelper.PrintMessage("fired a keyPressed event from oCommInterface in
				// MenuBar" + e.getKeyCode());
				OpenProject();
			}
			if ((e.getKeyCode() == KeyEvent.VK_M) && ((e.getModifiers() ^ KeyEvent.CTRL_MASK) == 0)) {
				consoleHelper.PrintMessage("fired a keyPressed event from oCommInterface in MenuBar" + e.getKeyCode());
				expertMode.setState(!expertMode.getState());
				SetExpertMode();
			}
			if ((e.getKeyCode() == KeyEvent.VK_S) && ((e.getModifiers() ^ KeyEvent.CTRL_MASK) == 0)) {
				SaveProject(currentProjectName);
			}
			if ((e.getKeyCode() == KeyEvent.VK_S) && ((e.getModifiers() ^ (KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK)) == 0)) {
				SaveProject();
			}
			if ((e.getKeyCode() == KeyEvent.VK_I) && ((e.getModifiers() ^ KeyEvent.CTRL_MASK) == 0)) {
				expertMode.setState(!expertMode.getState());
				SelectImage();
			}
			if ((e.getKeyCode() == KeyEvent.VK_D) && ((e.getModifiers() ^ KeyEvent.CTRL_MASK) == 0)) {
				SavePattern(false);
			}
			if ((e.getKeyCode() == KeyEvent.VK_D) && ((e.getModifiers() ^ (KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK)) == 0)) {
				SavePattern(true);
			}
			if ((e.getKeyCode() == KeyEvent.VK_X) && ((e.getModifiers() ^ (KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK)) == 0)) {
				System.exit(0);
			}
		}
	}

	@Override
	public void onInterObjectCommunicator_CommunicateEvent(String descriptor, Object o) {
		// TODO Auto-generated method stub

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
