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
 * Created on 27.06.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.kandid.ui;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.kandid.model.Emitter;

public interface Boolean {

   /**
    *
    * @version $Rev:$
    */
   public interface Listener {
      public void valueChanged(boolean newValue);
   }

   /**
    *
    * @version $Rev:$
    */
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

   /**
    *
    * @version $Rev:$
    */
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