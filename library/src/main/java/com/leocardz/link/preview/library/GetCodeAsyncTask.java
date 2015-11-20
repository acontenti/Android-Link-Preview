package com.leocardz.link.preview.library;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashMap;

class GetCodeAsyncTask extends AsyncTask<String, Void, SourceContent> {

	private int imageQuantity;
	private LinkPreview.Callback callback;

	public GetCodeAsyncTask(int imageQuantity, @NonNull LinkPreview.Callback callback) {
		this.imageQuantity = imageQuantity;
		this.callback = callback;
	}

	@Override
	protected void onPreExecute() {
		callback.onPreExecute();
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(SourceContent result) {
		callback.onResult(result, LinkPreviewUtils.isNull(result));
		super.onPostExecute(result);
	}

	@Override
	protected SourceContent doInBackground(String... params) {
		// Don't forget the http:// or https://
		ArrayList<String> urls = RegexHelper.matches(params[0]);
		SourceContent sourceContent = new SourceContent();

		if (urls.size() > 0) {
			sourceContent.setFinalUrl(LinkPreviewUtils.unshortenUrl(LinkPreviewUtils.extendedTrim(urls.get(0))));
		} else return null;

		if (!sourceContent.getFinalUrl().equals("")) {
			if (LinkPreviewUtils.isImage(sourceContent.getFinalUrl())) {
				sourceContent.setSuccess(true);
				sourceContent.getImages().add(sourceContent.getFinalUrl());
				sourceContent.setTitle("");
				sourceContent.setDescription("");
			} else {
				try {
					Document doc = Jsoup
							.connect(sourceContent.getFinalUrl())
							.userAgent("Mozilla")
							.get();

					HashMap<String, String> metaTags = LinkPreviewUtils.getMetaTags(sourceContent.getHtmlCode());

					sourceContent.setHtmlCode(LinkPreviewUtils.extendedTrim(doc.toString()));
					sourceContent.setMetaTags(metaTags);
					sourceContent.setTitle(metaTags.get("title"));
					sourceContent.setDescription(metaTags.get("description"));

					if (sourceContent.getTitle().equals("")) {
						String matchTitle = RegexHelper.pregMatch(sourceContent.getHtmlCode(), RegexHelper.TITLE_PATTERN, 2);
						if (!matchTitle.equals("")) {
							sourceContent.setTitle(LinkPreviewUtils.htmlDecode(matchTitle));
						}
					}

					if (sourceContent.getDescription().equals("")) {
						sourceContent.setDescription(LinkPreviewUtils.crawlCode(sourceContent.getHtmlCode()));
						sourceContent.setDescription(sourceContent.getDescription().replaceAll(RegexHelper.SCRIPT_PATTERN, ""));
						sourceContent.setDescription(LinkPreviewUtils.stripTags(sourceContent.getDescription()));
					}

					if (imageQuantity != LinkPreview.NO_IMAGES) {
						if (!metaTags.get("image").equals("")) {
							sourceContent.getImages().add(metaTags.get("image"));
						}
						sourceContent.getImages().addAll(LinkPreviewUtils.getImages(doc, imageQuantity));
					}

					sourceContent.setSuccess(true);
				} catch (Exception e) {
					sourceContent.setSuccess(false);
				}
			}
		}

		String[] finalLinkSet = sourceContent.getFinalUrl().split("\\?");
		sourceContent.setUrl(finalLinkSet[0]);
		sourceContent.setCanonicalUrl(LinkPreviewUtils.getCanonicalUrl(sourceContent.getFinalUrl()));

		return sourceContent;
	}
}
