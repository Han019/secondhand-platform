import { useMemo, useState } from 'react';
import { Bell, Bike, BookOpen, ChevronDown, Heart, MapPin, MessageCircle, MoreHorizontal, Search, Shirt, Smartphone, Sofa, Store, Thermometer, WashingMachine, Baby, Flame } from 'lucide-react';

const images = [
'https://www.figma.com/api/mcp/asset/79bd1309-5c07-460a-9684-26f493d39606.png',
'https://www.figma.com/api/mcp/asset/53269143-57a2-4379-913a-ba145366ac60.png',
'https://www.figma.com/api/mcp/asset/7b5cc3c1-34a2-4343-9f72-249fe0af63bd.png',
'https://www.figma.com/api/mcp/asset/21595244-38fc-4731-9b55-672218a771d0.png',
'https://www.figma.com/api/mcp/asset/9f279a2b-2fdd-49b3-a9ad-d085c620587c.png',
'https://www.figma.com/api/mcp/asset/f5586aa0-3571-4f7e-808c-6533667c8885.png',
'https://www.figma.com/api/mcp/asset/f62208e9-3737-48b5-ada1-09b997e68142.png',
'https://www.figma.com/api/mcp/asset/a24d1644-5ae2-4971-b9ca-0b6b226b66d4.png'];

const products = [
['맥북 프로 14인치 M2 Pro 실버 풀박스','역삼1동 · 2분 전','1,850,000','가격제안 불가'],
['노스피크 에어텐트 A6 풀세트 1회 피칭','도곡1동 · 8분 전','620,000','네고 가능'],
['자코모 3인용 천연 통가죽 소파','대치4동 · 15분 전','340,000','네고 가능'],
['에어팟 프로 2세대 USB-C 미개봉','역삼1동 · 21분 전','255,000','가격제안 불가'],
['닌텐도 스위치 OLED 화이트 + 젤다','역삼2동 · 35분 전','310,000','거래완료'],
['이케아 칼락스 4칸 수납 선반장','논현1동 · 42분 전','35,000','네고 가능'],
['아기 원목 블록 교구 & 도형 맞추기','역삼1동 · 50분 전','0','따뜻한 나눔'],
['자이언트 컨텐드 AR3 로드자전거','삼성2동 · 1시간 전','580,000','네고 가능']
];

const categories = [[Smartphone,'디지털기기'],[Sofa,'가구/인테리어'],[Baby,'유아동'],[WashingMachine,'생활가전'],[Bike,'스포츠/레저'],[Shirt,'의류/잡화'],[BookOpen,'도서'],[MoreHorizontal,'더보기']] as const;

function Header(){return <header><div className="header-inner"><div className="brand"><b>동네</b><span>마켓</span></div><button className="location"><MapPin size={15}/>역삼1동<ChevronDown size={13}/></button><label className="search"><input placeholder="물품이나 동네를 검색해보세요"/><Search size={18}/></label><nav><a>중고거래</a><a>동네생활</a><a>채팅하기</a><a>내 상점</a></nav><button className="sell">내 물건 팔기</button><Bell size={20}/></div></header>}

function ProductCard({p,i}:{p:string[],i:number}){const [liked,setLiked]=useState(false);return <article className={'product '+(p[3]==='거래완료'?'done':'')}><div className="photo"><img src={images[i]} alt={p[0]}/>{i===1&&<em>예약중</em>}{i===4&&<em className="gray">거래완료</em>}{i===6&&<em>나눔 🧡</em>}<button aria-label="찜" onClick={()=>setLiked(!liked)}><Heart size={18} fill={liked?'currentColor':'none'}/></button></div><div className="product-body"><h3>{p[0]}</h3><small>{p[1]}</small><strong>{p[2]}원</strong><div className="meta"><span className={p[3].includes('가능')?'green':''}>{p[3]}</span><span>♡ {i+4} · 💬 {i*3+2}</span></div></div></article>}

