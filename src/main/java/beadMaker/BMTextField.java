package beadMaker;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class BMTextField extends JTextField {

	BMSlider slider;

	BMTextField(String text, int columns, BMSlider mySlider) {
		super(text, columns);
		this.slider = mySlider;

		this.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				setSlider();				
			}
			public void removeUpdate(DocumentEvent e) {
				setSlider();
			}
			public void insertUpdate(DocumentEvent e) {
				setSlider();
			}
			
			public void setSlider() {
				//this line is broken since getText() is out of scope, so skipping it for now
				//slider.setValue(Integer.parseInt(getText()));
			}
		}); 
	}
}
