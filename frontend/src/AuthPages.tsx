import {useState,type FormEvent,type ReactNode} from 'react'
import {ArrowLeft,Eye,EyeOff,Leaf,ShieldCheck} from 'lucide-react'
import {Link,useLocation,useNavigate} from 'react-router-dom'
import {api,saveTokens} from './api'

const inputClass='w-full rounded-xl bg-[#eaf1fb] px-4 py-3 text-sm outline-none focus:ring-2 focus:ring-[#c44400]'
const buttonClass='rounded-full bg-[#c44400] px-5 py-3 text-sm font-semibold text-white disabled:opacity-50'
const errorMessage=(error:unknown)=>error instanceof Error?error.message:'요청에 실패했습니다.'

function Page({children}:{children:ReactNode}){return <div className="min-h-screen bg-[#f3f7fc] text-[#252b31]"><header className="h-14 bg-white"><div className="mx-auto flex h-full max-w-[1180px] items-center justify-between px-5"><Link to="/" className="flex items-center gap-2 font-semibold text-[#c44400]"><Leaf size={16}/>당근 마켓</Link><Link to="/" className="flex items-center gap-1 text-xs"><ArrowLeft size={13}/>홈으로 돌아가기</Link></div></header>{children}</div>}
function Field({label,children}:{label:string;children:ReactNode}){return <label className="block space-y-2"><span className="text-sm font-semibold">{label}</span>{children}</label>}
function Password({name,confirm=false}:{name:string;confirm?:boolean}){const [visible,setVisible]=useState(false);return <div className="relative"><input name={name} type={visible?'text':'password'} required minLength={confirm?8:undefined} maxLength={confirm?20:undefined} autoComplete={confirm?'new-password':'current-password'} className={`${inputClass} pr-12`}/><button type="button" onClick={()=>setVisible(!visible)} aria-label={visible?'비밀번호 숨기기':'비밀번호 보기'} className="absolute right-4 top-3.5">{visible?<EyeOff size={17}/>:<Eye size={17}/>}</button></div>}

export function LoginPage(){
  const navigate=useNavigate()
  const notice=useLocation().state?.notice as string|undefined
  const [error,setError]=useState('')
  const [busy,setBusy]=useState(false)
  async function submit(e:FormEvent<HTMLFormElement>){e.preventDefault();const form=new FormData(e.currentTarget);setBusy(true);setError('');try{const tokens=await api.login(String(form.get('loginId')).trim(),String(form.get('password')));saveTokens(tokens,form.get('remember')==='on');window.dispatchEvent(new Event('auth-change'));navigate('/')}catch(e){setError(errorMessage(e))}finally{setBusy(false)}}
  return <Page><main className="mx-auto max-w-[480px] px-5 py-10"><section className="rounded-[30px] bg-white p-8 shadow-sm"><div className="text-center"><Leaf className="mx-auto text-[#c44400]" size={30}/><h1 className="mt-3 text-2xl font-bold">당근 로그인</h1><p className="mt-2 text-xs text-[#687078]">당신 근처의 따뜻한 중고 직거래 마켓</p></div>
    {notice&&<p role="status" className="mt-6 text-sm text-[#138272]">{notice}</p>}<form onSubmit={submit} className="mt-8 space-y-5"><Field label="아이디"><input name="loginId" required autoComplete="username" className={inputClass} placeholder="로그인 아이디"/></Field><Field label="비밀번호"><Password name="password"/></Field><label className="flex items-center gap-2 text-xs"><input name="remember" type="checkbox"/>로그인 상태 유지</label>{error&&<p role="alert" className="text-sm text-red-700">{error}</p>}<button disabled={busy} className={`${buttonClass} w-full`}>{busy?'로그인 중...':'로그인 →'}</button></form>
    <p className="mt-6 text-center text-xs">아직 회원이 아니신가요? <Link to="/signup" className="text-[#c44400] underline">회원가입하기</Link></p><p className="mt-5 text-center text-xs text-[#7c858d]">휴대폰·소셜 로그인과 계정 찾기는 준비 중입니다.</p>
  </section><aside className="mt-5 flex gap-3 rounded-2xl bg-[#eaf1fb] p-4 text-xs text-[#616970]"><ShieldCheck size={18} className="text-[#c44400]"/><p>안전한 거래를 위해 계정 정보를 보호해 주세요.</p></aside></main></Page>
}

