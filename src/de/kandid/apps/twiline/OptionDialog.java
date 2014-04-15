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
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Spring;
import javax.swing.SpringLayout;

import de.kandid.ui.Action;
import de.kandid.ui.Keys;

public class OptionDialog extends JDialog {

	public OptionDialog(final Twiline.Model model) {
		JPanel panel = new JPanel(new BorderLayout());
		JPanel phrases = new JPanel(new SpringLayout());
		phrases.add(new JLabel(Messages.get("OptionDialog.Key"))); //$NON-NLS-1$
		phrases.add(new JLabel(Messages.get("OptionDialog.Phrase"))); //$NON-NLS-1$
		phrases.add(new JLabel(Messages.get("OptionDialog.Bold"))); //$NON-NLS-1$
		final ArrayList<Twiline.Phrase.Model> phraseModels = new ArrayList<>();
		for (int i = 0; i < model._value._phrases.length; ++i) {
			Twiline.Phrase.Model pm = new Twiline.Phrase.Model(model._value._phrases[i]);
			phrases.add(new JLabel("Alt-" + i));
			phrases.add(new JScrollPane(new JTextArea(pm._text, null, 2, 50)));
			phrases.add(new de.kandid.model.types.Boolean.View(pm._bold));
			phraseModels.add(pm);
		}
      makeCompactGrid(phrases, model._value._phrases.length + 1, 3, 0, 0, 5, 5);
      panel.add(phrases, BorderLayout.CENTER);

      JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
      buttons.add(new JButton(new Action(Messages.get("OptionDialog.Save"), Messages.get("OptionDialog.Save_long"), Keys.keys.c.get(KeyEvent.VK_ENTER)) { //$NON-NLS-1$ //$NON-NLS-2$
      	@Override
      	public void go() {
      		for (int i = 0; i < phraseModels.size(); ++i) {
      			Twiline.Phrase.Model pm = phraseModels.get(i);
      			model._value._phrases[i]._text = pm._text.getText();
      			model._value._phrases[i]._bold = pm._bold.getValue();
      		}
		      XmlIo.write(model.getValue());
		      setVisible(false);
	      }
      }.addKeysTo(panel)));
      buttons.add(new JButton(new Action(Messages.get("OptionDialog.Cancel"), Messages.get("OptionDialog.Cancel_long"), Keys.keys.get(KeyEvent.VK_ESCAPE)) { //$NON-NLS-1$ //$NON-NLS-2$
			@Override
			public void go() {
      		setVisible(false);
			}
		}.addKeysTo(panel)));
      panel.add(buttons, BorderLayout.SOUTH);
		add(panel);
      pack();
	}

	/**
	 * Aligns the first <code>rows</code> * <code>cols</code>
	 * components of <code>parent</code> in
	 * a grid. Each component in a column is as wide as the maximum
	 * preferred width of the components in that column;
	 * height is similarly determined for each row.
	 * The parent is made just big enough to fit them all.
	 *
	 * @param rows number of rows
	 * @param cols number of columns
	 * @param initialX x location to start the grid at
	 * @param initialY y location to start the grid at
	 * @param xPad x padding between cells
	 * @param yPad y padding between cells
	 */
	public static void makeCompactGrid(Container parent,
	                                   int rows, int cols,
	                                   int initialX, int initialY,
	                                   int xPad, int yPad) {
		SpringLayout layout;
		try {
			layout = (SpringLayout)parent.getLayout();
		} catch (ClassCastException exc) {
			System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
			return;
		}

		//Align all cells in each column and make them the same width.
		Spring x = Spring.constant(initialX);
		for (int c = 0; c < cols; c++) {
			Spring width = Spring.constant(0);
			for (int r = 0; r < rows; r++) {
				width = Spring.max(width,
						getConstraintsForCell(r, c, parent, cols).
								getWidth());
			}
			for (int r = 0; r < rows; r++) {
				SpringLayout.Constraints constraints =
						getConstraintsForCell(r, c, parent, cols);
				constraints.setX(x);
				constraints.setWidth(width);
			}
			x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
		}

		//Align all cells in each row and make them the same height.
		Spring y = Spring.constant(initialY);

		for (int r = 0; r < rows; r++) {
			Spring height = Spring.constant(0);
			for (int c = 0; c < cols; c++) {
				height = Spring.max(height,
						getConstraintsForCell(r, c, parent, cols).
								getHeight());
			}
			for (int c = 0; c < cols; c++) {
				SpringLayout.Constraints constraints =
						getConstraintsForCell(r, c, parent, cols);
				constraints.setY(y);
				constraints.setHeight(height);
			}
			y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
		}

		//Set the parent's size.
		SpringLayout.Constraints pCons = layout.getConstraints(parent);
		pCons.setConstraint(SpringLayout.SOUTH, y);
		pCons.setConstraint(SpringLayout.EAST, x);
	}

	/* Used by makeCompactGrid. */
	private static SpringLayout.Constraints getConstraintsForCell(
			int row, int col,
			Container parent,
			int cols) {
		SpringLayout layout = (SpringLayout) parent.getLayout();
		Component c = parent.getComponent(row * cols + col);
		return layout.getConstraints(c);
	}
}
