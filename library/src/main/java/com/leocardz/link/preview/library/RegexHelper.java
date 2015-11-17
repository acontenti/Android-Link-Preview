package com.leocardz.link.preview.library;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RegexHelper {
	static final String IMAGE_PATTERN = "(.+?)\\.(jpg|png|gif|bmp)$";
	static final String TITLE_PATTERN = "<title(.*?)>(.*?)</title>";
	static final String SCRIPT_PATTERN = "<script(.*?)>(.*?)</script>";
	static final String METATAG_PATTERN = "<meta(.*?)>";
	static final String METATAG_CONTENT_PATTERN = "content=\"(.*?)\"";

	static String pregMatch(String content, String pattern, int index) {
		String match = "";
		Matcher matcher = Pattern.compile(pattern).matcher(content);
		if (matcher.find()) {
			match = matcher.group(index);
		}
		return LinkPreviewUtils.extendedTrim(match);
	}

	static List<String> pregMatchAll(String content, String pattern, int index) {
		List<String> matches = new ArrayList<>();
		Matcher matcher = Pattern.compile(pattern).matcher(content);
		while (matcher.find()) {
			matches.add(LinkPreviewUtils.extendedTrim(matcher.group(index)));
		}
		return matches;
	}

	/**
	 * It finds urls inside the text and return the matched ones
	 */
	static ArrayList<String> matches(String text) {
		return matches(text, Integer.MAX_VALUE);
	}

	/**
	 * It finds urls inside the text and return the matched ones
	 */
	static ArrayList<String> matches(String text, int resultsNumber) {
		ArrayList<String> urls = new ArrayList<>();
		String[] splitString = (text.split(" "));
		for (String s : splitString) {
			try {
				URL item = new URL(s);
				urls.add(item.toString());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			if (urls.size() >= resultsNumber) break;
		}
		return urls;
	}
}
