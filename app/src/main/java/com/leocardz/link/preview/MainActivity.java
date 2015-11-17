package com.leocardz.link.preview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.leocardz.link.preview.library.LinkPreview;
import com.leocardz.link.preview.library.SourceContent;

import java.util.List;
import java.util.Random;

@SuppressLint("InflateParams")
public class MainActivity extends AppCompatActivity {

	private EditText editText, editTextTitlePost, editTextDescriptionPost;
	private Button submitButton, postButton, randomButton;
	private ViewGroup dropPreview, dropPost;
	private TextView previewAreaTitle, postAreaTitle;
	private String currentTitle, currentUrl, currentCannonicalUrl, currentDescription;
	private Bitmap[] currentImageSet;
	private Bitmap currentImage;
	private int currentItem = 0;
	private boolean noThumb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		editText = (EditText) findViewById(R.id.input);
		editTextTitlePost = null;
		editTextDescriptionPost = null;

		/** --- From ShareVia Intent */
		if (getIntent().getExtras() != null) {
			String shareVia = (String) getIntent().getExtras().get(Intent.EXTRA_TEXT);
			if (shareVia != null) {
				editText.setText(shareVia);
			}
		}
		if (getIntent().getAction().equals(Intent.ACTION_VIEW)) {
			Uri data = getIntent().getData();
			String scheme = data.getScheme();
			String host = data.getHost();
			List<String> params = data.getPathSegments();
			String built = scheme + "://" + host + "/";

			for (String string : params) {
				built += string + "/";
			}
			if (data.getQuery() != null && !data.getQuery().equals("")) {
				built = built.substring(0, built.length() - 1);
				built += "?" + data.getQuery();
			}
			System.out.println(built);
			editText.setText(built);
		}

		submitButton = (Button) findViewById(R.id.action_go);
		randomButton = (Button) findViewById(R.id.random);
		postButton = (Button) findViewById(R.id.post);
		previewAreaTitle = (TextView) findViewById(R.id.preview_area);
		postAreaTitle = (TextView) findViewById(R.id.post_area);
		/** Where the previews will be dropped */
		dropPreview = (ViewGroup) findViewById(R.id.drop_preview);
		/** Where the previews will be dropped */
		dropPost = (ViewGroup) findViewById(R.id.drop_post);

