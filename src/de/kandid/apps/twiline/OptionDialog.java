/*
 * Created on 11.03.2014
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
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
import javax.swing.JTextField;

import de.kandid.ui.Action;
import de.kandid.ui.Keys;
import de.kandid.ui.swing.SpringLayout;
import de.kandid.ui.swing.SpringUtilities;

public class OptionDialog extends JDialog {

	public OptionDialog(final Twiline.Model model) {
		setLayout(new BorderLayout());
		JPanel phrases = new JPanel(new SpringLayout());
		phrases.add(new JLabel(Messages.get("OptionDialog.Key"))); //$NON-NLS-1$
		phrases.add(new JLabel(Messages.get("OptionDialog.Phrase"))); //$NON-NLS-1$
		phrases.add(new JLabel(Messages.get("OptionDialog.Bold"))); //$NON-NLS-1$
		final ArrayList<Twiline.Phrase.Model> phraseModels = new ArrayList<>();
		for (int i = 0; i < model._phrases.length; ++i) {
			Twiline.Phrase.Model pm = new Twiline.Phrase.Model(model._phrases[i]);
			phrases.add(new JLabel("Alt-" + i));
			phrases.add(new JTextField(pm._text, null, 50));
			phrases.add(new de.kandid.model.types.Boolean.View(pm._bold));
			phraseModels.add(pm);
		}
      SpringUtilities.makeCompactGrid(phrases, model._phrases.length + 1, 3, 0, 0, 5, 5);
      add(phrases, BorderLayout.CENTER);

      JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
      buttons.add(new JButton(new Action(Messages.get("OptionDialog.Save"), Messages.get("OptionDialog.Save_long"), Keys.keys.c.get(KeyEvent.VK_ENTER), 0) { //$NON-NLS-1$ //$NON-NLS-2$
      	@Override
      	public void go() {
      		for (int i = 0; i < phraseModels.size(); ++i) {
      			Twiline.Phrase.Model pm = phraseModels.get(i);
      			model._phrases[i]._text = pm._text.getText();
      			model._phrases[i]._bold = pm._bold.getValue();
      			setVisible(false);
      		}
      	}
      }));
      buttons.add(new JButton(new Action(Messages.get("OptionDialog.Cancel"), Messages.get("OptionDialog.Cancel_long"), Keys.keys.get(KeyEvent.VK_ESCAPE), 0) { //$NON-NLS-1$ //$NON-NLS-2$
			@Override
			public void go() {
      		setVisible(false);
			}
		}));
      add(buttons, BorderLayout.SOUTH);

      pack();
	}
}
