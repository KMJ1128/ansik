import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import {createRequire} from 'node:module';
const require=createRequire(import.meta.url);
const sharp=require('C:/Users/kim23/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/sharp');
for(const name of ['course','meal','search'])test(name+' has valid native shape/animation data',()=>{
 const a=JSON.parse(fs.readFileSync(`branding/memphis-motion-review/${name}.json`));
 assert.equal(a.fr,30);assert.equal(a.op,120);assert.deepEqual(a.assets,[]);
 assert(a.layers.length>30);assert.equal(new Set(a.layers.map(l=>l.ind)).size,a.layers.length);
 function check(v){if(typeof v==='number')assert(Number.isFinite(v));if(v&&typeof v==='object'){if(v.a===1){assert(v.k.length>=2);v.k.forEach((k,i)=>{assert(k.t>=0&&k.t<=120);if(i)assert(k.t>v.k[i-1].t);});}Object.values(v).forEach(check);}}
 check(a);
 for(const l of a.layers){assert.equal(l.ty,4);const b=l.shapes[0].ks.k;assert.equal(b.v.length,b.i.length);assert.equal(b.v.length,b.o.length);assert(b.v.length>1);}
});
test('Android launcher export sizes',async()=>{
 for(const [d,n]of Object.entries({mdpi:48,hdpi:72,xhdpi:96,xxhdpi:144,xxxhdpi:192}))for(const suffix of ['', '_round']){
 const m=await sharp(`app/src/main/res/mipmap-${d}/ic_launcher${suffix}.webp`).metadata();assert.equal(m.width,n);assert.equal(m.height,n);
 }
 const m=await sharp('app/src/main/res/drawable-nodpi/ansik_logo_adaptive.png').metadata();assert.equal(m.width,432);assert(m.hasAlpha);
});
test('Animation previews are not installed as app assets',()=>{
 for(const dir of ['app/src/main/assets','app/src/main/res/raw'])if(fs.existsSync(dir))for(const name of ['course.json','meal.json','search.json'])assert(!fs.existsSync(`${dir}/${name}`));
});
