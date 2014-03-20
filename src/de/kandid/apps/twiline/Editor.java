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
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.rtf.RTFEditorKit;
import javax.swing.undo.UndoManager;

import de.kandid.model.Condition;
import de.kandid.ui.Action;
import de.kandid.ui.Keys;
import de.kandid.util.KandidException;

public class Editor {
	public static interface Listener {

	}

	public static class Model extends de.kandid.model.Model.Abstract<Listener> {

		public Model() {
			super(Listener.class);
			_edit = new Action[] {
					def(DefaultEditorKit.cutAction, "edit-cut.png", Keys.keys.c.get(KeyEvent.VK_X)), //$NON-NLS-1$
					def(DefaultEditorKit.copyAction, "edit-copy.png", Keys.keys.c.get(KeyEvent.VK_C)), //$NON-NLS-1$
					def(DefaultEditorKit.pasteAction, "edit-paste.png", Keys.keys.c.get(KeyEvent.VK_V)) //$NON-NLS-1$
				};
			_doc.addUndoableEditListener(new UndoableEditListener() {
				@Override
				public void undoableEditHappened(UndoableEditEvent e) {
					_undos.addEdit(e.getEdit());
					update();
				}
			});
			update();
		}

		public void read(InputStream in) throws IOException {
			try {
				_doc.remove(0, _doc.getLength());
				_kit.read(in, _doc, 0);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void write(OutputStream out) throws IOException {
			try {
				_kit.write(out, _doc, 0, _doc.getLength());
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private Action def(String name, String icon, KeyStroke key) {
			for (final javax.swing.Action a : _kit.getActions()) {
				if (a.getValue(Action.NAME).equals(name)) {
					return new Action(Messages.get("Editor." + name), icon, name, key, 0) {
						@Override
						public void go() {
							a.actionPerformed(null);
						}
					};
				}
			}
			throw new KandidException("Unknown action: " + name); //$NON-NLS-1$
		}

		private void update() {
			new Condition(Messages.get("Editor.NothingToUndo")) {@Override public boolean isTrue() { //$NON-NLS-1$
				return _undos.canUndo();
			}}.applyTo(_undo);
			new Condition(Messages.get("Editor.NothingToRedo")) {@Override public boolean isTrue() { //$NON-NLS-1$
				return _undos.canRedo();
			}}.applyTo(_redo);
		}

		public final Action[] _edit;

		public final Action _undo = new Action(Messages.get("Editor.Undo"), "edit-undo.png", Messages.get("Editor.Undo_long"), Keys.keys.c.get(KeyEvent.VK_Z), 0) { //$NON-NLS-1$ //$NON-NLS-3$
			@Override
			public void go() {
				_undos.undo();
				update();
			}
		};

		public final Action _redo = new Action(Messages.get("Editor.Redo"), "edit-redo.png", Messages.get("Editor.Redo_long"), Keys.keys.c.s.get(KeyEvent.VK_Z), 0) { //$NON-NLS-1$ //$NON-NLS-3$
			@Override
			public void go() {
				_undos.redo();
				update();
			}
		};

		public final UndoManager _undos = new UndoManager();
		private final StyledEditorKit _kit = new RTFEditorKit();
		public final DefaultStyledDocument _doc = new DefaultStyledDocument();
	}
	public static class View extends JTextPane {

		public static abstract class StyledTextAction extends Action {
			public StyledTextAction(String name, String icon, String description, KeyStroke keyStroke) {
				super(name, icon, description, keyStroke, 0);
			}

			/**
			 * Applies the given attributes to character content. If there is a
			 * selection, the attributes are applied to the selection range. If
			 * there is no selection, the attributes are applied to the input
			 * attribute set which defines the attributes for any new text that
			 * gets inserted.
			 * @param editor  the editor
			 * @param attr    the attributes
			 * @param replace if true, then replace the existing attributes first
			 */
			protected final void setCharacterAttributes(View editor, AttributeSet attr, boolean replace) {
				int p0 = editor.getSelectionStart();
				int p1 = editor.getSelectionEnd();
				if (p0 != p1) {
					StyledDocument doc = editor.getDocument();
					doc.setCharacterAttributes(p0, p1 - p0, attr, replace);
				}
				StyledEditorKit k = editor.getEditorKit();
				MutableAttributeSet inputAttributes = k.getInputAttributes();
				if (replace) {
					inputAttributes.removeAttributes(inputAttributes);
				}
				inputAttributes.addAttributes(attr);
			}
		}

		public View(Model model) {
			super(model._doc);
			_faces = new Action[] {
				new StyledTextAction(Messages.get("Editor.Bold"), "format-text-bold.png", Messages.get("Editor.Bold_long"), Keys.keys.c.get(KeyEvent.VK_B)) { //$NON-NLS-1$ //$NON-NLS-3$
					@Override
					public void go() {
	                StyledEditorKit kit = getEditorKit();
	                MutableAttributeSet attr = kit.getInputAttributes();
	                boolean bold = !javax.swing.text.StyleConstants.isBold(attr);
	                SimpleAttributeSet sas = new SimpleAttributeSet();
	                StyleConstants.setBold(sas, bold);
	                setCharacterAttributes(View.this, sas, false);
	                requestFocusInWindow();
					}
				},
				new StyledTextAction(Messages.get("Editor.Italic"), "format-text-italic.png", Messages.get("Editor.Italic_long"), Keys.keys.c.get(KeyEvent.VK_I)) { //$NON-NLS-1$ //$NON-NLS-3$
					@Override
					public void go() {
	                StyledEditorKit kit = getEditorKit();
	                MutableAttributeSet attr = kit.getInputAttributes();
	                boolean italic = !javax.swing.text.StyleConstants.isItalic(attr);
	                SimpleAttributeSet sas = new SimpleAttributeSet();
	                StyleConstants.setItalic(sas, italic);
	                setCharacterAttributes(View.this, sas, false);
	                requestFocusInWindow();
					}
				},
				new StyledTextAction(Messages.get("Editor.Underline"), "format-text-underline.png", Messages.get("Editor.Underline_long"), Keys.keys.c.get(KeyEvent.VK_U)) { //$NON-NLS-1$ //$NON-NLS-3$
					@Override
					public void go() {
	                StyledEditorKit kit = getEditorKit();
	                MutableAttributeSet attr = kit.getInputAttributes();
	                boolean underline = !javax.swing.text.StyleConstants.isUnderline(attr);
	                SimpleAttributeSet sas = new SimpleAttributeSet();
	                StyleConstants.setUnderline(sas, underline);
	                setCharacterAttributes(View.this, sas, false);
	                requestFocusInWindow();
					}
				}
			};
			setPreferredSize(new Dimension(500, 500));
			for (Action[] group : new Action[][]{model._edit, _faces})
				for (Action a : group)
					a.addKeysTo(this);
		}

		@Override
		public StyledEditorKit getEditorKit() {
			return (StyledEditorKit) super.getEditorKit();
		}

		@Override
		public StyledDocument getDocument() {
			return (StyledDocument) super.getDocument();
		}

		public void insertText(String text, AttributeSet attributes) {
			try {
				int offs = getCaret().getDot();
				getDocument().insertString(offs, text, attributes);
				getEditorKit().getInputAttributes().addAttributes(_normal);
			} catch (BadLocationException e) {
			}
		}

		public final Action[] _faces;

		public final SimpleAttributeSet _normal = new SimpleAttributeSet() {
			{
				addAttribute(StyleConstants.Bold, Boolean.FALSE);
			}
		};

		public final SimpleAttributeSet _bold = new SimpleAttributeSet() {
			{
				addAttribute(StyleConstants.Bold, Boolean.TRUE);
			}
		};

	}

	public static void main(String[] args) {
		try {
			JFrame f = new JFrame("EditorTest");
			f.getContentPane().setLayout(new BorderLayout());
			Model model = new Model();
			final View view = new View(model);
			JToolBar toolbar = new JToolBar();
			Action.addToToolbar(toolbar, model._edit);
			toolbar.addSeparator();
			Action.addToToolbar(toolbar, view._faces);
			f.getContentPane().add(view, BorderLayout.CENTER);
			f.getContentPane().add(toolbar, BorderLayout.NORTH);
			f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			f.pack();
			f.setVisible(true);
		} catch (Exception e) {
			System.out.println("Exception caught: " + e);
			e.printStackTrace();
		}
	}
}