		initSubmitButton();
		initPostButton();
		initRandomButton();
	}

	/**
	 * Adding listener to the random button
	 */
	private void initRandomButton() {
		randomButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				editText.setText("");
				editText.setText(getRandomUrl());
			}
		});
	}

	/**
	 * Adding listener to the post button
	 */
	private void initPostButton() {
		postButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				postAreaTitle.setVisibility(View.VISIBLE);
				previewAreaTitle.setVisibility(View.GONE);
				postButton.setVisibility(View.GONE);
				submitButton.setEnabled(true);

				/** Inflating the preview layout */
				View mainView = getLayoutInflater().inflate(R.layout.main_view, null);
				LinearLayout linearLayout = (LinearLayout) mainView.findViewById(R.id.external);
				/** Inflating the post content */
				final View content = getLayoutInflater().inflate(R.layout.post_content, linearLayout);
				/** Fullfilling the content layout */
				final LinearLayout infoWrap = (LinearLayout) content.findViewById(R.id.info_wrap);
				final TextView contentTextView = (TextView) content.findViewById(R.id.post_content);
				final ImageView imageView = (ImageView) content.findViewById(R.id.image_post);
				final TextView titleTextView = (TextView) content.findViewById(R.id.title);
				final TextView urlTextView = (TextView) content.findViewById(R.id.url);
				final TextView descriptionTextView = (TextView) content.findViewById(R.id.description);

				contentTextView.setText(extendedTrim(editText.getText().toString()));

				if (currentImage != null && !noThumb)
					imageView.setImageBitmap(currentImage);
				else
					showHideImage(imageView, infoWrap, false);

				if (!currentTitle.equals(""))
					titleTextView.setText(currentTitle);
				else
					titleTextView.setVisibility(View.GONE);

				if (!currentDescription.equals(""))
					descriptionTextView.setText(currentDescription);
				else
					descriptionTextView.setVisibility(View.GONE);

				urlTextView.setText(currentCannonicalUrl);

				mainView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						String url = currentUrl;
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse(url));
						startActivity(i);
					}
				});
				dropPost.addView(mainView, 0);
				dropPreview.removeAllViews();
			}
		});
	}

	/**
	 * Adding listener to the button
	 */
	public void initSubmitButton() {
		submitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				LinkPreview.makePreview(editText.getText().toString(), callback);
			}
		});
	}

	/**
	 * Callback to update your view. Totally customizable.
	 * onPre() will be called before the crawling. onPos() after.
	 *
	 * You can customize this to update your view */
	private LinkPreview.Callback callback = new LinkPreview.Callback() {
		/**
		 * This view is used to be updated or added in the layout after getting the result
		 */
		private View mainView;
		private LinearLayout linearLayout;

		@Override
		public void onPreExecute() {
			hideSoftKeyboard();
			currentImageSet = null;
			currentItem = 0;

			postButton.setVisibility(View.GONE);
			previewAreaTitle.setVisibility(View.VISIBLE);

			currentImage = null;
			noThumb = false;
			currentTitle = currentDescription = currentUrl = currentCannonicalUrl = "";
			submitButton.setEnabled(false);
			/** Inflating the preview layout */
			mainView = getLayoutInflater().inflate(R.layout.main_view, null);
			linearLayout = (LinearLayout) mainView.findViewById(R.id.external);
			/** Inflating a loading layout into Main View LinearLayout */
			View loading = getLayoutInflater().inflate(R.layout.loading, linearLayout);
			linearLayout.addView(loading);
			dropPreview.addView(mainView);
		}

		@Override
		public void onResult(final SourceContent sourceContent, boolean isNull) {
			/** Removing the loading layout */
			linearLayout.removeAllViews();
			if (isNull) {
				/** Inflating the content layout into Main View LinearLayout */
				View failed = getLayoutInflater().inflate(R.layout.failed, linearLayout);
				TextView titleTextView = (TextView) failed.findViewById(R.id.text);
				titleTextView.setText(R.string.failed_preview);

				failed.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						releasePreviewArea();
					}
				});

			} else {
				postButton.setVisibility(View.VISIBLE);
				currentImageSet = new Bitmap[sourceContent.getImages().size()];

				/** Inflating the content layout into Main View LinearLayout */
				final View content = getLayoutInflater().inflate(R.layout.preview_content, linearLayout);
				/** Fullfilling the content layout */
				final LinearLayout infoWrap = (LinearLayout) content.findViewById(R.id.info_wrap);
				final LinearLayout titleWrap = (LinearLayout) infoWrap.findViewById(R.id.title_wrap);
				final LinearLayout thumbnailOptions = (LinearLayout) content.findViewById(R.id.thumbnail_options);
				final LinearLayout noThumbnailOptions = (LinearLayout) content.findViewById(R.id.no_thumbnail_options);
				final ImageView imageSet = (ImageView) content.findViewById(R.id.image_post_set);
				final TextView close = (TextView) titleWrap.findViewById(R.id.close);
				final TextView titleTextView = (TextView) titleWrap.findViewById(R.id.title);
				final EditText titleEditText = (EditText) titleWrap.findViewById(R.id.input_title);
				final TextView urlTextView = (TextView) content.findViewById(R.id.url);
				final TextView descriptionTextView = (TextView) content.findViewById(R.id.description);
				final EditText descriptionEditText = (EditText) content.findViewById(R.id.input_description);
				final TextView countTextView = (TextView) thumbnailOptions.findViewById(R.id.count);
				final CheckBox noThumbCheckBox = (CheckBox) noThumbnailOptions.findViewById(R.id.no_thumbnail_checkbox);
				final Button previousButton = (Button) thumbnailOptions.findViewById(R.id.post_previous);
				final Button forwardButton = (Button) thumbnailOptions.findViewById(R.id.post_forward);

				editTextTitlePost = titleEditText;
				editTextDescriptionPost = descriptionEditText;

				titleTextView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						titleTextView.setVisibility(View.GONE);
						titleEditText.setText(extendedTrim(titleTextView.getText().toString()));
						titleEditText.setVisibility(View.VISIBLE);
					}
				});
				titleEditText.setOnEditorActionListener(new OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
						if (arg2.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
							titleEditText.setVisibility(View.GONE);
							currentTitle = extendedTrim(titleEditText.getText().toString());
							titleTextView.setText(currentTitle);
							titleTextView.setVisibility(View.VISIBLE);
							hideSoftKeyboard();
						}
						return false;
					}
				});
				descriptionTextView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						descriptionTextView.setVisibility(View.GONE);
						descriptionEditText.setText(extendedTrim(descriptionTextView.getText().toString()));
						descriptionEditText.setVisibility(View.VISIBLE);
					}
				});
				descriptionEditText.setOnEditorActionListener(new OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
						if (arg2.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
							descriptionEditText.setVisibility(View.GONE);
							currentDescription = extendedTrim(descriptionEditText.getText().toString());
							descriptionTextView.setText(currentDescription);
							descriptionTextView.setVisibility(View.VISIBLE);
							hideSoftKeyboard();
						}
						return false;
					}
				});

				close.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						releasePreviewArea();
					}
				});

				noThumbCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
						noThumb = arg1;
						if (sourceContent.getImages().size() > 1)
							if (noThumb) thumbnailOptions.setVisibility(View.GONE);
							else thumbnailOptions.setVisibility(View.VISIBLE);
						showHideImage(imageSet, infoWrap, !noThumb);
					}
				});

				previousButton.setEnabled(false);
				previousButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (currentItem > 0)
							changeImage(previousButton,
									forwardButton,
									currentItem - 1,
									sourceContent,
									countTextView,
									imageSet,
									sourceContent.getImages().get(currentItem - 1)
							);
					}
				});
				forwardButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (currentItem < sourceContent.getImages().size() - 1)
							changeImage(previousButton,
									forwardButton,
									currentItem + 1,
									sourceContent,
									countTextView,
									imageSet,
									sourceContent.getImages().get(currentItem + 1)
							);
					}
				});

				if (sourceContent.getImages().size() > 0) {
					if (sourceContent.getImages().size() > 1) {
						countTextView.setText(String.format("1 %s %d", getString(R.string.of), sourceContent.getImages().size()));
						thumbnailOptions.setVisibility(View.VISIBLE);
					}
					noThumbnailOptions.setVisibility(View.VISIBLE);
					UrlImageViewHelper.setUrlDrawable(imageSet, sourceContent.getImages().get(0), new UrlImageViewCallback() {
						@Override
						public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
							if (loadedBitmap != null) {
								currentImage = loadedBitmap;
								currentImageSet[0] = loadedBitmap;
							}
						}
					});

				} else {
					showHideImage(imageSet, infoWrap, false);
				}

				titleTextView.setText(sourceContent.getTitle());
				urlTextView.setText(sourceContent.getCanonicalUrl());
				descriptionTextView.setText(sourceContent.getDescription());

				if (sourceContent.getTitle().equals(""))
					titleTextView.setText(R.string.enter_title);
				if (sourceContent.getDescription().equals(""))
					descriptionTextView.setText(R.string.enter_description);

				postButton.setVisibility(View.VISIBLE);
			}

			currentTitle = sourceContent.getTitle();
			currentDescription = sourceContent.getDescription();
			currentUrl = sourceContent.getUrl();
			currentCannonicalUrl = sourceContent.getCanonicalUrl();
		}
	};

	/**
	 * Change the current image in image set
	 */
	private void changeImage(Button previousButton, Button forwardButton, final int index, SourceContent sourceContent, TextView countTextView, ImageView imageSet, String url) {
		if (currentImageSet[index] != null) {
			currentImage = currentImageSet[index];
			imageSet.setImageBitmap(currentImage);
		} else {
			UrlImageViewHelper.setUrlDrawable(imageSet, url, new UrlImageViewCallback() {
				@Override
				public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
					if (loadedBitmap != null) {
						currentImage = loadedBitmap;
						currentImageSet[index] = loadedBitmap;
					}
				}
			});
		}

		currentItem = index;

		if (index == 0)
			previousButton.setEnabled(false);
		else
			previousButton.setEnabled(true);

		if (index == sourceContent.getImages().size() - 1)
			forwardButton.setEnabled(false);
		else
			forwardButton.setEnabled(true);

		countTextView.setText(String.format("%d %s %d", index + 1, getString(R.string.of), sourceContent.getImages().size()));
	}

	/**
	 * Show or hide the image layout according to the "No Thumbnail" ckeckbox
	 */
	private void showHideImage(View image, View parent, boolean show) {
		if (show) {
			image.setVisibility(View.VISIBLE);
			parent.setPadding(5, 5, 5, 5);
			parent.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 2f));
		} else {
			image.setVisibility(View.GONE);
			parent.setPadding(5, 5, 5, 5);
			parent.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 3f));
		}
	}

	/**
	 * Hide keyboard
	 */
	private void hideSoftKeyboard() {
		hideSoftKeyboard(editText);
		if (editTextTitlePost != null)
			hideSoftKeyboard(editTextTitlePost);
		if (editTextDescriptionPost != null)
			hideSoftKeyboard(editTextDescriptionPost);
	}

	private void hideSoftKeyboard(EditText editText) {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

	/**
	 * Just a set of urls
	 */
	private static final String[] RANDOM_URLS = {
			"http://vnexpress.net/ ",
			"http://facebook.com/ ",
			"http://gmail.com",
			"http://goo.gl/jKCPgp",
			"http://www3.nhk.or.jp/",
			"http://habrahabr.ru",
			"http://www.youtube.com/watch?v=cv2mjAgFTaI",
			"http://vimeo.com/67992157",
			"https://lh6.googleusercontent.com/-aDALitrkRFw/UfQEmWPMQnI/AAAAAAAFOlQ/mDh1l4ej15k/w337-h697-no/db1969caa4ecb88ef727dbad05d5b5b3.jpg",
			"http://www.nasa.gov/",
			"http://twitter.com",
			"http://bit.ly/14SD1eR"
	};

	/**
	 * Returns a random url
	 */
	private String getRandomUrl() {
		int random = new Random().nextInt(RANDOM_URLS.length);
		return RANDOM_URLS[random];
	}

	private void releasePreviewArea() {
		submitButton.setEnabled(true);
		postButton.setVisibility(View.GONE);
		previewAreaTitle.setVisibility(View.GONE);
		dropPreview.removeAllViews();
	}

	/**
	 * Removes extra spaces and trim the string
	 */
	private String extendedTrim(String content) {
		return content.replaceAll("\\s+", " ").replace("\n", " ")
				.replace("\r", " ").trim();
	}
}