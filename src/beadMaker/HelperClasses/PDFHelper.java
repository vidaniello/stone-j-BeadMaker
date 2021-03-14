package beadMaker.HelperClasses;

import java.awt.Cursor;
import java.awt.Font;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;

import processing.core.PFont;
import processing.core.PGraphics;
import processing.pdf.PGraphicsPDF;
import beadMaker.BMImage;
import beadMaker.ImageController;
import beadMaker.Palette;
import beadMaker.WindowController;

import core.ConsoleHelper;
import core.DialogBoxHelper;
import core.FileHelper;
import core.ProcessingHelper;
import core.SynchronousJFXFileChooser;

public class PDFHelper {
	
	ConsoleHelper consoleHelper;
	
	public volatile BMImage[][][] SavePatternPDF__pdfImage = null;
	public volatile BMImage[][] SavePatternPDF__localImage = null;
	public volatile String SavePatternPDF__myPDFFile;
	public volatile String SavePNG__myPNGFile;
	
	public List<BMImage[][]> SavePatternPDF__pdfImageList;
	
	public WindowController windowController;
	public ImageController imageController;
	public Palette pallette;
	
	public boolean useAppData;
	public String appDataFolderName;
	
	static final int SavePatternPDF_textArea_Height = 50;
	static final int CENTER   = 3;
	static final int TOP = 101;

	public PDFHelper(
		WindowController myWindowController,
		ImageController myImageController,
		boolean myUseAppData,
		String myAppDataFolderName
	) {
		consoleHelper = new ConsoleHelper();
		this.useAppData = myUseAppData;
		this.appDataFolderName = myAppDataFolderName;
		windowController = myWindowController;
		imageController = myImageController;
		pallette = imageController.pallette;		
	}

