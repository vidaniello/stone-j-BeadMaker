package beadMaker;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Box;

import processing.data.XML;
import core.ColorHelper;
import core.Comparator_IntArray;
import core.ConsoleHelper;
import core.InterObjectCommunicatorEventListener;
import core.StringHelper;
import core.ArrayHelper;
import beadMaker.HelperClasses.XMLWorker;


public class Palette implements InterObjectCommunicatorEventListener {

	//For InterObjectCommunicator identification
	private String objectName = "PALETTE";
	
	ConsoleHelper consoleHelper = new ConsoleHelper();
		
	public enum ExcludeTranslucents {
		FALSE,
		TRUE;

		private static final ExcludeTranslucents[] values = values(); // to avoid recreating array

		public ExcludeTranslucents Toggle() {
			switch(this) {
			case TRUE: return FALSE;
			case FALSE: return TRUE;
			}
			return null;
		}
	}
	public ExcludeTranslucents excludeTranslucents = ExcludeTranslucents.TRUE;

	
	public enum ExcludePearls {
		FALSE,
		TRUE;

		private static final ExcludePearls[] values = values(); // to avoid recreating array

		public ExcludePearls Toggle() {
			switch(this) {
			case TRUE: return FALSE;
			case FALSE: return TRUE;
			}
			return null;
		}
	}
	public ExcludePearls excludePearls = ExcludePearls.TRUE;

	
	public final String brandNamePerler		= "Perler";
	public final String brandNameHama		= "Hama";
	//public final String brandNameNabbi	= "Nabbi";
	public final String brandNameArtkalS	= "Artkal-S";

	public final int arrayIndex00_Red 					=  0;
	public final int arrayIndex01_Green 				=  1;
	public final int arrayIndex02_Blue 				=  2;
	//public final int arrayIndex03_RelativeUsefulness 	=  3;
	public final int arrayIndex04_ColorIndex 			=  4;
	public final int arrayIndex05_PixelCount 			=  5;
	public final int arrayIndex06_IsPearl 				=  6;
	public final int arrayIndex07_IsTranslucent 		=  7;
	public final int arrayIndex08_IsNeutral 			=  8;
	public final int arrayIndex09_IsGrayscale 			=  9;
	public final int arrayIndex10_Disabled 			= 10;
	public final int arrayIndex11_Brand 				= 11;
	public final int arrayIndex12_DisableGlobally 		= 12;
	public final int arrayIndex13_MapRed 				= 13;
	public final int arrayIndex14_MapGreen 			= 14;
	public final int arrayIndex15_MapBlue 				= 15;
	public final int arrayIndex16_IsChecked			= 16;
	//Added 2020-03-21
	public final int arrayIndex17_SortOrder			= 17;

	public final int brandIdPerler	= 1;
	public final int brandIdHama	= 2;
	public final int brandIdArtkalS	= 3;

//	public static enum PalletteMode {
//		ALLCOLORS,
//		NEUTRALS,
//		GRAYSCALE
//	}
	//public PalletteMode palletteMode;

	//Create final arrays based on number of colors in XML file
	public String[][] perlerColorsNames;
	public int[][] perlerColorsRGB;

	public int[] uncheckedColorIndices = new int[] {-1};

	public int usePerler = 1;
	public int useHama = 0;
	//public int useNabbi = 0;
	public int useArtkalS = 1;
	
	Color selectedColor = new Color(253,254,255); //Setting this to a random color that doesn't match any pallette colors. (previously it was set to -1, which was a color match to Arkal White.
	int selectedColorIndex = -1;
	
	public int[][] currentPallette;

	public List<PaletteSubPanel> palletteSubPanels;

	public int
	totalPalletteColors = 0;

	public int
	totalBeadsUsed,			//this counts beads in the full render OR selected tile
	totalBeadsUsedGlobally; //this counts beads ONLY in the full render

	//private XMLHelper xmlHelper;

	public PallettePanel pallettePanel = new PallettePanel();
	
	//public BeadMaker beadMaker;
	public InterObjectCommunicator oComm;
	
	Comparator_IntArray comparator_IntArray;
	

