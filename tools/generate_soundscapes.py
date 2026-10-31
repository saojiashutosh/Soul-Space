"""Generates the loopable ambient soundscapes bundled in app/src/main/assets/soundscapes.

Every file is an 8 second, 16 kHz, mono, 16-bit WAV named after the sound id used in
SoundCatalog (s1.wav ... s27.wav), plus bell.wav, a one-shot chime used by meditation sessions. They are synthesized, so there are no licensing
concerns; drop a real recording with the same name over any of them to replace it.

Usage:  python tools/generate_soundscapes.py
"""
import math
import os
import random
import struct
import wave

SR = 16000
SECONDS = 8
N = SR * SECONDS
TAU = 2 * math.pi
OUT = os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'assets', 'soundscapes')


# ------------------------------------------------------------------ helpers
def noise(rng, n=N):
    return [rng.uniform(-1, 1) for _ in range(n)]


def lowpass(x, cutoff):
    a = 1 - math.exp(-TAU * cutoff / SR)
    y, s = [], 0.0
    for v in x:
        s += a * (v - s)
        y.append(s)
    return y


def highpass(x, cutoff):
    lp = lowpass(x, cutoff)
    return [v - l for v, l in zip(x, lp)]


def bandpass(x, lo, hi):
    return lowpass(highpass(x, lo), hi)


def normalize(x, peak):
    m = max(abs(v) for v in x) or 1.0
    return [v * peak / m for v in x]


def loop_crossfade(x, fade=SR):
    """x has N + fade samples; blend the tail into the head so the loop is seamless."""
    out = x[:N]
    for i in range(fade):
        w = i / fade
        out[i] = x[i] * math.sin(w * math.pi / 2) + x[N + i] * math.cos(w * math.pi / 2)
    return out


def texture(rng, build):
    """Build N + 1 s of material with `build(rng, length)` and make it loop."""
    return loop_crossfade(build(rng, N + SR))


def mix(*tracks):
    return [sum(vals) for vals in zip(*tracks)]


def lfo(freq, phase=0.0, depth=1.0, base=0.0, n=N):
    return [base + depth * math.sin(TAU * freq * i / SR + phase) for i in range(n)]


def wrap_add(buf, start, samples):
    """Add `samples` into buf starting at `start`, wrapping so decays loop cleanly."""
    n = len(buf)
    for i, v in enumerate(samples):
        buf[(start + i) % n] += v


def tone(freq, length, decay, amp=1.0, phase=0.0):
    return [amp * math.exp(-decay * i / SR) * math.sin(TAU * freq * i / SR + phase) for i in range(length)]


# ------------------------------------------------------------------ sounds
def rain(rng, cutoff_hi, level, drops):
    def build(r, n):
        base = bandpass(noise(r, n), 500, cutoff_hi)
        base = normalize(base, level)
        for _ in range(int(drops * n / SR)):
            pos = r.randrange(n - 300)
            amp = r.uniform(0.05, 0.2) * level
            for k in range(160):
                base[pos + k] += amp * math.exp(-k / 25.0) * r.uniform(-1, 1)
        return base
    return texture(rng, build)


def ocean(rng):
    def build(r, n):
        surf = lowpass(noise(r, n), 500)
        hiss = bandpass(noise(r, n), 1200, 5000)
        swell = [0.55 + 0.45 * math.sin(TAU * i / (SR * 8) - 1.2) for i in range(n)]
        swell2 = [0.5 + 0.5 * math.sin(TAU * i / (SR * 4) + 0.5) for i in range(n)]
        return [s * 4.0 * w + h * 0.5 * w * w2 for s, h, w, w2 in zip(surf, hiss, swell, swell2)]
    return normalize(texture(rng, build), 0.6)


