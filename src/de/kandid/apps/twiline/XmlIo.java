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
		try (XMLCursor in = new XMLCursor(new FileInputStream(file), file.toString())) {
			return new Reader().readTwiline(in);
		}
	}
}
