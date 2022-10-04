package beadMaker;

import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;

import processing.core.PImage;
import core.helper.ColorHelper;
import core.helper.FileHelper;
import core.helper.MathHelper;
import core.jfxComponent.SynchronousJFXFileChooser;
import core.logging.ConsoleHelper;

public class BMImage extends PImage {
	
	ConsoleHelper consoleHelper;
	FileHelper fileHelper;

	static final int upscalerForPDFPrinting = 30;

	static final float
	scaleForPDFPrinting_Perler = 14.1741f,
	scaleForPDFPrinting_PerlerMini = 7.6766f; //7.795; //7.6374;

	int imageScale_FinalRender;
	int imageScale_FinalRender_SingleTile;
	int imageScale_Preview;

	public volatile String SavePNG__myPNGFile;
	public volatile String SaveSCAD__mySCADFile;
	//public volatile String SavePatternPDF__myPDFFile;
	
	static final float
	Perler_RedPerceptionCorrectionFactor 	= 11, //11,
	Perler_GreenPerceptionCorrectionFactor 	= 11, //11,
	Perler_BluePerceptionCorrectionFactor 	= 11, //11,

	//TODO: WHY DO WE HAVE THESE WEIRD WEIGHTS???
	//	Artkal_RedPerceptionCorrectionFactor 	= 10, //8.89, //10
	//	Artkal_GreenPerceptionCorrectionFactor 	= 7, //15.81, //7
	//	Artkal_BluePerceptionCorrectionFactor 	= 17; //4.02; //17	

	Artkal_RedPerceptionCorrectionFactor 	= 11, //8.89, //10
	Artkal_GreenPerceptionCorrectionFactor 	= 11, //15.81, //7
	Artkal_BluePerceptionCorrectionFactor 	= 11; //4.02; //17

	public enum DitherMethod {
		FLOYD_STEINBERG,
		JARVIS_JUDICE_NINKE,
		STUCKI,
		ATKINSON,
		BURKES,
		SIERRA,
		TWO_ROW_SIERRA,
		SIERRA_LITE;

		//Do we need this? Not sure if/what it's used for
		//private static final DitherMethod[] values = values(); // to avoid recreating array

	}
	public DitherMethod ditherMethod = DitherMethod.FLOYD_STEINBERG;


	static final int
	DitherArrayIndex0_XPosition 	= 0,
	DitherArrayIndex1_YPosition 	= 1,
	DitherArrayIndex2_Numerator 	= 2,
	DitherArrayIndex3_Denominator 	= 3;

	DitherMatrix ditherMatrix;
	
	public int totalBeadsHightlighted = 0;
	
	public Color[][][] lutArray; //holds the entire LUT
	Color lutColor; //holds the rgb color derived from the LUT transformation
	
	BMImage colorMap;
	
	boolean useAppData;
	String appDataFolderName;
	
	int swapToPerlerColors_RecursionDepth = 0;

	//------------------------------------------------------------
	//CONSTRUCTOR
	//------------------------------------------------------------
	public BMImage(int width, int height, boolean myUseAppData, String myAppDataFolderName) {
		super(width, height);
		//have to do this because the PImage constructor sets format to RGB, which eliminates transparency
		this.format = ARGB;
		this.useAppData = myUseAppData;
		this.appDataFolderName = myAppDataFolderName;
		consoleHelper = new ConsoleHelper();
		fileHelper = new FileHelper(useAppData, appDataFolderName);
		ditherMatrix = new DitherMatrix();
	}

	public BMImage(Image img, boolean myUseAppData, String myAppDataFolderName) {
		super(img);
		//have to do this because the PImage constructor sets format to RGB, which eliminates transparency
		this.format = ARGB;
		this.useAppData = myUseAppData;
		this.appDataFolderName = myAppDataFolderName;
		consoleHelper = new ConsoleHelper();
		fileHelper = new FileHelper(useAppData, appDataFolderName);
		ditherMatrix = new DitherMatrix();
	}
	
	public BMImage(int width, int height, BMImage colorMap, boolean myUseAppData, String myAppDataFolderName) {
		super(width, height);
		//have to do this because the PImage constructor sets format to RGB, which eliminates transparency
		this.format = ARGB;
		this.useAppData = myUseAppData;
		this.appDataFolderName = myAppDataFolderName;
		consoleHelper = new ConsoleHelper();
		fileHelper = new FileHelper(useAppData, appDataFolderName);
		ditherMatrix = new DitherMatrix();
		this.colorMap = colorMap;		
		MapColors();
	}

	//---------------------------------------------------------------------------
	//get
	//---------------------------------------------------------------------------
	@Override
	public synchronized BMImage get() {
		return new BMImage((Image)super.get().getNative(), useAppData, appDataFolderName);
	}

	//---------------------------------------------------------------------------
	//get
	//---------------------------------------------------------------------------
	@Override
	public synchronized BMImage get(int x, int y, int w, int h) {
		return new BMImage((Image)super.get(x, y, w, h).getNative(), useAppData, appDataFolderName);
	}
	
	
	//---------------------------------------------------------------------------
	//setPixels
	//---------------------------------------------------------------------------
	public void setPixels(int[] pixels, int width, int height) {
		this.init(width, height, PImage.ARGB);
		this.pixels = pixels;
	}


	//	//---------------------------------------------------------------------------
	//	//LoadInputImageFile
	//	//---------------------------------------------------------------------------
	//	public void LoadInputImageFile(WindowController windowController, RenderController renderController, String imageFile) {
	//		ConsoleHelper.PrintMessage("LoadInputImageFile");
	//
	//		ConsoleHelper.PrintMessage("imageFile = " + imageFile);
	//
	//		if (new File(imageFile).exists()) {
	//			this.originalCleanedImage = ProcessingHelper.loadImage(imageFile);  
	//			originalCleanedImage = ReplacePureBlack(originalCleanedImage);
	//
	//			//imageScale_FinalRender = GetImageScale(originalCleanedImage, window_Render, windowController);
	//			renderController.viewMode = RenderController.ViewMode.FULLRENDER_FULLCOLOR;
	//		} else {
	//			DialogBoxHelper.WarningDialog("The project's image file (" + imageFile + ") does not exist. Please edit the PBP file to use a valid image file path.");
	//		}
	//		ConsoleHelper.PrintMessage("The method LoadInputImageFile is setting the redrawMode. )))))))))))))))))))))))))))))))))))))))))))");
	//		renderController.SetRedrawMode(RenderController.redrawMode_Everything, RenderController.ActiveDrawThread.TOOLBOX);
	//	}





