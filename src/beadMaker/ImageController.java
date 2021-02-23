package beadMaker;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;

import processing.core.PImage;
import core.ColorHelper;
import core.ConsoleHelper;
import core.ImageHelper;
import core.InterObjectCommunicatorEventListener;
import core.MathHelper;
import core.ProcessingHelper;

public class ImageController implements InterObjectCommunicatorEventListener {

	/*
	Here's what we need:
	-----------------------------------------
	1. cleaned input image (original full image with pure black replaced)
	2. an original scale image with color corrections and bead mappings applied  (derived from #1)
	3. a scaled-up image with color corrections and bead mappings applied (derived from #2)
	4. an original scale image with single-color highlighting applied (derived from #2)
	5. a scaled-up image with single-color highlighting applied (derived from #4)
	6. AND THEN, single-tile crops of #2, #2 scaled up, and #4 

	FULLRENDER_FULLCOLOR:
		originalCleanedImage,
			(NEW) originalCleanedImage_Scaled,
				colorCorrectedBeadMappedImage,
					colorCorrectedBeadMappedImage_Scaled

	FULLRENDER_SINGLECOLOR:
		originalCleanedImage,
			(NEW) originalCleanedImage_Scaled,
				colorCorrectedBeadMappedImage,
					colorCorrectedBeadMappedImage_Scaled, //not actually used, but may be useful for other stuff
					colorCorrectedBeadMappedImage_SingleColor,
						colorCorrectedBeadMappedImage_SingleColor_Scaled	

	SINGLETILE_FULLCOLOR:
		originalCleanedImage,
			(NEW) originalCleanedImage_Scaled,
				colorCorrectedBeadMappedImage,
					colorCorrectedBeadMappedImage_Scaled //not actually used, but may be useful for other stuff
					singleTile_ColorCorrectedBeadMappedImage
						singleTile_ColorCorrectedBeadMappedImage_Scaled

	SINGLETILE_SINGLECOLOR:
		originalCleanedImage,
			(NEW) originalCleanedImage_Scaled,
				colorCorrectedBeadMappedImage,
					colorCorrectedBeadMappedImage_Scaled, //not actually used, but may be useful for other stuff
					colorCorrectedBeadMappedImage_SingleColor,
						singleTile_ColorCorrectedBeadMappedImage_SingleColor,
							singleTile_ColorCorrectedBeadMappedImage_SingleColor_Scaled

	 */
	
	//For InterObjectCommunicator identification
	private String objectName = "IMAGE_CONTROLLER";

	public BMImage
	originalCleanedImage,										//#1
	originalCleanedImage_Scaled,								//#1b
	colorCorrectedBeadMappedImage,								//#2, derived from #1b
	colorCorrectedBeadMappedImage_Scaled,						//#3, derived from #2

	colorCorrectedBeadMappedImage_SingleColor,					//#4, derived from #2
	colorCorrectedBeadMappedImage_SingleColor_Scaled,			//#5, derived from #4

	singleTile_ColorCorrectedBeadMappedImage,					//#6, derived from #2
	singleTile_ColorCorrectedBeadMappedImage_Scaled,			//#7, derived from #6

	singleTile_ColorCorrectedBeadMappedImage_SingleColor,		//#8, derived from #4
	singleTile_ColorCorrectedBeadMappedImage_SingleColor_Scaled;//#9, derived from #8


	//public BMImage previewImage;							//static final render showing original source image colors
	
	public BMImage lutImage;
	
	public BMImage colorMap;
	public BMImage colorMappedImage;
	
	public Color backgroundColor = new Color(0,0,0);

	//these numbers reflect the algorithmically adjusted values used for color weighting calculations
	public float
	colorMatchingWeight_R = 1.0f,
	colorMatchingWeight_G = 1.0f,
	colorMatchingWeight_B = 1.0f,
	colorMatchingWeight_Brightness = 0.0f,
	colorMatchingWeight_Contrast = 1.0f,
	colorMatchingWeight_Saturation = 0.0f,
	colorMatchingWeight_DitherLevel = 0.0f,
	colorMatchingWeight_ImageScale = 1.0f,
	colorMatchingWeight_Sharpness = 0.0f;

