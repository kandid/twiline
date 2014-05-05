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

public abstract class Condition {

	private Condition(String ifNot, boolean ignored) {
		_ifNot = ifNot;
		_false = null;
	}

	public Condition(String ifNot) {
		_ifNot = ifNot;
		_false = new Condition(ifNot, false) {
			@Override
			public boolean isTrue() {
				return false;
			}
			@Override
			public Condition and(Condition next) {
				return this;
			}
		};
	}

	public void applyTo(Action... actions) {
		if (isTrue()) {
			for (Action a : actions)
				a.setEnabled(true);
		} else {
			for (Action a : actions)
				a.setEnabled(false, _ifNot);
		}
	}

	public Condition and(Condition next) {
		return isTrue() ?	next : _false;
	}

	public abstract boolean isTrue();

	public final String _ifNot;
	public final Condition _false;
}
