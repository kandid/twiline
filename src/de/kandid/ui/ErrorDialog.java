package de.kandid.ui;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import de.kandid.model.Emitter;

public class ErrorDialog {

	public interface Listener {
		public void valueChanged(LogRecord old, LogRecord nu);
	}

	public static class Model {

		public void setValue(LogRecord value) {
			LogRecord old = _value;
			_value = value;
			_listeners.fire().valueChanged(old, value);
		}

		public LogRecord getValue() {
			return _value;
		}

		public final Emitter<Listener> _listeners = Emitter.makeEmitter(Listener.class);
		private LogRecord _value;
	}

	public static class View extends JPanel {

		public View(Model model) {
			super(new BorderLayout());
			add(_severity, BorderLayout.WEST);
			ScrollableText message = new ScrollableText(_message, 30, 0);
			message._text.setEditable(false);
			message._text.setBackground(message.getBackground());
			message.setVerticalScrollOnly();
			add(message, BorderLayout.CENTER);
			setModel(model);
		}

		public void setModel(Model model) {
			if (_model != null)
				_model._listeners.remove(this);
			_model = model;
			Listener listener = new Listener() {
				@Override
				public void valueChanged(LogRecord old, LogRecord nu) {
					final int severity = nu == null || nu.getLevel() == null ? -1 : nu.getLevel().intValue();
					Icon severityIcon = severity == -1 ?
							UIManager.getIcon("OptionPane.questionIcon") :
							severity >= Level.SEVERE.intValue() ?
									UIManager.getIcon("OptionPane.errorIcon") :
									severity >= Level.WARNING.intValue() ?
											UIManager.getIcon("OptionPane.warnigIcon") :
											UIManager.getIcon("OptionPane.informationIcon");
					_severity.setIcon(severityIcon);
					String message = nu.getMessage();
					if (nu.getThrown() != null)
						message += "\n" + nu.getThrown().getLocalizedMessage();
					_message.setText(message);
				}
			};
			listener.valueChanged(null, model.getValue());
			_model._listeners.add(this, listener);
		}

		private Model _model;
		private final JLabel _severity = new JLabel();
		private final TextLineModel _message = new TextLineModel();
	}

	public static class Log extends Handler {
		@Override
		public void publish(final LogRecord record) {
			if (record.getLevel().intValue() >= Level.SEVERE.intValue()) {
				Window active = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
				ErrorDialog.show(active, record);
			}
		}
		@Override public void flush() {}
		@Override public void close() throws SecurityException {}
	}

	public static void show(Window parent, LogRecord lr) {
		final JDialog d = new JDialog(parent, ModalityType.MODELESS);
		final Model m = new Model();
		m.setValue(lr);

		final JPanel view = new JPanel(new BorderLayout());
		view.add(new View(m), BorderLayout.CENTER);

		final Action close = new Action("OK", "Dismiss this dialog", "ESCAPE") {
			@Override
			public void go() {
				d.setVisible(false);
			}
		}.addKeysTo(view);
		final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttons.add(new JButton(close));

		view.add(buttons, BorderLayout.SOUTH);

		d.getContentPane().add(view);
		d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		d.pack();
		d.setLocationRelativeTo(parent);
		d.setVisible(true);
	}

	public static void main(String[] args) {
		try {
			final LogRecord lr = new LogRecord(Level.SEVERE, "Das ist eine\nzweizeilige Nachricht\nStimmt nicht (zumindest jetzt)\nEs sind vier Zeilen von denen die letzte ganz ausgesprochen - wenn nicht sogar furchtbar lang ist oder zumindest werden kann");
			lr.setThrown(new Exception("Da ging was schief"));
			show(null, lr);
		} catch (Exception e) {
			System.out.println("Exception caught: " + e);
			e.printStackTrace();
		}
	}
}