	//------------------------------------------------------------
	//CONSTRUCTOR
	//------------------------------------------------------------
	public Palette(String palletteXMLFileName, XMLWorker xMLHelper, InterObjectCommunicator myOComm) throws Exception
	{
		oComm = myOComm;
		oComm.setInterObjectCommunicatorEventListener(this);
		//this.beadMaker = myBeadMaker;
		GetPalletteFromXml(palletteXMLFileName, xMLHelper);
		
		GetPalletteWithFiltersApplied();
	}




	//	public void CreateLabels() {
	//		palletteLabels = new ArrayList<>();
	//		
	//		for (int i = 0; i < currentPallette.length; i++) {
	//			int currentIndex = currentPallette[i][arrayIndex04_ColorIndex];
	//			Color color = new Color(
	//					currentPallette[i][arrayIndex00_Red],
	//					currentPallette[i][arrayIndex01_Green],
	//					currentPallette[i][arrayIndex02_Blue]);
	//			palletteLabels.add(new PalletteLabel(perlerColorsNames[currentIndex][1] + " - " + perlerColorsNames[currentIndex][0], color));
	//		}
	//	}
	
	public void checkUncheck(int colorIndex, boolean checkedState) {
		for (int i = 0; i < currentPallette.length; i++) {
			if (currentPallette[i][arrayIndex04_ColorIndex] == colorIndex) {
				currentPallette[i][arrayIndex16_IsChecked] = (checkedState) ? 1 : 0;
			}
		}
		oComm.communicate("update images", "IMAGE_CONTROLLER");
		CreateButtons("pallette.checkUncheck()");
	}
	
	
	public void setSelectedColor(int colorIndex) {
		this.selectedColorIndex = colorIndex;
		oComm.communicate(colorIndex, "IMAGE_CONTROLLER");
	}


	public void CreateButtons(String caller) {
		consoleHelper.PrintMessage("CreateButtons: called by " + caller);
		
		palletteSubPanels = new ArrayList<>();
		
		//this loop is for any colors with beadCount > 0
		for (int i = 0; i < currentPallette.length; i++) {
			int currentIndex = currentPallette[i][arrayIndex04_ColorIndex];
			Color color = new Color(
					currentPallette[i][arrayIndex00_Red],
					currentPallette[i][arrayIndex01_Green],
					currentPallette[i][arrayIndex02_Blue]);
			if(currentPallette[i][arrayIndex05_PixelCount] > 0) {
				palletteSubPanels.add(new PaletteSubPanel(perlerColorsNames[currentIndex][1] + " " + perlerColorsNames[currentIndex][0]+ " - " + currentPallette[i][arrayIndex05_PixelCount], color, this, currentPallette[i][arrayIndex04_ColorIndex], true, oComm));
			}
		}
		
		//this loop is for any unchecked colors (beadCount = 0)
		for (int i = 0; i < currentPallette.length; i++) {
			int currentIndex = currentPallette[i][arrayIndex04_ColorIndex];
			Color color = new Color(
					currentPallette[i][arrayIndex00_Red],
					currentPallette[i][arrayIndex01_Green],
					currentPallette[i][arrayIndex02_Blue]);
			if (currentPallette[i][arrayIndex16_IsChecked] == 0) {
				palletteSubPanels.add(new PaletteSubPanel(perlerColorsNames[currentIndex][1] + " " + perlerColorsNames[currentIndex][0]+ " - " + currentPallette[i][arrayIndex05_PixelCount], color, this, currentPallette[i][arrayIndex04_ColorIndex], false, oComm));
			}
		}


		//remove existing buttons before adding more
		pallettePanel.removeAll();
		
//		if(beadMaker.controlPanel.showAllColorsButtonPanel != null) {
//			pallettePanel.add(beadMaker.controlPanel.showAllColorsButtonPanel);
//		}

		for(int i = 0; i < this.palletteSubPanels.size(); i++) {
			pallettePanel.add(palletteSubPanels.get(i));
			//pallettePanel.add(this.palletteButtons.get(i));
		}
		
		pallettePanel.add(Box.createVerticalGlue());
		
		try{
			oComm.communicate("totals", "<html>Colors:&nbsp;&nbsp;" + totalPalletteColors + "<br>Beads:&nbsp;&nbsp;" + totalBeadsUsed + "<html>", "BEAD_MAKER");
		} catch (NullPointerException e) {
			//do nothing
		}

		pallettePanel.revalidate();
		pallettePanel.repaint();
	}


