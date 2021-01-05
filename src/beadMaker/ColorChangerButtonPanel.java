package beadMaker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JViewport;

public class ColorChangerButtonPanel extends JPanel {
	ColorChangerButtonPanel(String label, ImageController imageController) {
		super();		
		init(label, imageController);
	}
	
	ColorChangerButtonPanel(String label, ImageController imageController, RenderLabel renderLabel) {
		super();
		init(label, imageController, renderLabel);
	}

	public void init(String label, final ImageController imageController) {
		final JViewport viewport = imageController.renderScrollPanel.getViewport();
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		setOpaque(false);
		setPreferredSize(new Dimension(100, 40));
		setMaximumSize(new Dimension(360, 40));
		
		final JButton colorChangeButton = new JButton(label);
		
		ActionListener bgColorChangeActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				Color initialColor = viewport.getBackground();
				Color newColor = JColorChooser.showDialog(null,
						"Color Picker", initialColor);
				if (newColor != null) {
					//bgColorChangeButton.setBackground(background);
					viewport.setBackground(newColor);
					imageController.backgroundColor = newColor;
				}
			}
		};
		
		colorChangeButton.addActionListener(bgColorChangeActionListener);
		
		add(colorChangeButton);
	}
	
	
	public void init(String label, final ImageController imageController, final RenderLabel renderLabel) {
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		setOpaque(false);
		setPreferredSize(new Dimension(100, 40));
		setMaximumSize(new Dimension(360, 40));
		
		final JButton colorChangeButton = new JButton(label);
		
		ActionListener gridColorChangeActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				Color initialColor = renderLabel.gridColor;
				Color newColor = JColorChooser.showDialog(null,
						"Color Picker", initialColor);
				if (newColor != null) {
					//bgColorChangeButton.setBackground(background);
					renderLabel.gridColor = newColor;
					imageController.updateImages();
				}
			}
		};
		
		colorChangeButton.addActionListener(gridColorChangeActionListener);
		
		add(colorChangeButton);
	}
}
