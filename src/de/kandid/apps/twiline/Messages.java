/*
 * Created on 13.03.2014
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.kandid.apps.twiline;

import java.nio.charset.Charset;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private static final String BUNDLE_NAME = "de.kandid.apps.twiline.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
//	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, Locale.GERMANY);

	private static final Charset _iso8859_1 = Charset.forName("ISO-8859-1");
	private static final Charset _utf8 = Charset.forName("UTF-8");

	private Messages() {
	}

	public static String get(String key) {
		try {
			return new String(RESOURCE_BUNDLE.getString(key).getBytes(_iso8859_1),_utf8);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
