/*
 * (C) Copyright 2009, by Dominikus Diesch.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Created on 28.11.2006
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.kandid.ui;

import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class TextLineModel extends PlainDocument {

   public TextLineModel() {
      super();
   }

   public TextLineModel(String text) {
   	setText(text);
   }

   public TextLineModel(Content c) {
      super(c);
   }

   public String getText() {
      try {
         return getText(0, getLength());
      } catch (BadLocationException e) {
         UIManager.getLookAndFeel().provideErrorFeedback(null);
      }
      return "";
   }

   public void setText(String text) {
      try {
         replace(0, getLength(), text, null);
      } catch (BadLocationException e) {
         UIManager.getLookAndFeel().provideErrorFeedback(null);
      }
   }

   public void clear() {
      try {
         remove(0, getLength());
      } catch (BadLocationException e) {
         UIManager.getLookAndFeel().provideErrorFeedback(null);
      }
   }
}
