#Link Preview

This is a fork of [Android-Link-Preview](https://github.com/LeonardoCardoso/Android-Link-Preview) by [@LeonardoCardoso](https://github.com/LeonardoCardoso). 

## How to use with Gradle

Simply add the repository to your build.gradle file:
```groovy
repositories {
	maven { url "https://jitpack.io" }
	// ...
}
```

And you can use the artifacts like this:
```groovy
dependencies {
	compile ('com.github.acontenti:Android-Link-Preview:1.1') {
		transitive = true
	}
	// ...
}
```

## Sample code

Basic syntax:
```java
LinkPreview.makePreview(String url, LinkPreview.Callback callback, int number_of_images);

LinkPreview.Callback callback = new Callback() {
		@Override
		public void onPreExecute() {
			// Called before fetching data
		}

		@Override
		public void onResult(SourceContent sourceContent, boolean isNull) {
			// Called after execution
			// Provides a SourceContent object
		}
	};
```

#License

	Copyright 2015 Alessandro Contenti
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	   http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
