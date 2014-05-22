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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.kandid.apps.twiline.Editor.View.StyledTextAction;
import de.kandid.environment.Places;
import de.kandid.ui.Action;
import de.kandid.ui.Boolean;
import de.kandid.ui.ErrorDialog;
import de.kandid.ui.TextLineModel;

public class Twiline {

	public static final FileNameExtensionFilter _rtf = new FileNameExtensionFilter(Messages.get("Twiline.Filter.rtf"), "rtf");
	public static class Phrase {
		public static class Model {
			public Model(Phrase value) {
				_text.setText(value._text);
				_bold.setValue(value._bold);
			}
			public final TextLineModel _text = new TextLineModel();
			public final de.kandid.ui.Boolean.Model _bold = new de.kandid.ui.Boolean.Model();
		}

		public Phrase() {
		}

		public Phrase(String text, boolean bold) {
			_text = text;
			_bold = bold;
		}
		public String _text;
		public boolean _bold;
	}

	public static class Model {
		public Model(Twiline twiline) {
			_value = twiline;
			_player.setValue(_value._player);
			_appendExt.setValue(true);
		}

		public Twiline getValue() {
			_player.getValue();
			return _value;
		}

		public final Action _save = new Action(Messages.get("Twiline.SaveTranscript"), "document-save.png", Messages.get("Twiline.SaveTranscript_long"), "ctrl S") { //$NON-NLS-1$ //$NON-NLS-3$
			@Override
			public void go() {
				if (_file == null) {
					_saveAs.perform();
					return;
				}
				try {
					_text.write(new FileOutputStream(_file));
				} catch (Exception e) {
					Logger.getLogger(Twiline.class.getName()).log(Level.SEVERE, "Unable to save text", e);
				}
			}
		};