	public void GetPalletteFromXml(String fileName, XMLWorker xMLHelper) {

		consoleHelper.PrintMessage("GetPalletteFromXml");

		XML[] colorXml = xMLHelper.GetXMLFromFile(fileName)[0].getChildren("color");
		
		//Create final arrays based on number of colors in XML file		
		perlerColorsNames = new String[colorXml.length][2];
		perlerColorsRGB = new int[colorXml.length][18];

		for (int i = 0; i < colorXml.length; i++) {

			perlerColorsNames[i][0							] = colorXml[i].getChild("name")			.getContent();
			perlerColorsNames[i][1							] = colorXml[i].getChild("productCode")		.getContent();

			perlerColorsRGB[i][arrayIndex00_Red				] = colorXml[i].getChild("red")  			.getIntContent();
			perlerColorsRGB[i][arrayIndex01_Green			] = colorXml[i].getChild("green") 			.getIntContent();
			perlerColorsRGB[i][arrayIndex02_Blue			] = colorXml[i].getChild("blue")			.getIntContent();

			perlerColorsRGB[i][arrayIndex04_ColorIndex		] = colorXml[i].getInt  ("colorIndex");
			perlerColorsRGB[i][arrayIndex05_PixelCount		] = 0;
			perlerColorsRGB[i][arrayIndex06_IsPearl			] = colorXml[i].getChild("isPearl")			.getIntContent();
			perlerColorsRGB[i][arrayIndex07_IsTranslucent	] = colorXml[i].getChild("isTranslucent")	.getIntContent();
			perlerColorsRGB[i][arrayIndex08_IsNeutral		] = colorXml[i].getChild("isNeutral")		.getIntContent();
			perlerColorsRGB[i][arrayIndex09_IsGrayscale		] = colorXml[i].getChild("isGrayscale")		.getIntContent();
			perlerColorsRGB[i][arrayIndex10_Disabled		] = colorXml[i].getChild("disabled")		.getIntContent();
			perlerColorsRGB[i][arrayIndex12_DisableGlobally	] = colorXml[i].getChild("disabled")		.getIntContent();
			perlerColorsRGB[i][arrayIndex16_IsChecked		] = 1;
			if (colorXml[i].getChild("sortOrder") != null) {
				perlerColorsRGB[i][arrayIndex17_SortOrder	] = colorXml[i].getChild("sortOrder")		.getIntContent(); //if it doesn't exist, default to the ColorIndex instead (backward compatibility with old palette XML files)
			} else {
				perlerColorsRGB[i][arrayIndex17_SortOrder	] = perlerColorsRGB[i][arrayIndex04_ColorIndex];
			}
			
			switch(colorXml[i].getChild("brand").getContent()) {
			case brandNamePerler:
				perlerColorsRGB[i][arrayIndex11_Brand] = brandIdPerler;
				break;
			case brandNameHama:
				perlerColorsRGB[i][arrayIndex11_Brand] = brandIdHama;
				break;
			case brandNameArtkalS:
				perlerColorsRGB[i][arrayIndex11_Brand] = brandIdArtkalS;
				break;
			default:
				consoleHelper.PrintMessage("WE HAVE HIT THE DEFAULT CASE-- THIS SHOULD NEVER HAPPEN");
				break;
			}

			//custom pallettes use the mapRed, mapGreen and mapBlue values that differ from the red, green, blue values,
			// but the default pallette does not, so it just uses the red, green, blue values for pixel color mapping.
			if (colorXml[i].getChild("mapRed") != null) {			
				perlerColorsRGB[i][arrayIndex13_MapRed	] = colorXml[i].getChild("mapRed")	.getIntContent(); //destination: mapRed
				perlerColorsRGB[i][arrayIndex14_MapGreen] = colorXml[i].getChild("mapGreen").getIntContent(); //destination: mapGreen
				perlerColorsRGB[i][arrayIndex15_MapBlue	] = colorXml[i].getChild("mapBlue")	.getIntContent(); //destination: mapBlue
			} else {
				perlerColorsRGB[i][arrayIndex13_MapRed	] = colorXml[i].getChild("red")		.getIntContent(); //destination: mapRed
				perlerColorsRGB[i][arrayIndex14_MapGreen] = colorXml[i].getChild("green")	.getIntContent(); //destination: mapGreen
				perlerColorsRGB[i][arrayIndex15_MapBlue	] = colorXml[i].getChild("blue")	.getIntContent(); //destination: mapBlue
			}
		}
		
		ArrayHelper.GetArraySorted(perlerColorsRGB, arrayIndex17_SortOrder);
	}
	

