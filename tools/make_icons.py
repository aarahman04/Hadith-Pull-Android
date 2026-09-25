#!/usr/bin/env python3
"""Regenerates the Android launcher icon set and the in-app brand mark from the source logo.
Run from anywhere; paths are relative to this file's location. Requires Pillow (PIL)."""
from PIL import Image, ImageDraw
import pathlib

REPO_ROOT = pathlib.Path(__file__).resolve().parents[1]
WEB_REPO = REPO_ROOT.parent / "Hadith-Pull"
SOURCE = WEB_REPO / "logos and favicons" / "hadith-pull-logo_app.png"
RES = REPO_ROOT / "app" / "src" / "main" / "res"

BG_RGB = (246, 242, 234)  # #F6F2EA
# The background this specific source file was rendered on (measured 2026-09-25: corner pixels
# (0,0)=(255,255,255), (511,0)=(255,255,255), (0,511)=(255,255,255), center-edge samples=(252,251,240)).
# White corners are outside the frame; the (252,251,240) cream is the actual card background.
CARD_BG = (252, 251, 240)
KEY_TOLERANCE = 40  # sum of |channel diffs|; matches the bbox-detection tolerance used to verify this file


def load_source() -> Image.Image:
    return Image.open(SOURCE).convert("RGB")


def key_out_background(im: Image.Image) -> Image.Image:
    """Returns an RGBA image with CARD_BG (and the white corner) keyed to transparent."""
    im = im.convert("RGBA")
    px = im.load()
    w, h = im.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if abs(r - CARD_BG[0]) + abs(g - CARD_BG[1]) + abs(b - CARD_BG[2]) <= KEY_TOLERANCE or \
               abs(r - 255) + abs(g - 255) + abs(b - 255) <= 10:
                px[x, y] = (r, g, b, 0)
    return im


def content_bbox(im: Image.Image):
    bbox = im.getbbox()
    if bbox is None:
        raise SystemExit("make_icons: keyed image has no visible content -- check CARD_BG/KEY_TOLERANCE")
    return bbox


def cropped_mark(im: Image.Image, pad_frac: float = 0.02) -> Image.Image:
    l, t, r, b = content_bbox(im)
    w, h = r - l, b - t
    pad_x, pad_y = int(w * pad_frac), int(h * pad_frac)
    l2, t2 = max(0, l - pad_x), max(0, t - pad_y)
    r2, b2 = min(im.width, r + pad_x), min(im.height, b + pad_y)
    return im.crop((l2, t2, r2, b2))


def fit_on_canvas(mark: Image.Image, canvas_px: int, safe_frac: float) -> Image.Image:
    """Fits `mark` into the central safe_frac of a canvas_px square, transparent elsewhere."""
    safe_px = int(canvas_px * safe_frac)
    scale = min(safe_px / mark.width, safe_px / mark.height)
    new_size = (max(1, round(mark.width * scale)), max(1, round(mark.height * scale)))
    resized = mark.resize(new_size, Image.LANCZOS)
    canvas = Image.new("RGBA", (canvas_px, canvas_px), (0, 0, 0, 0))
    ox, oy = (canvas_px - new_size[0]) // 2, (canvas_px - new_size[1]) // 2
    canvas.alpha_composite(resized, (ox, oy))
    return canvas


def to_monochrome(im: Image.Image) -> Image.Image:
    """White silhouette, same alpha channel (Android 13 themed-icon layer)."""
    alpha = im.getchannel("A")
    white = Image.new("RGBA", im.size, (255, 255, 255, 0))
    white.putalpha(alpha)
    return white


FOREGROUND_SIZES = {"mdpi": 108, "hdpi": 162, "xhdpi": 216, "xxhdpi": 324, "xxxhdpi": 432}
LEGACY_SIZES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
SAFE_ZONE_FRACTION = 66 / 108  # §2.7: fits the central 66dp of a 108dp adaptive-icon canvas


def main():
    keyed = key_out_background(load_source())
    mark = cropped_mark(keyed)

    for density, px in FOREGROUND_SIZES.items():
        fg = fit_on_canvas(mark, px, SAFE_ZONE_FRACTION)
        d = RES / f"mipmap-{density}"
        fg.save(d / "ic_launcher_foreground.png")
        to_monochrome(fg).save(d / "ic_launcher_monochrome.png")

    # Legacy composites: background square + the same fitted mark, flattened, at legacy sizes.
    for density, px in LEGACY_SIZES.items():
        bg = Image.new("RGBA", (px, px), BG_RGB + (255,))
        fg = fit_on_canvas(mark, px, SAFE_ZONE_FRACTION)
        square = Image.alpha_composite(bg, fg)
        square.save(RES / f"mipmap-{density}" / "ic_launcher.png")

        mask = Image.new("L", (px, px), 0)
        ImageDraw.Draw(mask).ellipse((0, 0, px, px), fill=255)
        round_icon = Image.new("RGBA", (px, px), (0, 0, 0, 0))
        round_icon.paste(square, (0, 0), mask)
        round_icon.save(RES / f"mipmap-{density}" / "ic_launcher_round.png")

    # Brand mark: the keyed, cropped logo centred on a 192x192 transparent canvas, no safe-zone
    # shrink (R-Q2) -- this is a full-bleed mark for a 24dp tile, not an adaptive-icon foreground.
    brand = fit_on_canvas(mark, 192, safe_frac=0.92)
    (RES / "drawable-nodpi").mkdir(parents=True, exist_ok=True)
    brand.save(RES / "drawable-nodpi" / "brand_logo.png")

    print("make_icons: wrote foreground/monochrome/legacy icons at 5 densities, and brand_logo.png")


if __name__ == "__main__":
    main()
