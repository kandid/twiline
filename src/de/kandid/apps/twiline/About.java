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

import java.awt.Desktop;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class About extends JDialog {

	public About(JFrame parent) {
		super(parent);
		JEditorPane text = new JEditorPane();
		text.setContentType("text/html; charset=UTF-8");
		text.setEditable(false);
		String content = "Sorry! Couldn't load the text to display";
		try (Scanner sc = new Scanner(getClass().getResourceAsStream("about.html"), "UTF-8")) {
			content = sc.useDelimiter("\\A").next();
		}
		text.setText(content);
		text.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (IOException | URISyntaxException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		int width = 400;
		text.setSize(width, Integer.MAX_VALUE);
		Dimension prefSize = text.getPreferredSize();
		text.setPreferredSize(new Dimension(width, prefSize.height));
		text.setBackground(getBackground());
		text.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		setContentPane(new JScrollPane(text));
		pack();
	}

	public static void main(String[] args) {
		try {
			About a = new About(null);
			a.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			a.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
