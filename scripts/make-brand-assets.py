"""Genera le varianti del logo Dueffe usate da app, sito e README.

Parte da branding/logo-light.png (ff nero e oro su fondo bianco) e produce:
- branding/ff-ivory.png: ff avorio e oro su trasparente (per fondi scuri)
- branding/ff-ink.png: ff scuro e oro su trasparente (per fondi chiari)
- icone launcher Android (legacy, adaptive foreground, monochrome)
- icone e favicon per la versione web
"""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parent.parent
SRC = Image.open(ROOT / "branding/logo-light.png").convert("RGB")
IVORY = (255, 250, 240)
INK = (22, 16, 6)
BG = (5, 5, 5)
GOLD = (255, 216, 77)


def recolor(dark_rgb):
    w, h = SRC.size
    out = Image.new("RGBA", (w, h))
    sp, op = SRC.load(), out.load()
    for y in range(h):
        for x in range(w):
            r, g, b = sp[x, y]
            # Bianco -> trasparente; nero -> colore scelto; oro -> oro brand.
            lum = (r + g + b) / 3
            sat = max(r, g, b) - min(r, g, b)
            if sat > 40:  # parte oro
                a = min(255, int(sat * 255 / 150))
                op[x, y] = (*GOLD, a)
            else:
                a = max(0, min(255, int((255 - lum) * 255 / 230)))
                op[x, y] = (*dark_rgb, a)
    bbox = out.getbbox()
    return out.crop(bbox)


def pad_square(img, size, scale):
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    w, h = img.size
    k = size * scale / max(w, h)
    im = img.resize((max(1, int(w * k)), max(1, int(h * k))), Image.LANCZOS)
    canvas.alpha_composite(im, ((size - im.width) // 2, (size - im.height) // 2))
    return canvas


def glow_tile(mark, size, radius_ratio=0.0, scale=0.56):
    tile = Image.new("RGBA", (size, size), (*BG, 255))
    # alone dorato morbido dietro al monogramma
    glow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(glow)
    r = size * 0.36
    d.ellipse((size / 2 - r, size / 2 - r * 0.9, size / 2 + r, size / 2 + r * 1.1), fill=(255, 184, 0, 70))
    glow = glow.filter(ImageFilter.GaussianBlur(size * 0.12))
    tile.alpha_composite(glow)
    tile.alpha_composite(pad_square(mark, size, scale))
    if radius_ratio:
        mask = Image.new("L", (size, size), 0)
        ImageDraw.Draw(mask).rounded_rectangle((0, 0, size - 1, size - 1), int(size * radius_ratio), fill=255)
        tile.putalpha(mask)
    return tile


ivory = recolor(IVORY)
ink = recolor(INK)
ivory.save(ROOT / "branding/ff-ivory.png")
ink.save(ROOT / "branding/ff-ink.png")

res = ROOT / "app/src/main/res"
densities = {"mdpi": 1, "hdpi": 1.5, "xhdpi": 2, "xxhdpi": 3, "xxxhdpi": 4}
for name, k in densities.items():
    d = res / f"mipmap-{name}"
    d.mkdir(parents=True, exist_ok=True)
    s = int(48 * k)
    glow_tile(ivory, s, 0.22).save(d / "ic_launcher.png")
    round_tile = glow_tile(ivory, s)
    mask = Image.new("L", (s, s), 0)
    ImageDraw.Draw(mask).ellipse((0, 0, s - 1, s - 1), fill=255)
    round_tile.putalpha(mask)
    round_tile.save(d / "ic_launcher_round.png")
    fs = int(108 * k)
    pad_square(ivory, fs, 0.42).save(d / "ic_launcher_foreground.png")
    mono = pad_square(ivory, fs, 0.42)
    mono.putdata([(255, 255, 255, a) for (_, _, _, a) in mono.getdata()])
    mono.save(d / "ic_launcher_monochrome.png")

drawable = res / "drawable-nodpi"
drawable.mkdir(parents=True, exist_ok=True)
pad_square(ivory, 720, 0.98).save(drawable / "ff_mark.png")
pad_square(ivory, 288, 0.62).save(drawable / "splash_mark.png")

web = ROOT / "web/assets"
web.mkdir(parents=True, exist_ok=True)
pad_square(ivory, 720, 0.98).save(web / "ff-mark.png")
for s in (512, 192):
    glow_tile(ivory, s).save(web / f"icon-{s}.png")
glow_tile(ivory, 180).save(web / "apple-touch-icon.png")
glow_tile(ivory, 64, 0.22).save(web / "favicon.png")
glow_tile(ivory, 1024, 0.22).save(ROOT / "branding/dueffe-home-icon.png")
print("ok")
