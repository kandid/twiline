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
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import de.kandid.ui.Action;
import de.kandid.ui.Keys;
import de.kandid.ui.swing.SpringLayout;
import de.kandid.ui.swing.SpringUtilities;

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
      SpringUtilities.makeCompactGrid(phrases, model._value._phrases.length + 1, 3, 0, 0, 5, 5);
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
}