	//---------------------------------------------------------------------------
	// SplitImageIntoTiles
	//---------------------------------------------------------------------------
	public synchronized static BMImage[] SplitImageIntoTiles(BMImage bmImage, RenderLabel renderLabel) {

		BMImage[] pdfImage = new BMImage[bmImage.GetTileCountForImage(bmImage, renderLabel)];
		BMImage localImage;

		int pdfImageIndex = 0;

		for (int g = 0; g <= Math.ceil((bmImage.height - 1) / renderLabel.pegboardPegsHigh); g++) {
			
			for (int f = 0; f <= Math.ceil((bmImage.width - 1) / renderLabel.pegboardPegsWide); f++) {

				//localImage = myImage.get(x,y,w,h)
				localImage =
						bmImage.get(
								f * renderLabel.pegboardPegsWide,
								g * renderLabel.pegboardPegsHigh,
								renderLabel.pegboardPegsWide,
								renderLabel.pegboardPegsHigh
								);

				pdfImage[pdfImageIndex++] = localImage;

			}
		}

		return pdfImage;
	}


	//---------------------------------------------------------------------------
	// GetTileCountForImage
	//---------------------------------------------------------------------------
	public synchronized int GetTileCountForImage(BMImage myImage, RenderLabel renderLabel) {
		return (int)(Math.ceil((float)myImage.width / renderLabel.pegboardPegsWide) * Math.ceil((float)myImage.height / renderLabel.pegboardPegsHigh));
	}


