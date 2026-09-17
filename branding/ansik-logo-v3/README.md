# Ansik logo v3

Generated with the built-in image generation tool, then mechanically trimmed,
scaled and padded for Android. Original generated source is preserved here.

Prompt: Create one production app icon for Ansik, a Korean travel and health-aware
dining itinerary app. A friendly flat vector-style emblem combining a map pin,
an organic leaf and a curved route/smile. Deep teal #28766C, small coral accent,
warm ivory #FFF9F0. Centered, readable at 48px, safe Android mask margins. No text,
letters, mockups, shadows, gradients or rounded-square container.

Refinement prompt: Preserve the silhouette, leaf and route. Fix alpha artifacts;
make the teal pin uniformly opaque #28766C, leaf/route #FFF9F0 and dot #EF6272.
Use an opaque ivory background. Remove highlights, mottling and texture.

Outputs: in-app 512px transparent PNG, adaptive foreground 432px transparent PNG,
Play Store 512px opaque PNG, five legacy launcher densities (square/round WebP).
Launcher background is ivory. Existing resource names are retained so login and
in-app fallback images use the new logo too. No server settings changed.

Regenerate exports: `node tools/export-logo-v3.mjs <source PNG path>`.
