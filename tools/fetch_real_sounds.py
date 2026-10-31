"""Downloads real ambient recordings from Wikimedia Commons and turns them into seamless loops.

Every source file is listed in SOUNDS below together with the section to use. Only recordings
released as CC0 / public domain / CC BY are used; licence, author and source URL are written to
app/src/main/assets/soundscapes/CREDITS.md automatically.

Needs ffmpeg on PATH (or `pip install imageio-ffmpeg`). Usage:  python tools/fetch_real_sounds.py [--check | id ...]
"""
import json
import os
import re
import subprocess
import sys
import tempfile
import time
import urllib.error
import urllib.parse
import urllib.request

API = "https://commons.wikimedia.org/w/api.php"
UA = {"User-Agent": "SoulSpaceSoundFetcher/1.0 (personal project; contact via repo owner)"}
OUT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets", "soundscapes")

# id -> (Commons file title, start seconds, length seconds, crossfade seconds)
# crossfade 0 = a single struck sound (bowl, bell, gong): loops with only a tiny edge fade.
SOUNDS = {
    # Nature
    "s1": ("Rain (1).ogg", 2, 40, 4),
    "s15": ("Listening to Raindrops (1035382 - heavy loop).mp3", 0, 30, 3),
    "s2": ("Oceanwavescrushing.ogg", 20, 60, 5),
    "s19": ("Cascade de Syratu.ogg", 0, 16, 3),
    "s3": ("20090610 0 ambience.ogg", 20, 60, 5),
    "s4": ("Swale.ogg", 0, 30, 3),
    "s5": ("Gentle breeze and birds singing.ogg", 0, 33, 3),
    "s16": ("Campfire sound ambience.ogg", 0, 56, 4),
    "s17": ("Cicadas in Greece.ogg", 0, 27, 3),
    "s20": ("Royal Natal National Park dawn chorus (W1CDR0001146 BD3).ogg", 15, 60, 5),
    # Focus
    "s8": ("Water flowing pouring trickling.ogg", 5, 60, 5),
    "s39": ("Air conditioner hum 2 (Gravity Sound).wav", 0, 17, 2),
    # Sleep
    "s9": ("Rain against the window.ogg", 5, 62, 5),
    "s10": ("Rain and thunder (1).ogg", 0, 56, 4),
    "s11": ("Nature sounds ambience in a Dordogne pond.ogg", 2, 52, 4),
    "s18": ("Perseverance rover's SuperCam records wind on Mars.oga", 0, 39, 3),
    "s23": ("Human heart beating at 61 bpm (Cc-by-3.0).ogg", 0, 13.77, 0),
    "s24": ("Windglockenspiel.Koshi.ogg", 1, 54, 4),
    # Meditation
    "s12": ("SingingBowl1.ogg", 0, 12.5, 0),
    "s13": ("Bonsyou5599.ogg", 0, 28.5, 0),
    "s26": ("The sound of a singing bowl.wav", 0, 27.5, 0),
    "s27": ("240382 the-very-real-horst chinese-gong-finish-session-2014-06-10-29-143.wav", 0, 28.5, 0),
}

# Very long sources: only the first N bytes are downloaded (HTTP Range), enough for the section used.
PARTIAL = {}

# One-shot clips (not looped): id -> (Commons file title, start, length)
ONE_SHOTS = {
    "bell": ("Gong or bell vibrant (short).ogg", 0, 5.5),
}


def ffmpeg_exe():
    try:
        import imageio_ffmpeg
        return imageio_ffmpeg.get_ffmpeg_exe()
    except Exception:
        return "ffmpeg"


FFMPEG = ffmpeg_exe()


def api(params, retries=6):
    url = API + "?" + urllib.parse.urlencode(params)
    for attempt in range(retries):
        try:
            with urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=60) as r:
                return json.load(r)
        except urllib.error.HTTPError as e:
            if e.code == 429 and attempt < retries - 1:
                time.sleep(20 * (attempt + 1))
                continue
            raise