	//---------------------------------------------------------------------------
	// SavePNG
	//---------------------------------------------------------------------------
	void SavePNG() {
		consoleHelper.PrintMessage("SavePNG");

		File dataDir;		
		
		//if (useAppData) {
		//	dataDir = new File(System.getenv("APPDATA") + File.separator + appDataFolderName, File.separator);
		//} else {
		//	dataDir = new File(System.getProperty("user.dir"), File.separator);
		//}	
		
		//returns "my documents" directory
		//https://stackoverflow.com/questions/9677692/getting-my-documents-path-in-java
		dataDir = new JFileChooser().getFileSystemView().getDefaultDirectory();
		
		File selectedFile;
		
		SynchronousJFXFileChooser chooser = new SynchronousJFXFileChooser (
        	dataDir,
        	"Portable Network Graphics (*.png)",
        	new String[] {"png"}
        );
        selectedFile = chooser.showSaveDialog();	            
        
        if (selectedFile != null) {
        	
        	SavePNG__myPNGFile = selectedFile.toString();

			if (!fileHelper.getExtension(SavePNG__myPNGFile).equals("png")) {
				SavePNG__myPNGFile += ".png";
			}

			consoleHelper.PrintMessage("PNG Filename = " + SavePNG__myPNGFile);

			consoleHelper.PrintMessage("setting activeDrawThread = ActiveDrawThread.IMAGEPROCESSING");
			
			this.save(SavePNG__myPNGFile);

		}
		else {
			consoleHelper.PrintMessage("PNG file creation process failed");
			return; //bail out because the user did not select a file
		}
	}	
	
	
	//---------------------------------------------------------------------------
	// SaveSCAD
	//---------------------------------------------------------------------------
	void SaveSCAD() {
		consoleHelper.PrintMessage("SaveSCAD");

		File dataDir;		
		
		if (useAppData) {
			dataDir = new File(System.getenv("APPDATA") + File.separator + appDataFolderName, File.separator);
		} else {
			dataDir = new File(System.getProperty("user.dir"), File.separator);
		}
		
		File selectedFile;
		
		SynchronousJFXFileChooser chooser = new SynchronousJFXFileChooser (
        	dataDir,
        	"OpenSCAD Designs (*.scad)",
        	new String[] {"scad"}
        );
        selectedFile = chooser.showSaveDialog();	            
        
        if (selectedFile != null) {
        	
        	SaveSCAD__mySCADFile = selectedFile.toString();

			if (!fileHelper.getExtension(SaveSCAD__mySCADFile).equals("scad")) {
				SaveSCAD__mySCADFile += ".scad";
			}

			consoleHelper.PrintMessage("SCAD Filename = " + SavePNG__myPNGFile);

			consoleHelper.PrintMessage("setting activeDrawThread = ActiveDrawThread.IMAGEPROCESSING");
			
			//Do the business
			
			Color myPixelColor;
			
			PrintWriter scadOutput = null;
			try {
				scadOutput = new PrintWriter(SaveSCAD__mySCADFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			scadOutput.println("//width=" + this.width);
			scadOutput.println("//height=" + this.height);
			
			scadOutput.println(
				"CubePoints = [" +
					"[-10,  10,  0 ]," +
					"[" + (this.width * 5 + 5) + ", 10,  0 ]," +
					"[" + (this.width * 5 + 5) + "," + (0 - (this.height * 5 + 5)) + ", 0 ]," +
					"[-10," + (0 - (this.height * 5 + 5)) + ",  0 ]," +
					"[-10,  10, -20]," +
					"[" + (this.width * 5 + 5) + ", 10, -10]," +
					"[" + (this.width * 5 + 5) + "," + (0 - (this.height * 5 + 5)) + ", -10]," +
					"[-10," + (0 - (this.height * 5 + 5)) + ", -10]," +
				"];");
			scadOutput.println(
				"CubeFaces = [" + 
					"[0,1,2,3]," +
					"[4,5,1,0]," +
					"[7,6,5,4]," +
					"[5,6,2,1]," +
					"[6,7,3,2]," +
					"[7,4,0,3]"  +
				"];");
			scadOutput.println(
					"color([" +
						(0   / 255.0f) + ", " +
						(0 / 255.0f) + ", " +
						(0  / 255.0f) + 
					"]) polyhedron(CubePoints, CubeFaces);");
			
			for (int y = 0; y < this.height; y++) {
				for (int x = 0; x < this.width; x++) {
					myPixelColor = ColorHelper.pixeltoColor(this.pixels[y * this.width + x]);
					if(myPixelColor.getAlpha() == 255) {	
						scadOutput.println("translate([" + (x * 5) + ", " + (0 - (y * 5)) + ", 0])");
						scadOutput.println("color([" + (myPixelColor.getRed() / 255.0f) + ", " + (myPixelColor.getGreen() / 255.0f)+ ", " + (myPixelColor.getBlue() / 255.0f) + "]) hollowCylinder(d=4.75, h=5, wallWidth=1.0);");
					}
				}
			}
			
			scadOutput.println("$vpr = [0, 0, $t * 360];");
			scadOutput.println();
			scadOutput.println("module hollowCylinder(d=5, h=10, wallWidth=1, $fn=22)");
			scadOutput.println("{");
			scadOutput.println("	difference()");
			scadOutput.println("	{");
			scadOutput.println("		cylinder(d=d, h=h);");
			scadOutput.println("		translate([0, 0, -0.1]) { cylinder(d=d-(wallWidth*2), h=h+0.2); }");
			scadOutput.println("	}");
			scadOutput.println("}");
			
			scadOutput.close();

		}
		else {
			consoleHelper.PrintMessage("SCAD file creation process failed");
			return; //bail out because the user did not select a file
		}
	}	


	//---------------------------------------------------------------------------
	// GetPerlerPalletteIndexForSinglePixel
	//---------------------------------------------------------------------------
	//DONE: THIS FUNCTION NEEDS TO CONSIDER DITHERING
	int GetPerlerPaletteIndexForSinglePixel(Color myColor, Palette palette) {
		int pixelR;
		int pixelG;
		int pixelB;
		int pixelAlpha;

		int localPaletteIndex = -1;

		int RGBDistance;
		int BestRGBDistance;
		int perlerColorsArrayIndex;

		//myColor = ColorCorrect_SinglePixel(myColor);

		pixelR =     myColor.getRed();
		pixelG =     myColor.getGreen();
		pixelB =     myColor.getBlue();
		pixelAlpha = myColor.getAlpha();

		if (pixelAlpha == 255) { //(only get pixels with full alpha [255])

			//try to match the current color with a perler bead color
			RGBDistance = 10000000;
			BestRGBDistance = 10000000;
			perlerColorsArrayIndex = 0;

			for (int j = 0; j < palette.currentPalette.length; j++) {
				RGBDistance = 
						(int) (
								Math.pow(Math.abs(palette.currentPalette[j][palette.arrayIndex00_Red	] - pixelR) + 1, 2) + 
								Math.pow(Math.abs(palette.currentPalette[j][palette.arrayIndex01_Green	] - pixelG) + 1, 2) +
								Math.pow(Math.abs(palette.currentPalette[j][palette.arrayIndex02_Blue	] - pixelB) + 1, 2)
								);

				if (RGBDistance < BestRGBDistance) {
					BestRGBDistance = RGBDistance;
					//if we find a good match, set the colorPallette perlerColorsArrayIndex to the current perlerColor
					perlerColorsArrayIndex = j;
				}
			}
			//set the Color of the output color to the perler-mapped color
			localPaletteIndex = perlerColorsArrayIndex;
		}
		return localPaletteIndex;
	}



	//---------------------------------------------------------------------------
	// SwapToPerlerColors
	//---------------------------------------------------------------------------
	//synchronized public PImage SwapToPerlerColors(PImage image, int[][] pallette) {
	public void SwapToPerlerColors(Palette palette, float colorMatchingWeight_DitherLevel, float colorMatchingWeight_R, float colorMatchingWeight_G, float colorMatchingWeight_B, float colorMatchingWeight_Saturation, float colorMatchingWeight_Contrast, float colorMatchingWeight_Brightness, float colorMatchingWeight_Sharpness, BMImage lutImage, int minBeads, boolean isRecursiveCall) {
		consoleHelper.PrintMessage("SwapToPerlerColors");
		
		if(isRecursiveCall) {
			consoleHelper.PrintMessage("swapToPerlerColors_RecursionDepth = " + Integer.toString(++swapToPerlerColors_RecursionDepth));
		}
		else {		
			swapToPerlerColors_RecursionDepth = 0;
		
			this.Sharpen(colorMatchingWeight_Sharpness);
			this.ColorCorrect(
				colorMatchingWeight_R,
				colorMatchingWeight_G,
				colorMatchingWeight_B,
				colorMatchingWeight_Saturation,
				colorMatchingWeight_Contrast,
				colorMatchingWeight_Brightness,
				lutImage
			);
		}

		int
			pixelR,
			pixelG,
			pixelB;

		float
			RedPerceptionCorrectionFactor,
			GreenPerceptionCorrectionFactor,
			BluePerceptionCorrectionFactor;

		int rgbDistance;

		int bestRGBDistance;
		int perlerColorsArrayIndex;

		int[] rDitherError;
		int[] gDitherError;
		int[] bDitherError;

		//clear out previous counts
		for (int h = 0; h < palette.currentPalette.length; h++) {
			palette.currentPalette[h][palette.arrayIndex05_PixelCount] = 0;
			//palette.currentPalette[h][palette.arrayIndex18_SecondBestPixelCount] = 0;
			if(!isRecursiveCall) {
				palette.currentPalette[h][palette.arrayIndex19_FailedMinBeadCheck] = 0;
			}
		}

		palette.totalPalletteColors = 0;
		palette.totalBeadsUsed = 0;

		this.loadPixels();

		if (!GlobalConstants.showOriginalImageInsteadOfPixelMappedImage) {
			
			//create arrays (r, g, b) to store dither error (1 for each pixel)
			rDitherError = new int[this.height * this.width];
			gDitherError = new int[this.height * this.width];
			bDitherError = new int[this.height * this.width];
	
			//loop through each pixel in the original image, and add its color to the colorPallette if it has not yet been added.
			for (int i = 0; i < this.height * this.width; i++) {
	
				//Color myPixelColor = ProcessingHelper.pixeltoColor(this.pixels[i]);
				Color myPixelColor = ColorHelper.pixeltoColor(this.pixels[i]);
	
				//only care about pixels that are not part of the background 
				if (myPixelColor.getAlpha() == 255) { //(only get pixels with full alpha [255])
	
					//get the color of the pixel, +/- any dither error from adjacent pixels
					pixelR = MathHelper.ensureRange(myPixelColor.getRed() 	+ rDitherError[i], 0, 255);
					pixelG = MathHelper.ensureRange(myPixelColor.getGreen() + gDitherError[i], 0, 255);
					pixelB = MathHelper.ensureRange(myPixelColor.getBlue() 	+ bDitherError[i], 0, 255);
	
					//try to match the current color with a perler bead color
					rgbDistance = 10000000;
					bestRGBDistance = 10000000;
					perlerColorsArrayIndex = 0;
					
					for (int j = 0; j < palette.currentPalette.length; j++) {
						//if the color's checkbox is checked, try to color map it
						if(palette.currentPalette[j][palette.arrayIndex16_IsChecked] == 1 && palette.currentPalette[j][palette.arrayIndex19_FailedMinBeadCheck] == 0) {
	
							//Artkal
							if(palette.currentPalette[j][palette.arrayIndex11_Brand] == palette.brandIdArtkalS) {
								RedPerceptionCorrectionFactor 	= Artkal_RedPerceptionCorrectionFactor;
								GreenPerceptionCorrectionFactor = Artkal_GreenPerceptionCorrectionFactor;
								BluePerceptionCorrectionFactor 	= Artkal_BluePerceptionCorrectionFactor;
							//Perler
							} else { 
								RedPerceptionCorrectionFactor 	=  Perler_RedPerceptionCorrectionFactor;
								GreenPerceptionCorrectionFactor =  Perler_GreenPerceptionCorrectionFactor;
								BluePerceptionCorrectionFactor 	=  Perler_BluePerceptionCorrectionFactor;
							}					
	
							rgbDistance =
								(int)(
									Math.pow(Math.abs(palette.currentPalette[j][palette.arrayIndex13_MapRed		] - pixelR) * 100 / RedPerceptionCorrectionFactor  , 2) +
									Math.pow(Math.abs(palette.currentPalette[j][palette.arrayIndex14_MapGreen	] - pixelG) * 100 / GreenPerceptionCorrectionFactor, 2) +
									Math.pow(Math.abs(palette.currentPalette[j][palette.arrayIndex15_MapBlue		] - pixelB) * 100 / BluePerceptionCorrectionFactor , 2)
								);
							if (rgbDistance < bestRGBDistance) {
								//set the best value to the current value
								bestRGBDistance = rgbDistance;
								//if we find a good match, set the colorPallette perlerColorsArrayIndex to the current perlerColor
								perlerColorsArrayIndex = j;
							}
						}
					}
	
					//----------------------------------------------------------------------------------
					//DITHERING
					//----------------------------------------------------------------------------------
					//All of the dithering algorithms come from here:
					//http://www.tannerhelland.com/4660/dithering-eleven-algorithms-source-code/
					// This process takes dither offsets from the current pixel and applies them to future pixels.
					// It does *not* affect the coloring of the current pixel.
					//----------------------------------------------------------------------------------
					int myDitherMethod = ditherMethod.ordinal();
					int ditherDivisor = ditherMatrix.ditherMatrix[myDitherMethod][0][DitherArrayIndex3_Denominator];
	
					for (int k = 0; k < ditherMatrix.ditherMatrix[myDitherMethod].length; k++) {
						//based on XPosition and YPosition values in DitherMatrices, target a specific pixel for dithering positionally relative to the current pixel
						int targetPixel =
								i +
								ditherMatrix.ditherMatrix[myDitherMethod][k][DitherArrayIndex0_XPosition] +
								ditherMatrix.ditherMatrix[myDitherMethod][k][DitherArrayIndex1_YPosition] * this.width;
	
						//if the forward dither is in bounds of the image,
						if (targetPixel < rDitherError.length) {
							//add the error to the forward pixel, adjusted by the error weight and the dither slider (0-100)
							rDitherError[targetPixel] += (pixelR - palette.currentPalette[perlerColorsArrayIndex][palette.arrayIndex13_MapRed	]) * colorMatchingWeight_DitherLevel * ditherMatrix.ditherMatrix[myDitherMethod][k][DitherArrayIndex2_Numerator] / ditherDivisor;
							gDitherError[targetPixel] += (pixelG - palette.currentPalette[perlerColorsArrayIndex][palette.arrayIndex14_MapGreen	]) * colorMatchingWeight_DitherLevel * ditherMatrix.ditherMatrix[myDitherMethod][k][DitherArrayIndex2_Numerator] / ditherDivisor;
							bDitherError[targetPixel] += (pixelB - palette.currentPalette[perlerColorsArrayIndex][palette.arrayIndex15_MapBlue	]) * colorMatchingWeight_DitherLevel * ditherMatrix.ditherMatrix[myDitherMethod][k][DitherArrayIndex2_Numerator] / ditherDivisor;
						}
					}
					//----------------------------------------------------------------------------------
					//----------------------------------------------------------------------------------
	
					//increment the count of the matched perler color by 1
					palette.currentPalette[perlerColorsArrayIndex][palette.arrayIndex05_PixelCount]++;
					palette.totalBeadsUsed++;
					//if this is the first time we've encountered this perler color, 
					if (palette.currentPalette[perlerColorsArrayIndex][palette.arrayIndex05_PixelCount] == 1) {
						//add 1 to the total number of colors used.
						palette.totalPalletteColors++;
					}
					//set the pixel of the output image to the perler-mapped color
					//added the 255 alpha in an attempt to squash the random pixel colorization bug
	
					//See Utilities.pixeltoColor for bit shifting background
					this.pixels[i] = ColorHelper.rgbaToProcessingColor(
						palette.currentPalette[perlerColorsArrayIndex][palette.arrayIndex00_Red],
						palette.currentPalette[perlerColorsArrayIndex][palette.arrayIndex01_Green],
						palette.currentPalette[perlerColorsArrayIndex][palette.arrayIndex02_Blue],
						255
					);
				}
			}
	
	//		//add all unchecked colors to the totalPalletteColors
	//		for (int i = 0; i < pallette.currentPallette.length; i++) {
	//			if (pallette.currentPallette[i][pallette.arrayIndex16_IsChecked] == 0) {
	//				pallette.totalPalletteColors++;
	//			}
	//		}
			
			
			//----------------------------------------------------------------------------------
			// Check for colors that do not meet min N and call SwapToPerlerColors recursively
			//----------------------------------------------------------------------------------
			int FailedMinBeadCheck_LowestPixelCount = 10000000;
			int FailedMinBeadCheck_PerlerColorsArrayIndex = -1;
			
			//turn off all zero colors
			if(!isRecursiveCall) {
				for (int i = 0; i < palette.currentPalette.length; i++) {
					if (
						palette.currentPalette[i][palette.arrayIndex16_IsChecked] == 1 &&
						palette.currentPalette[i][palette.arrayIndex05_PixelCount] == 0
						
					) {
						palette.currentPalette[i][palette.arrayIndex19_FailedMinBeadCheck] = 1;
					}
				}
			}
			
			//add all unchecked colors to the totalPalletteColors
			for (int i = 0; i < palette.currentPalette.length; i++) {
				if (
					   palette.currentPalette[i][palette.arrayIndex16_IsChecked] == 1
					&& palette.currentPalette[i][palette.arrayIndex19_FailedMinBeadCheck] == 0
					//&& palette.currentPalette[i][palette.arrayIndex05_PixelCount] > 0
				) {
					int beadCheckCount =
						palette.currentPalette[i][palette.arrayIndex05_PixelCount]; //+
						//palette.currentPalette[i][palette.arrayIndex18_SecondBestPixelCount];
					if(palette.currentPalette[i][palette.arrayIndex05_PixelCount] < minBeads) {
						//if(beadCheckCount < FailedMinBeadCheck_LowestPixelCount) {
							FailedMinBeadCheck_LowestPixelCount = beadCheckCount;
							FailedMinBeadCheck_PerlerColorsArrayIndex = i;
							break; //exit for loop if we've identified a color below minBeads
						//}
					}
				}
			}
				
			//if one or more colors failed the min N check, do this
			if(FailedMinBeadCheck_PerlerColorsArrayIndex > -1) {
				consoleHelper.PrintMessage("FailedMinBeadCheckColors = (index:)" + Integer.toString(FailedMinBeadCheck_PerlerColorsArrayIndex) + " " + palette.perlerColorsNames[FailedMinBeadCheck_PerlerColorsArrayIndex][1] + " " + palette.perlerColorsNames[FailedMinBeadCheck_PerlerColorsArrayIndex][0]);
				palette.currentPalette[FailedMinBeadCheck_PerlerColorsArrayIndex][palette.arrayIndex19_FailedMinBeadCheck] = 1;
				SwapToPerlerColors(
					palette,
					colorMatchingWeight_DitherLevel,
					colorMatchingWeight_R,
					colorMatchingWeight_G,
					colorMatchingWeight_B,
					colorMatchingWeight_Saturation,
					colorMatchingWeight_Contrast,
					colorMatchingWeight_Brightness,
					colorMatchingWeight_Sharpness,
					lutImage,
					minBeads,
					true
				);
				//skip the updatePixels() step
				return;
			}
			
			
		}
		
		

		this.updatePixels();
	}


	//---------------------------------------------------------------------------
	// HighlightSelectedColor
	//---------------------------------------------------------------------------
	public synchronized BMImage HighlightSelectedColor(BMImage bmImage, Palette pallette, int myPalletteIndex) {
		consoleHelper.PrintMessage("HighlightSelectedColor, looking for index " + myPalletteIndex);
		
		BMImage image = bmImage.get();
		int colorIndex = 0;
		
		for (int i = 0; i < pallette.currentPalette.length; i++) {
			if (pallette.currentPalette[i][pallette.arrayIndex04_ColorIndex] == myPalletteIndex) {
				colorIndex = i;
				consoleHelper.PrintMessage("HighlightSelectedColor - color chosen: " + pallette.perlerColorsNames[pallette.currentPalette[i][pallette.arrayIndex04_ColorIndex]][1] + " " + pallette.perlerColorsNames[pallette.currentPalette[i][pallette.arrayIndex04_ColorIndex]][0]);
			}
		}

		//ConsoleHelper.PrintMessage("Looking for pallette index " + myPalletteIndex);
		//ConsoleHelper.PrintMessage("redValue = " + pallette.currentPallette[colorIndex][pallette.arrayIndex13_MapRed	]);
		//ConsoleHelper.PrintMessage("grnValue = " + pallette.currentPallette[colorIndex][pallette.arrayIndex14_MapGreen	]);
		//ConsoleHelper.PrintMessage("bluValue = " + pallette.currentPallette[colorIndex][pallette.arrayIndex15_MapBlue	]);

		//USED FOR PDF PATTERNS
		image.totalBeadsHightlighted = 0;

		image.loadPixels();
		
		for (int i = 0; i < image.height * image.width; i++) {
			
			Color myPixelColor = ColorHelper.pixeltoColor(image.pixels[i]);

			//only care about pixels that are not part of the background
			if (myPixelColor.getAlpha() == 255) {

				if (
					myPixelColor.getRed() 	== pallette.currentPalette[colorIndex][pallette.arrayIndex00_Red	] &&
					myPixelColor.getGreen() == pallette.currentPalette[colorIndex][pallette.arrayIndex01_Green	] &&
					myPixelColor.getBlue() 	== pallette.currentPalette[colorIndex][pallette.arrayIndex02_Blue	]
				) {
					//ConsoleHelper.PrintMessage("incrementing totalBeadsHightlighted");
					image.totalBeadsHightlighted++;
					//ConsoleHelper.PrintMessage("totalBeadsHightlighted = " + totalBeadsHightlighted);
					image.pixels[i] = ColorHelper.colorToProcessingColor(new Color(1, 2, 3));
				} else {
					image.pixels[i] = ColorHelper.colorToProcessingColor(new Color(myPixelColor.getRed() / 4 + 175, myPixelColor.getGreen() / 4 + 175, myPixelColor.getBlue() / 4 + 175)); //subdued color
				}
			}
		}
		
		image.updatePixels();
		return image;
	}


	
	


	//---------------------------------------------------------------------------
	// ContrastAdjust
	// --------------------------------------------------------------------------
	synchronized int ContrastAdjust(int colorValue, float colorMatchingWeight_Contrast) {

		return MathHelper.ensureRange((int)(colorValue * colorMatchingWeight_Contrast + 128f), 0, 255);
	}


	//---------------------------------------------------------------------------
	// BrightnessAdjust
	// --------------------------------------------------------------------------
	synchronized int BrightnessAdjust(int colorValue, float colorMatchingWeight_Brightness) {

		return MathHelper.ensureRange(colorValue + (int)colorMatchingWeight_Brightness, 0, 255);
	}


	//---------------------------------------------------------------------------
	// SaturationAdjust
	// --------------------------------------------------------------------------
	synchronized Color SaturationAdjust(Color myPixel, float colorMatchingWeight_Saturation) {

		int myRed, myGreen, myBlue, myAlpha;
		int maxVal, minVal, avgVal;
		int distanceToFullSaturation, distanceToZeroSaturation;

		myRed   = myPixel.getRed();
		myGreen = myPixel.getGreen();
		myBlue  = myPixel.getBlue();
		myAlpha = myPixel.getAlpha();

		if (myAlpha != 255) { //Only modify fully opaque pixels
			return new Color(myRed, myGreen, myBlue, myAlpha);
		}

		maxVal = Math.max(Math.max(myRed,myGreen),myBlue);
		minVal = Math.min(Math.min(myRed,myGreen),myBlue);
		avgVal = (maxVal + minVal) / 2;
		distanceToFullSaturation  = Math.min(minVal, 255 - maxVal);
		distanceToZeroSaturation  = maxVal - avgVal;

		//Assume that colorMatchingWeight_Saturation is a range between -1.0 and 1.0
		if (colorMatchingWeight_Saturation > 0.0) {
			myRed   += (int)(colorMatchingWeight_Saturation * distanceToFullSaturation * ((myRed   - avgVal) / (float)distanceToZeroSaturation));
			myGreen += (int)(colorMatchingWeight_Saturation * distanceToFullSaturation * ((myGreen - avgVal) / (float)distanceToZeroSaturation));
			myBlue  += (int)(colorMatchingWeight_Saturation * distanceToFullSaturation * ((myBlue  - avgVal) / (float)distanceToZeroSaturation));
		}

		else if (colorMatchingWeight_Saturation < 0.0) {
			myRed   += (int)(colorMatchingWeight_Saturation * distanceToZeroSaturation * ((myRed   - avgVal) / (float)distanceToZeroSaturation));
			myGreen += (int)(colorMatchingWeight_Saturation * distanceToZeroSaturation * ((myGreen - avgVal) / (float)distanceToZeroSaturation));
			myBlue  += (int)(colorMatchingWeight_Saturation * distanceToZeroSaturation * ((myBlue  - avgVal) / (float)distanceToZeroSaturation));
		}

		return new Color (
				MathHelper.ensureRange(myRed, 0, 255),
				MathHelper.ensureRange(myGreen, 0, 255),
				MathHelper.ensureRange(myBlue, 0, 255), myAlpha
				);
	}


	//---------------------------------------------------------------------------
	// Sharpen
	// --------------------------------------------------------------------------
	public void Sharpen(float colorMatchingWeight_Sharpness) {
		consoleHelper.PrintMessage("Sharpen");
		
		boolean allNeighborPixelsAreOpaque;

		//float kernelMultiplier = 0;

		// float[][] kernel = {{  -0.05, -0.1,  -0.05}, 
		// { -0.1,  1.6, -0.1}, 
		// {  -0.05, -0.1,  -0.05}};

		// float[][] kernel = {{  -0.1, -0.2,  -0.1}, 
		// { -0.2,  2.2, -0.2}, 
		// {  -0.1, -0.2,  -0.1}};

		//this one is too strong unless the image is scaled to 200%
		// float[][] kernel = {{  -0.2, -0.4,  -0.2}, 
		// { -0.4,  3.4, -0.4}, 
		// {  -0.2, -0.4,  -0.2}};

		// float[][] kernel = {{  -0.05, -0.2,  -0.05}, 
		// { -0.2,  1.0, -0.2}, 
		// {  -0.05, -0.2,  -0.05}};

		float[][] kernel = {
				{ -0.5f, -1.0f, -0.5f}, 
				{ -1.0f,  6.0f, -1.0f}, 
				{ -0.5f, -1.0f, -0.5f}
		};

		// float[][] kernel = {{ 0, -1, 0}, 
		// { -1, 5, -1}, 
		// { 0, -1, 0}};

		//image(img, 0, 0); // Displays the image from point (0,0) 
		//this.loadPixels();
		// Create an opaque image of the same size as the original
		BMImage edgeImg = this.get();

		// Loop through every pixel in the image.
		for (int y = 1; y < this.height-1; y++) { // Skip top and bottom edges

			for (int x = 1; x < this.width-1; x++) { // Skip left and right edges

				//set to default starting value
				allNeighborPixelsAreOpaque = true;
				
				Color myPixelColor = ColorHelper.pixeltoColor(this.pixels[y * this.width + x]);

				//coreA refers to the alpha value of the target (center of the matrix) pixel
				float coreA = myPixelColor.getAlpha();

				float
				sumR = 0,
				sumG = 0,
				sumB = 0;

				for (int ky = -1; ky <= 1; ky++) {

					for (int kx = -1; kx <= 1; kx++) {

						// Calculate the adjacent pixel for this kernel point
						int pos = (y + ky)*this.width + (x + kx);

						Color adjacentPixelColor = ColorHelper.pixeltoColor(this.pixels[pos]);

						float valR = adjacentPixelColor.getRed();
						float valG = adjacentPixelColor.getGreen();
						float valB = adjacentPixelColor.getBlue();
						//valA refers to the alpha value of the current kernel matrix pixel
						float valA = adjacentPixelColor.getAlpha();

						float kernelPlusOne = (ky==0 && kx==0 ? 1.0f : 0.0f);

						if (valA == 255 && coreA == 255) {
							// Multiply adjacent pixels based on the kernel values
							sumR += (kernel[ky+1][kx+1] * colorMatchingWeight_Sharpness + kernelPlusOne) * valR;
							sumG += (kernel[ky+1][kx+1] * colorMatchingWeight_Sharpness + kernelPlusOne) * valG;
							sumB += (kernel[ky+1][kx+1] * colorMatchingWeight_Sharpness + kernelPlusOne) * valB;
						} else {
							//do not sharpen pixels that are on the edge of a transparent area
							allNeighborPixelsAreOpaque = false;
						}
					}
				}
				
				//do not sharpen pixels that are on the edge of a transparent area
				if (allNeighborPixelsAreOpaque) {
					// For this pixel in the new image, set the RGB values
					// based on the sum from the kernel
					edgeImg.pixels[y*this.width + x] = 
							ColorHelper.rgbaToProcessingColor(
							MathHelper.ensureRange((int)sumR, 0, 255),
							MathHelper.ensureRange((int)sumG, 0, 255),
							MathHelper.ensureRange((int)sumB, 0, 255),
							(int)coreA
						);
				}				
			}
		}
		// State that there are changes to edgeImg.pixels[]
		//edgeImg.updatePixels();
		//image(edgeImg, width/2, 0); // Draw the new image

		this.pixels = edgeImg.pixels;
		//edgeImg.updatePixels();
		//this.updatePixels();
	}

	//---------------------------------------------------------------------------
	// ColorCorrect
	// --------------------------------------------------------------------------
	// This function is used to adjust image colors/brightness/contrast BEFORE
	// the SwapToPerlerColors function is called on the image
	//---------------------------------------------------------------------------
	public void ColorCorrect(float colorMatchingWeight_R, float colorMatchingWeight_G, float colorMatchingWeight_B, float colorMatchingWeight_Saturation, float colorMatchingWeight_Contrast, float colorMatchingWeight_Brightness, BMImage lutImage) {
		consoleHelper.PrintMessage("ColorCorrect");
		
		consoleHelper.PrintMessage("colorMatchingWeight_R = " + colorMatchingWeight_R);

		//color myPixel;
		Color myPixel;
		int myAlpha;

		this.loadPixels();

		//for some reason pure black does not work properly, so this hack replaces color(0,0,0) with color(1,1,1)
		//This is a hack fix for a known Processing bug: https://github.com/processing/processing/wiki/Troubleshooting
		for (int i = 0; i < this.height * this.width; i++) {

			myPixel = ColorCorrect_SinglePixel(ColorHelper.pixeltoColor(this.pixels[i]), colorMatchingWeight_R, colorMatchingWeight_G, colorMatchingWeight_B, colorMatchingWeight_Saturation, colorMatchingWeight_Contrast, colorMatchingWeight_Brightness, lutImage);

			myAlpha = myPixel.getAlpha();

			if(myAlpha == 0) {
				this.pixels[i] = ColorHelper.rgbaToProcessingColor(255, 255, 255, 0);
			} else {
				this.pixels[i] = ColorHelper.colorToProcessingColor(myPixel);
			}
		}
		this.updatePixels();
	}

	//---------------------------------------------------------------------------
	// ColorCorrect_SinglePixel
	// --------------------------------------------------------------------------
	synchronized Color ColorCorrect_SinglePixel(Color myColor, float colorMatchingWeight_R, float colorMatchingWeight_G, float colorMatchingWeight_B, float colorMatchingWeight_Saturation, float colorMatchingWeight_Contrast, float colorMatchingWeight_Brightness, BMImage lutImage) {

		int myRed, myGreen, myBlue, myAlpha;

		myColor = SaturationAdjust(myColor, colorMatchingWeight_Saturation);

		myRed 	= BrightnessAdjust(ContrastAdjust((int)(myColor.getRed() 	* colorMatchingWeight_R - 128f), colorMatchingWeight_Contrast), colorMatchingWeight_Brightness);
		myGreen = BrightnessAdjust(ContrastAdjust((int)(myColor.getGreen() 	* colorMatchingWeight_G - 128f), colorMatchingWeight_Contrast), colorMatchingWeight_Brightness);
		myBlue 	= BrightnessAdjust(ContrastAdjust((int)(myColor.getBlue() 	* colorMatchingWeight_B - 128f), colorMatchingWeight_Contrast), colorMatchingWeight_Brightness);
		if (myColor.getAlpha() == 255) {
			myAlpha = 255;
		} else {
			myAlpha = 0;
		}
				
		if (GlobalConstants.applyLUT == 1) {
			Color lutColor = ApplyLUT(new Color(myRed, myGreen, myBlue, myAlpha), lutImage);
			myRed = lutColor.getRed();
			myGreen = lutColor.getGreen();
			myBlue = lutColor.getBlue();
		}

		return new Color(myRed, myGreen, myBlue, myAlpha);
	}
	
	
	//---------------------------------------------------------------------------
	// ApplyLUT
	// --------------------------------------------------------------------------
	synchronized Color ApplyLUT(Color myColor, BMImage lutImage) {

		int myRed, myGreen, myBlue, myAlpha;
		Color myLUTColor;
		
		myRed 	= myColor.getRed();
		myGreen = myColor.getGreen();
		myBlue 	= myColor.getBlue();
		myAlpha = myColor.getAlpha();
		
//		ConsoleHelper.PrintMessage("myRed: " + myRed);
//		ConsoleHelper.PrintMessage("myGreen: " + myGreen);
//		ConsoleHelper.PrintMessage("myBlue: " + myBlue);
//		
//		ConsoleHelper.PrintMessage("myRed bitshifted: " + (myRed >> 2));
//		ConsoleHelper.PrintMessage("myGreen bitshifted: " + (myGreen >> 2));
//		ConsoleHelper.PrintMessage("myBlue bitshifted: " + (myBlue >> 2));
//		
//		ConsoleHelper.PrintMessage("myRed bitshifted and multiplied: " + (myRed >> 2));
//		ConsoleHelper.PrintMessage("myGreen bitshifted and multiplied: " + ((myGreen >> 2) * 64));
//		ConsoleHelper.PrintMessage("myBlue bitshifted and multiplied: " + ((myBlue >> 2) * 4096));
//		
//		ConsoleHelper.PrintMessage("LUT image contains " + lutImage.pixels.length + " pixels.");
//		
//		ConsoleHelper.PrintMessage("Looking for LUT Pixel number: " + (((myBlue >> 2) * 4096) + ((myGreen >> 2) * 64) + (myRed >> 2)));
		
		//if using a 512 x 512 LUT (64r x 64g x 64b), drop the two least significant bits in each of R, G and B.
		//(red on X axis, green on Y axis, blue on Z axis)
		myLUTColor = ColorHelper.pixeltoColor(lutImage.pixels[((myBlue >> 2) % 8 * 64) + ((myBlue >> 2) / 8 * 64 * 512) + ((myGreen >> 2) * 512) + (myRed >> 2)]);
				
		return new Color(
			myLUTColor.getRed(),
			myLUTColor.getGreen(),
			myLUTColor.getBlue(),
			myAlpha
		);			
	}
	
	
	//---------------------------------------------------------------------------
	// FlipHorizontally
	// --------------------------------------------------------------------------
	public void FlipHorizontally() {

		consoleHelper.PrintMessage("FlipHorizontally");
		
		BMImage sourceImage = this.get();			
		
		this.loadPixels();
		sourceImage.loadPixels();
		
		for (int y = 0; y < this.height; y++) {

			for (int x = 0; x < this.width; x++) {
				this.pixels[y * this.width + x] = sourceImage.pixels[y * this.width + (this.width - 1 - x)];
			}
		}
		
		this.updatePixels();
	}
		
		

			

	//---------------------------------------------------------------------------
	// ReplacePureBlack
	//---------------------------------------------------------------------------
	//This is a hack fix for a known Processing bug: https://github.com/processing/processing/wiki/Troubleshooting
	/*
	EXCERPT FROM WEBSITE:
	--------------------------
	color(0, 0, 0, 0) is Black.

	The reason this doesn't work is that color(0, 0, 0, 0) creates an int that is simply '0'.
	Which means that fill(color(0, 0, 0, 0)) is the same as fill(0), which is...black.
	This is a problem of 'color' not being a real type, but just an int,
	plus the fact that we overload fill() to use both int/color for a color,
	and also an int for a gray. Since this is unlikely to be fixed anytime soon (if ever),
	there are multiple workarounds that you can use:

	use fill(0, 0, 0, 0)
	fill(c, 0) where c = color(0, 0, 0)
	color almostTransparent = color(0, 0, 0, 1);
	color almostBlack = color(1, 1, 1, 0);
	 */
	public BMImage ReplacePureBlack(BMImage image) {

		consoleHelper.PrintMessage("ReplacePureBlack");
		int myRed, myGreen, myBlue, myAlpha;

		image.loadPixels();

		//for some reason pure black does not work properly, so this hack replaces color(0,0,0) with color(1,1,1)
		//This is a hack fix for a known Processing bug: https://github.com/processing/processing/wiki/Troubleshooting
		for (int i = 0; i < image.height * image.width; i++) {

			Color myPixel = ColorHelper.pixeltoColor(image.pixels[i]);

			myRed 	= myPixel.getRed();
			myGreen = myPixel.getGreen();
			myBlue 	= myPixel.getBlue();
			myAlpha = myPixel.getAlpha();

			if(myRed == 0 && myGreen == 0 && myBlue == 0) {
				image.pixels[i] = ColorHelper.rgbaToProcessingColor(1, 1, 1, myAlpha);
			}

			if(myAlpha == 0) {
				image.pixels[i] = ColorHelper.rgbaToProcessingColor(255, 255, 255, 0);
			}
		}
		image.updatePixels();
		return image;
	}
	
	
	//---------------------------------------------------------------------------
	// MapColors
	//---------------------------------------------------------------------------
	public void MapColors() {
		consoleHelper.PrintMessage("MapColors");

//		int
//		pixelR,
//		pixelG,
//		pixelB;
//
//		float
//		RedPerceptionCorrectionFactor,
//		GreenPerceptionCorrectionFactor,
//		BluePerceptionCorrectionFactor;
//
//		int rgbDistance;
//
//		int bestRGBDistance;
//		int perlerColorsArrayIndex;
//
//		int[] rDitherError;
//		int[] gDitherError;
//		int[] bDitherError;
//
//		//clear out previous counts
//		for (int h = 0; h < pallette.currentPallette.length; h++) {
//			pallette.currentPallette[h][pallette.arrayIndex05_PixelCount] = 0;
//		}
//
//		pallette.totalPalletteColors = 0;
//		pallette.totalBeadsUsed = 0;
//
//		this.loadPixels();
//
//		if (!GlobalConstants.showOriginalImageInsteadOfPixelMappedImage) {
//			rDitherError = new int[this.height * this.width];
//			gDitherError = new int[this.height * this.width];
//			bDitherError = new int[this.height * this.width];
//	
//			//loop through each pixel in the original image, and add its color to the colorPallette if it has not yet been added.
//			for (int i = 0; i < this.height * this.width; i++) {
//	
//				//Color myPixelColor = ProcessingHelper.pixeltoColor(this.pixels[i]);
//				Color myPixelColor = ColorHelper.pixeltoColor(this.pixels[i]);
//	
//				//only care about pixels that are not part of the background 
//				if (myPixelColor.getAlpha() == 255) { //(only get pixels with full alpha [255])
//	
//					//get the color of the pixel, +/- any dither error from adjacent pixels
//					pixelR = MathHelper.ensureRange(myPixelColor.getRed() 	+ rDitherError[i], 0, 255);
//					pixelG = MathHelper.ensureRange(myPixelColor.getGreen() + gDitherError[i], 0, 255);
//					pixelB = MathHelper.ensureRange(myPixelColor.getBlue() 	+ bDitherError[i], 0, 255);
//	
//					//try to match the current color with a perler bead color
//					rgbDistance = 10000000;
//					bestRGBDistance = 10000000;
//					perlerColorsArrayIndex = 0;
//	
//					for (int j = 0; j < pallette.currentPallette.length; j++) {
//						//if the color's checkbox is checked, try to color map it
//						if(pallette.currentPallette[j][pallette.arrayIndex16_IsChecked] == 1) {
//	
//							//Artkal
//							if(pallette.currentPallette[j][pallette.arrayIndex11_Brand] == pallette.brandIdArtkalS) {
//								RedPerceptionCorrectionFactor 	= Artkal_RedPerceptionCorrectionFactor;
//								GreenPerceptionCorrectionFactor = Artkal_GreenPerceptionCorrectionFactor;
//								BluePerceptionCorrectionFactor 	= Artkal_BluePerceptionCorrectionFactor;
//								//Perler
//							} else { 
//								RedPerceptionCorrectionFactor 	=  Perler_RedPerceptionCorrectionFactor;
//								GreenPerceptionCorrectionFactor =  Perler_GreenPerceptionCorrectionFactor;
//								BluePerceptionCorrectionFactor 	=  Perler_BluePerceptionCorrectionFactor;
//							}					
//	
//							rgbDistance =
//									(int)(
//											Math.pow(Math.abs(pallette.currentPallette[j][pallette.arrayIndex13_MapRed		] - pixelR) * 100 / RedPerceptionCorrectionFactor  , 2) +
//											Math.pow(Math.abs(pallette.currentPallette[j][pallette.arrayIndex14_MapGreen	] - pixelG) * 100 / GreenPerceptionCorrectionFactor, 2) +
//											Math.pow(Math.abs(pallette.currentPallette[j][pallette.arrayIndex15_MapBlue	] - pixelB) * 100 / BluePerceptionCorrectionFactor , 2)
//											);
//							//If there is a better match for the pixel, do this
//							if (rgbDistance < bestRGBDistance) {
//								bestRGBDistance = rgbDistance;
//								//if we find a good match, set the colorPallette perlerColorsArrayIndex to the current perlerColor
//								perlerColorsArrayIndex = j;
//							}
//						}
//					}
//	
//					//----------------------------------------------------------------------------------
//					//DITHERING
//					//----------------------------------------------------------------------------------
//					//All of the dithering algorithms come from here:
//					//http://www.tannerhelland.com/4660/dithering-eleven-algorithms-source-code/
//					//----------------------------------------------------------------------------------
//					int myDitherMethod = ditherMethod.ordinal();
//					int ditherDivisor = ditherMatrix.ditherMatrix[myDitherMethod][0][DitherArrayIndex3_Denominator];
//	
//					for (int k = 0; k < ditherMatrix.ditherMatrix[myDitherMethod].length; k++) {
//						//based on XPosition and YPosition values in DitherMatrices, target a specific pixel for dithering positionally relative to the current pixel
//						int targetPixel =
//								i +
//								ditherMatrix.ditherMatrix[myDitherMethod][k][DitherArrayIndex0_XPosition] +
//								ditherMatrix.ditherMatrix[myDitherMethod][k][DitherArrayIndex1_YPosition] * this.width;
//	
//						//if the forward dither is in bounds of the image,
//						if (targetPixel < rDitherError.length) {
//							//add the error to the forward pixel, adjusted by the error weight and the dither slider (0-100)
//							rDitherError[targetPixel] += (pixelR - pallette.currentPallette[perlerColorsArrayIndex][pallette.arrayIndex13_MapRed	]) * colorMatchingWeight_DitherLevel * ditherMatrix.ditherMatrix[myDitherMethod][k][DitherArrayIndex2_Numerator] / ditherDivisor;
//							gDitherError[targetPixel] += (pixelG - pallette.currentPallette[perlerColorsArrayIndex][pallette.arrayIndex14_MapGreen	]) * colorMatchingWeight_DitherLevel * ditherMatrix.ditherMatrix[myDitherMethod][k][DitherArrayIndex2_Numerator] / ditherDivisor;
//							bDitherError[targetPixel] += (pixelB - pallette.currentPallette[perlerColorsArrayIndex][pallette.arrayIndex15_MapBlue	]) * colorMatchingWeight_DitherLevel * ditherMatrix.ditherMatrix[myDitherMethod][k][DitherArrayIndex2_Numerator] / ditherDivisor;
//						}
//					}
//					//----------------------------------------------------------------------------------
//					//----------------------------------------------------------------------------------
//	
//					//increment the count of the matched perler color by 1
//					pallette.currentPallette[perlerColorsArrayIndex][pallette.arrayIndex05_PixelCount]++;
//					pallette.totalBeadsUsed++;
//					//if this is the first time we've encountered this perler color, 
//					if (pallette.currentPallette[perlerColorsArrayIndex][pallette.arrayIndex05_PixelCount] == 1) {
//						//add 1 to the total number of colors used.
//						pallette.totalPalletteColors++;
//					}
//					//set the pixel of the output image to the perler-mapped color
//					//added the 255 alpha in an attempt to squash the random pixel colorization bug
//	
//					//See Utilities.pixeltoColor for bit shifting background
//					this.pixels[i] = ColorHelper.rgbaToProcessingColor(
//							pallette.currentPallette[perlerColorsArrayIndex][pallette.arrayIndex00_Red],
//							pallette.currentPallette[perlerColorsArrayIndex][pallette.arrayIndex01_Green],
//							pallette.currentPallette[perlerColorsArrayIndex][pallette.arrayIndex02_Blue],
//							255
//							);
//				}
//			}
//	
//	//		//add all unchecked colors to the totalPalletteColors
//	//		for (int i = 0; i < pallette.currentPallette.length; i++) {
//	//			if (pallette.currentPallette[i][pallette.arrayIndex16_IsChecked] == 0) {
//	//				pallette.totalPalletteColors++;
//	//			}
//	//		}
//		}
//
//		this.updatePixels();
	}

}