	public InterObjectCommunicator oComm;
	public Palette pallette;
	WindowController windowController;
	public RenderLabel renderLabel;
	//RenderLabel previewLabel = new RenderLabel();
	//RenderJPanel renderJPanel = new RenderJPanel();
	BMScrollPane renderScrollPanel;
	
	PImage pixelsContainer;
	
	public boolean splitSuperPegboard = true;

	//public boolean showPixelsAsBeads = false;
	
	ConsoleHelper consoleHelper = new ConsoleHelper();
	ImageHelper imageHelper = new ImageHelper();

	public int zoomFactor = 500; //this gets overridden by the config file immediately

	public static enum PegboardMode {
		PERLER,
		PERLER_SUPERPEGBOARD_PORTRAIT,
		PERLER_SUPERPEGBOARD_LANDSCAPE,
		PERLERMINI,
		CUSTOM_40x40,
		CUSTOM_41x49,
		CUSTOM_40x48,
		LEGO_8x8,
		PERLERMINI_FORPDFPRINTING;
	}
	public PegboardMode pegboardMode;


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

	public int selectedColorIndex = -1;
	
	public boolean flipImage = false;

	public BeadMaker beadMaker;

	//--------------------------------------------------------------
	// CONSTRUCTOR
	//--------------------------------------------------------------
	ImageController(BeadMaker myBeadMaker, Palette myPallette, WindowController myWindowController, InterObjectCommunicator myOComm) {
		oComm = myOComm;
		oComm.setInterObjectCommunicatorEventListener(this);
		this.pallette = myPallette;
		this.windowController = myWindowController;
		this.beadMaker = myBeadMaker;
		
		renderLabel = new RenderLabel(this, beadMaker.controlPanel, beadMaker.oComm);
		renderScrollPanel = new BMScrollPane(beadMaker, renderLabel);
		
		String imagePath = beadMaker.xmlWorker.GetAbsoluteFilePathStringFromXml("imageFile", beadMaker.xmlWorker.projectXML);
		consoleHelper.PrintMessage("imagePath from project XML = " + imagePath);

		if (GlobalConstants.applyLUT == 1) {
			loadLUT(System.getProperty("user.dir") + "\\LUTs\\" + "default.png");
		}		
		
		setOriginalCleanedImage(imagePath);
		
		if (GlobalConstants.pixelArtMultiPaletteMode == 1) {
			loadColorMap(System.getProperty("user.dir") + "\\ColorMaps\\" + "default.png");
		}
		
		oComm.communicate("create buttons", "PALETTE");
		//pallette.CreateButtons("ImageController constructor");		
	}


	public void setOriginalCleanedImage(String imagePath) {
		//originalCleanedImage = (BMImage)ProcessingHelper.loadImage(imagePath);
		//originalCleanedImage = new BMImage((Image)ProcessingHelper.loadImage(imagePath).getNative());
		originalCleanedImage = new BMImage(imageHelper.getBufferedImageWithAlphaChannelFromURL(imagePath));
		updateImages();
	}
	
	
	public void loadLUT(String imagePath) {
		lutImage = new BMImage(imageHelper.getBufferedImageWithAlphaChannelFromURL(imagePath));
	}
	
