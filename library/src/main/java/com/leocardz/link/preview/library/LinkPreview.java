package com.leocardz.link.preview.library;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class LinkPreview {

	public static final int ALL_IMAGES = -1;
	public static final int NO_IMAGES = -2;

	private LinkPreview() {
	}

	public static LinkPreview makePreview(@NonNull String url, @NonNull Callback callback) {
		return LinkPreview.makePreview(url, ALL_IMAGES, callback);
	}

	public static LinkPreview makePreview(@NonNull String url, int imageQuantity, @NonNull Callback callback) {
		LinkPreview linkPreview = new LinkPreview();
		new GetCodeAsyncTask(imageQuantity, callback).execute(url);
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
}
