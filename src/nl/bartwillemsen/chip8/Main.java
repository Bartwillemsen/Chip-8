package nl.bartwillemsen.chip8;

import nl.bartwillemsen.chip8.chip.Chip;
import nl.bartwillemsen.chip8.ui.ChipFrame;

public class Main extends Thread
{
	private Chip chip8;
	private ChipFrame frame;

	public Main()
	{
		chip8 = new Chip();
		chip8.loadProgram("./pong2.c8");

		frame = new ChipFrame(chip8);
		frame.setVisible(true);
	}

	@Override
	public void run()
	{
		while (true) {
			chip8.run();
			if (chip8.needsRedraw()) {
				frame.repaint();
				chip8.removeDrawFlag();
			}

			try {
				Thread.sleep(16);
			} catch (InterruptedException e) {
				// Never fires, so too lazy to properly handle it. :)
			}
		}
	}

	public static void main(String[] args)
	{
		Main main = new Main();
		main.start();
	}
}
