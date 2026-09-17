# Motion study 02 — awaiting user approval

Three original vector illustrations based on the user's flat checklist/person
style reference. No animation files are installed into Android resources.

- course.json: staggered checklist strokes and shoulder movement
- meal.json: ingredient check and shoulder movement (not a safety verdict)
- search.json: route stroke and destination movement

600 × 420, 30 fps, 120 frames / 4-second loops. Shapes and strokes only; no fonts,
remote assets or raster images. PNGs are static design references. Open index.html
for the real lottie-web SVG player. Reduced-motion preference disables autoplay;
pause and restart controls are provided. App-runtime testing remains pending.

Rebuild: `node tools/build-motion-previews.mjs`.

Reference research (not copied or purchased):
https://lottiefiles.com/animation/itinerary-travel-checklist-animated-icon_8836143

Player: lottie-web 5.13.0 (MIT), Airbnb and contributors.
https://github.com/airbnb/lottie-web
https://cdnjs.cloudflare.com/ajax/libs/lottie-web/5.13.0/lottie.min.js