		public final Action _saveAs = new Action(Messages.get("Twiline.SaveAs"), "document-save-as.png", Messages.get("Twiline.SaveAs_long"), null) { //$NON-NLS-1$ //$NON-NLS-3$
			@Override
			public void go() {
				JFileChooser fc = new JFileChooser(_file) {
					@Override
					protected JDialog createDialog(Component parent) throws HeadlessException {
						JDialog ret = super.createDialog(parent);
						JPanel option = new JPanel(new FlowLayout(FlowLayout.LEADING));
						option.add(new Boolean.View(_appendExt));
						option.add(new JLabel(Messages.get("Twiline.SaveAs.AppendExt")));
						JPanel pane = new JPanel(new BorderLayout());
						pane.add(ret.getContentPane(), BorderLayout.CENTER);
						pane.add(option, BorderLayout.SOUTH);
						ret.setContentPane(pane);
						return ret;
					};
				};
				fc.setDialogTitle((String) getValue(NAME));
				fc.setFileFilter(_rtf);
				fc.setAcceptAllFileFilterUsed(true);
				fc.setSelectedFile(_file);
				if (fc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
					return;
				_file = fc.getSelectedFile();
				if (_appendExt.getValue() && !_file.toString().toLowerCase().endsWith(".rtf"))
					_file = new File(_file.toString() + ".rtf");
				_save.perform();
			}
		};

		public final Action _open = new Action(Messages.get("Twiline.Open"), "document-open.png", Messages.get("Twiline.Open_long"), "ctrl O") { //$NON-NLS-1$ //$NON-NLS-3$
			@Override
			public void go() {
				JFileChooser fc = new JFileChooser(_file);
				fc.setDialogTitle((String)getValue(NAME));
				fc.setFileFilter(_rtf);
				fc.setAcceptAllFileFilterUsed(true);
				if (fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
					return;
				_file = fc.getSelectedFile();
				try {
					_text.read(new FileInputStream(_file));
				} catch (Exception e) {
					Logger.getLogger(Twiline.class.getName()).log(Level.SEVERE, "Unable to read text", e);
				}
			}
		};

		public final Action _openAudio = new Action(Messages.get("Twiline.OpenAudio"), "document-open-data.png", Messages.get("Twiline.OpenAudio_long"), null) { //$NON-NLS-1$ //$NON-NLS-3$
			@Override
			public void go() {
				JFileChooser fc = new JFileChooser();
				fc.setDialogTitle((String)getValue(NAME));
				fc.setFileFilter(Player._wav);
				fc.setAcceptAllFileFilterUsed(true);
				if (fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
					return;
				try {
					SeekablePCMSource.PcmFile sp = new SeekablePCMSource.PcmFile(fc.getSelectedFile());
					_player.open(sp);
				} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
					Logger.getLogger(Twiline.class.getName()).log(Level.SEVERE, "Unable to open audio stream", e);
				}
			}
		};

		private Boolean.Model _appendExt = new Boolean.Model();
		public final Player.Model _player = new Player.Model();
		public final Editor.Model _text = new Editor.Model();
		public File _file;
		public Twiline _value;
	}

	public static class View extends JPanel {
		public View(final Model model) {
			super(new BorderLayout());

			JPanel player = new JPanel(new BorderLayout());
			player.add(new Player.View(model._player), BorderLayout.NORTH);
			add(player, BorderLayout.WEST);

			JPanel editor = new JPanel(new BorderLayout());
			_text = new Editor.View(model._text);
			_text.setPreferredSize(new Dimension(600, 600));
			editor.add(new JScrollPane(_text), BorderLayout.CENTER);
			JPanel editorControls = new JPanel(new FlowLayout(FlowLayout.LEADING));
			JToolBar tb = new JToolBar();
			Action.addToToolbar(tb, model._openAudio, model._open, model._save, model._saveAs);
			Action.addToToolbar(tb, null, _settings);
			Action.addToToolbar(tb, null, model._text._undo, model._text._redo, null);
			Action.addToToolbar(tb, model._text._edit);
			Action.addToToolbar(tb, null, _insertTimestamp);
			for (StyledTextAction style : _text._styles) {
				JToggleButton button = new JToggleButton(style);
				button.setModel(style._model);
            button.setHideActionText(true);
				tb.add(button);
			}
			editorControls.add(tb);
			for (Action a : new Action[]{
					model._player._stop, model._player._play, model._player._back, model._player._forward,
					model._text._undo, model._text._redo,
					_insertTimestamp
			})
				a.addKeysTo(_text);
			editor.add(editorControls, BorderLayout.NORTH);
			add(editor, BorderLayout.CENTER);
			add(new Player.PositionView(model._player), BorderLayout.SOUTH);
			_model = model;

			makePhraseActions(model, _text);
		}

		public View addToMenu(JMenuBar bar) {
			JMenu file = Action.addToMenu(new JMenu(Action.menu(Messages.get("Twiline.Menu.File"))),
					_model._open, _model._save, _model._saveAs, null, _model._openAudio,
					null, _settings
			);
			bar.add(file);

			JMenu edit = new JMenu(Action.menu(Messages.get("Twiline.Menu.Edit")));
			Action.addToMenu(edit, _model._text._edit);
			Action.addToMenu(edit, null, _model._text._undo, _model._text._redo, null);
			Action.addToMenu(edit, _text._styles);
			Action.addToMenu(edit, null, _insertTimestamp);
			bar.add(edit);

			JMenu player = Action.addToMenu(new JMenu(Action.menu(Messages.get("Twiline.Menu.Player"))),
					_model._player._play, _model._player._stop, _model._player._back, _model._player._forward
			);
			bar.add(player);
			return this;
		}

		private void makePhraseActions(final Model model, final Editor.View text) {
			for (int i = 0; i < model._value._phrases.length; ++i) {
				final int ii = i;
				(new Action(Messages.get("Twiline.Phrase") + i, Messages.get("Twiline.InsertPhrase") + i, "alt " + i) { //$NON-NLS-1$ //$NON-NLS-2$
					@Override
					public void go() {
						final Phrase phrase = model._value._phrases[ii];
						_text.insertText(phrase._text, phrase._bold ? _text._bold : _text._normal);
					}
				}).addKeysTo(text);
			}
		}

		public final Action _settings = new Action(Messages.get("Twiline.Settings"), "preferences.png", Messages.get("Twiline.Settings_long"), null) { //$NON-NLS-1$ //$NON-NLS-2$
			@Override
			public void go() {
				new OptionDialog(_model).setVisible(true);
			}
		};

		public final Action _insertTimestamp = new Action(Messages.get("Twiline.InsertTimestamp_s"), "insert-timestamp.png", Messages.get("Twiline.InsertTimestamp"), "ctrl T") {
			@Override
			public void go() {
				String ts = Player.formatTime((int)_model._player.asMillis(_model._player.getPos()));
				_text.insertText("#" + ts + "#", _text._normal);
			}
		};

		private final Model _model;
		private final Editor.View _text;
	}

	public Twiline() {
		this(new Phrase[] {
				new Phrase(Messages.get("Twiline.Init0"), true), //$NON-NLS-1$
				new Phrase(Messages.get("Twiline.Init1"), true), //$NON-NLS-1$
				new Phrase(Messages.get("Twiline.Init2"), true), //$NON-NLS-1$
				new Phrase(Messages.get("Twiline.Init3"), true), //$NON-NLS-1$
				new Phrase(Messages.get("Twiline.Init4"), true), //$NON-NLS-1$
				new Phrase(Messages.get("Twiline.Init5"), true), //$NON-NLS-1$
				new Phrase(Messages.get("Twiline.Init6"), true), //$NON-NLS-1$
				new Phrase(Messages.get("Twiline.Init7"), true) //$NON-NLS-1$
		});
	}

	public Twiline(Phrase[] phrases) {
		_phrases = phrases;
	}
	public Phrase[] _phrases;
	public Player _player = new Player();

	public static void main(String[] args) {
		Logger.getLogger("").addHandler(new ErrorDialog.Log());
		try {
			Twiline twiline = new Twiline();
			try {
				twiline = XmlIo.read(new File(Places.get().getConfigRead("de.kandid.twiline")[0], "config.xml"));
			} catch (Exception e) {
				// Nothing to do. Use the default
				Logger.getLogger(Twiline.class.getName()).info("No user configuration found");
			}
			final Model m = new Model(twiline);
			final JFrame f = new JFrame(Messages.get("Twiline.Title")); //$NON-NLS-1$
			f.getContentPane().setLayout(new BorderLayout());
			JMenuBar bar = new JMenuBar();
			f.getContentPane().add(bar, BorderLayout.NORTH);
			f.getContentPane().add(new View(m).addToMenu(bar), BorderLayout.CENTER);
			bar.add(Action.addToMenu(new JMenu(Action.menu(Messages.get("Twiline.Menu.Help"))), new Action(Messages.get("Twiline.Menu.Help.About"), null, null) {
				@Override
				public void go() {
					new About(f).setVisible(true);
				}
			}));
			f.pack();
			f.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					XmlIo.write(m.getValue());
					System.exit(0);
				}
			});
			f.setVisible(true);
		} catch (Exception e) {
			Logger.getLogger(Twiline.class.getName()).log(Level.SEVERE, "Exception in main", e);
		}
	}
}
