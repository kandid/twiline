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

package de.kandid.ui;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

public abstract class Action extends AbstractAction {

	private static class Mnemonic {

		private static Pattern _mnemonicFinder = Pattern.compile("_._");

		public Mnemonic(String name) {
			Matcher m = _mnemonicFinder.matcher(name);
			if (m.find()) {
				_char = name.charAt(m.start() + 1);
				_index = m.start();
				_name = name.substring(0, m.start()) + (char) _char + name.substring(m.end());
			} else {
				_name = name;
				_index = -1;
			}
		}
		public Mnemonic(String name, int mnemonic, int index) {
			_name = name;
			_char = mnemonic;
			_index = index;
		}
		public String _name;
		public int _char;
		public int _index;
	}

	private static KeyStroke asKs(String s) {
		if (s == null)
			return null;
		KeyStroke ret = KeyStroke.getKeyStroke(s);
   	if (ret == null)
   		throw new RuntimeException("Could not resolve key stroke '" + s + "'");
   	return ret;
	}

   public Action(String name, String description, String keyStroke) {
   	init(new Mnemonic(name), (Icon) null, description, asKs(keyStroke));
   }

   public Action(String name, Icon icon, String description, KeyStroke keyStroke) {
   	init(new Mnemonic(name), icon, description, keyStroke);
   }

	public Action(String name, String iconName, String description, String keyStroke) {
		init(new Mnemonic(name), makeIcon(iconName), description, asKs(keyStroke));
	}

	public Action(String name, Icon icon, String description, KeyStroke keyStroke, int mnemonic, int mnemonicIndex) {
     	init(name, icon, description, keyStroke, mnemonic, mnemonicIndex);
   }

	private void init(Mnemonic mn, Icon icon, String description, KeyStroke keyStroke) {
		init(mn._name, icon, description, keyStroke, mn._char, mn._index);
	}
   private void init(String name, Icon icon, String description, KeyStroke keyStroke, int mnemonic, int mnemonicIndex) {
      putValue(NAME, name);
      putValue(SMALL_ICON, icon);
      putValue(SHORT_DESCRIPTION, _description = description);
      putValue(ACCELERATOR_KEY, keyStroke);
	   if (mnemonic != 0)
         putValue(MNEMONIC_KEY, new Integer(mnemonic));
	   if (mnemonicIndex >= 0)
		   putValue(DISPLAYED_MNEMONIC_INDEX_KEY, new Integer(mnemonicIndex));
   }

	public void actionPerformed(ActionEvent e) {
	   perform();
	}

   public void perform() {
		if (!enabled)
			return;
		go();
	}

	public Action addKeysTo(JComponent c) {
      final KeyStroke keyStroke = (KeyStroke)getValue(Action.ACCELERATOR_KEY);
      c.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyStroke, this);
      c.getInputMap(JComponent.WHEN_FOCUSED).put(keyStroke, this);
      c.getActionMap().put(this, this);
		return this;
	}

	@Override
	public void setEnabled(boolean newValue) {
		//System.out.println("" + getValue(NAME) + ": setting to " + newValue);
		putValue(SHORT_DESCRIPTION, _description);
		super.setEnabled(newValue);
	}

	public void setEnabled(boolean newValue, String disabledReason) {
		if (newValue) {
			setEnabled(true);
			return;
		}
		putValue(SHORT_DESCRIPTION, "<html>" + _description + "<br/>(Disabled: " + disabledReason + ")</html>");
		super.setEnabled(false);
	}

	public ImageIcon getIcon() {
	   return (ImageIcon) getValue(SMALL_ICON);
	}

	public abstract void go();

	private ImageIcon makeIcon(String name) {
		if (name != null) {
			try {
				return new ImageIcon(ImageIO.read(getClass().getResourceAsStream(name)));
			} catch (IOException e) {
				//TODO
			}
		}
		return null;
	}

	public static Action menu(String name) {
		return new Action(name, null, null) {
			@Override
			public void go() {
				super.actionPerformed(null);
			}
		};
	}

	public static void addTo(JMenu menu, JToolBar tb, javax.swing.Action... actions) {
	   if (menu != null)
	      addToMenu(menu, actions);
	   if (tb != null)
	      addToToolbar(tb, actions);
	}

   public static JMenu addToMenu(JMenu menu, javax.swing.Action... actions) {
      for (javax.swing.Action a : actions) {
         if (a == null)
            menu.addSeparator();
         else
            menu.add(a);
      }
      return menu;
   }

	public static JToolBar addToToolbar(JToolBar tb, javax.swing.Action... actions) {
	   if (tb == null)
	      tb = new JToolBar();
	   for (javax.swing.Action a : actions) {
	      if (a == null)
	         tb.addSeparator();
	      else
	         tb.add(a);
	   }
	   return tb;
	}

	private String _description;
}