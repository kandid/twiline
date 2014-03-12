/*
 * Created on 01.03.2014
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.kandid.apps.transcriber;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

public interface SeekablePCMSource {

	public static class MemorySource implements SeekablePCMSource {

		public MemorySource(AudioInputStream in) throws IOException {
			_format = in.getFormat();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] tmp = new byte[_format.getFrameSize() * 16384];
			for (;;) {
				int read = in.read(tmp);
				if (read == -1)
					break;
				out.write(tmp, 0, read);
			}
			_data = out.toByteArray();
			_pos = 0;
		}

		@Override
		public int read(byte[] buf, int offset, int length) {
			if (_pos >= _data.length)
				return -1;
			length = Math.min(_data.length - _pos, length);
			System.arraycopy(_data, _pos, buf, offset, length);
			_pos += length;
			return length;
		}

		@Override
		public void seek(long frames) {
			_pos = (int) frames * _format.getFrameSize();
		}

		@Override
		public AudioFormat getAudioFormat() {
			return _format;
		}

		@Override
		public long getLength() {
			return (long) (_data.length / _format.getFrameSize() / _format.getFrameRate() * 1000000);
		}

		public final AudioFormat _format;
		private final byte[] _data;
		private int _pos;
	}

	/**
	 * Read the data from the current position
	 * @param buf		the buffer to place the data into
	 * @param offset	the offset in the bufer
	 * @param length	the requested length of the data in bytes
	 * @return the number of bytes actually readss
	 */
	public int read(byte[] buf, int offset, int length);

	/**
	 * Seek to the position specified in frames from the start of
	 * the clip.
	 * @param frames	the position to seek to in frames
	 */
	public void seek(long frames);

	/**
	 * Returns the format of the data
	 * @return the format object
	 */
	public AudioFormat getAudioFormat();

	/**
	 * Returns the length of this source in µs
	 * @return	length of this source in µs
	 */
	public long getLength();
}
