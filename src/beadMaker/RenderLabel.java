package beadMaker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;

import javax.swing.JLabel;

import processing.core.PImage;
import core.ConsoleHelper;
import core.ColorHelper;
import core.Graphics2DMaker;
import core.InterObjectCommunicatorEventListener;
import core.MathHelper;
import core.StringHelper;

public class RenderLabel extends JLabel implements MouseMotionListener, MouseListener, InterObjectCommunicatorEventListener {

	//For InterObjectCommunicator identification
	private String objectName = "RENDER_LABEL";
			
	int yOffset = 0;

	public int gridColumns = 0;
	public int gridRows = 0;
	public int imageWidth = 0;
	public int imageHeight = 0;
	public int pegboardPegsWide = 29;
	public int pegboardPegsHigh = 29;
	public Color gridColor = new Color(1,2,3);
	
	public boolean showGrid = true;

	public PImage renderImage;
	public ImageController imageController;
	public ControlPanel controlPanel;
	public InterObjectCommunicator oComm;

	public boolean renderPixelsAsBeads = false;

	public int pegboardSeparatorThickness = 2;

	private Point clickPoint;
	private Point dragPoint;
	private Rectangle selection;
	
	private int xPos;
	private int yPos;

	private int pixelX;
	private int pixelY;


	RenderLabel(ImageController myImageController, ControlPanel myControlPanel, InterObjectCommunicator myOComm) {
		super();
		oComm = myOComm;
		oComm.setInterObjectCommunicatorEventListener(this);
		this.imageController = myImageController;
		this.controlPanel = myControlPanel;
		//this.controlPanel = myControlPanel;
		this.setVerticalAlignment(JLabel.TOP);
		setFocusable(true);
		addMouseMotionListener(this);
		addMouseListener(this);
		//addKeyListener(this);
	}
	
//	public void KeyPressed_FromDelegator(KeyEvent e) {
//        //ConsoleHelper.PrintMessage("fired a keyPressed event " + e.getKeyCode());
//        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
//        	dragPoint = null;
//    		repaint();
//        }
//    }


//	@Override
//	public void setFocusable(boolean b) {
//	    super.setFocusable(b);
//	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		xPos = e.getX();
		yPos = e.getY();

		pixelX = e.getX() * 100 / imageController.zoomFactor;
		pixelY = e.getY() * 100 / imageController.zoomFactor;

		int imageWidth = imageController.colorCorrectedBeadMappedImage.width;
		int imageHeight = imageController.colorCorrectedBeadMappedImage.height;

