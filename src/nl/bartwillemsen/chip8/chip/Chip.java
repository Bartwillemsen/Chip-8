package nl.bartwillemsen.chip8.chip;

import java.io.*;

public class Chip
{
	/**
	 * Chip-8 has 4kb (4096 bytes) of memory.
	 */
	private char[] memory;

	/**
	 * There are 16 8-bit registers which are referred to as Vx, where x
	 * is a hexadecimal digit. The VF register is used as a flag by
	 * some instructions.
	 */
	private char[] V;

	/**
	 * 16-bit register used to store memory addresses. Only 12 bits are
	 * used.
	 */
	private char I;

	/**
	 * Program Counter. Stores the currently executing memory address.
	 */
	private char PC;

	/**
	 * An array of 16 16-bit values used to store the address where the
	 * interpreter should return to when finishing a subroutine. It allows
	 * for 16 levels of nested subroutines.
	 */
	private char stack[];

	/**
	 * Stack Pointer. Used to point to the topmost, free slot in the stack.
	 */
	private int SP;

	/**
	 * Used to delay events in the Chip-8 program.
	 */
	private int delay_timer;

	/**
	 * As long as the sound timer is greater than 0, a buzzer will sound.
	 */
	private int sound_timer;

	/**
	 * Used to store the keyboard state.
	 */
	private byte[] keys;

	/**
	 * Used to represent the screen. It has a resolution of 64x32.
	 */
	private byte[] display;

	/**
	 * Indicates if the screen needs to be redrawn.
	 */
	private boolean needRedraw;

	public Chip()
	{
		init();
	}

	/**
	 * Reset all variables to their default values.
	 */
	public void init()
	{
		memory = new char[4096];
		V = new char[16];
		I = 0x0;
		PC = 0x200;

		stack = new char[16];
		SP = 0;

		delay_timer = 0;
		sound_timer = 0;

		keys = new byte[16];

		display = new byte[64 * 32];

		needRedraw = false;

		loadFontset();
	}

	/**
	 * Run the operation.
	 */
	public void run()
	{
		// Get the opcode. All instructions are 2 bytes long.
		//
		// To make room for the next byte we shift the first byte eight
		// positions to the left. We then do an OR operation to place
		// the next byte in the created area.
		char opcode = (char) (memory[PC] << 8 | memory[PC + 1]);
		System.out.print(Integer.toHexString(opcode).toUpperCase() + ": ");

		decodeOpcode(opcode);
	}

