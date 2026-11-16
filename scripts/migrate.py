#!/usr/bin/env python3
"""Re-apply the Universal Podcasts rebrand after each upstream sync (idempotent)."""
from pathlib import Path
import os, shutil
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent

def main():
    REPL = [
        ('de.danoeh.antennapod','com.universalpodcast.app'),
        ('github.com/AntennaPod','github.com/yashhasan570/UniversalPodcasts'),
        ('antennapod.org','github.com/yashhasan570/UniversalPodcasts'),
        ('AntennaPod','Universal Podcasts'),
        ('danoeh','universalpodcast'),
        ('antennapod','universalpodcast'),
    ]
    TEXT_EXTS = {'.kt','.java','.xml','.gradle','.kts','.md','.json','.yml','.yaml','.txt','.properties','.pro','.html','.js','.sh'}
    SKIP = {'.github', '.git','node_modules','scripts'}
    changed = 0
    for dirpath, dirnames, filenames in os.walk(ROOT):
        dirnames[:] = [d for d in dirnames if d not in SKIP]
        for fn in filenames:
            p = Path(dirpath)/fn
            if p.suffix.lower() not in TEXT_EXTS and p.suffix != '': continue
            try: s = p.read_text(encoding='utf-8')
            except Exception: continue
            o = s
            for a,b in REPL: s = s.replace(a,b)
            if s != o:
                p.write_text(s, encoding='utf-8'); changed += 1
    moved = 0
    for src in list(ROOT.glob('**/de/danoeh/antennapod')):
        if '.git' in src.parts: continue
        target = src.parents[2] / 'com' / 'universalpodcast' / 'app'
        target.parent.mkdir(parents=True, exist_ok=True)
        if target.exists():
            for item in src.iterdir():
                shutil.move(str(item), str(target/item.name))
        else:
            src.rename(target)
        for pdir in [src.parent, src.parent.parent, src.parent.parent.parent]:
            try: pdir.rmdir()
            except OSError: pass
        moved += 1
    badge = ROOT/'scripts/icons/badge.png'
    n = 0
    if badge.exists():
        img = Image.open(badge).convert('RGBA')
        for fg in ROOT.glob('**/mipmap-*/ic_launcher_foreground.png'):
            if '.git' in fg.parts: continue
            ref = Image.open(fg); w,h = ref.size
            canvas = Image.new('RGBA',(w,h),(0,0,0,0))
            inner = img.resize((int(w*0.66),int(w*0.66)), Image.LANCZOS)
            canvas.paste(inner, ((w-inner.size[0])//2,(h-inner.size[1])//2), inner)
            canvas.save(fg)
            bg = fg.parent/'ic_launcher_background.png'
            if bg.exists():
                Image.new('RGBA',(w,h),(13,27,42,255)).save(bg)
            n+=1
        for legacy in ROOT.glob('**/mipmap-*/ic_launcher.png'):
            if '.git' in legacy.parts: continue
            ref = Image.open(legacy); w,h = ref.size
            canvas = Image.new('RGBA',(w,h),(0,0,0,0))
            inner = img.resize((w,h), Image.LANCZOS)
            canvas.paste(inner,(0,0),inner)
            canvas.save(legacy); n+=1
    print(f'migrate: {changed} files swept, {moved} package dirs moved, {n} icon files updated')

if __name__ == '__main__':
    main()