	//---------------------------------------------------------------------------
	// GetPalletteWithFiltersApplied
	//---------------------------------------------------------------------------
	public void GetPalletteWithFiltersApplied() {
		consoleHelper.PrintMessage("GetPalletteWithFiltersApplied");

		int arrayLength = 0;
		int arrayIndex = 0;

		//TODO: LOAD THE XML COLOR PALLETTE DATA HERE
		//GetPalletteFromXml();

		//Set all colors to "Active"
		for (int i = 0; i < perlerColorsRGB.length; i++) {
			perlerColorsRGB[i][arrayIndex10_Disabled] = 1;
		}

		//-------------------------------------------------------------------------------
		//Look at the various pallette options and remove colors that are not qualified
		//-------------------------------------------------------------------------------
		//disableGlobally: This is for colors that exist in the pallette but are not used in the program (missing RGB values, etc.)
		for (int i = 0; i < perlerColorsRGB.length; i++) {
			if (perlerColorsRGB[i][arrayIndex12_DisableGlobally	] == 1) {
				perlerColorsRGB[i][arrayIndex10_Disabled		] = 0;
			}
		}

		//Perler Brand
		if (usePerler == GlobalConstants.off) {
			for (int i = 0; i < perlerColorsRGB.length; i++) {
				if (perlerColorsRGB[i][arrayIndex11_Brand		] == brandIdPerler) {
					perlerColorsRGB[i][arrayIndex10_Disabled	] = 0;
				}
			}
		}

		//Hama Brand
		if (useHama == GlobalConstants.off) {
			for (int i = 0; i < perlerColorsRGB.length; i++) {
				if (perlerColorsRGB[i][arrayIndex11_Brand		] == brandIdHama) {
					perlerColorsRGB[i][arrayIndex10_Disabled	] = 0;
				}
			}
		}

		//Artkal-S Brand
		if (useArtkalS == GlobalConstants.off) {
			for (int i = 0; i < perlerColorsRGB.length; i++) {
				if (perlerColorsRGB[i][arrayIndex11_Brand		] == brandIdArtkalS) {
					perlerColorsRGB[i][arrayIndex10_Disabled	] = 0;
				}
			}
		}

//		//Neutrals
//		if (palletteMode == PalletteMode.NEUTRALS) {
//			for (int i = 0; i < perlerColorsRGB.length; i++) {
//				if (perlerColorsRGB[i][arrayIndex08_IsNeutral	] == 0) {
//					perlerColorsRGB[i][arrayIndex10_Disabled	] = 0;
//				}
//			}
//		}
//
//		//Grayscale
//		if (palletteMode == PalletteMode.GRAYSCALE) {
//			for (int i = 0; i < perlerColorsRGB.length; i++) {
//				if (perlerColorsRGB[i][arrayIndex09_IsGrayscale	] == 0) {
//					perlerColorsRGB[i][arrayIndex10_Disabled	] = 0;
//				}
//			}
//		}

		//Translucents
		if (excludeTranslucents == ExcludeTranslucents.TRUE) {
			for (int i = 0; i < perlerColorsRGB.length; i++) {
				if (perlerColorsRGB[i][arrayIndex07_IsTranslucent	] == 1) {
					perlerColorsRGB[i][arrayIndex10_Disabled		] = 0;
				}
			}
		}

		//Pearls
		if (excludePearls == ExcludePearls.TRUE) {
			for (int i = 0; i < perlerColorsRGB.length; i++) {
				if (perlerColorsRGB[i][arrayIndex06_IsPearl	] == 1) {
					perlerColorsRGB[i][arrayIndex10_Disabled] = 0;
				}
			}
		}

		//------------------------------------------------------------------------------------
		//Count the number of currently "Active" colors now that all filters have been applied
		//------------------------------------------------------------------------------------
		for (int i = 0; i < perlerColorsRGB.length; i++) {
			if (perlerColorsRGB[i][arrayIndex10_Disabled] == 1) {
				arrayLength++;
			}
		}

		//Create final array based on number of active colors
		int[][] returnPallette = new int[arrayLength][18];

		//Populate final array
		for (int i = 0; i < perlerColorsRGB.length; i++) {
			if (perlerColorsRGB[i][arrayIndex10_Disabled] == 1) {
				returnPallette[arrayIndex] = perlerColorsRGB[i];
				arrayIndex++;
			}
		}

		consoleHelper.PrintMessage("%%%%%%%%%%%%%%%%%%TOTAL PALLETTE COLORS = " + arrayLength + "%%%%%%%%%%%%%%%%%%");
		currentPallette = returnPallette;
	}


