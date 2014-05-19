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
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
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

import de.kandid.ui.Action;
import de.kandid.ui.Condition;

public class Editor {

	public static class Model {

		public Model() {
			_edit = new Action[] {
					def(DefaultEditorKit.cutAction, "edit-cut.png", "ctrl X"), //$NON-NLS-1$
					def(DefaultEditorKit.copyAction, "edit-copy.png", "ctrl C"), //$NON-NLS-1$
					def(DefaultEditorKit.pasteAction, "edit-paste.png", "ctrl V") //$NON-NLS-1$
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

		public void read(InputStream in) throws IOException, BadLocationException {
			_doc.remove(0, _doc.getLength());
			_kit.read(in, _doc, 0);
		}

		public void write(OutputStream out) throws IOException, BadLocationException {
			_kit.write(out, _doc, 0, _doc.getLength());
		}

		private Action def(String name, String icon, String key) {
			for (final javax.swing.Action a : _kit.getActions()) {
				if (a.getValue(Action.NAME).equals(name)) {
					String msgId = "Editor." + name;
					return new Action(Messages.get(msgId), icon, Messages.get(msgId + "_long"), key) {
						@Override
						public void go() {
							a.actionPerformed(null);
						}
					};
				}
			}
			throw new RuntimeException("Unknown action: " + name); //$NON-NLS-1$
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

		public final Action _undo = new Action(Messages.get("Editor.Undo"), "edit-undo.png", Messages.get("Editor.Undo_long"), "ctrl Z") { //$NON-NLS-1$ //$NON-NLS-3$
			@Override
			public void go() {
				_undos.undo();
				update();
			}
		};

		public final Action _redo = new Action(Messages.get("Editor.Redo"), "edit-redo.png", Messages.get("Editor.Redo_long"), "ctrl shift Z") { //$NON-NLS-1$ //$NON-NLS-3$
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

		public class StyledTextAction extends Action {
			
			public StyledTextAction(Object attr, String name, String icon, String description, String keyStroke) {
				super(name, icon, description, keyStroke);
				_attr = attr;
			}
			
			@Override
			public void go() {
            StyledEditorKit kit = getEditorKit();
            MutableAttributeSet attr = kit.getInputAttributes();
            Boolean b = (Boolean) attr.getAttribute(_attr);
            if (b == null)
            	b = Boolean.FALSE;
            SimpleAttributeSet sas = new SimpleAttributeSet();
            sas.addAttribute(_attr, Boolean.valueOf(!b.booleanValue()));
            int start = View.this.getSelectionStart();
				int end = View.this.getSelectionEnd();
				if (start != end) {
					StyledDocument doc = View.this.getDocument();
					doc.setCharacterAttributes(start, end - start, sas, false);
				}
				attr.addAttributes(sas);
            requestFocusInWindow();
			}

			private final Object _attr;
		}

		public View(Model model) {
			super(model._doc);
			_faces = new Action[] {
				new StyledTextAction(StyleConstants.Bold, Messages.get("Editor.Bold"), "format-text-bold.png", Messages.get("Editor.Bold_long"), "ctrl B"), //$NON-NLS-1$ //$NON-NLS-3$
				new StyledTextAction(StyleConstants.Italic, Messages.get("Editor.Italic"), "format-text-italic.png", Messages.get("Editor.Italic_long"), "ctrl I"), //$NON-NLS-1$ //$NON-NLS-3$
				new StyledTextAction(StyleConstants.Underline, Messages.get("Editor.Underline"), "format-text-underline.png", Messages.get("Editor.Underline_long"), "ctrl U") //$NON-NLS-1$ //$NON-NLS-3$
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
			} catch (BadLocationException ignored) {
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
