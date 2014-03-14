/*
 * Created on 10.03.2014
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.kandid.apps.transcriber;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import de.kandid.apps.transcriber.SeekablePCMSource.MemorySource;
import de.kandid.model.TextLineModel;
import de.kandid.ui.Action;
import de.kandid.ui.Keys;

public class Transcriber {
	public static interface Listener {

	}

	public static class Phrase {
		public static class Model {
			public Model(Phrase value) {
				_text.setText(value._text);
				_bold.setValue(value._bold);
			}
			public final TextLineModel _text = new TextLineModel();
			public final de.kandid.model.types.Boolean.Model _bold = new de.kandid.model.types.Boolean.Model();
		}

		public Phrase(String text, boolean bold) {
			_text = text;
			_bold = bold;
		}
		public String _text;
		public boolean _bold;
	}

	public static class Model extends de.kandid.model.Model.Abstract<Listener> {
		public Model() {
			super(Listener.class);
		}

		public final Phrase[] _phrases = new Phrase[] {
			new Phrase(Messages.get("Transcriber.Init0"), true), //$NON-NLS-1$
			new Phrase(Messages.get("Transcriber.Init1"), true), //$NON-NLS-1$
			new Phrase(Messages.get("Transcriber.Init2"), true), //$NON-NLS-1$
			new Phrase(Messages.get("Transcriber.Init3"), true), //$NON-NLS-1$
			new Phrase(Messages.get("Transcriber.Init4"), true), //$NON-NLS-1$
			new Phrase(Messages.get("Transcriber.Init5"), true), //$NON-NLS-1$
			new Phrase(Messages.get("Transcriber.Init6"), true), //$NON-NLS-1$
			new Phrase(Messages.get("Transcriber.Init7"), true) //$NON-NLS-1$
		};

		public final Action _save = new Action(Messages.get("Transcriber.SaveTranscript"), "document-save.png", Messages.get("Transcriber.SaveTranscript_long"), Keys.keys.c.get(KeyEvent.VK_S), 0) { //$NON-NLS-1$ //$NON-NLS-3$
			@Override
			public void go() {
				if (_file == null) {
					_saveAs.perform();
					return;
				}
				try {
					_text.write(new FileOutputStream(_file));
				} catch (Exception e) {
					//TODO
					e.printStackTrace();
				}
			}
		};
		public final Action _saveAs = new Action(Messages.get("Transcriber.SaveAs"), "document-save-as.png", Messages.get("Transcriber.SaveAs_long"), null, 0) { //$NON-NLS-1$ //$NON-NLS-3$
			@Override
			public void go() {
				JFileChooser fc = new JFileChooser(_file);
				if (fc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
					return;
				_file = fc.getSelectedFile();
				_save.perform();
			}
		};
		public final Action _open = new Action(Messages.get("Transcriber.Open"), "document-open.png", Messages.get("Transcriber.Open_long"), Keys.keys.c.get(KeyEvent.VK_O), 0) { //$NON-NLS-1$ //$NON-NLS-3$
			@Override
			public void go() {
				JFileChooser fc = new JFileChooser(_file);
				if (fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
					return;
				_file = fc.getSelectedFile();
				try {
					_text.read(new FileInputStream(_file));
				} catch (Exception e) {
					//TODO
					e.printStackTrace();
				}
			}
		};

		public final Player.Model _player = new Player.Model();
		public final Editor.Model _text = new Editor.Model();
		public File _file;
	}

	public static class View extends JPanel {
		public View(final Model model) {
			super(new BorderLayout());

			JPanel player = new JPanel(new BorderLayout());
			player.add(new Player.View(model._player), BorderLayout.NORTH);
			add(player, BorderLayout.WEST);

			JPanel editor = new JPanel(new BorderLayout());
			final Editor.View text = new Editor.View(model._text);
			text.setPreferredSize(new Dimension(600, 600));
			editor.add(new JScrollPane(text), BorderLayout.CENTER);
			JPanel editorControls = new JPanel(new FlowLayout(FlowLayout.LEADING));
			editorControls.add(Action.addToToolbar(new JToolBar(), _settings));
			editorControls.add(Action.addToToolbar(new JToolBar(), _openAudio, model._open, model._save, model._saveAs));
			editorControls.add(Action.addToToolbar(new JToolBar(), model._text._undo, model._text._redo));
			editorControls.add(Action.addToToolbar(new JToolBar(), model._text._edit));
			JToolBar tb = new JToolBar();
			for (Action a : text._faces) {
				JToggleButton b = new JToggleButton(a);
				b.setText("");
				tb.add(b);
			}
			editorControls.add(tb);
			for (Action a : new Action[]{
					model._player._stop, model._player._play, model._player._back, model._player._forward,
					model._text._undo, model._text._redo
			})
				a.addKeysTo(text);
			editor.add(editorControls, BorderLayout.NORTH);
			add(editor, BorderLayout.CENTER);
			_model = model;

			final SimpleAttributeSet normal = new SimpleAttributeSet();
			normal.addAttribute(StyleConstants.Bold, Boolean.FALSE);
			final SimpleAttributeSet bold = new SimpleAttributeSet();
			bold.addAttribute(StyleConstants.Bold, Boolean.TRUE);
			for (int i = 0; i < model._phrases.length; ++i) {
				final int ii = i;
				KeyStroke ks = KeyStroke.getKeyStroke("alt " + i);
				(new Action(Messages.get("Transcriber.Phrase") + i, Messages.get("Transcriber.InsertPhrase") + i, ks, 0) { //$NON-NLS-1$ //$NON-NLS-2$
					@Override
					public void go() {
						try {
							int offs = text.getCaret().getDot();
							final Phrase phrase = model._phrases[ii];
							model._text._doc.insertString(offs, phrase._text, phrase._bold ? bold : normal);
							text.getEditorKit().getInputAttributes().addAttributes(normal);
						} catch (BadLocationException e) {
						}
					}
				}).addKeysTo(text);
			}
		}

		public final Action _openAudio = new Action(Messages.get("Transcriber.OpenAudio"), "document-open-data.png", Messages.get("Transcriber.OpenAudio_long"), null, 0) { //$NON-NLS-1$ //$NON-NLS-3$
			@Override
			public void go() {
				JFileChooser fc = new JFileChooser();
				if (fc.showOpenDialog(View.this) != JFileChooser.APPROVE_OPTION)
					return;
				try {
					AudioInputStream ais = AudioSystem.getAudioInputStream(fc.getSelectedFile());
					MemorySource sp = new SeekablePCMSource.MemorySource(ais);
					_model._player.setSource(sp);
				} catch (UnsupportedAudioFileException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (LineUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		public final Action _settings = new Action(Messages.get("Transcriber.Settings"), Messages.get("Transcriber.Settings_long"), null, 0) { //$NON-NLS-1$ //$NON-NLS-2$
			@Override
			public void go() {
				new OptionDialog(_model).setVisible(true);
			}
		};

		private final Model _model;
	}

	public static void main(String[] args) {
		try {
			Model m = new Model();
			JFrame f = new JFrame(Messages.get("Transcriber.Title")); //$NON-NLS-1$
			f.getContentPane().add(new View(m));
			f.pack();
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.setVisible(true);;
		} catch (Exception e) {
			System.out.println("Exception caught: " + e);
			e.printStackTrace();
		}
	}
}
