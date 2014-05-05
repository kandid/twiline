/*
 * (C) Copyright 2014, by Dominikus Diesch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.kandid.apps.twiline;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

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

	public static class PcmFile implements SeekablePCMSource {

		private static class MarkStream extends InputStream {

			public MarkStream(File file) throws FileNotFoundException {
				_raf = new RandomAccessFile(file, "r");
			}

			public long getFilePointer() throws IOException {
				return _raf.getFilePointer();
			}

			public void seek(long pos) throws IOException {
				_raf.seek(pos);
			}

			public long length() throws IOException {
				return _raf.length();
			}

			@Override
			public long skip(long n) throws IOException {
				_raf.seek(_raf.getFilePointer() + n);
				return n;
			}

			@Override
			public int available() throws IOException {
				return (int) Math.min(_raf.length() - _raf.getFilePointer(), Integer.MAX_VALUE);
			}

			@Override
			public synchronized void mark(int readlimit) {
				try {
					_mark = _raf.getFilePointer();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public synchronized void reset() throws IOException {
				_raf.seek(_mark);
			}

			@Override
			protected void finalize() throws Throwable {
				super.finalize();
			}

			@Override
			public boolean markSupported() {
				return true;
			}

			@Override
			public int read() throws IOException {
				return _raf.read();
			}

			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				return _raf.read(b, off, len);
			}

			@Override
			public void close() throws IOException {
				_raf.close();
			}

			private final RandomAccessFile _raf;
			private long _mark;
		}

		public PcmFile(File in) throws IOException, UnsupportedAudioFileException {
			_ms = new MarkStream(in);
			AudioInputStream as = AudioSystem.getAudioInputStream(_ms);
			_format = as.getFormat();
			_offset = _ms.getFilePointer();
			_frames = (_ms.length() - _offset) / _format.getFrameSize();
		}

		@Override
		public int read(byte[] buf, int offset, int length) throws IOException {
			return _ms.read(buf, offset, length);
		}

		@Override
		public void seek(long frames) throws IOException {
			_ms.seek(_offset + frames * _format.getFrameSize());
		}

		@Override
		public AudioFormat getAudioFormat() {
			return _format;
		}

		@Override
		public long getLength() {
			return _frames;
		}

		private final MarkStream _ms;
		public final AudioFormat _format;
		private final long _offset;
		private final long _frames;
	}

	/**
	 * Read the data from the current position
	 * @param buf		the buffer to place the data into
	 * @param offset	the offset in the bufer
	 * @param length	the requested length of the data in bytes
	 * @return the number of bytes actually readss
	 */
	public int read(byte[] buf, int offset, int length) throws IOException;

	/**
	 * Seek to the position specified in frames from the start of
	 * the clip.
	 * @param frames	the position to seek to in frames
	 */
	public void seek(long frames) throws IOException;

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