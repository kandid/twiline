package de.kandid.ui;

import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.Document;
import javax.swing.text.View;

public class ScrollableText extends JScrollPane {

	public ScrollableText(Document model, int cols, final int rows) {
		setViewportView(_text);
		_text.setDocument(model);
		_text.setColumns(cols);
		_text.setRows(rows);
	}

	public void setVerticalScrollOnly() {
		_text.setWrapStyleWord(true);
		_text.setLineWrap(true);
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	}

	public static int getWrappedLines(JTextArea component, Dimension pref) {
		View view = component.getUI().getRootView(component).getView(0);
		view.setSize(pref.width, 0);
		int preferredHeight = (int)view.getPreferredSpan(View.Y_AXIS);
		return preferredHeight;
	}

	public final JTextArea _text = new JTextArea() {
		@Override
		public Dimension getPreferredScrollableViewportSize() {
			final Dimension preferredSize = super.getPreferredScrollableViewportSize();
			if (_text.getRows() > 0)
				return preferredSize;
			Insets insets = getInsets();
			View view = getUI().getRootView(this).getView(0);
			view.setSize(preferredSize.width, 0);
			return new Dimension(preferredSize.width, (int)view.getPreferredSpan(View.Y_AXIS) + insets.top + insets.bottom);
		};
	};
}