	public void loadColorMap(String imagePath) {
		consoleHelper.PrintMessage("loadColorMap");
		
		BMImage colorMappedSprite;
		
		colorMap = new BMImage(imageHelper.getBufferedImageWithAlphaChannelFromURL(imagePath));
		
		consoleHelper.PrintMessage("colorMap.width = " + colorMap.width);

		colorMappedImage = new BMImage(
			originalCleanedImage.width * colorMap.width,
			originalCleanedImage.height
			);
		
		//colorMappedImage = originalCleanedImage.get();
		
		consoleHelper.PrintMessage("originalCleanedImage.width = " + originalCleanedImage.width);
		consoleHelper.PrintMessage("originalCleanedImage.height = " + originalCleanedImage.height);
		
		consoleHelper.PrintMessage("colorMappedImage.width = " + colorMappedImage.width);
		
		for(int i = 0; i < colorMap.width; i++) {
			colorMappedSprite = originalCleanedImage.get();
			for (int y = 0; y < colorMappedSprite.height; y++) {
				for (int x = 0; x < colorMappedSprite.width; x++) {
					for(int j = 0; j < colorMap.height; j++) {
						if (colorMappedSprite.pixels[y * colorMappedSprite.width + x] == colorMap.pixels[j * colorMap.width]) {
							colorMappedSprite.pixels[y * colorMappedSprite.width + x] = colorMap.pixels[j * colorMap.width + i];
						}
					}
				}
			}
			colorMappedImage = (BMImage)imageHelper.ReplacePortionOfImage(colorMappedImage, colorMappedSprite, i * colorMappedSprite.width, 0);
		}
		//colorMappedImage = (BMImage)ImageHelper.ReplacePortionOfImage(colorMappedImage, originalCleanedImage, 0, 0);
		
		originalCleanedImage = colorMappedImage.get();
		
		consoleHelper.PrintMessage("originalCleanedImage.width = " + originalCleanedImage.width);
		
		updateImages();
	}


	//---------------------------------------------------------------------------
	//setPegboardMode
	//---------------------------------------------------------------------------
	public void setPegboardMode(PegboardMode myPegboardMode) {
		switch (myPegboardMode) {
		case PERLER:
			renderLabel.pegboardPegsWide = 29;
			renderLabel.pegboardPegsHigh = 29;
			break;
		case PERLER_SUPERPEGBOARD_PORTRAIT:
			renderLabel.pegboardPegsWide = 49;
			renderLabel.pegboardPegsHigh = 69;
			break;
		case PERLER_SUPERPEGBOARD_LANDSCAPE:
			renderLabel.pegboardPegsWide = 69;
			renderLabel.pegboardPegsHigh = 49;
			break;
		case PERLERMINI:
			renderLabel.pegboardPegsWide = 28;
			renderLabel.pegboardPegsHigh = 28;
			break;
		case CUSTOM_40x40:
			renderLabel.pegboardPegsWide = 40;
			renderLabel.pegboardPegsHigh = 40;
			break;
		case CUSTOM_41x49:
			renderLabel.pegboardPegsWide = 41;
			renderLabel.pegboardPegsHigh = 49;
			break;
		case CUSTOM_40x48:
			renderLabel.pegboardPegsWide = 40;
			renderLabel.pegboardPegsHigh = 48;
			break;
		case LEGO_8x8:
			renderLabel.pegboardPegsWide = 8;
			renderLabel.pegboardPegsHigh = 8;
			break;
		case PERLERMINI_FORPDFPRINTING:
			renderLabel.pegboardPegsWide = 56;
			renderLabel.pegboardPegsHigh = 84;
			break;
		}
		pegboardMode = myPegboardMode;
		updateImages();
	}


	//---------------------------------------------------------------------------
	//setDitherMethod
	//---------------------------------------------------------------------------
	public void setDitherMethod(DitherMethod myDitherMethod) {
		ditherMethod = myDitherMethod;
		updateImages();
	}
	
	
	//---------------------------------------------------------------------------
	//flipImage
	//---------------------------------------------------------------------------
	public void flipImage(boolean state) {
		this.flipImage = state;
		updateImages();
	}


