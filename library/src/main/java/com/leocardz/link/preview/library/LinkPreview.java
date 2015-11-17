package com.leocardz.link.preview.library;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashMap;

public class LinkPreview {

	public static final int ALL_IMAGES = -1;
	public static final int NO_IMAGES = -2;
	private Callback callback;

	private LinkPreview() {
	}

	public static LinkPreview makePreview(@NonNull String url, @NonNull Callback callback) {
		return LinkPreview.makePreview(url, ALL_IMAGES, callback);
	}

	public static LinkPreview makePreview(@NonNull String url, int imageQuantity, @NonNull Callback callback) {
		LinkPreview linkPreview = new LinkPreview();
		linkPreview.callback = callback;
		linkPreview.new GetCodeAsyncTask(imageQuantity).execute(url);
		return linkPreview;
	}

	/**
	 * Callback that is invoked with before and after loading a link preview
	 */
	public interface Callback {

		void onPreExecute();

		/**
		 * @param sourceContent Class with all contents from preview.
		 * @param isNull        Indicates if the content is null.
		 */
		void onResult(@Nullable SourceContent sourceContent, boolean isNull);
	}

	private class GetCodeAsyncTask extends AsyncTask<String, Void, SourceContent> {

		private int imageQuantity;

		public GetCodeAsyncTask(int imageQuantity) {
			this.imageQuantity = imageQuantity;
		}

		@Override
		protected void onPreExecute() {
			if (callback != null) callback.onPreExecute();
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(SourceContent result) {
			if (callback != null) callback.onResult(result, isNull(result));
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
				if (LinkPreviewUtils.isImage(sourceContent.getFinalUrl()) && !sourceContent.getFinalUrl().contains("dropbox")) {
					//TODO Why not Dropbox?
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

						if (imageQuantity != NO_IMAGES) {
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

		/**
		 * Checks if the content could not be retrieved
		 */
		private boolean isNull(SourceContent sourceContent) {
			return sourceContent != null &&
					!sourceContent.isSuccessful() &&
					LinkPreviewUtils.extendedTrim(sourceContent.getHtmlCode()).equals("") &&
					!LinkPreviewUtils.isImage(sourceContent.getFinalUrl());
		}
	}

}
