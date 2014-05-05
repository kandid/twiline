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