	//---------------------------------------------------------------------------
	//updateImages
	//---------------------------------------------------------------------------
	public void updateImages() {
		consoleHelper.PrintMessage("updateImages");
		
		//previewImage = originalCleanedImage.get();
		colorCorrectedBeadMappedImage = originalCleanedImage.get();
		
		consoleHelper.PrintMessage("colorCorrectedBeadMappedImage.width = " + colorCorrectedBeadMappedImage.width);
		
		if (flipImage) {
			colorCorrectedBeadMappedImage.FlipHorizontally();
		}
		colorCorrectedBeadMappedImage.ditherMethod = BMImage.DitherMethod.values()[this.ditherMethod.ordinal()];
		colorCorrectedBeadMappedImage.resize((int)Math.floor(originalCleanedImage.width * colorMatchingWeight_ImageScale), 0);
		//colorCorrectedBeadMappedImage.Sharpen(this.colorMatchingWeight_Sharpness);
		if (GlobalConstants.pixelArtMultiPaletteMode == 0) {
			colorCorrectedBeadMappedImage.SwapToPerlerColors(
				pallette,
				colorMatchingWeight_DitherLevel,
				colorMatchingWeight_R,
				colorMatchingWeight_G,
				colorMatchingWeight_B,
				colorMatchingWeight_Saturation,
				colorMatchingWeight_Contrast,
				colorMatchingWeight_Brightness,
				colorMatchingWeight_Sharpness,
				lutImage
				);
		}

		ImageIcon perlerSwappedIcon;

		if (selectedColorIndex == -1) {
			perlerSwappedIcon =  new ImageIcon((BufferedImage)colorCorrectedBeadMappedImage.getNative());
			//renderLabel needs a copy of the image for its PaintComponent method (when renderPixelsAsBeads is turned on)
			renderLabel.renderImage = colorCorrectedBeadMappedImage.get();
		} else {
			colorCorrectedBeadMappedImage_SingleColor = colorCorrectedBeadMappedImage.HighlightSelectedColor(colorCorrectedBeadMappedImage, pallette, selectedColorIndex);
			perlerSwappedIcon =  new ImageIcon((BufferedImage)colorCorrectedBeadMappedImage_SingleColor.getNative());
			//renderLabel needs a copy of the image for its PaintComponent method (when renderPixelsAsBeads is turned on)
			renderLabel.renderImage = colorCorrectedBeadMappedImage_SingleColor.get();
		}

		ImageIcon zoomedIcon = new ImageIcon(perlerSwappedIcon.getImage().getScaledInstance(perlerSwappedIcon.getIconWidth() * zoomFactor / 100, perlerSwappedIcon.getIconHeight() * zoomFactor / 100, Image.SCALE_REPLICATE));

		renderLabel.gridColumns = colorCorrectedBeadMappedImage.width;
		renderLabel.imageWidth = zoomedIcon.getIconWidth();
		renderLabel.gridRows = colorCorrectedBeadMappedImage.height;
		renderLabel.imageHeight = zoomedIcon.getIconHeight();

		renderLabel.setIcon(zoomedIcon);
	}


