import fs from 'node:fs';
import path from 'node:path';
import {createRequire} from 'node:module';
const require=createRequire(import.meta.url);
const sharp=require('C:/Users/kim23/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/sharp');
const out=path.resolve('branding/memphis-motion-review');
// Original vector illustrations. Approval is required before installing into Android.
const p={ink:'#1E3455',blue:'#548EE6',mint:'#7DC6AD',teal:'#28766C',coral:'#EF6272',skin:'#F5AD99',gold:'#F6CC68',gray:'#DFE7EA'};
const S=k=>({a:0,k});
const rgb=h=>h.slice(1).match(/../g).map(v=>parseInt(v,16)/255).concat(1);
const keys=pts=>({a:1,k:pts.map(([t,v],i)=>({t,s:[v],...(i<pts.length-1?{e:[pts[i+1][1]],o:{x:[.42],y:[0]},i:{x:[.58],y:[1]}}:{})}))});
function bezier(d){const q=d.match(/[MLCZ]|-?\d*\.?\d+/g),v=[],i=[],o=[];let n=0,c=false;while(n<q.length){let cmd=q[n++];if(cmd==='Z'){c=true;continue;}if(cmd==='M'||cmd==='L'){v.push([+q[n++],+q[n++]]);i.push([0,0]);o.push([0,0]);}else if(cmd==='C'){let a=[+q[n++],+q[n++]],b=[+q[n++],+q[n++]],pt=[+q[n++],+q[n++]];o[v.length-1]=a.map((x,k)=>x-v.at(-1)[k]);v.push(pt);i.push(b.map((x,k)=>x-pt[k]));o.push([0,0]);}else throw Error(cmd);}return {v,i,o,c};}
const oval=(x,y,rx,ry)=>`M ${x+rx} ${y} C ${x+rx} ${y+ry*.5523} ${x+rx*.5523} ${y+ry} ${x} ${y+ry} C ${x-rx*.5523} ${y+ry} ${x-rx} ${y+ry*.5523} ${x-rx} ${y} C ${x-rx} ${y-ry*.5523} ${x-rx*.5523} ${y-ry} ${x} ${y-ry} C ${x+rx*.5523} ${y-ry} ${x+rx} ${y-ry*.5523} ${x+rx} ${y} Z`;
const rect=(x,y,w,h)=>`M ${x} ${y} L ${x+w} ${y} L ${x+w} ${y+h} L ${x} ${y+h} Z`;
function scene(mode){const a=[];const add=(name,d,fill,opt={})=>a.push({name,d,fill,...opt});const line=(name,d,stroke,width=3,opt={})=>add(name,d,null,{stroke,width,...opt});
add('Backdrop','M 99 306 C 42 232 92 133 188 145 C 274 159 300 78 384 112 C 480 155 533 292 464 333 C 365 382 169 365 99 306 Z','#E5F0FC');
add('Ground',oval(302,361,221,24),'#C6E0EF');
add('Tall leaf','M 463 354 C 428 318 421 247 461 242 C 509 238 527 301 463 354 Z',p.mint,{pivot:[463,354],sway:3});
add('Middle leaf','M 467 355 C 478 301 516 278 533 306 C 553 341 496 364 467 355 Z',p.teal,{pivot:[467,355],sway:-3});
add('Low leaf','M 465 355 C 407 356 393 317 416 303 C 445 286 470 324 465 355 Z','#43A58A',{pivot:[465,355],sway:2});
line('Leaf spine','M 463 354 C 464 320 468 280 461 254','#388A74',1.4,{pivot:[463,354],sway:3});
line('Leaf vein','M 465 314 C 452 305 447 297 444 287','#388A74',1.2,{pivot:[463,354],sway:3});
line('Small vein','M 468 354 C 492 337 510 321 521 312',p.mint,1.3,{pivot:[467,355],sway:-3});
add('Paper edge','M 142 105 C 141 91 151 80 166 80 L 303 80 C 319 80 325 89 328 102 L 362 348 L 180 348 Z','#BBC9D5');
add('Paper','M 151 101 C 149 88 157 76 174 76 L 308 76 C 322 76 330 85 333 101 L 367 344 L 188 344 Z','#FFFFFF');
add('Clip','M 207 84 L 211 68 C 214 59 220 62 219 51 C 216 32 242 30 245 48 C 246 60 250 63 258 65 L 269 84 Z',p.blue);
add('Clip hole',oval(231,48,4,4),p.ink);
if(mode==='course'){
 for(let r=0;r<5;r++){add('Row '+r,rect(184+r*4,121+r*39,90-(r%2)*15,7),r<3?p.gold:p.gray);add('Check base '+r,oval(301+r*5,124+r*39,11,11),'#EDF4F6');if(r<3)line('Check '+r,`M ${295+r*5} ${124+r*39} L ${301+r*5} ${130+r*39} L ${310+r*5} ${116+r*39}`,r===1?p.gold:p.teal,4,{draw:r*24+8});}
}else if(mode==='meal'){
 add('Menu heading',rect(190,113,88,7),p.gold);add('Plate rim',oval(267,203,62,63),'#E5F0FC');add('Plate',oval(267,203,48,49),'#FFFFFF');
 add('Rice','M 239 209 C 224 175 268 159 276 188 C 293 214 255 231 239 209 Z',p.gold);
 add('Vegetable','M 273 217 C 257 197 276 180 294 184 C 311 201 293 224 273 217 Z',p.teal);
 line('Vegetable vein','M 272 215 L 292 192',p.mint,2);add('Tomato',oval(245,221,12,11),p.coral);add('Ingredient row',rect(213,287,77,6),p.gray);
 line('Ingredient check','M 306 286 L 312 292 L 323 278',p.teal,4,{draw:36});
}else{
 add('Map left','M 178 127 L 225 137 L 246 272 L 200 261 Z','#E2EFE8');add('Map middle','M 235 138 L 270 126 L 293 260 L 256 273 Z','#D3E8F8');add('Map right','M 281 124 L 321 135 L 341 265 L 303 258 Z','#E2EFE8');
 line('Route','M 214 238 C 269 238 235 180 298 178',p.gold,5,{draw:8});
 add('Destination','M 278 170 C 256 141 276 119 294 128 C 314 136 308 157 278 170 Z',p.coral,{pivot:[285,170],sway:4});add('Pin hole',oval(287,143,7,7),'#FFFFFF',{pivot:[285,170],sway:4});add('Subtitle',rect(210,300,103,6),p.gray);
}
// Curved articulated seated figure, anchored at shoulder and knees.
add('Far leg','M 395 272 C 384 275 372 292 361 314 L 335 356 L 316 353 C 322 323 330 287 345 269 C 360 255 381 256 395 272 Z','#263F69');
add('Far shoe','M 316 347 L 334 353 L 332 362 C 318 365 303 369 299 365 C 299 359 310 355 316 347 Z',p.ink);
add('Bent leg','M 405 271 C 429 299 409 321 378 327 L 357 333 C 369 341 403 342 431 348 L 431 361 C 394 367 347 362 335 350 C 322 337 340 316 371 308 L 379 292 Z',p.ink);
add('Sock','M 427 347 L 443 348 L 440 359 L 428 360 Z','#FFFFFF');add('Near shoe','M 442 347 C 450 353 456 357 464 359 L 468 367 C 455 371 442 365 435 360 Z',p.ink);
add('Neck','M 375 168 L 387 166 L 393 186 L 378 191 Z',p.skin);
add('Face','M 363 144 C 359 131 380 129 389 139 L 390 155 C 397 163 383 171 377 170 L 370 158 L 365 158 Z',p.skin);
add('Hair','M 362 143 C 352 137 354 126 365 124 C 382 120 397 130 397 143 C 397 153 391 160 386 163 L 383 145 L 377 151 L 374 140 C 368 143 366 145 362 143 Z',p.ink);add('Ear',oval(381,149,3,5),p.skin);
add('Shirt','M 378 182 C 391 173 406 184 414 201 C 424 225 419 251 410 276 C 394 287 374 284 363 272 L 362 230 L 350 211 C 354 198 365 187 378 182 Z',p.coral);
add('Shirt shade','M 395 187 C 421 207 424 242 410 276 C 399 283 386 281 382 279 C 399 252 405 214 395 187 Z','#E54F66');
add('Resting arm','M 407 198 C 426 214 438 239 438 271 L 433 291 L 425 289 L 428 269 C 426 244 414 228 402 215 Z',p.coral);add('Resting hand','M 426 283 L 434 285 C 435 298 429 310 424 307 C 419 305 423 297 424 293 Z',p.skin);
const arm={pivot:[365,205],sway:mode==='search'?3:-2.5};
add('Pointing sleeve','M 370 194 C 383 204 374 219 359 224 C 333 237 311 233 289 210 L 298 201 C 324 222 340 213 353 200 Z',p.coral,arm);
add('Pointing hand','M 299 204 L 292 213 C 281 205 278 195 270 191 L 266 186 C 264 180 269 180 274 184 L 277 186 L 269 175 C 267 171 272 170 275 174 L 287 188 Z',p.skin,arm);
line('Sleeve seam','M 349 207 C 339 219 326 222 315 218','#FA8A93',1.6,arm);
add('Floating triangle','M 451 128 L 469 150 L 444 154 Z',p.blue,{pivot:[455,145],sway:9});add('Small triangle','M 109 234 L 124 244 L 108 251 Z',p.blue,{pivot:[116,245],sway:-12});return a;}
function layer(s,index){let shapes=[{ty:'sh',ks:S(bezier(s.d)),nm:s.name}];if(s.fill)shapes.push({ty:'fl',c:S(rgb(s.fill)),o:S(100),r:1});if(s.stroke)shapes.push({ty:'st',c:S(rgb(s.stroke)),o:S(100),w:S(s.width),lc:2,lj:2});if(s.draw!==undefined)shapes.push({ty:'tm',s:S(0),e:keys([[0,0],[s.draw,0],[s.draw+22,100],[108,100],[120,0]]),o:S(0),m:1});const pt=s.pivot||[0,0];return {ddd:0,ind:index+1,ty:4,nm:s.name,sr:1,ks:{o:S(100),r:s.sway?keys([[0,-s.sway],[60,s.sway],[120,-s.sway]]):S(0),p:S([...pt,0]),a:S([...pt,0]),s:S([100,100,100])},shapes,ip:0,op:120,st:0,bm:0};}
const docs=[];for(const [key,title,caption] of [['course','여행 코스 만들기','체크리스트를 차례로 채우는 여행자'],['meal','식사 정보 살펴보기','식재료와 메뉴를 확인하는 여행자'],['search','주변 장소 찾기','지도 위 경로를 찾는 여행자']]){const shapes=scene(key),data={v:'5.13.0',fr:30,ip:0,op:120,w:600,h:420,nm:title,ddd:0,assets:[],layers:[...shapes].reverse().map(layer)};fs.writeFileSync(path.join(out,key+'.json'),JSON.stringify(data));const svg=`<svg xmlns="http://www.w3.org/2000/svg" width="900" height="630" viewBox="0 0 600 420"><rect width="600" height="420" fill="#FFF9F0"/>${shapes.map(s=>`<path d="${s.d}" fill="${s.fill||'none'}" stroke="${s.stroke||'none'}" stroke-width="${s.width||0}" stroke-linecap="round" stroke-linejoin="round"/>`).join('')}</svg>`;fs.writeFileSync(path.join(out,key+'.svg'),svg);await sharp(Buffer.from(svg)).png().toFile(path.join(out,key+'.png'));docs.push({key,title,caption,data});}
fs.writeFileSync(path.join(out,'index.html'),`<!doctype html><html lang="ko"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>안식 모션 시안 02</title><style>*{box-sizing:border-box}body{margin:0;background:#FFF9F0;color:#1E3455;font-family:Segoe UI,Malgun Gothic,sans-serif}main{max-width:1400px;margin:auto;padding:48px 30px}small{color:#28766C;letter-spacing:2px}h1{font-size:32px}p{line-height:1.8;color:#626477}section{display:grid;grid-template-columns:repeat(3,1fr);gap:20px;margin:30px 0}article{background:white;border-radius:24px;padding:18px;border:1px solid #E7E1D8}.motion{aspect-ratio:600/420;background:#FFF9F0;border-radius:16px}h2{font-size:19px}button,a{display:inline-block;background:#28766C;color:white;border:0;border-radius:10px;padding:10px 16px;font:inherit;text-decoration:none;cursor:pointer}a{background:#E1F1E9;color:#194E49}footer{font-size:13px;color:#626477}@media(max-width:850px){section{grid-template-columns:1fr}}</style><main><small>ANSIK / MOTION STUDY 02</small><h1>여행을 차근차근 준비하는 순간</h1><p>인물의 손짓, 차례로 그려지는 체크와 경로, 작게 흔들리는 잎.<br>새 시안입니다. 아직 앱에 적용하지 않았습니다.</p><button id="toggle">일시 정지</button> <button id="restart">처음부터 보기</button><p id="error"></p><section>${docs.map(d=>`<article><div class="motion" id="${d.key}" role="img" aria-label="${d.caption}"></div><h2>${d.title}</h2><p>${d.caption}</p><a href="${d.key}.json" download>Lottie JSON</a></article>`).join('')}</section><footer>직접 제작한 벡터 일러스트 · 실제 lottie-web 5.13.0 플레이어 · 4초 반복 · 30fps<br>안전 판정이 아닌 로딩 일러스트입니다. 동작 줄이기 설정에서는 자동 재생하지 않습니다.</footer></main><script src="lottie.min.js"></script><script>const docs=${JSON.stringify(docs)};let paused=matchMedia('(prefers-reduced-motion: reduce)').matches;const players=[];if(!window.lottie){document.getElementById('error').textContent='플레이어 파일을 찾지 못했습니다. 같은 폴더의 PNG를 확인하세요.'}else{for(const d of docs){players.push(lottie.loadAnimation({container:document.getElementById(d.key),renderer:'svg',loop:true,autoplay:!paused,animationData:d.data}))}}const b=document.getElementById('toggle');const label=()=>b.textContent=paused?'재생':'일시 정지';label();b.onclick=()=>{paused=!paused;players.forEach(p=>paused?p.pause():p.play());label()};document.getElementById('restart').onclick=()=>players.forEach(p=>paused?p.goToAndStop(0,true):p.goToAndPlay(0,true));</script></html>`);
console.log('Generated '+docs.length+' review-only Lottie illustrations.');
