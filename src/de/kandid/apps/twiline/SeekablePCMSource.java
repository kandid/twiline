/*
 *  Copyright (C) 2014  Dominikus Diesch
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.kandid.apps.twiline;

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
			return _data.length / _format.getFrameSize();
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
	 * Returns the length of this source in frames
	 * @return	length of this source in frames
	 */
	public long getLength();
}