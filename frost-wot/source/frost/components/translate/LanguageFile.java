/*
  LanguageFile.java
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of
  the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package frost.components.translate;

import java.io.*;

/**
 * Reads and generates Frost language resource files.
 * @author Jantho
 */
public class LanguageFile {

	/**
	 * Generates the complete class file with comments
	 * that can directly be compiled with frost.
	 */
	public static String generateFile(
		TranslateTableModel tableModel,
		String languageCode) {
		StringBuffer content = new StringBuffer();
		int rowCount = tableModel.getRowCount();

		content.append("/**\n");
		content.append(" * Language file for Frost\n");
		content.append(" *\n");
		content.append(" * This file has been created automatically.\n");
		content.append(
			" * Do NOT edit unless you REALLY know what you're doing!\n");
		content.append(" *\n");
		content.append(" * Language: " + languageCode + "\n");
		content.append(" */\n\n");

		content.append("package res;\n\n");
		content.append("import java.util.ListResourceBundle;\n\n");

		if (languageCode.equals("en"))
			content.append(
				"public class LangRes extends ListResourceBundle {\n\n");
		else
			content.append(
				"public class LangRes_"
					+ languageCode
					+ " extends ListResourceBundle {\n\n");

		content.append("public Object[][] getContents() {\n");
		content.append("    return contents;\n");
		content.append("}\n\n");

		content.append("static final Object[][] contents = {\n");

		for (int i = 0; i < rowCount; i++) {
			content.append(
				"{\""
					+ replaceSpecialCharacters(
						new StringBuffer((String) tableModel.getValueAt(i, 0)))
					+ "\",");
			content.append(
				"\""
					+ replaceSpecialCharacters(
						new StringBuffer((String) tableModel.getValueAt(i, 1)))
					+ "\"},");
			content.append("\n");
		}

		content.append("};\n");
		content.append("}\n");

		return content.toString();
	}

	/**
	 * There are some problems with linefeed and return characters.
	 * To prevent that these characters are executed on saving (instead
	 * of being saved as character) they had to be exchanged.
	 */
	private static StringBuffer replaceSpecialCharacters(StringBuffer content) {

		while (content.indexOf("\n") != -1) {
			int index = content.indexOf("\n");
			content.replace(index, index + 1, "\\n");
		}
		while (content.indexOf("\r") != -1) {
			int index = content.indexOf("\r");
			content.replace(index, index + 1, "\\r");
		}

		return content;
	}

	/**
	 * Reads a language file and returns the contents in
	 * a TranslateTableModel.
	 * @param tableModel Empty table
	 * @param locale Language locale, normaly two letters (for example de for german)
	 * @return TranslateTableModel with content from language file
	 */
	public static TranslateTableModel readLanguageFile(
		TranslateTableModel tableModel,
		File file) {
		String content = ReadAccess.readFile(file);
		String originalText = new String();
		String translatedText = new String();
		int cursor = 0;
		int positions[] = { 0, 0, 0, 0 };
		tableModel.setRowCount(0);

		while (cursor != -1) {
			for (int i = 0; i < 4; i++) {
				positions[i] = content.indexOf("\"", cursor + 1);
				cursor = positions[i];
				if (cursor == -1)
					break;
			}

			if (positions[0] != -1
				&& positions[1] != -1
				&& positions[2] != -1
				&& positions[3] != -1) {
				String row[] =
					{
						new String(
							content.substring(positions[0] + 1, positions[1])),
						new String(
							content.substring(positions[2] + 1, positions[3]))};
				tableModel.addRow(row);
				System.out.println(row[0] + " --- " + row[1]);
			}
		}

		return tableModel;
	}

}