def strip(html):
    return re.sub(r"<[^>]+>", "", html or "").strip()


def file_info(title):
    data = api({"action": "query", "format": "json", "titles": "File:" + title,
                "prop": "imageinfo", "iiprop": "url|extmetadata"})
    page = next(iter(data["query"]["pages"].values()))
    ii = page["imageinfo"][0]
    meta = ii["extmetadata"]
    return {
        "url": ii["url"],
        "page": "https://commons.wikimedia.org/wiki/File:" + urllib.parse.quote(title.replace(" ", "_")),
        "license": (meta.get("LicenseShortName") or {}).get("value", "?"),
        "artist": strip((meta.get("Artist") or {}).get("value", "")) or "unknown",
    }


def download(url, path, max_bytes=None):
    headers = dict(UA)
    if max_bytes:
        headers["Range"] = f"bytes=0-{max_bytes - 1}"
    for attempt in range(6):
        try:
            with urllib.request.urlopen(urllib.request.Request(url, headers=headers), timeout=120) as r, open(path, "wb") as f:
                f.write(r.read())
            return
        except urllib.error.HTTPError as e:
            if e.code == 429 and attempt < 5:
                wait = int(e.headers.get("Retry-After") or 0) or 60 * (attempt + 1)
                print(f"  rate limited, waiting {wait}s", flush=True)
                time.sleep(wait)
                continue
            raise


def run(args):
    p = subprocess.run([FFMPEG, "-hide_banner", "-y"] + args, capture_output=True, text=True)
    if p.returncode != 0:
        raise RuntimeError(p.stderr[-600:])


def make_loop(src, dst, start, length, xfade):
    with tempfile.TemporaryDirectory() as tmp:
        seg = os.path.join(tmp, "seg.wav")
        # mono, loudness-matched section
        run(["-ss", str(start), "-t", str(length), "-i", src, "-ac", "1", "-ar", "44100",
             "-af", "highpass=f=25,loudnorm=I=-26:TP=-3:LRA=7", seg])
        if xfade > 0:
            d, x = float(length), float(xfade)
            graph = (f"[0:a]asplit=3[a][b][c];"
                     f"[a]atrim=start={x}:end={d - x},asetpts=PTS-STARTPTS[mid];"
                     f"[b]atrim=start={d - x}:end={d},asetpts=PTS-STARTPTS[tail];"
                     f"[c]atrim=start=0:end={x},asetpts=PTS-STARTPTS[head];"
                     # equal-power crossfade: the tail fades out while the head fades in
                     f"[tail]afade=t=out:st=0:d={x}:curve=qsin[t];"
                     f"[head]afade=t=in:st=0:d={x}:curve=qsin[h];"
                     f"[t][h]amix=inputs=2:duration=longest:normalize=0[blend];"
                     f"[blend][mid]concat=n=2:v=0:a=1[out]")
            run(["-i", seg, "-filter_complex", graph, "-map", "[out]", "-c:a", "libvorbis", "-q:a", "3", dst])
        else:
            run(["-i", seg, "-af", f"afade=t=in:d=0.02,afade=t=out:st={float(length) - 0.05}:d=0.05",
                 "-c:a", "libvorbis", "-q:a", "3", dst])


def balance(dst, target=-28.0, peak=-3.0):
    """Gives every loop a similar average level, so mixed sounds sit well together."""
    p = subprocess.run([FFMPEG, "-hide_banner", "-i", dst, "-af", "volumedetect", "-f", "null", "-"],
                       capture_output=True, text=True)
    mean = float(re.search(r"mean_volume: (-?[\d.]+) dB", p.stderr).group(1))
    top = float(re.search(r"max_volume: (-?[\d.]+) dB", p.stderr).group(1))
    gain = min(target - mean, peak - top)  # never push the loudest peak above `peak`
    if abs(gain) < 0.7:
        return
    tmp = dst + ".tmp.ogg"
    run(["-i", dst, "-af", f"volume={gain:.2f}dB", "-c:a", "libvorbis", "-q:a", "3", tmp])
    os.replace(tmp, dst)