	//---------------------------------------------------------------------------
	//GetImageScale
	//---------------------------------------------------------------------------
	public int GetImageScale(BMImage myBMImage, JPanel jPanel) {
		consoleHelper.PrintMessage("GetImageScale");

		int imageScaleX = 0, imageScaleY = 0;
		int windowWidth, windowHeight;

		Rectangle r = jPanel.getBounds();
		CompoundBorder compoundBorder = (CompoundBorder) jPanel.getBorder();
		Insets outerInsets = compoundBorder.getOutsideBorder().getBorderInsets(jPanel);
		//Insets innerInsets = compoundBorder.getInsideBorder().getBorderInsets(jPanel);
		int borderThickness = outerInsets.top;

		consoleHelper.PrintMessage("borderThickness = " + Integer.toString(borderThickness));

		windowWidth = r.width - borderThickness * 2;
		windowHeight = r.height - borderThickness * 2;

		consoleHelper.PrintMessage("window height = " + Integer.toString(r.height));
		consoleHelper.PrintMessage("window width = " + Integer.toString(r.width));

		imageScaleX = (int) Math.floor(windowWidth * 1.0f / myBMImage.width);
		imageScaleY = (int) Math.floor(windowHeight * 1.0f / myBMImage.height);


		return MathHelper.SetNonZeroValue(imageScaleY < imageScaleX ? imageScaleY : imageScaleX);
	}


//	//---------------------------------------------------------------------------
//	// resizeImage
//	//---------------------------------------------------------------------------
//	synchronized BMImage resizeImage(BMImage image, int scale) {
//		ConsoleHelper.PrintMessage("resizeImage");
//
//		BMImage localImage = image.get();
//		BMImage localCopyImage = image.get();
//
//		int imageHeight = localImage.height;
//		int imageWidth = localImage.width;
//
//		localImage.loadPixels();
//
//		ConsoleHelper.PrintMessage("image dimensions: " + imageWidth + "px W x " + imageHeight + "px H");
//
//		ConsoleHelper.PrintMessage("scale: " + scale);
//
//		if (scale < 1) {
//			return localImage;
//		}
//
//		//clear out pixels
//		for (int i = 0; i < imageHeight * imageWidth; i++) {
//			localImage.pixels[i] = ColorHelper.singleValueToGrayscaleProcessingColor(255);
//		}
//
//		localImage.updatePixels();
//
//		//using the built-in resize function alone results in a blurred result, so we do a custom resize below.
//		localImage.resize(0, imageHeight * scale);
//
//		localImage.loadPixels();
//
//		//loop through each pixel in the original image
//		for (int i = 0; i < imageHeight * imageWidth; i++) {
//			//loop for pixel columns
//			for (int j = 0; j < scale; j++) {
//				//loop for pixel rows
//				for (int k = 0; k < scale; k++) {
//					//I don't understand what this line is doing, but I think it's beautiful. Somehow this is upscaling the image.
//					localImage.pixels[j + k * imageWidth * scale + i * scale * scale - i % imageWidth * scale * (scale - 1)] = localCopyImage.pixels[i];
//				}
//			}
//		}
//		localImage.updatePixels();
//		return localImage;
//	}
	
	
	//-----------------------------------------------------------------
	// resizeImageforPDFOutput
	//---------------------------------------------------------------------------
	synchronized public BMImage[] resizeImageforPDFOutput(BMImage image, ImageController.PegboardMode myPegboardMode, boolean mySplitSuperPegboard) {
		consoleHelper.PrintMessage("resizeImageforPDFOutput");

		BMImage[] localImage = new BMImage[2];
		BMImage[] outputImage = new BMImage[2];

		localImage[0] = localImage[1] = image.get();

		if(!mySplitSuperPegboard) {
			switch (myPegboardMode) {
			case PERLER_SUPERPEGBOARD_PORTRAIT:
				myPegboardMode = ImageController.PegboardMode.PERLER;
				break;
			case PERLER_SUPERPEGBOARD_LANDSCAPE:
				myPegboardMode = ImageController.PegboardMode.PERLER;
				break;
			default:
				break;
			}
		}

		switch (myPegboardMode) {

			case PERLER:
			case CUSTOM_40x40:
			case CUSTOM_40x48:
			case CUSTOM_41x49:
			case LEGO_8x8:
				//localImage[0] = resizeImage(localImage[0], localImage[0].upscalerForPDFPrinting);
				pixelsContainer = imageHelper.resizeImage(localImage[0], BMImage.upscalerForPDFPrinting);
				localImage[0].setPixels(pixelsContainer.pixels, pixelsContainer.width, pixelsContainer.height);
				localImage[0].resize(0, (int)Math.ceil(localImage[0].height * BMImage.scaleForPDFPrinting_Perler / (float)BMImage.upscalerForPDFPrinting));
				outputImage[0] = localImage[0];
				outputImage[1] = null;
				break;
	
			case PERLER_SUPERPEGBOARD_PORTRAIT:
				//get the top half of the image
				//localImage[0] = resizeImage(localImage[0], localImage[0].upscalerForPDFPrinting);
				pixelsContainer = imageHelper.resizeImage(localImage[0], BMImage.upscalerForPDFPrinting);
				localImage[0].setPixels(pixelsContainer.pixels, pixelsContainer.width, pixelsContainer.height);
				outputImage[0] = localImage[0].get(0, 0, localImage[0].width, (int)Math.floor(localImage[0].height / 2.0f));
				outputImage[0].resize((int)Math.ceil((float)localImage[0].width * BMImage.scaleForPDFPrinting_Perler / BMImage.upscalerForPDFPrinting), 0);
				//get the bottom half of the image
				//localImage[0] = resizeImage(localImage[0], localImage[0].upscalerForPDFPrinting);
				pixelsContainer = imageHelper.resizeImage(localImage[1], BMImage.upscalerForPDFPrinting);
				localImage[1].setPixels(pixelsContainer.pixels, pixelsContainer.width, pixelsContainer.height);
				outputImage[1] = localImage[1].get(0, (int)Math.floor(localImage[1].height / 2.0f), localImage[1].width, (int)Math.ceil(localImage[1].height / 2.0f));
				outputImage[1].resize((int)Math.ceil((float)localImage[1].width * BMImage.scaleForPDFPrinting_Perler / BMImage.upscalerForPDFPrinting), 0);
				break;
	
			case PERLER_SUPERPEGBOARD_LANDSCAPE:
				//get the left half of the image
				//localImage[0] = resizeImage(localImage[0], localImage[0].upscalerForPDFPrinting);
				pixelsContainer = imageHelper.resizeImage(localImage[0], BMImage.upscalerForPDFPrinting);
				localImage[0].setPixels(pixelsContainer.pixels, pixelsContainer.width, pixelsContainer.height);
				outputImage[0] = localImage[0].get(0, 0, (int)Math.floor(localImage[0].width / 2.0f), localImage[0].height);
				outputImage[0].resize(0, (int)Math.ceil((float)localImage[0].height * BMImage.scaleForPDFPrinting_Perler / BMImage.upscalerForPDFPrinting));
				//get the right half of the image
				//localImage[1] = resizeImage(localImage[1], localImage[0].upscalerForPDFPrinting);
				pixelsContainer = imageHelper.resizeImage(localImage[1], BMImage.upscalerForPDFPrinting);
				localImage[1].setPixels(pixelsContainer.pixels, pixelsContainer.width, pixelsContainer.height);
				outputImage[1] = localImage[1].get((int)Math.floor(localImage[1].width / 2.0f), 0, (int)Math.floor(localImage[1].width / 2.0f), localImage[1].height);
				outputImage[1].resize(0, (int)Math.ceil((float)localImage[1].height * BMImage.scaleForPDFPrinting_Perler / BMImage.upscalerForPDFPrinting));
				break;
	
			case PERLERMINI:
			case PERLERMINI_FORPDFPRINTING:
				//localImage[0] = resizeImage(localImage[0], localImage[0].upscalerForPDFPrinting);
				pixelsContainer = imageHelper.resizeImage(localImage[0], BMImage.upscalerForPDFPrinting);
				localImage[0].setPixels(pixelsContainer.pixels, pixelsContainer.width, pixelsContainer.height);
				localImage[0].resize(0, (int)Math.ceil((float)localImage[0].height * BMImage.scaleForPDFPrinting_PerlerMini / BMImage.upscalerForPDFPrinting));
				outputImage[0] = localImage[0];
				outputImage[1] = null;
				break;
		}
		return outputImage;
	}


