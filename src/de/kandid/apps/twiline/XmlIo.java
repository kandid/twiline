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
import java.io.OutputStream;
import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.sun.xml.internal.ws.api.streaming.XMLStreamWriterFactory;

import de.kandid.xml.IndentStreamWriter;
import de.kandid.xml.XMLCursor;

/**
 *
 */
public class XmlIo {

	public static class Writer {
		public void write(IndentStreamWriter out, Twiline twiline) throws XMLStreamException {
			out.writeStartElement("twiline");
			out.writeAttribute("version", "0.1");
			write(out, twiline._phrases);
			for (Twiline.Phrase p : twiline._phrases)
				write(out, p);
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
			XMLCursor twiline = in.required("twiline");
			ArrayList<Twiline.Phrase> phrases = new ArrayList<>();
			for (XMLCursor p : twiline.required("phrases").multiple("phrase")) {
				Twiline.Phrase pa = new Twiline.Phrase();
				pa._bold = p.getBoolean("bold");
				pa._text = p.getText();
				phrases.add(pa);
			}
			return new Twiline(phrases.toArray(new Twiline.Phrase[phrases.size()]));
		}
	}

	public static void write(File file, Twiline twiline) throws XMLStreamException, FileNotFoundException {
		try (IndentStreamWriter out = new IndentStreamWriter(XMLStreamWriterFactory.create(new FileOutputStream(file)))) {
			new Writer().write(out, twiline);
		}
	}

	public static Twiline read(File file) throws XMLStreamException, FileNotFoundException {
		try (XMLCursor in = new XMLCursor(new FileInputStream(file))) {
			return new Reader().readTwiline(in);
		}
	}
}
