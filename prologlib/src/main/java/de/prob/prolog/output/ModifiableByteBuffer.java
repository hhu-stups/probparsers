package de.prob.prolog.output;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Simple growable byte buffer that allows direct access to internal buffer and arbitrary writes.
 */
final class ModifiableByteBuffer extends OutputStream {

	private byte[] buffer;
	private int size;

	ModifiableByteBuffer() {
		this.buffer = new byte[16];
		this.size = 0;
	}

	byte[] bytes() {
		return this.buffer;
	}

	int size() {
		return this.size;
	}

	void setSize(int size) {
		this.size = size;
	}

	void reset() {
		this.size = 0;
	}

	private void ensureCapacity(int minCapacity) {
		byte[] buffer = this.buffer;
		int len = buffer.length;
		if (minCapacity > 0 && len < minCapacity) {
			while (len < minCapacity) {
				len *= 2;
				if (len <= 0) {
					throw new IllegalArgumentException("size overflow - cannot grow byte buffer");
				}
			}
			this.buffer = Arrays.copyOf(buffer, len);
		}
	}

	@Override
	public void write(int value) {
		this.ensureCapacity(this.size + 1);
		this.buffer[this.size++] = (byte) value;
	}

	@Override
	public void write(byte[] values, int off, int len) {
		this.ensureCapacity(this.size + len);
		System.arraycopy(values, off, this.buffer, this.size, len);
		this.size += len;
	}

	void writeNullTerminatedString(String s) {
		byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
		// a zero byte would end the string early, so the NUL character is written as 0xC0 0x80
		// ("modified UTF-8", as used by SICStus for NUL in atoms)
		int start = 0;
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] == 0) {
				this.write(bytes, start, i - start);
				this.write(0xC0);
				this.write(0x80);
				start = i + 1;
			}
		}
		this.write(bytes, start, bytes.length - start);
		this.write((byte) 0);
	}

	void set(int pos, int value) {
		this.ensureCapacity(pos + 1);
		this.buffer[pos] = (byte) value;
	}

	public void shiftRight(int pos, int amount) {
		this.ensureCapacity(this.size + amount);
		System.arraycopy(this.buffer, pos, this.buffer, pos + amount, this.size - pos);
		this.size += amount;
	}
}