	//---------------------------------------------------------------------------
	// SavePatternPDF
	//DONE: The built-in resize method blurs the resulting image-- but since we need a very specific scale, try using our custom resize method to upsize, and then down-size using the built-in resize method.
	//DONE: Fix: This routine infrequently causes the output pdfs to have glitched pixels in them
	//---------------------------------------------------------------------------
	public void SavePatternPDF(boolean fullColorPDFPrinting) {

		DialogBoxHelper dialogBoxHelper = new DialogBoxHelper();
		FileHelper fileHelper = new FileHelper(useAppData, appDataFolderName);
		ProcessingHelper processingHelper = new ProcessingHelper();
		
		int pdfWidth = 1;
		int	pdfHeight = 1;
		
		consoleHelper.PrintMessage("SavePatternPDF");

		if(
			imageController.pegboardMode == ImageController.PegboardMode.PERLER_SUPERPEGBOARD_PORTRAIT ||
			imageController.pegboardMode == ImageController.PegboardMode.PERLER_SUPERPEGBOARD_LANDSCAPE
		) {
			imageController.splitSuperPegboard = 
				dialogBoxHelper.YesNoDialog (
					"Do you want to split this pegboard into two pages so it fits on 8.5 x 11 paper?",
					"Question"
				);
		} else {
			imageController.splitSuperPegboard = false;
		}

		if(imageController.pegboardMode == ImageController.PegboardMode.PERLERMINI) {
			boolean myResponse = 
					dialogBoxHelper.YesNoDialog (
					"Would you like a 2x3 grid of mini pegboards on each page?\n(The alternative is 1 mini pegboard per page.)",
					"Question"
				);

			if (myResponse) {
				imageController.setPegboardMode(ImageController.PegboardMode.PERLERMINI_FORPDFPRINTING);
			}
		}
		
		File dataDir;
//		if (useAppData) {
//			dataDir = new File(System.getenv("APPDATA") + File.separator + appDataFolderName, File.separator);
//		} else {
//			dataDir = new File(System.getProperty("user.dir"), File.separator);
//		}
		
		//returns "my documents" directory
		//https://stackoverflow.com/questions/9677692/getting-my-documents-path-in-java
		dataDir = new JFileChooser().getFileSystemView().getDefaultDirectory();
		
		File selectedFile;
		
		//File initialDirectory, String extensionFilterDescription, String[] extensionFilterFiletypes
        SynchronousJFXFileChooser chooser = new SynchronousJFXFileChooser (
        	dataDir,
        	"Portable Document Format (*.pdf)",
        	new String[] {"*.pdf"}
        );
        selectedFile = chooser.showSaveDialog();
        
        if (selectedFile != null) {
        	SavePatternPDF__myPDFFile = selectedFile.toString(); 
        	
        	if (!fileHelper.getExtension(SavePatternPDF__myPDFFile).equals("pdf")) {
				SavePatternPDF__myPDFFile += ".pdf";
			}	
        	
        	consoleHelper.PrintMessage("PDF Filename = ");
			consoleHelper.PrintMessage(SavePatternPDF__myPDFFile);

			consoleHelper.PrintMessage("Saving PDF Pattern");

			// turn on the hourglass while stuff is running
			windowController.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			// FULLRENDER_FULLCOLOR:
			consoleHelper.PrintMessage("Running PDF render for FULLRENDER_FULLCOLOR");
			consoleHelper.PrintMessage("Number of colors: " + pallette.currentPallette.length);
			consoleHelper.PrintMessage("Number of tiles: " + imageController.originalCleanedImage.GetTileCountForImage(imageController.originalCleanedImage, imageController.renderLabel));
			consoleHelper.PrintMessage("Number of tiles: " + imageController.originalCleanedImage.GetTileCountForImage(imageController.originalCleanedImage, imageController.renderLabel));
			
			//if "color printing" is selected, do this stuff
			if(fullColorPDFPrinting) {
				SavePatternPDF__pdfImage 	= new BMImage	[1]									[imageController.colorCorrectedBeadMappedImage.GetTileCountForImage(imageController.colorCorrectedBeadMappedImage, imageController.renderLabel)][2]; // n
				SavePatternPDF__localImage 	= new BMImage	[1]									[imageController.colorCorrectedBeadMappedImage.GetTileCountForImage(imageController.colorCorrectedBeadMappedImage, imageController.renderLabel)];
			} else {
				SavePatternPDF__pdfImage 	= new BMImage	[pallette.currentPallette.length]	[imageController.colorCorrectedBeadMappedImage.GetTileCountForImage(imageController.colorCorrectedBeadMappedImage, imageController.renderLabel)][2]; // n
				SavePatternPDF__localImage 	= new BMImage	[pallette.currentPallette.length]	[imageController.colorCorrectedBeadMappedImage.GetTileCountForImage(imageController.colorCorrectedBeadMappedImage, imageController.renderLabel)];
			}

			//if "color printing" is selected, do this stuff
			if(fullColorPDFPrinting) {
				SavePatternPDF__localImage[0][0] = imageController.colorCorrectedBeadMappedImage.get();
				SavePatternPDF__localImage[0] = BMImage.SplitImageIntoTiles(SavePatternPDF__localImage[0][0], imageController.renderLabel);
			} else {
				// need to iterate over currentPallette and fire once for every pallette
				// index (array position 4)
				for (int h = 0; h < pallette.currentPallette.length; h++) {
	
					if (pallette.currentPallette[h][pallette.arrayIndex05_PixelCount] > 0 && pallette.currentPallette[h][pallette.arrayIndex16_IsChecked] == 1) {
						SavePatternPDF__localImage[h][0] = imageController.colorCorrectedBeadMappedImage.get();
						//SavePatternPDF__localImage[h][0].save("F:\\My Documents\\Dropbox\\Workspace\\BeadMaker2018\\poop\\SavePatternPDF__localImage_" + h + ".png");			
						SavePatternPDF__localImage[h] = BMImage.SplitImageIntoTiles(SavePatternPDF__localImage[h][0], imageController.renderLabel);
						for (int m = 0; m < SavePatternPDF__localImage[h].length; m++) {
							//have to use this temp image so that the BMImage.totalBeadsHightlighted doesn't get defaulted to 0.
							//tempLocalImage = SavePatternPDF__localImage[h][m].HighlightSelectedColor(pallette, h).get();
							//SavePatternPDF__beadCount[h][m] = SavePatternPDF__localImage[h][m].totalBeadsHightlighted;
							//BMImage.HighlightSelectedColor(SavePatternPDF__localImage[h][m], pallette, pallette.currentPallette[h][pallette.arrayIndex04_ColorIndex]).save("F:\\My Documents\\Dropbox\\Workspace\\BeadMaker2018\\poop\\SavePatternPDF__localImage_" + h + "_" + m + ".png");
							SavePatternPDF__localImage[h][m] = SavePatternPDF__localImage[h][m].HighlightSelectedColor(SavePatternPDF__localImage[h][m], pallette, pallette.currentPallette[h][pallette.arrayIndex04_ColorIndex]);
							//SavePatternPDF__localImage[h][m].save("F:\\My Documents\\Dropbox\\Workspace\\BeadMaker2018\\poop\\SavePatternPDF__localImage_" + h + "_" + m + ".png");
							consoleHelper.PrintMessage("Bead count = " + SavePatternPDF__localImage[h][m].totalBeadsHightlighted);
						}
					}
				}
			}
			
			//if "color printing" is selected, do this stuff
			if(fullColorPDFPrinting) {
				for (int i = 0; i < SavePatternPDF__localImage[0].length; i++) {
					
					BMImage[] localLoopBMImage = new BMImage[2];

					localLoopBMImage = imageController.resizeImageforPDFOutput(
						SavePatternPDF__localImage[0][i],
						imageController.pegboardMode,
						imageController.splitSuperPegboard);

					SavePatternPDF__pdfImage[0][i][0] = localLoopBMImage[0];
					if(localLoopBMImage[1] != null) {
						SavePatternPDF__pdfImage[0][i][1] = localLoopBMImage[1];
					}
				}
				
				pdfWidth = SavePatternPDF__pdfImage[0][0][0].width;
				pdfHeight = SavePatternPDF__pdfImage[0][0][0].height;

			} else {
				for (int h = 0; h < pallette.currentPallette.length; h++) {
					
					if (pallette.currentPallette[h][pallette.arrayIndex05_PixelCount] > 0 && pallette.currentPallette[h][pallette.arrayIndex16_IsChecked] == 1) {
						
						for (int i = 0; i < SavePatternPDF__localImage[h].length; i++) {
	
							if (SavePatternPDF__localImage[h][i].totalBeadsHightlighted > 0) {
								
								BMImage[] localLoopBMImage = new BMImage[2];
		
								localLoopBMImage = imageController.resizeImageforPDFOutput(
									SavePatternPDF__localImage[h][i],
									imageController.pegboardMode,
									imageController.splitSuperPegboard);
			
								SavePatternPDF__pdfImage[h][i][0] = localLoopBMImage[0];
								if(localLoopBMImage[1] != null) {
									SavePatternPDF__pdfImage[h][i][1] = localLoopBMImage[1];
								}
							}
						}
					}
				}

				//need to put this loop here to avoid a null pointer exception
				// when the first pallette color is unchecked
				for (int h = 0; h < pallette.currentPallette.length; h++) {
					if(SavePatternPDF__pdfImage[h][0][0] != null) {
						pdfWidth = SavePatternPDF__pdfImage[h][0][0].width;
						pdfHeight = SavePatternPDF__pdfImage[h][0][0].height;
						break; //when a non-null image is found, exit the for loop
					}
				}
			}
			
			PGraphics pdfCanvas = processingHelper.createGraphics(
					pdfWidth + 1,
					pdfHeight + 1 + SavePatternPDF_textArea_Height,
					"processing.pdf.PGraphicsPDF",
					fileHelper.removeExtension(SavePatternPDF__myPDFFile) + ".pdf");

			pdfCanvas.beginDraw();

			PGraphicsPDF pdfg = (PGraphicsPDF) pdfCanvas;

			pdfCanvas.stroke(0); // black
			pdfCanvas.textAlign(CENTER, TOP);
			PFont font = new PFont(new Font("Arial", 0, 16), true); // Arial, 16 point, anti-aliasing on

			boolean isFirstPage = true;

			// tile iterator
			consoleHelper.PrintMessage("Attempting to iterate over " + SavePatternPDF__pdfImage[0].length + " tiles.");
			for (int j = 0; j < SavePatternPDF__pdfImage[0].length; j++) {

				//if "color printing" is selected, do this stuff
				if(fullColorPDFPrinting) {
					
					if (isFirstPage == false)
						pdfg.nextPage();
					
					isFirstPage = false;

					pdfCanvas.image(SavePatternPDF__pdfImage[0][j][0], 0, 0);
					
					//SavePatternPDF__pdfImage[k][j][0].save("F:\\My Documents\\Dropbox\\Workspace\\BeadMaker2018\\poop\\SavePatternPDF__localImage_" + j + "_" + k + ".png");

					//ConsoleHelper.PrintMessage("Attempting DrawPDFTileBorder");
					this.DrawPDFTileBorder(
							pdfCanvas,
							pdfHeight + 1,
							pdfWidth + 1);

					this.DrawPDFFooter(
						pdfCanvas,
						font,
						"Tile " + Integer.toString(j + 1),
						pdfHeight,
						pdfWidth);

					if (SavePatternPDF__pdfImage[0][j][1] != null) {
						pdfg.nextPage();

						pdfCanvas.image(SavePatternPDF__pdfImage[0][j][1], 0, 0);

						DrawPDFTileBorder(
							pdfCanvas,
							pdfHeight + 1,
							pdfWidth + 1
						);

						DrawPDFFooter(
							pdfCanvas,
							font,
							"Tile " + Integer.toString(j + 1),
							pdfHeight,
							pdfWidth
						);
						
						if (SavePatternPDF__pdfImage[0][j][1] != null) {			
							pdfg.nextPage();
								
							pdfCanvas.image(SavePatternPDF__pdfImage[0][j][1], 0, 0);
							
							DrawPDFTileBorder (
								pdfCanvas,
								pdfHeight + 1,
								pdfWidth + 1
							);
							
							DrawPDFFooter (
								pdfCanvas,
								font,
								"Tile " + Integer.toString(j + 1),
								pdfHeight,
								pdfWidth
							);
						}
					}
				} else {
					// pallette iterator
					for (int k = 0; k < SavePatternPDF__pdfImage.length; k++) {
	
						if (pallette.currentPallette[k][pallette.arrayIndex05_PixelCount] > 0 && pallette.currentPallette[k][pallette.arrayIndex16_IsChecked] == 1) {
	
							if (SavePatternPDF__localImage[k][j].totalBeadsHightlighted > 0) {
								//ConsoleHelper.PrintMessage("SavePatternPDF__beadCount[k][j] > 0");
	
								if (isFirstPage == false)
									pdfg.nextPage();
								
								isFirstPage = false;
	
								pdfCanvas.image(SavePatternPDF__pdfImage[k][j][0], 0, 0);
								
								//SavePatternPDF__pdfImage[k][j][0].save("F:\\My Documents\\Dropbox\\Workspace\\BeadMaker2018\\poop\\SavePatternPDF__localImage_" + j + "_" + k + ".png");
	
								//ConsoleHelper.PrintMessage("Attempting DrawPDFTileBorder");
								this.DrawPDFTileBorder(
										pdfCanvas,
										pdfHeight + 1,
										pdfWidth + 1);
	
								this.DrawPDFFooter(
									pdfCanvas,
									font,
									"Tile "
										+ Integer.toString(j + 1)
										+ "\r\n"
										+ pallette.perlerColorsNames[pallette.currentPallette[k][pallette.arrayIndex04_ColorIndex]][1]
										+ " "
										+ pallette.perlerColorsNames[pallette.currentPallette[k][pallette.arrayIndex04_ColorIndex]][0]
										+ " (Beads: "
										+ Integer.toString(SavePatternPDF__localImage[k][j].totalBeadsHightlighted)
										+ ")",
									pdfHeight,
									pdfWidth);
	
								if (SavePatternPDF__pdfImage[k][j][1] != null) {
									pdfg.nextPage();
	
									pdfCanvas.image(SavePatternPDF__pdfImage[k][j][1], 0, 0);
	
									DrawPDFTileBorder(
										pdfCanvas,
										pdfHeight + 1,
										pdfWidth + 1);
	
									DrawPDFFooter(
										pdfCanvas,
										font,
										"Tile "
											+ Integer.toString(j + 1)
											+ "\r\n"
											+ pallette.perlerColorsNames[pallette.currentPallette[k][pallette.arrayIndex04_ColorIndex]][1]
											+ " "
											+ pallette.perlerColorsNames[pallette.currentPallette[k][pallette.arrayIndex04_ColorIndex]][0]
											+ " (Beads: "
											+ Integer.toString(SavePatternPDF__localImage[k][j].totalBeadsHightlighted)
											+ ")",
										pdfHeight,
										pdfWidth);
									
									if (SavePatternPDF__pdfImage[k][j][1] != null) {			
										pdfg.nextPage();
											
										pdfCanvas.image(SavePatternPDF__pdfImage[k][j][1], 0, 0);
										
										DrawPDFTileBorder (
											pdfCanvas,
											pdfHeight + 1,
											pdfWidth + 1
										);
										
										DrawPDFFooter (
											pdfCanvas,
											font,
											//"Tile " + Integer.toString(j + 1) + "\r\n" + beadColorPrefix + perlerColorsNames[currentPallette[k][arrayIndex04_ColorIndex]][0] + " (Beads: " + Integer.toString(SavePatternPDF__beadCount[k][j]) + ")",
											"Tile " + Integer.toString(j + 1) + "\r\n" + pallette.perlerColorsNames[pallette.currentPallette[k][pallette.arrayIndex04_ColorIndex]][1] + " " + pallette.perlerColorsNames[pallette.currentPallette[k][pallette.arrayIndex04_ColorIndex]][0] + " (Beads: " + Integer.toString(SavePatternPDF__localImage[k][j].totalBeadsHightlighted) + ")",
											pdfHeight,
											pdfWidth
										);
									}
								}
							}
						}
					}
				} //end "if color printing"
			}
			
			pdfCanvas.endDraw();
			pdfCanvas.dispose();
			// }
			// If the file is locked (open), the exception will throw
			// catch (RuntimeException e) {
			// PrintMessage("The PDF file is in use by another program and cannot be overwritten.");
			// WarningDialog("The PDF file is in use by another program and cannot be overwritten.");
			// System.out.println(e.getMessage());
			// }
			
			windowController.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		else {
			//imageProcessingFunction = ImageProcessingFunction.NONE;
			consoleHelper.PrintMessage("PDF file creation process failed");
			return; //bail out because the user did not select a file
		}
		return;
	}

	//---------------------------------------------------------------------------
	//DrawPDFTileBorder
	//---------------------------------------------------------------------------
	synchronized void DrawPDFTileBorder (PGraphics myCanvas, int imageHeight, int imageWidth) {
		consoleHelper.PrintMessage("DrawPDFTileBorder");
		//Draw a solid black outline around the whole tile
		myCanvas.fill(0,0,0,0); //full transparent
		myCanvas.rect (
			0,
			0,
			imageWidth,
			imageHeight		
		);
	}
	
	
	//---------------------------------------------------------------------------
	// DrawPDFFooter
	//---------------------------------------------------------------------------
	synchronized void DrawPDFFooter (PGraphics myCanvas, PFont myFont, String myText, int imageHeight, int imageWidth) {
		consoleHelper.PrintMessage("DrawPDFFooter");
		myCanvas.fill(0); //black
		myCanvas.textAlign(CENTER, CENTER);
		myCanvas.textFont(myFont);
		myCanvas.text (
			myText,
			0,
			imageHeight,
			imageWidth,
			SavePatternPDF_textArea_Height
		);
	}
}