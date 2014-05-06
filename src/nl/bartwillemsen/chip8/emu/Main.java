package nl.bartwillemsen.chip8.emu;

import nl.bartwillemsen.chip8.chip.Chip;

public class Main
{
	public static void main(String[] args)
	{
		Chip c = new Chip();

		ChipFrame frame = new ChipFrame(c);
		frame.setVisible(true);
//		c.run();
	}
}
