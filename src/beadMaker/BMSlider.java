package beadMaker;

import java.awt.Dimension;

import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BMSlider extends JSlider {

	ImageController imageController;
	JTextField jTextField;
	
	InterObjectCommunicator oComm;

	boolean updatePalletteOnChange = true;

	BMSlider(int min, int max, String name, ImageController imageController, JTextField myJTextField, int initialValue, boolean myUpdatePalletteOnChange, InterObjectCommunicator myOComm) {
		super(min, max);
		oComm = myOComm;
		updatePalletteOnChange = myUpdatePalletteOnChange;
		init(min, max, name, imageController, myJTextField, initialValue);
	}
	
	
	BMSlider(int min, int max, String name, ImageController imageController, JTextField myJTextField, InterObjectCommunicator myOComm) {
		this(min, max, name, imageController, myJTextField, (min + max) / 2, true, myOComm);
	}


	BMSlider(int min, int max, String name, ImageController imageController, JTextField myJTextField, int initialValue, InterObjectCommunicator myOComm) {
		this(min, max, name, imageController, myJTextField, initialValue, true, myOComm);
	}
	

	public class SliderChangeListener implements ChangeListener
	{
		public void stateChanged(ChangeEvent e)
		{
			BMSlider source = (BMSlider)e.getSource();
			imageController.setColorMatchingWeight(source.getName(), source.getValue());
			jTextField.setText(Integer.toString(source.getValue()));
			//only update pallette once the slider is released
			if (!source.getValueIsAdjusting()) {
				if (updatePalletteOnChange) {
					oComm.communicate("create buttons", "PALETTE");
				}
			}
		}
	} 


	public void init(int min, int max, String name, ImageController myImageController, JTextField myJTextField, int initialValue) {
		setOrientation(JSlider.HORIZONTAL);
		setMinorTickSpacing(10);
		setMajorTickSpacing((max - min) / 2);
		setPaintTicks(false);
		setPaintLabels(true);

		this.jTextField = myJTextField;

		this.setPreferredSize(new Dimension(260, 36));
		this.setName(name);

		this.setValue(initialValue);
		
		setOpaque(false);

		//this.setBorder(new BorderMaker(BorderMaker.NONE, 8, 0));

		this.imageController = myImageController;
		this.addChangeListener(new SliderChangeListener());
	}
}
