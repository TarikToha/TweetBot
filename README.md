# TweetBot

![MIT License](https://img.shields.io/badge/License-MIT-blue.svg)

TweetBot is an Android app that turns photos into tweet-style captions using Google Cloud Vision and
the TextCortex LLM. Snap a picture, detect key objects, and instantly generate a shareable
summary—perfect for creators and social media tools. Demoed at Sitterson Hall on October 23, 2023.

---

## Requirements

- Android Studio
- Android 8.0+ (API level 26+)
- Google Cloud Vision API key
- TextCortex API key

Add the keys to a `.env` file:

```env
CV_KEY=your_google_cloud_vision_api_key_here
GPT_KEY=your_textcortex_or_gemini_key_here
```

---

## Capture & Generate

1. Tap **Start Camera** to launch the camera
2. Cloud Vision detects labels from the image
3. Labels are forwarded to TextCortex
4. A tweet-style summary is generated and displayed

---

## Run Locally

Open the project in Android Studio and run on a device or emulator with camera support.

Make sure the `.env` file is properly configured before building.

---

## License

MIT © 2025

---

## Credits

- [Google Cloud Vision API](https://cloud.google.com/vision)
- [TextCortex API](https://docs.textcortex.com/)
- [Android Developers](https://developer.android.com)
- [Volley](https://developer.android.com/training/volley)