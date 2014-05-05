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
