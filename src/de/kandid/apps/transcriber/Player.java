/*
 * Created on 02.03.2014
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.kandid.apps.transcriber;

import static de.kandid.ui.Keys.keys;
import static java.awt.event.KeyEvent.VK_F5;

import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.kandid.apps.transcriber.SeekablePCMSource.MemorySource;
import de.kandid.model.Condition;
import de.kandid.ui.Action;

public class Player {

	public enum State {Playing, Stopped}

	public static interface Listener {
		public void positionChanged(State state, long µs);
	}
	private enum Cmd {Done, Seek, Play, Stop}

	public static class Model extends de.kandid.model.Model.Abstract<Listener> {

		public Model() {
			super(Listener.class);
			_playLoop = new Thread(getClass().getName()) {
				@Override
				public void run() {
					playLoop();
				}
			};
			_cmd = Cmd.Stop;
			_playLoop.start();
			update();
		}

		public void setSource(SeekablePCMSource src) throws LineUnavailableException {
			synchronized (_playLoop) {
				close();
				_source = src;
				final AudioFormat af = _source.getAudioFormat();
				_sdl = AudioSystem.getSourceDataLine(af);
				_sdl.open(src.getAudioFormat());
				_buf = new byte[_sdl.getBufferSize()];
			}
			update();
		}

		public void close() {
			synchronized (_playLoop) {
				if (_sdl == null)
					return;
				_cmd = Cmd.Stop;
				_sdl.close();
				_sdl = null;
			}
			update();
		}

		public long asMillis(long frames) {
			synchronized (_playLoop) {
				if (_sdl == null)
					return 0;
				return (long)((double) frames / _sdl.getFormat().getFrameRate() * 1000);
			}
		}

		public long getPos() {
			synchronized (_playLoop) {
				if (_sdl == null)
					return 0;
				return _offset + _sdl.getLongFramePosition() - _sdlStart;
			}
		}

		private void playLoop() {
			synchronized (_playLoop) {
				try {
					for (;;) {
						switch (_cmd) {
							case Stop:
								_playLoop.wait();
								break;
							case Play:
								int needed = Math.min(_sdl.available(), _buf.length);
								int read = _source.read(_buf, 0, needed);
								if (read < 0) {
									_cmd = Cmd.Stop;
								} else {
									_sdl.write(_buf, 0, read);
								}
								_playLoop.wait(10);
								break;

							default:
						}
					}
				} catch (Exception e) {
				} finally {
					_playLoop = null;
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
		}

		public void play() {
			synchronized (_playLoop) {
				_cmd = Cmd.Play;
				_source.seek(getPos());
				_sdl.start();
				_playLoop.notify();
			}
		}

		public void pause() {
			synchronized (_playLoop) {
				_sdl.stop();
				_sdl.flush();
				_cmd = Cmd.Stop;
				_playLoop.notify();
			}
		}

		public void step(long frames) {
			seek(getPos() + frames);
		}

		private void update() {
			Condition trackLoaded = new Condition("No track loaded") { @Override public boolean isTrue() {
				return _sdl != null;
			}};
			trackLoaded.applyTo(_play, _stop, _back, _forward);
		}

		public final Action _play = new Action("F4", "media-playback-start.png", "Play the track", keys.get(KeyEvent.VK_F4), 0) {
			@Override
			public void go() {
				play();
			}
		};

		public final Action _stop = new Action("F1", "media-playback-pause.png", "Stop the track", keys.get(KeyEvent.VK_F1), 0) {
			@Override
			public void go() {
				pause();
			}
		};

		public final Action _back = new Action("F3", "media-seek-backward.png", "Go a little back", keys.get(KeyEvent.VK_F3), 0) {
			@Override
			public void go() {
				step((long)-_sdl.getFormat().getFrameRate());
			}
		};

		public final Action _forward = new Action("F5", "media-seek-forward.png", "Go a little forward", keys.get(VK_F5), 0) {
			@Override
			public void go() {
				step((long)_sdl.getFormat().getFrameRate());
			}
		};
		private final Action[] _actions = new Action[]{_play, _stop, _back, _forward};

		private volatile Cmd _cmd;
		private Thread _playLoop;
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
				b.setFocusable(false);
				controls.add(b);
			}
			add(controls);
			add(Box.createVerticalGlue());
			Thread t = new Thread(getClass().getName()) {
				@Override
				public void run() {
					final String format = "%02d:%02d:%02d.%03d";
					try {
						for (;;)	{
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
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
							Thread.sleep(10);
						}
					} catch (InterruptedException e) {
					}
				}
			};
			t.start();
		}
	}

	public static void main(String[] args) {
		try {
			File file = new File("/home/dominik/Freizeit/Music/processed02.wav");
			AudioInputStream ais = AudioSystem.getAudioInputStream(file);
			MemorySource sp = new SeekablePCMSource.MemorySource(ais);
			System.out.println("Length: " + sp.getLength() + "µs");
			System.gc();
			Model m = new Model();
			m.setSource(sp);
			JFrame f = new JFrame("Player (" + file.getName() + ")");
			f.getContentPane().add(new View(m));
			f.pack();
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.setLocation(500, 500);
			f.setVisible(true);
		} catch (Exception e) {
			System.out.println("Exception caught: " + e);
			e.printStackTrace();
		}
	}
}
