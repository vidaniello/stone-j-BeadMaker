package beadMaker;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import core.BorderMaker;
import core.ConsoleHelper;
import core.StringHelper;

public class CheckBoxPanel extends JPanel {
	
	public BMCheckBox checkbox;
	public ImageController imageController;
	InterObjectCommunicator oComm;
	
	ConsoleHelper consoleHelper = new ConsoleHelper();

	CheckBoxPanel(String myLabel, boolean checkedState, Palette pallette, ImageController imageController, InterObjectCommunicator myOComm) {
		super();
		oComm = myOComm;
		init(myLabel, checkedState, pallette, imageController);
	}
	
	CheckBoxPanel(String myLabel, boolean checkedState, ImageController imageController, InterObjectCommunicator myOComm) {
		this(myLabel, checkedState, null, imageController, myOComm);
	}
	
	CheckBoxPanel(String myLabel, Palette pallette, ImageController imageController, InterObjectCommunicator myOComm) {
		this(myLabel, false, pallette, imageController, myOComm);
	}

	public void init(String myLabel, boolean checkedState, final Palette pallette, final ImageController myImageController) {

		imageController = myImageController;
		
		this.setLayout(new FlowLayout(FlowLayout.LEFT));

		checkbox = new BMCheckBox(myLabel, checkedState, oComm);
		
		add(checkbox);
		setOpaque(false);
		setPreferredSize(new Dimension(100, 20));
		setMaximumSize(new Dimension(360, 20));
		
		String myLabelNoXML = StringHelper.removeXML(myLabel);
		
		consoleHelper.PrintMessage(" %%%%%%%%%%%%%%%%%%%%%%%%myLabelNoXML =" + myLabelNoXML);

		if (myLabelNoXML.equals("Perler") || myLabelNoXML.equals("Artkal-S") || myLabelNoXML.equals("Hama")) { 

			ActionListener beadBrandActionListener = new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					BMCheckBox checkBox = (BMCheckBox)actionEvent.getSource();
					checkBox.setSelected(pallette.setBeadBrand(StringHelper.removeXML(checkBox.getText()), checkBox.isSelected()));
					oComm.communicate("update images", "IMAGE_CONTROLLER");
					oComm.communicate("create buttons", "PALETTE");
					consoleHelper.PrintMessage("FIRING beadBrandActionListener");
				}
			};

			checkbox.addActionListener(beadBrandActionListener);
		}
		if (myLabelNoXML.equals("Show Grid")) { 

			ActionListener showGridActionListener = new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					BMCheckBox checkBox = (BMCheckBox)actionEvent.getSource();
					imageController.renderLabel.showGrid = checkBox.isSelected();
					imageController.updateImages();
				}
			};

			checkbox.addActionListener(showGridActionListener);
		}
		
		if (myLabelNoXML.equals("Show Pixels as Beads")) { 

			ActionListener renderPixelsAsBeadsActionListener = new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					BMCheckBox checkBox = (BMCheckBox)actionEvent.getSource();
					imageController.setRenderPixelsAsBeads(checkBox.isSelected());
				}
			};

			checkbox.addActionListener(renderPixelsAsBeadsActionListener);
		}
		
		if (myLabelNoXML.equals("Exclude Pearls")) { 

			ActionListener excludePearlsActionListener = new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					pallette.selectedColorIndex = -1;
					imageController.selectedColorIndex = -1;
					pallette.excludePearls = pallette.excludePearls.Toggle();
					pallette.GetPalletteWithFiltersApplied();
					imageController.updateImages();
					pallette.CreateButtons("Exclude Pearls ActionListener");
				}
			};

			checkbox.addActionListener(excludePearlsActionListener);
		}
		
		if (myLabelNoXML.equals("Exclude Translucents")) { 

			ActionListener excludeTranslucentsActionListener = new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					pallette.selectedColorIndex = -1;
					imageController.selectedColorIndex = -1;
					pallette.excludeTranslucents = pallette.excludeTranslucents.Toggle();
					pallette.GetPalletteWithFiltersApplied();
					imageController.updateImages();
					pallette.CreateButtons("Exclude Translucents ActionListener");
				}
			};

			checkbox.addActionListener(excludeTranslucentsActionListener);
		}
		
		
		if (myLabelNoXML.equals("Flip Image")) { 
			consoleHelper.PrintMessage(" %%%%%%%%%%%%%%%%%%%%%%%%Adding ActionListener for Flip Image");
			ActionListener flipImageActionListener = new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					BMCheckBox checkBox = (BMCheckBox)actionEvent.getSource();
					consoleHelper.PrintMessage(" %%%%%%%%%%%%%%%%%%%%%%%%Flip Image click event detected");
					imageController.flipImage(checkBox.isSelected());
				}
			};

			checkbox.addActionListener(flipImageActionListener);
		}
		
		
	}
}
