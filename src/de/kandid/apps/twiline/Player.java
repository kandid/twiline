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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import de.kandid.model.Emitter;
import de.kandid.ui.Action;
import de.kandid.ui.Condition;

public class Player {

	//@Emitter.Listener
	public static interface Listener {
		void trackChanged();
		void positionChanged(long frames);
	}

	private enum Cmd {Done, Play, Pause}

	public static class Model {

		public class Position extends DefaultBoundedRangeModel {

			@Override
			public void setValue(int n) {
				seek(n);
				super.setValue(n);
			}

			void updateTo(long frames) {
				super.setValue((int) frames);
			}
		}

		public Model() {
			_playLoop = new Thread(getClass().getName()) {
				@Override
				public void run() {
					playLoop();
				}
			};
			_cmd = Cmd.Pause;
			_playLoop.start();
			_updater.setRepeats(true);
			update();
		}

		public void setValue(Player value) {
			_value = value;
			_seekInterval.setValue(value._seekInterval);
		}

		public Player getValue() {
			_value._seekInterval = _seekInterval.getValue();
			return _value;
		}

		public void dispose() {
			synchronized (_playLoop) {
				_cmd = Cmd.Done;
				_playLoop.notify();
			}
		}

		public void open(SeekablePCMSource src) throws LineUnavailableException {
			synchronized (_playLoop) {
				close();
				_source = src;
				final AudioFormat af = _source.getAudioFormat();
				_sdl = AudioSystem.getSourceDataLine(af);
				_sdl.open(src.getAudioFormat());
				_buf = new byte[_sdl.getBufferSize()];
			}
			_position.setMaximum((int) _source.getLength());
			_listeners.fire().trackChanged();
			update();
		}

		/**
		 * Close the stream currently played.
		 */
		public void close() {
			synchronized (_playLoop) {
				if (_sdl == null)
					return;
				_cmd = Cmd.Pause;
				_sdl.close();
				_sdl = null;
			}
			_listeners.fire().trackChanged();
			update();
		}

		/**
		 * Convert number of frames to a time in milliseconds
		 * @param frames  number of the frames from the beginning
		 * @return time offset in milliseconds
		 */
		public long asMillis(long frames) {
			synchronized (_playLoop) {
				if (_sdl == null)
					return 0;
				return (long)((double) frames / _sdl.getFormat().getFrameRate() * 1000);
			}
		}

		public long asFrames(long millis) {
			synchronized (_playLoop) {
				if (_sdl == null)
					return 0;
				return (long)((double) millis * _sdl.getFormat().getFrameRate() / 1000);
			}
		}

		/**
		 * Return the current playing position in number of frames. You may convert
		 * this value with {@link #asMillis(long)} to a time in ms.
		 * @return the current position of the player
		 */
		public long getPos() {
			synchronized (_playLoop) {
				if (_sdl == null)
					return 0;
				return _offset + _sdl.getLongFramePosition() - _sdlStart;
			}
		}

		public long getLength() {
			return _source != null ? _source.getLength() : 0;
		}

		private void playLoop() {
			synchronized (_playLoop) {
				try {
					for (;;) {
						switch (_cmd) {
							case Pause:
								_playLoop.wait();
								break;
							case Play:
								int needed = Math.min(_sdl.available(), _buf.length);
								int read = _source.read(_buf, 0, needed);
								if (read < 0) {
									_cmd = Cmd.Pause;
									break;
								}
								_sdl.write(_buf, 0, read);
								_playLoop.wait(10);
								break;
							case Done:
								return;
						}
					}
				} catch (Exception ignored) {
				}
			}
		}

		public void seek(long frames) {
			frames = Math.max(0, frames);
			synchronized (_playLoop) {
				try {
					_source.seek(frames);
					_offset = frames;
					_sdlStart = _sdl.getLongFramePosition();
				} catch (IOException e) {
					//TODO
					e.printStackTrace();
				}
			}
			_listeners.fire().positionChanged(getPos());
		}

		public void play() {
			synchronized (_playLoop) {
				_cmd = Cmd.Play;
				try {
					_source.seek(getPos());
				} catch (IOException e) {
					// TODO
					e.printStackTrace();
				}
				_sdl.start();
				_playLoop.notify();
			}
			_updater.start();
		}

		public void pause() {
			synchronized (_playLoop) {
				_sdl.stop();
				_sdl.flush();
				_cmd = Cmd.Pause;
				_playLoop.notify();
			}
			_updater.stop();
		}

		public void step(long frames) {
			seek(getPos() + frames);
		}

		private void update() {
			Condition trackLoaded = new Condition(Messages.get("Player.noTrack")) { @Override public boolean isTrue() { //$NON-NLS-1$
				return _sdl != null;
			}};
			trackLoaded.applyTo(_play, _stop, _back, _forward);
		}

		public final Action _play = new Action(Messages.get("Player.play_s"), "media-playback-start.png", Messages.get("Player.play"), "alt UP") { //$NON-NLS-3$
			@Override
			public void go() {
				play();
			}
		};

