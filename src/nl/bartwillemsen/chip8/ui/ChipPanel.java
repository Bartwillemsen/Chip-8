package nl.bartwillemsen.chip8.ui;

import nl.bartwillemsen.chip8.chip.Chip;

import javax.swing.*;
import java.awt.*;

public class ChipPanel extends JPanel
{
	private Chip chip;

	public ChipPanel(Chip chip)
	{
		this.chip = chip;
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;
		super.paintComponent(g2d);

		// Get the current display state.
		byte[] display = chip.getDisplay();

		// Spin through each pixel and set the right color depending on
		// its state.
		for (int i = 0; i < display.length; i++) {

			g2d.setColor(Color.black);

			if (display[i] == 1) {
				g2d.setColor(Color.white);
			}

			int x = i % 64;
			int y = (int) Math.floor(i / 64);

			g2d.fillRect(x * ChipFrame.SCALE, y * ChipFrame.SCALE, ChipFrame.SCALE, ChipFrame.SCALE);
		}
	}
}