	//---------------------------------------------------------------------------
	//setRenderPixelsAsBeads
	//---------------------------------------------------------------------------
	public void setRenderPixelsAsBeads(boolean renderPixelsAsBeads) {
		renderLabel.renderPixelsAsBeads = renderPixelsAsBeads;

		updateImages();
	}
	
	
	//---------------------------------------------------------------------------
	//showAllColors
	//---------------------------------------------------------------------------
	public void showAllColors() {
		
		selectedColorIndex = -1;
		updateImages();
	}


	//---------------------------------------------------------------------------
	//setColorMatchingWeight
	//---------------------------------------------------------------------------
	public void setColorMatchingWeight(String name, int value) {
		switch (name) {
		case "Red":
			this.colorMatchingWeight_R = (value + 50f) / 50.0f;
			break;
		case "Green":
			this.colorMatchingWeight_G = (value + 50f) / 50.0f;
			break;
		case "Blue":
			this.colorMatchingWeight_B = (value + 50f) / 50.0f;
			break;
		case "Brightness":
			this.colorMatchingWeight_Brightness = value * 5f;
			break;
		case "Contrast":
			this.colorMatchingWeight_Contrast = (float)((Math.pow(4.0, Math.pow(4.0, (value + 50f) / 100f)) - 1.0f) / 15f);
			consoleHelper.PrintMessage("colorMatchingWeight_Contrast = " + colorMatchingWeight_Contrast);
			break;
		case "Saturation":
			this.colorMatchingWeight_Saturation = value / 50.0f;
			break;
		case "Dither":
			this.colorMatchingWeight_DitherLevel = value / 100.0f;
			break;
		case "Sharpness":
			this.colorMatchingWeight_Sharpness = value / 100.0f;
			consoleHelper.PrintMessage("colorMatchingWeight_Sharpness = " + Float.toString(colorMatchingWeight_Sharpness));
			break;
		case "Scale %":
			this.colorMatchingWeight_ImageScale = value / 100.0f;
			if (colorMatchingWeight_ImageScale <= 1.03f && colorMatchingWeight_ImageScale >= 0.97f) {
				colorMatchingWeight_ImageScale = 1.0f;
			}
			break;
		case "Zoom %":
			this.zoomFactor = value;
			renderLabel.pegboardSeparatorThickness = MathHelper.ensureRange(value / 300, 2, 10);
			consoleHelper.PrintMessage("zoomFactor = " + zoomFactor);
			break;
		}			

		updateImages();
	}