	//---------------------------------------------------------------------------
	// SetPalletteMode
	//---------------------------------------------------------------------------
//	public void SetPalletteMode(PalletteMode myPalletteMode) {
//		ConsoleHelper.PrintMessage("SetPalletteMode");
//
//		selectedColor = new Color(253,254,255); //Setting this to a random color that doesn't match any pallette colors. (previously it was set to -1, which was a color match to Arkal White.
//		palletteMode = myPalletteMode;
//
//		GetPalletteWithFiltersApplied();
//
//		/* if we are in single color mode,
//		switch to full color mode,
//		since our currently selected color may not exist in the pallette we are switching to. */
//		// if (viewMode % 2 == 0) {
//		// viewMode -= 1;
//		// }
//		//myRenderController.setFullColorViewMode();
//	}

	//---------------------------------------------------------------------------
	// setBeadBrand
	//---------------------------------------------------------------------------
	public boolean setBeadBrand(String name, boolean selected) {
		boolean returnVal = false;

		//if we only have one brand active, and the user is trying to turn a brand off, bail out
		if (usePerler + useHama + useArtkalS <= 1 && !selected) {
			returnVal = !selected;
		} else {

			switch(name) {
			case "Perler":
				usePerler = (selected) ? 1 : 0;
				break;
			case "Hama":
				useHama = (selected) ? 1 : 0;
				break;
			case "Artkal-S":
				useArtkalS = (selected) ? 1 : 0;
				break;
			}

			returnVal = selected;

			GetPalletteWithFiltersApplied();
		}

		return returnVal;
	}
	
	
	//---------------------------------------------------------------------------
	// showAllColors
	//---------------------------------------------------------------------------
	public void showAllColors() {
		
		selectedColorIndex = -1;
		
		Font labelFont = palletteSubPanels.get(0).paletteButton.getFont();
		
		for(int i = 0; i < palletteSubPanels.size(); i++) {
			palletteSubPanels.get(i).paletteButton.setFont(new Font(labelFont.getName(), Font.BOLD, labelFont.getSize()));
			palletteSubPanels.get(i).paletteButton.setForeground(ColorHelper.GetTextColorForBGColor(palletteSubPanels.get(i).paletteButton.bgColor));
		}
	}


	@Override
	public void onInterObjectCommunicator_CommunicateEvent(Object o) {
		if (o instanceof String) {
			String s = ((String) o);
			consoleHelper.PrintMessage("fired an event from oCommInterface in Palette: " + s);
	        if (s.equals("show all colors")) {
	        	showAllColors();
	        }
	        if (s.equals("create buttons")) {
	        	consoleHelper.PrintMessage("CreateButtons()");
	        	CreateButtons("");
	        }
		}
		if (o instanceof ActionEvent) {
			if (o instanceof ActionEvent) {
				ActionEvent a = ((ActionEvent) o);
				if (a.getSource() instanceof PaletteCheckBox) {
					PaletteCheckBox pcb = ((PaletteCheckBox) a.getSource());
					consoleHelper.PrintMessage("fired an event from oCommInterface in Palette: PaletteCheckBox action event");
					checkUncheck(pcb.colorIndex, pcb.isSelected());
				}
			}
		}
		if (o instanceof KeyEvent) {
			KeyEvent e = ((KeyEvent) o);
			if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
				consoleHelper.PrintMessage("fired a keyPressed event from oCommInterface in Palette" + e.getKeyCode());
				showAllColors();
	        }			
		}
		if (o instanceof Integer) {
			int colorIndex = ((int) o);
			setSelectedColor(colorIndex);
			CreateButtons("");
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
		if (descriptor.equals("set bead brand")) {
			if (o instanceof BMCheckBox) {
				consoleHelper.PrintMessage("running set bead brand for BMCheckBox");
				BMCheckBox cb = ((BMCheckBox) o);
				return setBeadBrand(StringHelper.removeXML(cb.getText()), !cb.isSelected());
			}
		}
		return null;
	}
}
