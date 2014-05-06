package nl.bartwillemsen.chip8.chip;

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
	}

	/**
	 * Run the operation.
	 */
	public void run()
	{
		// Some test values..
		memory[0x200] = 5;
		memory[0x201] = 3;

		// Get the opcode. All instructions are 2 bytes long.
		//
		// To make room for the next byte we shift the first byte eight
		// positions to the left. We then do an OR operation to place
		// the next byte in the created area.
		char opcode = (char) (memory[PC] << 8 | memory[PC + 1]);

		System.out.println(Integer.toBinaryString(opcode));
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

			case 0x1000: // 1nnn - "jump to location nnn"
				break;

			default:
				System.err.println("Unsupported opcode.");
				System.exit(1);
		}
	}
}