export default function App(){const [radius,setRadius]=useState(3);const [filter,setFilter]=useState('전체보기');const visible=useMemo(()=>products.filter(p=>filter==='나눔만 보기'?p[3].includes('나눔'):filter==='가격제안 가능'?p[3].includes('가능'):true),[filter]);return <><Header/><main><section className="control"><div className="verified"><span>➤</span><div><b>역삼1동 <i>인증완료</i></b><small>주변 반경 이웃 매물을 탐색 중입니다</small></div></div><div className="radius">탐색 반경 {[1,3,5,10].map(x=><button className={radius===x?'active':''} onClick={()=>setRadius(x)}>{x}km</button>)}</div><div className="filters">{['전체보기','가격제안 가능','나눔만 보기'].map(x=><button className={filter===x?'active':''} onClick={()=>setFilter(x)}>{x}</button>)}<button>정렬⌄</button></div></section><section className="category"><div className="section-title"><b>인기 카테고리</b><span>전체 카테고리 ›</span></div><div className="category-grid">{categories.map(([Icon,name])=><button><span><Icon size={23}/></span>{name}</button>)}</div></section><section className="trend"><div><span><Flame size={14}/> 실시간 급상승 키워드</span><b>지금 역삼1동 이웃들이 많이 찾는 인기 매물 🔥</b><small>선선해진 봄 날씨, 피크닉 & 자전거 용품의 거래가 38% 증가했어요!</small></div><div className="tags"><button>#캠핑용품</button><button>#맥북에어</button><button>#러닝화</button><button>#나눔</button></div></section><div className="layout"><section className="feed"><div className="section-title"><b>역삼1동 근처 매물 <mark>2,410개</mark></b><span>최신순 · 인기순 · 저가순</span></div><div className="products">{visible.map((p,i)=><ProductCard p={p} i={products.indexOf(p)} key={p[0]}/>)}</div><button className="more">중고거래 매물 더보기⌄</button><small className="scroll-hint">마우스 스크롤을 내리면 추가 매물이 자동으로 로드됩니다</small></section><aside><div className="panel"><div className="panel-title"><b><Thermometer size={19}/> 내 매너온도</b><span>자세히</span></div><div className="temp"><div><strong>39.8°C 😊</strong><em>상위 12%</em></div><progress value="62" max="100"/><small>기본 36.5°C　　　　　　　　　목표 42.0°C</small></div><p>최근 거래에서 <b>'친절하고 매너가 좋아요'</b> 평가를 14번 받았어요!</p></div><div className="panel"><div className="panel-title"><b><MessageCircle size={18}/> 이웃들의 따뜻한 후기</b><span>전체보기</span></div><div className="review"><b>🐰 강남토끼님 <small>10분 전</small></b><p>“약속시간도 정확히 지켜주시고, 상품 상태도 설명보다 훨씬 깨끗했어요!”</p><span>♡ 친절하고 매너가 좋아요</span></div><div className="review"><b>🐑 역삼동지박령 <small>1시간 전</small></b><p>“나눔해주신 책 너무 잘 읽고 있습니다. 좋은 이웃이 있어 든든해요.”</p><span>♡ 나눔을 실천해요</span></div></div><div className="panel"><div className="panel-title"><b><Store size={18}/> 우리 동네 인기 가게</b><span>더보기</span></div><div className="shop">🥐 <div><b>역삼 베이커리 르보눼</b><small>소금빵 & 깜파뉴 당일 갓 구움</small><span>당근 단골 10% 할인 쿠폰</span></div></div><div className="shop">🌿 <div><b>플라워 아틀리에 윤슬</b><small>생화 미니 꽃다발 & 반려식물</small><span>첫 방문 화분 영양제 증정</span></div></div></div></aside></div></main><footer><b>동네마켓</b><span>따뜻한 동네를 만드는 우리 동네 중고 직거래 마켓</span><span>서비스 안내　 중고거래 · 동네생활 · 당근알바</span><span>© Secondhand Platform</span></footer></>}