		if(pixelX < imageWidth && pixelY < imageHeight) {

			Color pixelColor = ColorHelper.pixeltoColor(imageController.colorCorrectedBeadMappedImage.pixels[pixelX + (pixelY * imageWidth)]);

			if (pixelColor.getAlpha() == 255) {

				int myPalletteIndex = imageController.colorCorrectedBeadMappedImage.GetPerlerPalletteIndexForSinglePixel(pixelColor, imageController.pallette);

				String
				hoverText = 		imageController.pallette.perlerColorsNames[imageController.pallette.currentPallette[myPalletteIndex][imageController.pallette.arrayIndex04_ColorIndex]][1];
				hoverText += " " + 	imageController.pallette.perlerColorsNames[imageController.pallette.currentPallette[myPalletteIndex][imageController.pallette.arrayIndex04_ColorIndex]][0];
				hoverText += " (R: " + imageController.pallette.perlerColorsRGB[imageController.pallette.currentPallette[myPalletteIndex][imageController.pallette.arrayIndex04_ColorIndex]][imageController.pallette.arrayIndex00_Red];
				hoverText += " G: " + imageController.pallette.perlerColorsRGB[imageController.pallette.currentPallette[myPalletteIndex][imageController.pallette.arrayIndex04_ColorIndex]][imageController.pallette.arrayIndex01_Green];
				hoverText += " B: " + imageController.pallette.perlerColorsRGB[imageController.pallette.currentPallette[myPalletteIndex][imageController.pallette.arrayIndex04_ColorIndex]][imageController.pallette.arrayIndex02_Blue];
				hoverText += ")";

				imageController.beadMaker.controlPanel.hoveredPixelColor.setText(hoverText);
				imageController.beadMaker.controlPanel.hoveredPixelColor.bgColor = pixelColor;
				imageController.beadMaker.controlPanel.hoveredPixelColor.setForeground(ColorHelper.GetTextColorForBGColor(pixelColor));
				imageController.beadMaker.controlPanel.hoveredPixelColor.repaint();
			}
		}
		repaint();
	}


	@Override
	public void mouseClicked(MouseEvent e) {
		dragPoint = null;
		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		dragPoint = e.getPoint();
		//		Point dragPoint = e.getPoint();
		//        int x = Math.min(clickPoint.x, dragPoint.x);
		//        int y = Math.min(clickPoint.y, dragPoint.y);
		//
		//        int width = Math.max(clickPoint.x, dragPoint.x) - x;
		//        int height = Math.max(clickPoint.y, dragPoint.y) - y;
		//
		//        if (selection == null) {
		//            selection = new Rectangle(x, y, width, height);
		//        } else {
		//            selection.setBounds(x, y, width, height);
		//        }
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		//selection = null;
		//dragPoint = null;
		//repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		//DO NOTHING (has to be implemented for the MouseMotionListener interface, but we don't need it)
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		clickPoint = e.getPoint();
		//selection = null;
		dragPoint = null;
		ConsoleHelper.PrintMessage("fired a mousePressed event ");
	}
	
	public void paintComponent(Graphics g){
		
		if(!renderPixelsAsBeads) {
			super.paintComponent(g);
		} 

		if(renderPixelsAsBeads) {
			Graphics2D g2d = Graphics2DMaker.getGraphics2D(g);

			g2d.setRenderingHint(
					RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			g2d.setColor(Color.BLACK);

			g2d.setStroke(new BasicStroke(imageWidth / gridColumns / 2.5f));

			for (int x = 0; x < gridColumns; x++) {
				for (int y = 0; y < gridRows; y++) {
					Color myPixel = ColorHelper.pixeltoColor(renderImage.pixels[x + (y * gridColumns)]);
					g2d.setColor(myPixel);
					g2d.draw(new Ellipse2D.Double(
							x * imageWidth / gridColumns + (imageWidth / gridColumns / 5f), //add 1/5 of the cell width to accommodate for stroke thickness
							y * imageHeight / gridRows + (imageHeight / gridRows / 5f), //add 1/5 of the cell width to accommodate for stroke thickness
							imageWidth / gridColumns / 1.5f,
							imageHeight / gridRows / 1.5f)
							);
				}
			}
		}
		
		//g.drawString("pixelX = " + pixelX + ", pixelY = " + pixelY, xPos + 10, yPos + 10);
		
		if(showGrid) {
			Graphics2D g2d = (Graphics2D) g;

			g2d.setColor(gridColor);

			g2d.setStroke(new BasicStroke(1));

			for (int x = 0; x < gridColumns; x++) {
				g2d.drawLine(x * imageWidth / gridColumns, 0, x * imageWidth / gridColumns, imageHeight - 1);
				//ConsoleHelper.PrintMessage("drawing line at " + (x * (imageWidth / gridColumns)) + ", 0, " + (x * (imageWidth / gridColumns)) + ", " + (imageHeight - 1));
			}
			for (int y = 0; y < gridRows; y++) {
				g2d.drawLine(0, y * imageHeight / gridRows, imageWidth - 1, y * imageHeight / gridRows);
				//ConsoleHelper.PrintMessage("drawing line at " + (x * (imageWidth / gridColumns)) + ", 0, " + (x * (imageWidth / gridColumns)) + ", " + (imageHeight - 1));
			}        


			g2d.setStroke(new BasicStroke(pegboardSeparatorThickness));

			for (int x = 1; x < gridColumns; x++) {
				if (x % pegboardPegsWide == 0) {
					g2d.drawLine(x * imageWidth / gridColumns, 0, x * imageWidth / gridColumns, imageHeight - 1);
				}
			}
			for (int y = 1; y < gridRows; y++) {
				if (y % pegboardPegsHigh == 0) {
					g2d.drawLine(0, y * imageHeight / gridRows, imageWidth - 1, y * imageHeight / gridRows);
				}
			}  
		}

		//This code block is for the measurement helper (click & drag)
		if (dragPoint != null) {
			//g.setColor(Color.red);
			g.setColor(gridColor);
			g.drawLine(clickPoint.x, clickPoint.y, dragPoint.x, dragPoint.y);  
			g.setFont (new Font("Arial", 1, 18));
			float scale = imageController.zoomFactor / 100f;
			int xPosition = (int) Math.floor((clickPoint.x - (clickPoint.x % scale)) / scale);
			int yPosition = (int) Math.floor((clickPoint.y - (clickPoint.y % scale)) / scale);
			int xPositionLineEnd = (int) Math.floor(dragPoint.x / scale);
			int yPositionLineEnd = (int) Math.floor(dragPoint.y / scale);
			String xMessage = "";
			String yMessage = "";

			//If the line is diagonal, display a left/right and down/up measurement
			if(xPosition != xPositionLineEnd && yPosition != yPositionLineEnd) {
				if(xPositionLineEnd >= xPosition) {
					xMessage = "Right " + Math.abs(xPositionLineEnd - xPosition);
				} else if (xPositionLineEnd < xPosition) {
					xMessage = "Left " + Math.abs(xPositionLineEnd - xPosition);
				} 

				if(yPositionLineEnd >= yPosition) {
					yMessage = "Down " + Math.abs(yPositionLineEnd - yPosition);
				} else if (yPositionLineEnd < yPosition) {
					yMessage = "Up " + Math.abs(yPositionLineEnd - yPosition);
				}
			} else { //If the line is straight, display a length measurement
				if(xPositionLineEnd != xPosition) {
					xMessage = (Math.abs(xPositionLineEnd - xPosition) + 1) + " beads";
				}	            
				if(yPositionLineEnd != yPosition) {
					yMessage = (Math.abs(yPositionLineEnd - yPosition) + 1) + " beads";
				}
			}

			if(xPosition != xPositionLineEnd || yPosition != yPositionLineEnd) {
				//if the text position is too near the edge, adjust the position
				int xDrawPos = dragPoint.x < MathHelper.getMaximumValue((int)scale, 10) ? 10 : -10;
				int yDrawPos = dragPoint.y < MathHelper.getMaximumValue((int)scale, 10) ? 10 : 0;
				xDrawPos += dragPoint.y < MathHelper.getMaximumValue((int)scale, 10) ? 18 : 0;
				yDrawPos += dragPoint.y > clickPoint.y ? 22 : -14;
				//if the text position is in the way of the line, adjust the position
				//int yDrawPos = clickPoint.y > dragPoint.y ? 10 : -10;
				//Draw the drop shadow(s)
				StringHelper.drawString(
					g,
					xMessage + " " + yMessage,
					dragPoint.x + xDrawPos,
					dragPoint.y + yDrawPos,
					true
				);
			}
		}
	}

	@Override
	public void onInterObjectCommunicator_CommunicateEvent(Object o) {
		if (o instanceof KeyEvent) {
			KeyEvent e = ((KeyEvent) o);
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				ConsoleHelper.PrintMessage("fired a keyPressed event from oCommInterface in RenderLabel" + e.getKeyCode());
		        dragPoint = null;
	    		repaint();
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
	public void onInterObjectCommunicator_CommunicateEvent(String descriptor, Object o) {
		if (descriptor.equals("show grid")) {
			if (o instanceof Boolean) {
				boolean b = ((boolean) o);
				this.showGrid = b;	
			}
		}	
		
	}

	@Override
	public Object onInterObjectCommunicator_RequestEvent(String descriptor, Object o) {
		// TODO Auto-generated method stub
		return null;
	}
}