def forest(rng):
    def build(r, n):
        rustle = bandpass(noise(r, n), 250, 1800)
        lfo_a = [0.6 + 0.4 * math.sin(TAU * i / (SR * 8)) for i in range(n)]
        track = [v * 0.6 * w for v, w in zip(rustle, lfo_a)]
        track = normalize(track, 0.35)
        for _ in range(5):
            pos = r.randrange(n - SR // 2)
            f0 = r.uniform(2400, 3600)
            for k in range(int(SR * 0.18)):
                t = k / SR
                env = math.sin(math.pi * k / (SR * 0.18)) ** 2
                track[pos + k] += 0.12 * env * math.sin(TAU * (f0 + 900 * t / 0.18) * t)
        return track
    return normalize(texture(rng, build), 0.6)


def stream(rng):
    def build(r, n):
        base = bandpass(noise(r, n), 900, 5500)
        mod = lowpass(noise(r, n), 18)
        mod = normalize(mod, 1.0)
        return [b * (0.55 + 0.45 * m) for b, m in zip(base, mod)]
    return normalize(texture(rng, build), 0.6)


def wind(rng):
    def build(r, n):
        raw = noise(r, n)
        lo = bandpass(raw, 150, 700)
        hi = bandpass(raw, 700, 1600)
        a = [0.5 + 0.5 * math.sin(TAU * i / (SR * 8)) for i in range(n)]
        b = [0.5 + 0.5 * math.sin(TAU * i / (SR * 4) + 1.0) for i in range(n)]
        return [l * 2.0 * x + h * 1.2 * y for l, h, x, y in zip(lo, hi, a, b)]
    return normalize(texture(rng, build), 0.55)


def brown(rng):
    x = lowpass(noise(rng, N + SR), 180)
    return normalize(loop_crossfade(x), 0.7)


def white(rng):
    x = lowpass(noise(rng, N + SR), 7500)
    return normalize(loop_crossfade(x), 0.4)


def drone(rng):
    parts = []
    for f, a, ph in [(110, 1.0, 0.0), (165, 0.5, 0.7), (220, 0.35, 1.9), (277.5, 0.18, 0.3), (330, 0.12, 2.4)]:
        slow = [0.7 + 0.3 * math.sin(TAU * i / (SR * 8) + ph) for i in range(N)]
        parts.append([a * s * math.sin(TAU * f * i / SR + ph) for i, s in zip(range(N), slow)])
    return normalize(mix(*parts), 0.55)


def thunder(rng):
    def build(r, n):
        rumble = lowpass(noise(r, n), 120)
        env = [0.12 + 0.88 * math.exp(-((i % (SR * 8)) / SR - 1.0) ** 2 / 2.2) for i in range(n)]
        return [v * e for v, e in zip(rumble, env)]
    return normalize(texture(rng, build), 0.7)


def crickets(rng):
    out = [0.0] * N
    for freq, offset, rate in [(4300, 0.0, 0.62), (4750, 0.21, 0.5), (3900, 0.37, 0.8)]:
        t = offset
        while t < SECONDS:
            for p in range(3):
                start = int((t + p * 0.045) * SR)
                for k in range(int(SR * 0.03)):
                    env = math.sin(math.pi * k / (SR * 0.03)) ** 2
                    out[(start + k) % N] += 0.16 * env * math.sin(TAU * freq * k / SR)
            t += rate
    hum = lowpass(noise(rng, N), 400)
    return normalize(mix(out, [v * 0.15 for v in hum]), 0.5)


def bowl(rng):
    out = [0.0] * N
    for f, a, d in [(196.0, 1.0, 0.45), (196.0 * 2.71, 0.45, 0.7), (196.0 * 5.4, 0.18, 1.2), (198.5, 0.6, 0.5)]:
        wrap_add(out, 0, tone(f, N, d, a))
    return normalize(out, 0.7)


def bells(rng):
    out = [0.0] * N
    ratios = [(1.0, 1.0, 0.9), (2.0, 0.6, 1.3), (2.76, 0.5, 1.5), (5.4, 0.3, 2.2), (8.93, 0.15, 3.0)]
    for strike, base in [(0.0, 528.0), (4.0, 396.0)]:
        for r, a, d in ratios:
            wrap_add(out, int(strike * SR), tone(base * r, N, d, a))
    return normalize(out, 0.6)


def om(rng):
    parts = []
    for f, a in [(110.0, 1.0), (220.0, 0.6), (330.0, 0.35), (440.0, 0.2), (550.0, 0.1)]:
        breath = [0.75 + 0.25 * math.sin(TAU * i / (SR * 8) - 0.9) for i in range(N)]
        parts.append([a * b * math.sin(TAU * f * i / SR) for i, b in zip(range(N), breath)])
    return normalize(mix(*parts), 0.6)


def chirp(track, pos, f0, sweep, dur, amp):
    """Short bird-like pitch sweep, written into `track` (wraps around)."""
    length = int(SR * dur)
    for k in range(length):
        t = k / SR
        env = math.sin(math.pi * k / length) ** 2
        track[(pos + k) % len(track)] += amp * env * math.sin(TAU * (f0 + sweep * t / dur) * t)


def heavy_rain(rng):
    return normalize(rain(rng, 8000, 0.8, 45), 0.65)


def campfire(rng):
    def build(r, n):
        bed = lowpass(bandpass(noise(r, n), 60, 700), 700)
        flicker = [0.65 + 0.35 * math.sin(TAU * i / (SR * 4) + 0.7) for i in range(n)]
        track = [b * 2.5 * f for b, f in zip(bed, flicker)]
        for _ in range(int(14 * n / SR)):
            pos = r.randrange(n - 600)
            amp = r.uniform(0.15, 0.9)
            width = r.randrange(40, 380)
            for k in range(width):
                track[pos + k] += amp * math.exp(-k / (width / 4.0)) * r.uniform(-1, 1)
        return track
    return normalize(texture(rng, build), 0.65)


def cicadas(rng):
    out = [0.0] * N
    for carrier, pulse, phase, amp in [(4100, 62, 0.0, 1.0), (4650, 71, 1.3, 0.8), (3700, 55, 2.1, 0.7)]:
        for i in range(N):
            swell = 0.5 + 0.5 * math.sin(TAU * i / (SR * 2) + phase)
            buzz = 0.5 + 0.5 * math.sin(TAU * pulse * i / SR)
            out[i] += amp * swell * buzz * math.sin(TAU * carrier * i / SR)
    floor = lowpass(noise(rng, N), 600)
    return normalize(mix(out, [v * 1.5 for v in floor]), 0.5)


def night_wind(rng):
    def build(r, n):
        raw = lowpass(noise(r, n), 420)
        a = [0.55 + 0.45 * math.sin(TAU * i / (SR * 8) + 0.4) for i in range(n)]
        b = [0.6 + 0.4 * math.sin(TAU * 3 * i / (SR * 8)) for i in range(n)]
        return [v * x * y for v, x, y in zip(raw, a, b)]
    return normalize(texture(rng, build), 0.6)


def waterfall(rng):
    def build(r, n):
        raw = noise(r, n)
        body = bandpass(raw, 250, 7000)
        rumble = lowpass(raw, 160)
        return [b + 3.0 * u for b, u in zip(body, rumble)]
    return normalize(texture(rng, build), 0.6)


def dawn_birds(rng):
    def build(r, n):
        bed = bandpass(noise(r, n), 300, 1600)
        track = [v * 0.12 for v in bed]
        for _ in range(int(2.4 * n / SR)):
            pos = r.randrange(n - SR)
            f0 = r.uniform(2200, 3800)
            for note in range(r.randint(1, 4)):
                chirp(track, pos + int(note * SR * 0.13), f0 + r.uniform(-300, 300),
                      r.choice([-900, 700, 1200]), 0.1, r.uniform(0.08, 0.2))
        return track
    return normalize(texture(rng, build), 0.6)


def pink_noise(rng):
    base = noise(rng, N + SR)
    bands = [lowpass(base, f) for f in (60, 120, 240, 480, 960, 1920, 3840)]
    x = [sum(vals) for vals in zip(*bands)]
    return normalize(loop_crossfade(x), 0.55)


def fan(rng):
    def build(r, n):
        air = bandpass(noise(r, n), 200, 2600)
        hum = [0.25 * math.sin(TAU * 100 * i / SR) + 0.12 * math.sin(TAU * 200 * i / SR) for i in range(n)]
        blade = [0.9 + 0.1 * math.sin(TAU * 24 * i / SR) for i in range(n)]
        return [(a + h) * b for a, h, b in zip(air, hum, blade)]
    return normalize(texture(rng, build), 0.5)


def heartbeat(rng):
    out = [0.0] * N
    for beat in range(SECONDS):  # 60 bpm
        start = int(beat * SR)
        wrap_add(out, start, [a + b for a, b in zip(tone(55, SR // 3, 22, 1.0), tone(82, SR // 3, 30, 0.5))])
        wrap_add(out, start + int(0.3 * SR), [a + b for a, b in zip(tone(60, SR // 3, 26, 0.7), tone(90, SR // 3, 34, 0.35))])
    bed = lowpass(noise(rng, N), 120)
    return normalize(mix(out, [v * 0.1 for v in bed]), 0.75)


def chimes(rng):
    out = [0.0] * N
    notes = [523.3, 659.3, 784.0, 587.3, 880.0, 659.3, 523.3, 784.0]
    for i, f in enumerate(notes):
        start = int((i + rng.uniform(-0.1, 0.1)) * SR) % N
        for ratio, amp, decay in [(1.0, 1.0, 1.6), (2.0, 0.25, 2.4), (3.0, 0.1, 3.2)]:
            wrap_add(out, start, tone(f * ratio, SR * 3, decay, amp * 0.5))
    return normalize(out, 0.55)


def cavern(rng):
    parts = []
    for f, a, ph in [(55.0, 1.0, 0.0), (82.5, 0.55, 1.1), (110.0, 0.4, 2.2), (165.0, 0.18, 0.6)]:
        slow = [0.65 + 0.35 * math.sin(TAU * i / (SR * 8) + ph) for i in range(N)]
        parts.append([a * s * math.sin(TAU * f * i / SR + ph) for i, s in zip(range(N), slow)])
    air = lowpass(noise(rng, N), 260)
    return normalize(mix(*parts, [v * 0.6 for v in air]), 0.6)


def crystal_bowls(rng):
    out = [0.0] * N
    for i, f in enumerate([261.6, 329.6, 392.0, 523.3]):
        start = int(i * 2 * SR)
        for ratio, amp, decay in [(1.0, 1.0, 0.7), (2.76, 0.25, 1.1), (5.4, 0.08, 1.8)]:
            wrap_add(out, start, tone(f * ratio, N, decay, amp))
    return normalize(out, 0.55)


def gong(rng):
    out = [0.0] * N
    burst = [rng.uniform(-1, 1) * math.exp(-i / 500.0) for i in range(2000)]
    wrap_add(out, 0, [v * 0.4 for v in burst])
    for ratio, amp, decay in [(1.0, 1.0, 0.45), (1.51, 0.55, 0.6), (2.02, 0.45, 0.7), (2.77, 0.3, 0.9), (4.1, 0.15, 1.3)]:
        wrap_add(out, 0, tone(110.0 * ratio, N, decay, amp))
        wrap_add(out, 0, tone(110.0 * ratio * 1.004, N, decay, amp * 0.6))
    return normalize(out, 0.7)


def bell_once():
    """A single temple bell strike, 6 s, NOT looped; used as the start/end chime of meditation sessions."""
    n = SR * 6
    out = [0.0] * n
    for ratio, amp, decay in [(1.0, 1.0, 0.9), (2.0, 0.6, 1.3), (2.76, 0.5, 1.5), (5.4, 0.3, 2.2), (8.93, 0.15, 3.0)]:
        for i, v in enumerate(tone(528.0 * ratio, n, decay, amp)):
            out[i] += v
    return normalize(out, 0.7)


ONE_SHOTS = {'bell': bell_once}

SOUNDS = {
    's15': heavy_rain,
    's16': campfire,
    's17': cicadas,
    's18': night_wind,
    's19': waterfall,
    's20': dawn_birds,
    's21': pink_noise,
    's22': fan,
    's23': heartbeat,
    's24': chimes,
    's25': cavern,
    's26': crystal_bowls,
    's27': gong,
    's1': lambda r: normalize(rain(r, 6500, 0.5, 14), 0.6),
    's2': ocean,
    's3': forest,
    's4': stream,
    's5': wind,
    's6': brown,
    's7': white,
    's8': drone,
    's9': lambda r: normalize(rain(r, 3000, 0.5, 6), 0.5),
    's10': thunder,
    's11': crickets,
    's12': bowl,
    's13': bells,
    's14': om,
}


def write_wav(path, samples):
    with wave.open(path, 'wb') as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes(b''.join(struct.pack('<h', int(max(-1.0, min(1.0, v)) * 32767)) for v in samples))


if __name__ == '__main__':
    os.makedirs(OUT, exist_ok=True)
    for name, fn in SOUNDS.items():
        rng = random.Random(name)
        data = fn(rng)
        assert len(data) == N, (name, len(data))
        write_wav(os.path.join(OUT, name + '.wav'), data)
        print('wrote', name + '.wav')
    for name, fn in ONE_SHOTS.items():
        write_wav(os.path.join(OUT, name + '.wav'), fn())
        print('wrote', name + '.wav (one-shot)')
