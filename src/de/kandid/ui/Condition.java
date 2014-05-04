/*
 */
package de.kandid.ui;

import de.kandid.ui.Action;

/**
 *
 * @author dominik
 */
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