	/**
	 * Decode an opcode to determine what operation to execute.
	 *
	 * @param opcode The opcode that needs to be decoded.
	 */
	public void decodeOpcode(char opcode)
	{
		// Here we basically filter on the first nibble, since that digit
		// determines what we need to do.
		switch (opcode & 0xF000) {

			case 0x0000:
				switch (opcode & 0x00FF) {
					case 0x00E0: // 00E0 - CLS
						System.err.println("Unsupported opcode.");
						System.exit(1);
						break;

					case 0x00EE: // 00EE - "Return from a subroutine"
						SP--;
						PC = (char) (stack[SP] + 2);
						System.out.println("Returning to " + Integer.toHexString(PC).toUpperCase());
						break;

					default:
						System.err.println("Unsupported opcode.");
						System.exit(1);
						break;

				}
				break;

			case 0x1000: // 1nnn - "jump to location nnn"
				int nnn = opcode & 0x0FFF;
				PC = (char) nnn;
				break;

			case 0x2000:
				// 2nnn - The interpreter increments the stack pointer, then puts
				// the current PC on the top of the stack. The PC is then set to nnn.
				stack[SP] = PC;
				SP++;
				PC = (char) (opcode & 0x0FFF);
				System.out.println("Calling " + Integer.toHexString(PC).toUpperCase());
				break;

			case 0x3000: {// 3xkk - "Skip next instruction if Vx = kk"
				int x = (opcode & 0x0F00) >> 8;
				int kk = (opcode & 0x00FF);

				System.out.print("Is V[" + x + "] = " + kk + "? ");
				if (V[x] == kk) {
					System.out.println("Yes. Skip next instruction.");
					PC += 4;
				} else {
					System.out.println("No. Continue normally");
					PC += 2;
				}
				break;
			}

			case 0x6000: {// 6xkk - "Set Vx = kk"
				// We just need the value of position x! So we shift 8 bytes to the right.
				int x = (opcode & 0x0F00) >> 8;

				// The interpreter puts the value kk into register Vx.
				V[x] = (char) (opcode & 0x00FF);
				PC += 2;
				System.out.println("Setting V[" + x + "] to " + (int) V[x]);
				break;
			}

			case 0x7000: {// 7xkk - "Set Vx = Vx + kk"
				int x = (opcode & 0x0F00) >> 8;
				int kk = (opcode & 0x0FF);
				V[x] = (char) ((V[x] + kk) & 0xFF);
				PC += 2;
				System.out.println("Adding " + kk + " to V[" + x + "] = " + (int) V[x]);
				break;
			}

			case 0x8000: // Contains more data in last nibble.

				switch (opcode & 0x000F) {

					case 0x0000:
					default:
						System.err.println("Unsupported opcode.");
						System.exit(1);
						break;
				}
				break;

			case 0xA000: // Annn - The value of register I is set to nnn.
				I = (char) (opcode & 0x0FFF);
				PC += 2;
				System.out.println("Set I to " + Integer.toHexString(I).toUpperCase());
				break;

			case 0xD000: {
				// Dxyn - Draw a sprite at position x, y.
				int x = V[(opcode & 0x0F00) >> 8];
				int y = V[(opcode & 0x00F0) >> 4];
				int height = opcode & 0x000F;

				V[0xF] = 0;

				for (int _y = 0; _y < height; _y++) {
					int line = memory[I + _y];

					for (int _x = 0; _x < 8; _x++) {
						int pixel = line & (0x80 >> _x);

						if (pixel != 0) {
							int totalX = x + _x;
							int totalY = y + _y;
							int index = totalY * 64 + totalX;

							if (display[index] == 1) {
								V[0xF] = 1;
							}

							display[index] ^= 1;
						}
					}
				}

				PC += 2;
				needRedraw = true;
				System.out.println("Drawing at V[" + ((opcode & 0x0F00) >> 8) + "] = " + x + ", V[" + ((opcode & 0x00F0) >> 4) + "] = " + y);
				break;
			}

			default:
				System.err.println("Unsupported opcode.");
				System.exit(1);
		}
	}

	/**
	 * Get the current state of the full display
	 *
	 * @return The array of the state of all the screen pixels
	 */
	public byte[] getDisplay()
	{
		return display;
	}

	/**
	 * Does the screen needs to be redrawn?
	 *
	 * @return TRUE if we need to redraw or FALSE otherwise
	 */
	public boolean needsRedraw()
	{
		return needRedraw;
	}

	/**
	 * Set the redraw flag back to false.
	 */
	public void removeDrawFlag()
	{
		needRedraw = false;
	}

	/**
	 * Load a Chip-8 program into the memory.
	 *
	 * @param file The file that needs to be loaded
	 */
	public void loadProgram(String file)
	{
		DataInputStream input = null;

		try {
			input = new DataInputStream(new FileInputStream(new File(file)));

			int offset = 0;
			while (input.available() > 0) {
				memory[0x200 + offset] = (char) (input.readByte() & 0xFF);
				offset++;
			}

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} finally {

			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {}
			}
		}
	}

	/**
	 * Load the fontset into memory.
	 */
	public void loadFontset()
	{
		for (int i = 0; i < ChipData.fontset.length; i++) {
			memory[0x50 + i] = (char) (ChipData.fontset[i] & 0xFF);
		}
	}
}
