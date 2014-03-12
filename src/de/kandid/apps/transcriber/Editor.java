/*
 * Created on 10.03.2014
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.kandid.apps.transcriber;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;

import de.kandid.ui.Action;
import de.kandid.ui.Keys;
import de.kandid.util.KandidException;

public class Editor {
	public static class View extends JTextPane {

		public static abstract class StyledTextAction extends Action {
			public StyledTextAction(String name, String icon, String description, KeyStroke keyStroke, int mnemonic) {
				super(name, icon, description, keyStroke, mnemonic);
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

		public View(StyledDocument sd) {
			super(sd);
			sd.addUndoableEditListener(new UndoableEditListener() {
				@Override
				public void undoableEditHappened(UndoableEditEvent e) {

				}
			});
			_edit = new Action[] {
				def(DefaultEditorKit.cutAction, this, "edit-cut.png"),
				def(DefaultEditorKit.copyAction, this, "edit-copy.png"),
				def(DefaultEditorKit.pasteAction, this, "edit-paste.png")
			};
			_faces = new Action[] {
				new StyledTextAction("Bold", "format-text-bold.png", "Toggle boldface", Keys.keys.c.get(KeyEvent.VK_B), 0) {
					@Override
					public void go() {
	                StyledEditorKit kit = getEditorKit();
	                MutableAttributeSet attr = kit.getInputAttributes();
	                boolean bold = (StyleConstants.isBold(attr)) ? false : true;
	                SimpleAttributeSet sas = new SimpleAttributeSet();
	                StyleConstants.setBold(sas, bold);
	                setCharacterAttributes(View.this, sas, false);
	                requestFocusInWindow();
					}
				},
				new StyledTextAction("Italic", "format-text-italic.png", "Toggle italic", Keys.keys.c.get(KeyEvent.VK_I), 0) {
					@Override
					public void go() {
	                StyledEditorKit kit = getEditorKit();
	                MutableAttributeSet attr = kit.getInputAttributes();
	                boolean italic = (StyleConstants.isItalic(attr)) ? false : true;
	                SimpleAttributeSet sas = new SimpleAttributeSet();
	                StyleConstants.setItalic(sas, italic);
	                setCharacterAttributes(View.this, sas, false);
	                requestFocusInWindow();
					}
				},
				new StyledTextAction("Underline", "format-text-underline.png", "Toggle underline", Keys.keys.c.get(KeyEvent.VK_U), 0) {
					@Override
					public void go() {
	                StyledEditorKit kit = getEditorKit();
	                MutableAttributeSet attr = kit.getInputAttributes();
	                boolean underline = (StyleConstants.isUnderline(attr)) ? false : true;
	                SimpleAttributeSet sas = new SimpleAttributeSet();
	                StyleConstants.setUnderline(sas, underline);
	                setCharacterAttributes(View.this, sas, false);
	                requestFocusInWindow();
					}
				}
			};
			setPreferredSize(new Dimension(500, 500));
			for (Action[] group : new Action[][]{_edit, _faces})
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

		private Action def(String name, JTextPane tp, String icon) {
			for (final javax.swing.Action a : tp.getUI().getEditorKit(tp).getActions()) {
				if (a.getValue(Action.NAME).equals(name)) {
					return new Action(name, icon, name, null, 0) {
						@Override
						public void go() {
							a.actionPerformed(null);
						}
					};
				}
			}
			throw new KandidException("Unknown action: " + name);
		}

		public final Action[] _edit;
		public final Action[] _faces;
	}

	public static void main(String[] args) {
		try {
			DefaultStyledDocument sd = new DefaultStyledDocument();
			JFrame f = new JFrame("EditorTest");
			f.getContentPane().setLayout(new BorderLayout());
			final View view = new View(sd);
			JToolBar toolbar = new JToolBar();
			Action.addToToolbar(toolbar, view._edit);
			toolbar.addSeparator();
			Action.addToToolbar(toolbar, view._faces);
			f.getContentPane().add(view, BorderLayout.CENTER);
			f.getContentPane().add(toolbar, BorderLayout.NORTH);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.pack();
			f.setVisible(true);
		} catch (Exception e) {
			System.out.println("Exception caught: " + e);
			e.printStackTrace();
		}
	}
}