export function SignupPage(){
  const navigate=useNavigate()
  const [error,setError]=useState('')
  const [notice,setNotice]=useState('')
  const [busy,setBusy]=useState(false)
  const [loginId,setLoginId]=useState('')
  const [nickname,setNickname]=useState('')
  async function check(field:'login-id'|'nickname'){const value=(field==='login-id'?loginId:nickname).trim();if(!value)return;setError('');try{const result=await api.available(field,value);setNotice(result.isAvailable?'사용 가능한 값입니다.':'이미 사용 중인 값입니다.');}catch(e){setError(errorMessage(e))}}
  async function submit(e:FormEvent<HTMLFormElement>){e.preventDefault();const form=new FormData(e.currentTarget);const password=String(form.get('password'));if(password!==form.get('confirm')){setError('비밀번호가 일치하지 않습니다.');return}setBusy(true);setError('');try{await api.signup({loginId:loginId.trim(),password,email:String(form.get('email')).trim(),nickname:nickname.trim()});navigate('/login',{state:{notice:'회원가입이 완료되었습니다. 로그인해 주세요.'}})}catch(e){setError(errorMessage(e))}finally{setBusy(false)}}
  return <Page><main className="mx-auto max-w-[600px] px-5 py-8"><div className="text-center"><h1 className="text-2xl font-bold">당근마켓 회원가입</h1><p className="mt-2 text-sm text-[#727a81]">이웃들과 따뜻한 거래를 시작해 보세요.</p></div><section className="mt-7 rounded-[22px] bg-white p-8 shadow-sm">
    <form onSubmit={submit} className="space-y-5"><Field label="이메일 주소"><input name="email" type="email" required autoComplete="email" className={inputClass}/></Field><p className="text-xs text-[#7c858d]">이메일 인증은 준비 중입니다. 현재는 가입 시 이메일 소유 여부를 확인하지 않습니다.</p>
      <Field label="아이디"><div className="flex gap-2"><input required minLength={4} maxLength={16} autoComplete="username" value={loginId} onChange={e=>{setLoginId(e.target.value);setNotice('')}} className={inputClass} placeholder="4~16자"/><button type="button" onClick={()=>check('login-id')} className="shrink-0 rounded-full bg-[#dfe7f0] px-4 text-xs">중복확인</button></div></Field>
      <Field label="비밀번호 (8~20자)"><Password name="password" confirm/></Field><Field label="비밀번호 재확인"><Password name="confirm" confirm/></Field>
      <Field label="닉네임"><div className="flex gap-2"><input required minLength={2} maxLength={10} value={nickname} onChange={e=>{setNickname(e.target.value);setNotice('')}} className={inputClass} placeholder="2~10자"/><button type="button" onClick={()=>check('nickname')} className="shrink-0 rounded-full bg-[#dfe7f0] px-4 text-xs">중복확인</button></div></Field>
      {notice&&<p role="status" className="text-xs text-[#138272]">{notice}</p>}{error&&<p role="alert" className="text-sm text-red-700">{error}</p>}
      <div className="space-y-2 rounded-xl bg-[#edf3fb] p-4 text-xs"><label className="flex gap-2"><input type="checkbox" required/>서비스 이용약관 동의</label><label className="flex gap-2"><input type="checkbox" required/>개인정보 수집 및 이용 동의</label><label className="flex gap-2"><input type="checkbox" required/>만 14세 이상입니다.</label></div>
      <button disabled={busy} className={`${buttonClass} w-full`}>{busy?'가입 중...':'가입하기 →'}</button><p className="text-center text-xs">이미 계정이 있으신가요? <Link to="/login" className="text-[#c44400] underline">로그인하기</Link></p>
    </form></section></main></Page>
}
