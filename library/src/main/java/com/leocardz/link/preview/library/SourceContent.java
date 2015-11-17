package com.leocardz.link.preview.library;

import java.util.ArrayList;
import java.util.HashMap;

public class SourceContent {

	private boolean success = false;
	private String htmlCode = "";
	private String title = "";
	private String description = "";
	private String url = "";
	private String finalUrl = "";
	private String canonicalUrl = "";
	private HashMap<String, String> metaTags = new HashMap<>();
	private ArrayList<String> images = new ArrayList<>();

	public SourceContent() {
	}

	/**
	 * @return wether the operation was successfully accomplished
	 */
	public boolean isSuccessful() {
		return success;
	}

	/**
	 * @param success the success to set
	 */
	void setSuccess(boolean success) {
		this.success = success;
	}

	/**
	 * @return the htmlCode
	 */
	public String getHtmlCode() {
		return htmlCode;
	}

	/**
	 * @param htmlCode the htmlCode to set
	 */
	void setHtmlCode(String htmlCode) {
		this.htmlCode = htmlCode;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the finalUrl
	 */
	public String getFinalUrl() {
		return finalUrl;
	}

	/**
	 * @param finalUrl the finalUrl to set
	 */
	void setFinalUrl(String finalUrl) {
		this.finalUrl = finalUrl;
	}

	/**
	 * @return the canonicalUrl
	 */
	public String getCanonicalUrl() {
		return canonicalUrl;
	}

	/**
	 * @param canonicalUrl the canonicalUrl to set
	 */
	void setCanonicalUrl(String canonicalUrl) {
		this.canonicalUrl = canonicalUrl;
	}

	/**
	 * @return the metaTags
	 */
	public HashMap<String, String> getMetaTags() {
		return metaTags;
	}

	/**
	 * @param metaTags the metaTags to set
	 */
	void setMetaTags(HashMap<String, String> metaTags) {
		this.metaTags = metaTags;
	}

	/**
	 * @return the images
	 */
	public ArrayList<String> getImages() {
		return images;
	}

	/**
	 * @param images the images to set
	 */
	void setImages(ArrayList<String> images) {
		this.images = images;
	}
}
