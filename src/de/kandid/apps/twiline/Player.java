/*
 * Created on 02.03.2014
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.kandid.apps.twiline;

import static de.kandid.ui.Keys.keys;
import static java.awt.event.KeyEvent.VK_F5;
import static java.awt.event.KeyEvent.VK_RIGHT;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.BoundedRangeModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.kandid.apps.twiline.SeekablePCMSource.MemorySource;
import de.kandid.model.Condition;
import de.kandid.ui.Action;

public class Player {

	public static interface Listener {
		void trackChanged();
		void positionChanged(long frames);
	}

	private enum Cmd {Done, Play, Pause}

	public static class Model extends de.kandid.model.Model.Abstract<Listener> {

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
			super(Listener.class);
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
				_source.seek(frames);
				_offset = frames;
				_sdlStart = _sdl.getLongFramePosition();
			}
			_listeners.fire().positionChanged(getPos());
		}

		public void play() {
			synchronized (_playLoop) {
				_cmd = Cmd.Play;
				_source.seek(getPos());
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

		public final Action _play = new Action(Messages.get("Player.play_s"), "media-playback-start.png", Messages.get("Player.play"), keys.a.get(KeyEvent.VK_UP), 0) { //$NON-NLS-3$
			@Override
			public void go() {
				play();
			}
		};

		public final Action _stop = new Action(Messages.get("Player.pause_s"), "media-playback-pause.png", Messages.get("Player.pause"), keys.a.get(KeyEvent.VK_DOWN), 0) { //$NON-NLS-3$
			@Override
			public void go() {
				pause();
			}
		};

		public final Action _back = new Action(Messages.get("Player.back_s"), "media-seek-backward.png", Messages.get("Player.back"), keys.a.get(KeyEvent.VK_LEFT), 0) { //$NON-NLS-3$
			@Override
			public void go() {
				step((long)-_sdl.getFormat().getFrameRate());
			}
		};

		public final Action _forward = new Action(Messages.get("Player.forward_s"), "media-seek-forward.png", Messages.get("Player.forward"), keys.a.get(VK_RIGHT), 0) { //$NON-NLS-3$
			@Override
			public void go() {
				step((long)_sdl.getFormat().getFrameRate());
			}
		};

		public final Position _position = new Position();

		private Timer _updater = new Timer(50, new ActionListener() {
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
	}

	public static class View extends JPanel {
		public View(final Model model) {
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			final JLabel time = new JLabel("00:00:00.000");
			time.setFont(getFont().deriveFont(18f));
			time.setHorizontalAlignment(JLabel.TRAILING);
			add(time);
			JPanel controls = new JPanel(new GridLayout(0, 2, 5, 5));
			for (Action a : new Action[]{model._stop, model._play, model._back, model._forward}) {
				JButton b = new JButton(a);
				b.setText("");
				b.setFocusable(false);
				controls.add(b);
			}
			add(controls);
			add(Box.createVerticalGlue());

			final String format = "%02d:%02d:%02d.%03d";
			model.addListener(this, new Listener() {
				@Override
				public void trackChanged() {
				}
				@Override
				public void positionChanged(long frames) {
					int p = (int) model.asMillis(model.getPos());
					int millis = p % 1000;
					p /= 1000;
					int secs = p % 60;
					p /= 60;
					int mins = p % 60;
					p /= 60;
					time.setText(String.format(format, p, mins, secs, millis));
				}
			});
		}
	}

	public static class PositionView extends JSlider implements Listener {
		public PositionView(final Model model) {
			super(model._position);
			_model = model;
			model.addListener(this, this);
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

		private Model _model;
	}

	public static void main(String[] args) {
		try {
			File file = new File("/home/dominik/Freizeit/Music/Untitled002.wav");
			AudioInputStream ais = AudioSystem.getAudioInputStream(file);
			MemorySource sp = new SeekablePCMSource.MemorySource(ais);
			System.out.println("Length: " + sp.getLength() + "µs");
			System.gc();
			Model m = new Model();
			m.open(sp);
			JFrame f = new JFrame("Player (" + file.getName() + ")");
			Box view = new Box(BoxLayout.PAGE_AXIS);
			view.add(new View(m));
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