	@Override
	public void onInterObjectCommunicator_CommunicateEvent(Object o) {
		if (o instanceof String) {
			String s = ((String) o);
	        if (s.equals("show all colors")) {
	        	showAllColors();
	        }
	        if (s.equals("update images")) {
	        	consoleHelper.PrintMessage("updateImages()");
	        	updateImages();
	        }
	        if (s.equals("save PNG")) {
	        	colorCorrectedBeadMappedImage.SavePNG();
	        }
	        if (s.equals("save SCAD")) {
	        	colorCorrectedBeadMappedImage.SaveSCAD();
	        }
		}
		if (o instanceof Integer) {
			int colorIndex = ((int) o);
	        selectedColorIndex = colorIndex;
	    	updateImages();
		}
		if (o instanceof KeyEvent) {
			KeyEvent e = ((KeyEvent) o);
			if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
				consoleHelper.PrintMessage("fired a keyPressed event from oCommInterface in ImageController" + e.getKeyCode());
				showAllColors();
	        }			
		}
	}
	
	
	@Override
	public void onInterObjectCommunicator_CommunicateEvent(String descriptor, Object o) {
		if (descriptor.equals("set image file")) {
			if (o instanceof String) {
				setOriginalCleanedImage((String) o);
				if (GlobalConstants.pixelArtMultiPaletteMode == 1) {
					loadColorMap(System.getProperty("user.dir") + "\\ColorMaps\\" + "default.png");
				}
			}
	    }
		if (descriptor.equals("render pixels as beads")) {
			if (o instanceof Boolean) {
				setRenderPixelsAsBeads((boolean) o);
			}
	    }
		if (descriptor.equals("flip image")) {
			if (o instanceof Boolean) {
				flipImage((boolean) o);
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