def make_one_shot(src, dst, start, length):
    run(["-ss", str(start), "-t", str(length), "-i", src, "-ac", "1", "-ar", "44100",
         "-af", f"loudnorm=I=-24:TP=-3:LRA=7,afade=t=out:st={length - 0.5}:d=0.5", "-c:a", "libvorbis", "-q:a", "4", dst])


def preflight():
    """Checks every title exists and prints its licence; downloads nothing."""
    for sid, spec in list(SOUNDS.items()) + list(ONE_SHOTS.items()):
        try:
            info = file_info(spec[0])
            print(f"OK  {sid:<5} {info['license']:<14} {spec[0][:60]}")
        except Exception as e:
            print(f"ERR {sid:<5} {spec[0][:60]}  -> {e}")
        time.sleep(2)


CACHE = os.path.join(os.path.dirname(__file__), "credits_cache.json")
SOURCE_CACHE = os.path.join(tempfile.gettempdir(), "soulspace_sound_sources")


def load_cache():
    try:
        with open(CACHE, encoding="utf-8") as f:
            return json.load(f)
    except OSError:
        return {}


def write_credits(cache):
    with open(CACHE, "w", encoding="utf-8") as f:
        json.dump(cache, f, indent=1, ensure_ascii=False)
    with open(os.path.join(OUT, "CREDITS.md"), "w", encoding="utf-8") as f:
        f.write("# Sound credits\n\nRecordings from Wikimedia Commons, trimmed, loudness-matched and looped.\n\n")
        f.write("\n".join(cache[k] for k in sorted(cache)) + "\n")


def credit_line(sid, title, info):
    return f"- **{sid}** \"{title}\" by {info['artist']} ({info['license']}) - {info['page']}"


def process(cache, sid, title, only, build):
    """Fetch + build one clip. Clips that already exist are skipped unless named explicitly."""
    dst = os.path.join(OUT, sid + ".ogg")
    exists = os.path.exists(dst)
    if exists and not only:
        if sid not in cache:  # finished earlier: only the credit line is missing
            cache[sid] = credit_line(sid, title, file_info(title))
            write_credits(cache)
            time.sleep(3)
        return False
    info = file_info(title)
    print(f"{sid}: {title}  [{info['license']}]", flush=True)
    # Source files are cached so a re-run (e.g. after tweaking a loop) never downloads them again
    os.makedirs(SOURCE_CACHE, exist_ok=True)
    src = os.path.join(SOURCE_CACHE, re.sub(r"[^A-Za-z0-9._-]+", "_", title))
    downloaded = not os.path.exists(src)
    if downloaded:
        download(info["url"], src, PARTIAL.get(title))
    build(src, dst)
    cache[sid] = credit_line(sid, title, info)
    write_credits(cache)
    if not downloaded:
        time.sleep(2)
    return downloaded


def main(only):
    os.makedirs(OUT, exist_ok=True)
    cache = load_cache()
    for sid, (title, start, length, xfade) in SOUNDS.items():
        if only and sid not in only:
            continue
        if process(cache, sid, title, only, lambda src, dst: (make_loop(src, dst, start, length, xfade), balance(dst))):
            time.sleep(20)  # be gentle with Wikimedia's servers
    for sid, (title, start, length) in ONE_SHOTS.items():
        if only and sid not in only:
            continue
        if process(cache, sid, title, only, lambda src, dst: make_one_shot(src, dst, start, length)):
            time.sleep(20)


if __name__ == "__main__":
    if sys.argv[1:] == ["--check"]:
        preflight()
    else:
        main(set(sys.argv[1:]))
