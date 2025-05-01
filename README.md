# TweetBot

![MIT License](https://img.shields.io/badge/License-MIT-blue.svg)

TweetBot is an Android app that converts photos into tweet-style captions using Google Cloud Vision
and the TextCortex LLM. Simply snap a picture, extract object labels, and generate a concise
summary—ideal for creators, journalists, and social media automation. This app was demoed at
Sitterson Hall on October 23, 2023.

---

## Setup

- Android Studio
- Android 8.0+ (API level 26+)
- Google Cloud Vision API key
- TextCortex API key

Add your API keys to `local.properties`:

```properties
CV_KEY=your_google_cloud_vision_api_key_here
GPT_KEY=your_textcortex_or_gemini_key_here
```

---

## Usage

1. Tap **Start Camera** to take a photo
2. Cloud Vision identifies objects and generates labels
3. Labels are sent to TextCortex
4. The app displays a tweet-style AI-generated summary

---

## Development

Open the project in Android Studio and run it on a device or emulator with camera support.

Ensure your `local.properties` file is correctly configured with valid API keys.

---

## License

MIT © 2025

---

## Credits

- [Google Cloud Vision API](https://cloud.google.com/vision)
- [TextCortex API](https://docs.textcortex.com/)
- [Android Developers](https://developer.android.com)
- [Volley](https://developer.android.com/training/volley)
