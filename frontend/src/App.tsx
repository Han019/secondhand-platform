import {useEffect,useState,type FormEvent,type ReactNode} from 'react'
import {Bell,Heart,MapPin,Search,Upload,UserRound} from 'lucide-react'
import {Link,NavLink,Route,Routes,useNavigate,useParams,useSearchParams} from 'react-router-dom'
import {api,clearTokens,currentUserId,isLoggedIn,type Product,type ProductDetail as Detail,type ProductStatus} from './api'

const btn='rounded-full px-4 py-2 text-sm transition hover:opacity-90'
const price=(value:number)=>value.toLocaleString('ko-KR')+'원'
const statusText:Record<ProductStatus,string>={ON_SALE:'판매중',RESERVED:'예약중',SOLD_OUT:'거래완료'}
const message=(error:unknown)=>error instanceof Error?error.message:'요청에 실패했습니다.'
const emptyImage=<div className="grid h-full min-h-40 place-items-center bg-[#edf4fd] text-sm text-[#7c858d]">등록된 사진이 없습니다</div>

function Header(){
  const navigate=useNavigate()
  const [loggedIn,setLoggedIn]=useState(isLoggedIn())
  useEffect(()=>{const update=()=>setLoggedIn(isLoggedIn());window.addEventListener('auth-change',update);return()=>window.removeEventListener('auth-change',update)},[])
  async function logout(){try{await api.logout()}catch{}clearTokens();window.dispatchEvent(new Event('auth-change'));navigate('/')}
  return <header className="sticky top-0 z-50 border-b border-black/5 bg-white/90 backdrop-blur-xl"><div className="mx-auto flex min-h-20 max-w-[1200px] flex-wrap items-center gap-4 px-6 py-3">
    <Link to="/" className="flex items-center gap-2 font-bold text-[#a73400]">당근 <span className="rounded-full bg-[#ffdbcf] px-2 py-1 text-[#390c00]">마켓</span></Link>
    <span className={`${btn} flex items-center gap-1 bg-[#e7eff8]`}><MapPin size={14}/>지역 검색 준비 중</span>
    <form onSubmit={e=>{e.preventDefault();navigate('/search?keyword='+encodeURIComponent(String(new FormData(e.currentTarget).get('keyword')||'')))}} className="relative ml-auto max-w-md flex-1">
      <input name="keyword" className="w-full rounded-full bg-[#edf4fd] px-5 py-3 pr-11 outline-none" placeholder="물품을 검색해보세요"/><button aria-label="검색" className="absolute right-4 top-3.5 text-[#a73400]"><Search size={18}/></button>
    </form>
    <nav className="flex gap-4 text-sm"><NavLink to="/">중고거래</NavLink><NavLink to="/search">검색</NavLink><NavLink to="/chat">채팅</NavLink><NavLink to="/mypage">내 상점</NavLink></nav>
    <Link to={loggedIn?'/sell':'/login'} className={`${btn} bg-[#a73400] text-white`}>내 물건 팔기</Link>
    {loggedIn?<button onClick={logout} className="text-sm">로그아웃</button>:<Link to="/login" className="text-sm">로그인</Link>}<Bell size={20} className="text-[#9ca4ab]"/>
  </div></header>
}
function Shell({children}:{children:ReactNode}){return <><Header/>{children}<footer className="mt-16 bg-white px-8 py-10 text-center text-sm text-[#5b5f63]">당근마켓 · 동네 중고 직거래</footer></>}
function Card({children,className=''}:{children:ReactNode;className?:string}){return <div className={`rounded-[28px] bg-white shadow-[0_4px_24px_rgba(21,28,35,.06)] ${className}`}>{children}</div>}
function Notice({children}:{children:ReactNode}){return <p role="status" className="rounded-xl bg-[#edf4fd] p-4 text-sm text-[#5b5f63]">{children}</p>}

function useProducts(keyword='',sort='id,desc',page=0){
  const [items,setItems]=useState<Product[]>([])
  const [totalPages,setTotalPages]=useState(0)
  const [error,setError]=useState('')
  const [loading,setLoading]=useState(true)
  useEffect(()=>{
    let active=true
    setLoading(true);setError('')
    const params=new URLSearchParams({page:String(page),size:'10',sort})
    if(keyword.trim())params.set('keyword',keyword.trim())
    api.products(params).then(result=>{if(active){setItems(result.content);setTotalPages(result.totalPages)}}).catch(e=>{if(active)setError(message(e))}).finally(()=>{if(active)setLoading(false)})
    return()=>{active=false}
  },[keyword,sort,page])
  return {items,totalPages,error,loading}
}
function ProductCard({product}:{product:Product}){return <Link to={`/product/${product.id}`}><Card className="h-full overflow-hidden">{product.imageUrl?<img src={product.imageUrl} alt="" className="h-52 w-full object-cover"/>:emptyImage}<div className="p-4"><span className="text-xs text-[#a73400]">{statusText[product.status]}</span><h3 className="mt-1 font-semibold">{product.title}</h3><p className="mt-1 text-sm text-[#5b5f63]">{product.address||'거래 장소 미입력'}</p><strong className="mt-3 block">{price(product.price)}</strong></div></Card></Link>}

