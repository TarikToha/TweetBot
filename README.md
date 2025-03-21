# TweetBot Android App

## Project Summary

- TweetBot is an LLM-based Android app for intelligent image-to-text generation.
- Captures photos using the device camera.
- Uses Google Cloud Vision API to detect and extract object labels from images.
- Sends labels to a Large Language Model (LLM) via the TextCortex GPT API.
- Generates scene-based descriptions suitable for tweets or social media captions.
- Displays both the captured image and generated text in the app UI.
- Utilizes AsyncTask for background processing and Volley for network communication.
- Handles image storage securely using Android FileProvider.
