package com.leocardz.link.preview.library;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class LinkPreviewUtils {
	private static final String HTTP_PROTOCOL = "http://";
	private static final String HTTPS_PROTOCOL = "https://";

	/**
	 * Removes extra spaces and trim the string
	 */
	static String extendedTrim(String content) {
		return content.replaceAll("\\s+", " ").replace("\n", " ")
				.replace("\r", " ").trim();
	}

	/**
	 * Gets content from a html tag
	 */
	private static String getTagContent(String tag, String content) {

		String pattern = "<" + tag + "(.*?)>(.*?)</" + tag + ">";
		String result = "", currentMatch;

		List<String> matches = RegexHelper.pregMatchAll(content, pattern, 2);

		int matchesSize = matches.size();
		for (int i = 0; i < matchesSize; i++) {
			currentMatch = stripTags(matches.get(i));
			if (currentMatch.length() >= 120) {
				result = extendedTrim(currentMatch);
				break;
			}
		}

		if (result.equals("")) {
			String matchFinal = RegexHelper.pregMatch(content, pattern, 2);
			result = extendedTrim(matchFinal);
		}

		result = result.replaceAll("&nbsp;", "");

		return htmlDecode(result);
	}

	/**
	 * Gets images from the html code
	 */
	static List<String> getImages(Document document, int imageQuantity) {
		List<String> matches = new ArrayList<>();

		Elements media = document.select("[src]");

		for (Element srcElement : media) {
			if (srcElement.tagName().equals("img")) {
				matches.add(srcElement.attr("abs:src"));
			}
		}

		if (imageQuantity != LinkPreview.ALL_IMAGES)
			matches = matches.subList(0, imageQuantity);

		return matches;
	}

	/**
	 * Transforms from html to normal string
	 */
	static String htmlDecode(String content) {
		return Jsoup.parse(content).text();
	}

	/**
	 * Crawls the code looking for relevant information
	 */
	static String crawlCode(String content) {
		String result, resultSpan, resultParagraph, resultDiv;

		result = resultSpan = getTagContent("span", content);
		resultParagraph = getTagContent("p", content);
		resultDiv = getTagContent("div", content);

		if (resultParagraph.length() > resultSpan.length()) {
			result = resultParagraph;
		}
		if (resultDiv.length() > resultParagraph.length()) {
			result = resultDiv;
		}

		return htmlDecode(result);
	}

	/**
	 * Returns the canonical url
	 */
	static String getCanonicalUrl(String url) {

		String canonicalUrl = "";
		if (url.startsWith(HTTP_PROTOCOL)) {
			url = url.substring(HTTP_PROTOCOL.length());
		} else if (url.startsWith(HTTPS_PROTOCOL)) {
			url = url.substring(HTTPS_PROTOCOL.length());
		}

		int urlLength = url.length();
		for (int i = 0; i < urlLength; i++) {
			if (url.charAt(i) != '/') canonicalUrl += url.charAt(i);
			else break;
		}

		return canonicalUrl;

	}

	/**
	 * Strips the tags from an element
	 */
	static String stripTags(String content) {
		return Jsoup.parse(content).text();
	}

	/**
	 * Verifies if the url is an image
	 */
	static boolean isImage(String url) {
		return url.matches(RegexHelper.IMAGE_PATTERN);
	}

	/**
	 * Returns meta tags from html code
	 */
	static HashMap<String, String> getMetaTags(String content) {

		HashMap<String, String> metaTags = new HashMap<>();
		metaTags.put("url", "");
		metaTags.put("title", "");
		metaTags.put("description", "");
		metaTags.put("image", "");

		List<String> matches = RegexHelper.pregMatchAll(content,
				RegexHelper.METATAG_PATTERN, 1);

		for (String match : matches) {
			if (match.toLowerCase().contains("property=\"og:url\"")
					|| match.toLowerCase().contains("property='og:url'")
					|| match.toLowerCase().contains("name=\"url\"")
					|| match.toLowerCase().contains("name='url'"))
				metaTags.put("url", separeMetaTagsContent(match));
			else if (match.toLowerCase().contains("property=\"og:title\"")
					|| match.toLowerCase().contains("property='og:title'")
					|| match.toLowerCase().contains("name=\"title\"")
					|| match.toLowerCase().contains("name='title'"))
				metaTags.put("title", separeMetaTagsContent(match));
			else if (match.toLowerCase()
					.contains("property=\"og:description\"")
					|| match.toLowerCase()
					.contains("property='og:description'")
					|| match.toLowerCase().contains("name=\"description\"")
					|| match.toLowerCase().contains("name='description'"))
				metaTags.put("description", separeMetaTagsContent(match));
			else if (match.toLowerCase().contains("property=\"og:image\"")
					|| match.toLowerCase().contains("property='og:image'")
					|| match.toLowerCase().contains("name=\"image\"")
					|| match.toLowerCase().contains("name='image'"))
				metaTags.put("image", separeMetaTagsContent(match));
		}

		return metaTags;
	}

	/**
	 * Gets content from metatag
	 */
	private static String separeMetaTagsContent(String content) {
		String result = RegexHelper.pregMatch(content, RegexHelper.METATAG_CONTENT_PATTERN, 1);
		return htmlDecode(result);
	}

	/**
	 * Unshortens a short url
	 */
	static String unshortenUrl(String shortURL) {
		if (!shortURL.startsWith(HTTP_PROTOCOL) && !shortURL.startsWith(HTTPS_PROTOCOL)) return "";

		URLConnection urlConn = connectURL(shortURL);
		urlConn.getHeaderFields();

		String finalResult = urlConn.getURL().toString();

		urlConn = connectURL(finalResult);
		urlConn.getHeaderFields();

		shortURL = urlConn.getURL().toString();

		while (!shortURL.equals(finalResult)) {
			finalResult = unshortenUrl(finalResult);
		}

		return finalResult;
	}

	/**
	 * Takes a valid url and return a URL object representing the url address.
	 */
	private static URLConnection connectURL(String strURL) {
		URLConnection conn = null;
		try {
			URL inputURL = new URL(strURL);
			conn = inputURL.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return conn;
	}
}
