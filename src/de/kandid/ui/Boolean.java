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

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.kandid.model.Emitter;

public interface Boolean {

   public interface Listener {
      public void valueChanged(boolean newValue);
   }

   public static class View extends JCheckBox {

      public View(final Model model) {
         model._listeners.add(this, new Listener() {
            public void valueChanged(boolean newValue) {
               setSelected(newValue);
            }
         });
         addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
               model.setValue(isSelected());
            }
         });
         setSelected(model.getValue());
      }
   }

   public static class Model {

      public void setValue(boolean value) {
         if (value == _value)
            return;
         _value = value;
         _listeners.fire().valueChanged(value);
      }

      public boolean getValue() {
         return _value;
      }

      private boolean _value;
      public final Emitter<Listener> _listeners = Emitter.makeEmitter(Listener.class);
   }
}