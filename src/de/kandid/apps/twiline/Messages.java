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
