package com.buddycloud.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.Html;
import android.text.Spanned;

public class TextUtils {

	private static final String LINKS_REGEX = new StringBuilder()
			.append("(?:")
			.append("\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]"
					+ "{2,4}/)(?:[^\\s()<>]+|\\((?:[^\\s()<>]+|(?:\\([^\\s()<>]+\\)))*\\))+(?:\\((?:"
					+ "[^\\s()<>]+|(?:\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:\'\".,<>?������]))\\b")
			.append("|")
			.append("\\b([\\w\\d][\\w\\d-_%&+<>.]+@[\\w\\d-]{3,}\\.[\\w\\d-]{2,}(?:\\.[\\w]{2,6})?)\\b")
			.append(")").toString();

	private static final Pattern LINKS_PATTERN = Pattern.compile(LINKS_REGEX);

	public static Spanned anchor(String text) {
		if (text == null) {
			return null;
		}
		Matcher matcher = LINKS_PATTERN.matcher(text.trim());

		StringBuffer textBuffer = new StringBuffer();
		while (matcher.find()) {
			if (matcher.group(1) != null) {
				matcher.appendReplacement(textBuffer, "<a href=\"$1\">$1</a>");
			} else if (matcher.group(2) != null) {
				matcher.appendReplacement(textBuffer,
						"<a href=\"buddycloud:$2\">$2</a>");
			}
		}
		matcher.appendTail(textBuffer);
		text = textBuffer.toString().replaceAll("\\n", "<br>");
		return Html.fromHtml(text);
	}

	public static boolean isEmpty(String text) {
		return android.text.TextUtils.isEmpty(text);
	}

	public static String capitalize(String str) {
		return capitalize(str, null);
	}
	
    public static String capitalize(String str, char[] delimiters) {
        if (str == null || str.length() == 0) {
            return str;
        }
        int strLen = str.length();
        StringBuffer buffer = new StringBuffer(strLen);

        int delimitersLen = 0;
        if(delimiters != null) {
            delimitersLen = delimiters.length;
        }

        boolean capitalizeNext = true;
        for (int i = 0; i < strLen; i++) {
            char ch = str.charAt(i);

            boolean isDelimiter = false;
            if(delimiters == null) {
                isDelimiter = Character.isWhitespace(ch);
            } else {
                for(int j=0; j < delimitersLen; j++) {
                    if(ch == delimiters[j]) {
                        isDelimiter = true;
                        break;
                    }
                }
            }

            if (isDelimiter) {
                buffer.append(ch);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                buffer.append(Character.toTitleCase(ch));
                capitalizeNext = false;
            } else {
                buffer.append(ch);
            }
        }
        return buffer.toString();
    }
}
