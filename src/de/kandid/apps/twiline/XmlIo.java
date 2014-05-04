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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;

import de.kandid.environment.Places;
import de.kandid.xml.IndentStreamWriter;
import de.kandid.xml.XMLCursor;

/**
 *
 */
public class XmlIo {

	public static class Writer {
		public void write(IndentStreamWriter out, Twiline twiline) throws XMLStreamException {
			out.writeStartElement("twiline");
			out.writeAttribute("version", "0.2");
			write(out, twiline._phrases);
			write(out, twiline._player);
			out.writeEndElement();
		}

		public void write(IndentStreamWriter out, Player player) throws XMLStreamException {
			out.writeStartElement("player");
			out.writeAttribute("seek-interval", Integer.toString(player._seekInterval));
			out.writeEndElement();
		}
		public void write(IndentStreamWriter out, Twiline.Phrase[] phrases) throws XMLStreamException {
			out.writeStartElement("phrases");
			for (Twiline.Phrase p : phrases)
				write(out, p);
			out.writeEndElement();
		}

		public void write(IndentStreamWriter out, Twiline.Phrase phrase) throws XMLStreamException {
			out.writeStartElement("phrase");
			out.writeAttribute("bold", Boolean.toString(phrase._bold));
			out.writeCharacters(phrase._text);
			out.writeEndElement();
		}
	}

	public static class Reader {

		public Twiline readTwiline(XMLCursor in) throws XMLStreamException {
			Twiline ret = new Twiline();
			XMLCursor twiline = in.required("twiline");
			ArrayList<Twiline.Phrase> phrases = new ArrayList<>();
			for (XMLCursor p : twiline.required("phrases").multiple("phrase")) {
				Twiline.Phrase pa = new Twiline.Phrase();
				pa._bold = p.getBoolean("bold");
				pa._text = p.getText();
				phrases.add(pa);
			}
			XMLCursor player = twiline.optional("player");
			if (player != null) {
				ret._player._seekInterval = player.getInt("seek-interval");
			}
			ret._phrases = phrases.toArray(new Twiline.Phrase[phrases.size()]);
			return ret;
		}
	}

	public static void write(Twiline twiline) {
		try {
			write(new File(Places.get().getConfigWrite("de.kandid.twiline"), "config.xml"), twiline);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public static void write(File file, Twiline twiline) throws XMLStreamException, FileNotFoundException {
		try (IndentStreamWriter out = new IndentStreamWriter(new FileOutputStream(file))) {
			new Writer().write(out, twiline);
		}
	}

	public static Twiline read(File file) throws XMLStreamException, FileNotFoundException {
		try (XMLCursor in = new XMLCursor(new FileInputStream(file))) {
			return new Reader().readTwiline(in);
		}
	}
}
