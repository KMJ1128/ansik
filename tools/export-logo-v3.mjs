import fs from 'node:fs';
import path from 'node:path';
import {createRequire} from 'node:module';
const require=createRequire(import.meta.url);
const sharp=require('C:/Users/kim23/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/sharp');
const source=process.argv[2];
if(!source)throw Error('Pass the approved/generated source PNG path');
const out=path.resolve('branding/ansik-logo-v3');
fs.mkdirSync(out,{recursive:true});
fs.copyFileSync(source,path.join(out,'source-generated.png'));
// Mechanical export only: preserve generated artwork; no redrawing or recoloring.
const mark=await sharp(source).trim().toBuffer();
async function canvas(size,ratio,bg){const art=await sharp(mark).resize(Math.round(size*ratio),Math.round(size*ratio),{fit:'inside'}).toBuffer();return sharp({create:{width:size,height:size,channels:4,background:bg}}).composite([{input:art,gravity:'centre'}]).png().toBuffer();}
const clear={r:0,g:0,b:0,alpha:0};
fs.writeFileSync(path.join(out,'ansik-in-app-512.png'),await canvas(512,.86,clear));
fs.writeFileSync(path.join(out,'ansik-adaptive-foreground-432.png'),await canvas(432,.46,clear));
fs.writeFileSync(path.join(out,'ansik-play-store-512.png'),await canvas(512,.74,'#FFF9F0'));
fs.copyFileSync(path.join(out,'ansik-in-app-512.png'),'app/src/main/res/drawable-nodpi/ansik_logo_final.png');
fs.copyFileSync(path.join(out,'ansik-adaptive-foreground-432.png'),'app/src/main/res/drawable-nodpi/ansik_logo_adaptive.png');
for(const [density,size] of Object.entries({mdpi:48,hdpi:72,xhdpi:96,xxhdpi:144,xxxhdpi:192})){
 const square=await canvas(size,.72,'#FFF9F0');
 await sharp(square).webp({lossless:true}).toFile(`app/src/main/res/mipmap-${density}/ic_launcher.webp`);
 const mask=Buffer.from(`<svg width="${size}" height="${size}"><circle cx="${size/2}" cy="${size/2}" r="${size/2}" fill="white"/></svg>`);
 await sharp(square).composite([{input:mask,blend:'dest-in'}]).webp({lossless:true}).toFile(`app/src/main/res/mipmap-${density}/ic_launcher_round.webp`);
}
console.log('Logo exported to '+out+' and Android launcher/login resources.');