		public final Action _stop = new Action(Messages.get("Player.pause_s"), "media-playback-pause.png", Messages.get("Player.pause"), "alt DOWN") { //$NON-NLS-3$
			@Override
			public void go() {
				pause();
			}
		};

		public final Action _back = new Action(Messages.get("Player.back_s"), "media-seek-backward.png", Messages.get("Player.back"), "alt LEFT") { //$NON-NLS-3$
			@Override
			public void go() {
				step((long)-_source.getAudioFormat().getFrameRate() * _seekInterval.getValue() / 10);
			}
		};

		public final Action _forward = new Action(Messages.get("Player.forward_s"), "media-seek-forward.png", Messages.get("Player.forward"), "alt RIGHT") { //$NON-NLS-3$
			@Override
			public void go() {
				step((long)_source.getAudioFormat().getFrameRate() * _seekInterval.getValue() / 10);
			}
		};

		public final Position _position = new Position();

		public final DefaultBoundedRangeModel _seekInterval = new DefaultBoundedRangeModel(10, 0, 0, 50);

		private final Timer _updater = new Timer(50, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_position.updateTo(getPos());
				_listeners.fire().positionChanged(getPos());
			}
		});

		private volatile Cmd _cmd;
		private final Thread _playLoop;
		private long _offset;
		private long _sdlStart;
		private SourceDataLine _sdl;
		private SeekablePCMSource _source;
		private byte[] _buf;
		private Player _value;
		public final Emitter<Listener> _listeners = Emitter.makeEmitter(Listener.class);
	}

	public static class View extends Box {
		public View(final Model model) {
			super(BoxLayout.PAGE_AXIS);
			final JLabel time = new JLabel(formatTime(0));
			time.setFont(new Font("Dialog", Font.PLAIN, 18));
			add(make(time, BorderLayout.EAST));
			JPanel controls = new JPanel(new GridLayout(0, 2, 5, 5));
			for (Action a : new Action[]{model._stop, model._play, model._back, model._forward}) {
				JButton b = new JButton(a);
				b.setText("");
				b.setFocusable(false);
				controls.add(b);
			}
			add(controls);

			add(Box.createVerticalStrut(10));
			add(make(new JLabel(Messages.get("Player.SeekInterval")), BorderLayout.WEST));
			final JSlider seekInterval = new JSlider(model._seekInterval);
			seekInterval.setOrientation(JSlider.HORIZONTAL);
			seekInterval.setPaintLabels(true);
			seekInterval.setPaintTicks(true);
			seekInterval.setMajorTickSpacing(10);
			Hashtable<Integer, JComponent> labels = new Hashtable<>();
			for (int i = model._seekInterval.getMinimum(); i <= model._seekInterval.getMaximum(); i += 10)
				labels.put(i, new JLabel(Integer.toString(i / 10) + "s"));
			seekInterval.setLabelTable(labels);
			add(seekInterval);

			add(Box.createVerticalGlue());

			model._listeners.add(this, new Listener() {
				@Override
				public void trackChanged() {
				}
				@Override
				public void positionChanged(long frames) {
					time.setText(formatTime((int) model.asMillis(model.getPos())));
				}
			});
		}
		private static JPanel make(JComponent c, String alignment) {
			JPanel ret = new JPanel(new BorderLayout());
			ret.add(c, alignment);
			return ret;
		}
	}

	public static class PositionView extends JSlider implements Listener {
		public PositionView(final Model model) {
			super(model._position);
			_model = model;
			model._listeners.add(this, this);
			setPaintTicks(true);
			trackChanged();
		}
		@Override
		public void trackChanged() {
			long millis = _model.asMillis(_model.getLength());
			setMajorTickSpacing((int) _model.asFrames(60 * 1000));
		}
		@Override
		public void positionChanged(long frames) {
		}

		private final Model _model;
	}

	public static String formatTime(int millis) {
		int tens = millis % 1000 / 100;
		millis /= 1000;
		int secs = millis % 60;
		millis /= 60;
		int mins = millis % 60;
		millis /= 60;
		final String format = "%02d:%02d:%02d-%1d";
		return String.format(format, millis, mins, secs, tens);
	}

	public int _seekInterval = 20;

	public static void main(String[] args) {
		try {
			File file = new File("/home/dominik/Freizeit/Music/Untitled002.wav");
//			AudioInputStream ais = AudioSystem.getAudioInputStream(file);
//			MemorySource sp = new SeekablePCMSource.MemorySource(ais);
			SeekablePCMSource.PcmFile sp = new SeekablePCMSource.PcmFile(file);
			System.out.println("Length: " + sp.getLength() + "µs");
			System.gc();
			Model m = new Model();
			m.setValue(new Player());
			m.open(sp);
			JFrame f = new JFrame("Player (" + file.getName() + ")");
			Box view = new Box(BoxLayout.PAGE_AXIS);
			view.add(new View(m));
			view.add(Box.createVerticalStrut(15));
			view.add(new PositionView(m));
			f.getContentPane().add(view);
			f.pack();
			f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			f.setLocation(500, 500);
			f.setVisible(true);
		} catch (Exception e) {
			System.out.println("Exception caught: " + e);
			e.printStackTrace();
		}
	}
}
