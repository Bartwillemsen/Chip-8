package nl.bartwillemsen.chip8.ui;

import nl.bartwillemsen.chip8.chip.Chip;

import javax.swing.*;
import java.awt.*;

public class ChipFrame extends JFrame
{
	public static final int SCALE = 10;

	private ChipPanel panel;

	public ChipFrame(Chip c)
	{
		setPreferredSize(new Dimension(64 * SCALE, 32 * SCALE));
		pack();
		setPreferredSize(new Dimension(640 + getInsets().left + getInsets().right, 320 + getInsets().top + getInsets().bottom));
		panel = new ChipPanel(c);
		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Chip-8 Emulator");
		pack();
		setLocationRelativeTo(null);
	}
}