function Home(){
  const [page,setPage]=useState(0)
  const {items,totalPages,error,loading}=useProducts('', 'id,desc',page)
  return <Shell><main className="mx-auto max-w-[1200px] px-6 py-8"><div className="mb-6 flex items-center justify-between"><div><h1 className="text-2xl font-bold">중고거래 상품</h1><p className="text-sm text-[#5b5f63]">최근 등록된 상품을 살펴보세요.</p></div><Link to="/search" className="text-[#a73400]">검색하기 →</Link></div>
    {error?<Notice>{error}</Notice>:loading?<Notice>상품을 불러오는 중입니다.</Notice>:items.length?<div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">{items.map(product=><ProductCard key={product.id} product={product}/>)}</div>:<Notice>등록된 상품이 없습니다.</Notice>}
    <Pager page={page} total={totalPages} setPage={setPage}/></main></Shell>
}
function Pager({page,total,setPage}:{page:number;total:number;setPage:(page:number)=>void}){return total>1?<div className="mt-6 flex justify-center gap-3"><button disabled={page===0} onClick={()=>setPage(page-1)} className={btn}>이전</button><span className="px-2 py-2 text-sm">{page+1} / {total}</span><button disabled={page+1>=total} onClick={()=>setPage(page+1)} className={btn}>다음</button></div>:null}
function SearchPage(){
  const [params,setParams]=useSearchParams()
  const keyword=params.get('keyword')||''
  const sort=params.get('sort')||'id,desc'
  const [query,setQuery]=useState(keyword)
  const [page,setPage]=useState(0)
  const {items,totalPages,error,loading}=useProducts(keyword,sort,page)
  useEffect(()=>{setQuery(keyword);setPage(0)},[keyword,sort])
  function search(e:FormEvent){e.preventDefault();setPage(0);setParams({keyword:query.trim(),sort})}
  return <Shell><main className="mx-auto max-w-[1100px] px-6 py-8"><h1 className="mb-5 text-2xl font-bold">상품 검색</h1>
    <form onSubmit={search} className="flex gap-3"><input value={query} onChange={e=>setQuery(e.target.value)} aria-label="검색어" className="flex-1 rounded-full bg-white px-5 py-3 shadow outline-none" placeholder="제목 또는 설명 검색"/><button className={`${btn} bg-[#a73400] text-white`}>검색</button></form>
    <div className="my-6 flex items-center justify-between"><p className="text-sm">{keyword?`“${keyword}” 검색 결과`:'전체 상품'}</p><select aria-label="정렬" value={sort} onChange={e=>{setPage(0);setParams({keyword,sort:e.target.value})}} className="rounded-xl bg-white p-2 text-sm"><option value="id,desc">최신순</option><option value="price,asc">낮은 가격순</option><option value="price,desc">높은 가격순</option></select></div>
    {error?<Notice>{error}</Notice>:loading?<Notice>검색 중입니다.</Notice>:items.length?<div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">{items.map(product=><ProductCard key={product.id} product={product}/>)}</div>:<Notice>검색 결과가 없습니다.</Notice>}
    <Pager page={page} total={totalPages} setPage={setPage}/><p className="mt-6 text-xs text-[#7c858d]">가격·상태·지역 필터는 준비 중입니다.</p></main></Shell>
}

function ProductDetail(){
  const {id}=useParams()
  const navigate=useNavigate()
  const [product,setProduct]=useState<Detail|null>(null)
  const [error,setError]=useState('')
  const [busy,setBusy]=useState(false)
  const [selected,setSelected]=useState(0)
  const [editing,setEditing]=useState(false)
  async function reload(){if(id) setProduct(await api.product(id))}
  useEffect(()=>{let active=true;setProduct(null);setError('');if(id)api.product(id).then(p=>{if(active)setProduct(p)}).catch(e=>{if(active)setError(message(e))});return()=>{active=false}},[id])
  async function run(action:()=>Promise<unknown>){setBusy(true);setError('');try{await action();await reload();return true}catch(e){setError(message(e));return false}finally{setBusy(false)}}
  if(!product)return <Shell><main className="mx-auto max-w-[1100px] px-6 py-8"><Notice>{error||'상품을 불러오는 중입니다.'}</Notice></main></Shell>
  const current=product
  const mine=currentUserId()===product.seller.id
  const image=product.images[Math.min(selected,product.images.length-1)]
  async function saveEdit(e:FormEvent<HTMLFormElement>){e.preventDefault();const data=new FormData(e.currentTarget);if(await run(()=>api.updateProduct(current.id,{title:String(data.get('title')).trim(),description:String(data.get('description')).trim(),price:Number(data.get('price'))})))setEditing(false)}
  async function remove(){if(!confirm('이 상품을 삭제하시겠습니까?'))return;setBusy(true);try{await api.deleteProduct(current.id);navigate('/')}catch(e){setError(message(e));setBusy(false)}}
  async function move(index:number,direction:number){const ids=current.images.map(x=>x.id);[ids[index],ids[index+direction]]=[ids[index+direction],ids[index]];if(await run(()=>api.reorderImages(current.id,ids)))setSelected(index+direction)}
  return <Shell><main className="mx-auto max-w-[1100px] px-6 py-8">
    {error&&<Notice>{error}</Notice>}
    <div className="grid gap-8 lg:grid-cols-2"><div><Card className="overflow-hidden">{image?<img src={image.url} alt={product.title} className="h-[460px] w-full object-cover"/>:emptyImage}</Card><div className="mt-3 flex gap-2 overflow-auto">{product.images.map((img,index)=><button key={img.id} onClick={()=>setSelected(index)} aria-label={`사진 ${index+1}`} className={selected===index?'ring-2 ring-[#a73400]':''}><img src={img.url} alt="" className="h-20 w-20 object-cover"/></button>)}</div></div>
    <div><span className="rounded-full bg-[#ffdbcf] px-3 py-1 text-sm">{statusText[product.status]}</span><h1 className="mt-4 text-3xl font-bold">{product.title}</h1><p className="mt-2 text-[#5b5f63]">{product.address||'거래 장소 미입력'}</p><h2 className="mt-6 text-2xl font-bold">{price(product.price)}</h2><div className="mt-6 flex gap-3"><button disabled title="관심 기능 준비 중" className={`${btn} bg-[#e7eff8] opacity-50`}><Heart size={18}/></button><button disabled title="채팅 기능 준비 중" className={`${btn} flex-1 bg-[#a73400] text-white opacity-50`}>채팅 준비 중</button></div><Card className="mt-6 p-5"><b>상품 설명</b><p className="mt-3 whitespace-pre-wrap leading-7 text-[#5b5f63]">{product.description}</p></Card><Card className="mt-5 p-5"><b>판매자 정보</b><p className="mt-3">{product.seller.nickname} · 매너온도 {product.seller.mannerScore}°C</p></Card></div></div>
    {mine&&<Card className="mt-8 space-y-5 p-6"><h2 className="text-lg font-bold">내 상품 관리</h2><div className="flex flex-wrap gap-3"><button onClick={()=>setEditing(!editing)} className={`${btn} bg-[#e7eff8]`}>상품 정보 수정</button><select disabled={busy||product.status==='SOLD_OUT'} value={product.status} onChange={e=>run(()=>api.changeStatus(product.id,e.target.value as ProductStatus))} className="rounded-full bg-[#e7eff8] px-4"><option value="ON_SALE">판매중</option><option value="RESERVED">예약중</option><option value="SOLD_OUT">거래완료</option></select><button disabled={busy} onClick={remove} className={`${btn} bg-red-50 text-red-700`}>상품 삭제</button></div>
      {editing&&<form onSubmit={saveEdit} className="grid gap-3"><input name="title" required defaultValue={product.title} className="rounded-xl bg-[#edf4fd] p-3"/><textarea name="description" required defaultValue={product.description} className="rounded-xl bg-[#edf4fd] p-3"/><input name="price" type="number" min="0" required defaultValue={product.price} className="rounded-xl bg-[#edf4fd] p-3"/><button disabled={busy} className={`${btn} bg-[#a73400] text-white`}>변경 저장</button></form>}
      <div><b>사진 관리</b><p className="text-xs text-[#7c858d]">첫 사진이 목록에 표시됩니다. 최대 10장까지 등록할 수 있습니다.</p><div className="mt-3 flex flex-wrap gap-3">{product.images.map((img,index)=><div key={img.id} className="space-y-2"><img src={img.url} alt="" className="h-24 w-24 object-cover"/><div className="flex gap-1 text-xs"><button disabled={busy||index===0} onClick={()=>move(index,-1)}>←</button><button disabled={busy||index===product.images.length-1} onClick={()=>move(index,1)}>→</button><button disabled={busy} onClick={()=>run(()=>api.deleteImage(product.id,img.id))}>삭제</button></div></div>)}</div><label className="mt-4 block text-sm">사진 추가 <input type="file" accept="image/*" multiple disabled={busy||product.images.length>=10} onChange={e=>{const files=Array.from(e.target.files||[]);if(files.length){if(files.length+product.images.length>10){setError('사진은 최대 10장까지 등록할 수 있습니다.');return}run(()=>api.uploadImages(product.id,files))}e.target.value=''}} className="mt-2 block"/></label></div>
    </Card>}
  </main></Shell>
}

function SellPage(){
  const navigate=useNavigate()
  const [images,setImages]=useState<File[]>([])
  const [error,setError]=useState('')
  const [busy,setBusy]=useState(false)
  if(!isLoggedIn())return <Shell><main className="mx-auto max-w-[800px] px-6 py-8"><Notice>상품을 등록하려면 <Link to="/login" className="underline">로그인</Link>해 주세요.</Notice></main></Shell>
  async function submit(e:FormEvent<HTMLFormElement>){e.preventDefault();if(!images.length){setError('사진을 1장 이상 선택해 주세요.');return}const form=new FormData(e.currentTarget);setBusy(true);setError('');try{const result=await api.createProduct({title:String(form.get('title')).trim(),description:String(form.get('description')).trim(),price:Number(form.get('price')),address:String(form.get('address')).trim()},images);navigate(`/product/${result.productId}`)}catch(e){setError(message(e))}finally{setBusy(false)}}
  return <Shell><main className="mx-auto max-w-[900px] px-6 py-8"><Link to="/" className="text-sm">← 중고거래로 돌아가기</Link><Card className="mt-5 p-8"><h1 className="text-2xl font-bold">내 물건 팔기</h1><p className="mt-2 text-sm text-[#5b5f63]">상품 정보와 사진을 함께 등록합니다.</p>{error&&<div className="mt-5"><Notice>{error}</Notice></div>}
    <form onSubmit={submit} className="mt-7 space-y-5"><label className="block"><b>상품 사진 (1~10장)</b><span className="mt-2 flex items-center gap-2"><Upload size={18}/><input type="file" accept="image/*" multiple onChange={e=>setImages(Array.from(e.target.files||[]))}/></span><span className="text-xs text-[#7c858d]">{images.length}장 선택</span></label>
      <label className="block"><b>글 제목</b><input name="title" required maxLength={100} className="mt-2 w-full rounded-xl bg-[#edf4fd] p-4 outline-none"/></label>
      <label className="block"><b>판매 가격</b><input name="price" type="number" min="0" step="1" required className="mt-2 w-full rounded-xl bg-[#edf4fd] p-4 outline-none"/></label>
      <label className="block"><b>자세한 설명</b><textarea name="description" required rows={7} className="mt-2 w-full rounded-xl bg-[#edf4fd] p-4 outline-none"/></label>
      <label className="block"><b>거래 희망 장소</b><input name="address" placeholder="예: 역삼역 3번 출구" className="mt-2 w-full rounded-xl bg-[#edf4fd] p-4 outline-none"/></label>
      <button disabled={busy||images.length>10} className={`${btn} bg-[#a73400] px-7 text-white disabled:opacity-50`}>{busy?'등록 중...':'작성 완료하기'}</button>
    </form><p className="mt-4 text-xs text-[#7c858d]">카테고리, 임시저장, 지도 위치 선택은 준비 중입니다.</p></Card></main></Shell>
}
function ComingSoon({title}:{title:string}){return <Shell><main className="mx-auto max-w-[900px] px-6 py-12"><Card className="p-8"><div className="flex items-center gap-3"><UserRound/><h1 className="text-2xl font-bold">{title}</h1></div><p className="mt-5 text-[#5b5f63]">백엔드 기능을 준비 중입니다.</p><Link to="/" className="mt-5 inline-block text-[#a73400]">상품 보러 가기 →</Link></Card></main></Shell>}
export default function App(){return <Routes><Route path="/" element={<Home/>}/><Route path="/search" element={<SearchPage/>}/><Route path="/product/:id" element={<ProductDetail/>}/><Route path="/chat" element={<ComingSoon title="채팅"/>}/><Route path="/mypage" element={<ComingSoon title="내 상점"/>}/><Route path="/sell" element={<SellPage/>}/></Routes>}
