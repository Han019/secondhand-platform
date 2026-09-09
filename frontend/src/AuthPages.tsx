import {useState, type FormEvent, type ReactNode} from 'react'
import {ArrowLeft,CheckCircle2,Eye,EyeOff,Leaf,LockKeyhole,Mail,MessageCircle,ShieldCheck,Smartphone,UserRound} from 'lucide-react'
import {Link} from 'react-router-dom'

const ORANGE='#c44400'
const pageBg='bg-[#f3f7fc]'
const inputClass='w-full rounded-xl border border-transparent bg-[#eaf1fb] px-4 py-3 text-sm outline-none transition placeholder:text-[#8d969f] focus:border-[#c44400]/30 focus:bg-white focus:ring-4 focus:ring-[#c44400]/8'

function BrandHeader(){
  return <header className="h-14 border-b border-black/[.04] bg-white">
    <div className="mx-auto flex h-full max-w-[1180px] items-center justify-between px-5 sm:px-8">
      <Link to="/" className="flex items-center gap-2 text-sm font-semibold text-[#c44400]">
        <Leaf size={15} fill="currentColor"/> 당근 <span className="rounded-full bg-[#f8e0d7] px-2 py-1 text-[11px] text-[#8f2f00]">마켓</span>
      </Link>
      <Link to="/" className="flex items-center gap-1 text-xs text-[#737b82] hover:text-[#c44400]"><ArrowLeft size={13}/>홈으로 돌아가기</Link>
    </div>
  </header>
}

function Footer(){
  return <footer className="pb-8 pt-6 text-center text-[11px] text-[#8c949b]">
    <nav className="mb-8 flex flex-wrap justify-center gap-x-5 gap-y-2 text-xs text-[#676f76]">
      <button>자주 묻는 질문</button><span>·</span><button>고객센터 문의</button><span>·</span><button>이용약관 및 개인정보</button>
    </nav>
    <p className="text-[#c1c7cc]">© Danggeun Market Inc. All rights reserved.</p>
  </footer>
}

function Page({children}:{children:ReactNode}){
  return <div className={`min-h-screen ${pageBg} text-[#252b31]`}><BrandHeader/>{children}</div>
}

function PasswordInput({placeholder='비밀번호 입력',autoComplete='current-password'}:{placeholder?:string,autoComplete?:string}){
  const [visible,setVisible]=useState(false)
  return <div className="relative">
    <LockKeyhole className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[#89939c]" size={15}/>
    <input type={visible?'text':'password'} required autoComplete={autoComplete} placeholder={placeholder} className={`${inputClass} pl-10 pr-10`}/>
    <button type="button" onClick={()=>setVisible(!visible)} aria-label={visible?'비밀번호 숨기기':'비밀번호 보기'} className="absolute right-3.5 top-1/2 -translate-y-1/2 text-[#7d858c]">
      {visible?<EyeOff size={15}/>:<Eye size={15}/>} 
    </button>
  </div>
}

export function LoginPage(){
  const [loginMode,setLoginMode]=useState<'id'|'phone'>('id')
  const submit=(event:FormEvent<HTMLFormElement>)=>event.preventDefault()

  return <Page>
    <main className="mx-auto max-w-[540px] px-5 pb-3 pt-2 sm:pt-3">
      <div className="mb-3 flex justify-center"><span className="rounded-full bg-[#e2f5f1] px-3 py-1 text-[11px] font-medium text-[#138272]">● 이웃 3,600만이 함께하는 안심 직거래</span></div>

      <section className="rounded-[30px] bg-[radial-gradient(circle_at_75%_0%,#fbe9e2_0,#fff7f3_25%,white_58%)] px-7 pb-7 pt-7 shadow-[0_10px_28px_rgba(48,59,70,.06)] sm:px-8">
        <div className="text-center">
          <div className="mx-auto grid h-12 w-12 place-items-center rounded-full bg-[#f9d8ca] text-[#c44400]"><Leaf size={22} fill="currentColor"/></div>
          <h1 className="mt-3 text-sm font-semibold">당근 로그인</h1>
          <p className="mt-1 text-xs text-[#687078]">당신 근처의 따뜻한 중고 직거래 마켓</p>
        </div>

        <form onSubmit={submit} className="mt-7 space-y-4">
          <div>
            <div className="mb-2 flex items-center justify-between text-[11px]">
              <div className="flex gap-4">
                <button type="button" onClick={()=>setLoginMode('id')} className={loginMode==='id'?'font-semibold text-[#252b31]':'text-[#8a9298]'}>아이디 또는 전화번호</button>
                <button type="button" onClick={()=>setLoginMode('phone')} className={loginMode==='phone'?'font-semibold text-[#252b31]':'text-[#8a9298]'}>휴대폰 번호 로그인 지원</button>
              </div>
            </div>
            <div className="relative">
              {loginMode==='id'?<UserRound className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[#89939c]" size={15}/>:<Smartphone className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[#89939c]" size={15}/>} 
              <input required placeholder={loginMode==='id'?'아이디 또는 휴대폰 번호 입력':'휴대폰 번호 입력'} className={`${inputClass} pl-10`}/>
            </div>
          </div>

          <div>
            <div className="mb-2 flex items-center justify-between text-[11px]"><span className="font-semibold">비밀번호</span><button type="button" className="text-[#6e757b]">아이디 찾기 · 비밀번호 찾기</button></div>
            <PasswordInput/>
          </div>

          <div className="flex items-center justify-between text-[11px]">
            <label className="flex items-center gap-2"><input type="checkbox" className="h-3.5 w-3.5 accent-[#c44400]"/>로그인 상태 유지</label>
            <span className="rounded-full bg-[#dff6e9] px-2.5 py-1 font-medium text-[#2b9a6d]">안전모드 켬</span>
          </div>

          <button type="submit" className="w-full rounded-full bg-[#db4b00] py-2.5 text-xs font-semibold text-white shadow-[0_8px_18px_rgba(196,68,0,.25)] transition hover:opacity-90">로그인 →</button>
        </form>

        <div className="my-4 flex items-center gap-3 text-[10px] text-[#9aa1a7]"><div className="h-px flex-1 bg-[#edf0f2]"/><span>또는 다른 방법으로 로그인</span><div className="h-px flex-1 bg-[#edf0f2]"/></div>
        <div className="space-y-2">
          <button className="flex w-full items-center justify-center gap-2 rounded-full bg-[#fee500] py-2.5 text-xs font-medium text-[#191919]"><MessageCircle size={14} fill="currentColor"/>카카오로 시작하기</button>
          <div className="grid grid-cols-3 gap-2">
            <button className="rounded-full bg-[#e1f7ec] py-2 text-[11px] font-medium text-[#17a96b]">N 네이버</button>
            <button className="rounded-full bg-[#e6ebf1] py-2 text-[11px] font-medium text-[#20262c]">● Apple</button>
            <button className="rounded-full bg-[#dfe7ef] py-2 text-[11px] font-medium text-[#56616b]">▣ 간편인증</button>
          </div>
        </div>

        <div className="mt-8 text-center text-xs text-[#747b82]">아직 당근 회원이 아니신가요?<br/><Link to="/signup" className="mt-1 inline-block font-medium text-[#c44400]">이메일로 회원가입하기</Link></div>
      </section>

      <aside className="mx-auto mt-4 rounded-[22px] bg-[#edf2f8] px-5 py-4 text-xs text-[#616970]">
        <div className="flex gap-3"><ShieldCheck size={17} className="mt-0.5 shrink-0 text-[#db4b00]"/><div><b className="font-semibold text-[#3a4147]">당근 안심 보호 정책</b><p className="mt-1 leading-5">안전하고 따뜻한 거래를 위해 본인인증이 완료된 계정으로 이용해주세요. 타인의 명의 도용 및 사기 거래 시 즉각 제재 조치됩니다.</p></div></div>
      </aside>
    </main>
    <Footer/>
  </Page>
}

function FieldLabel({children,right}:{children:ReactNode,right?:ReactNode}){
  return <div className="mb-2 flex items-center justify-between gap-3 text-[11px]"><span className="font-semibold">{children}</span>{right&&<span className="text-[10px] text-[#7c858d]">{right}</span>}</div>
}

function SideButton({children}:{children:ReactNode}){
  return <button type="button" className="shrink-0 rounded-full bg-[#dfe7f0] px-4 py-3 text-[11px] font-medium text-[#4f5962] transition hover:bg-[#d5dee8]">{children}</button>
}

export function SignupPage(){
  const [codeSent,setCodeSent]=useState(false)
  const [pwVisible,setPwVisible]=useState(false)
  const submit=(event:FormEvent<HTMLFormElement>)=>event.preventDefault()

  return <Page>
    <main className="mx-auto max-w-[680px] px-5 pb-4 pt-0 sm:pt-0">
      <div className="text-center">
        <span className="inline-flex -translate-y-1 items-center rounded-b-lg bg-[#f8d9cc] px-3 py-1.5 text-[10px] font-semibold text-[#a13a09]">👥 따뜻한 우리 동네 생활의 시작</span>
        <h1 className="mt-2 text-2xl font-semibold">당근마켓 회원가입</h1>
        <p className="mt-2 text-xs text-[#727a81]">이웃들과 따뜻한 온기를 나누며 믿을 수 있는 직거래를 경험해보세요.</p>
      </div>

      <div className="mx-auto mt-6 flex max-w-[520px] items-center rounded-2xl bg-[#edf2f8] p-2 text-[10px] text-[#9ca4ab]">
        <span className="flex items-center gap-2 rounded-xl bg-[#c44400] px-4 py-2 font-semibold text-white"><b className="grid h-5 w-5 place-items-center rounded-full bg-white text-[#c44400]">1</b>본인인증 및 계정생성</span>
        <div className="mx-2 h-1 flex-1 rounded-full bg-[#d7e0e9]"><div className="h-full w-1/3 rounded-full bg-[#c44400]"/></div>
        <span className="flex items-center gap-1 px-2"><b className="grid h-5 w-5 place-items-center rounded-full bg-[#e2e8ee] text-[#8b949c]">2</b>정보 입력</span>
        <span className="flex items-center gap-1 px-2"><b className="grid h-5 w-5 place-items-center rounded-full bg-[#e2e8ee] text-[#8b949c]">3</b>가입 완료</span>
      </div>

      <section className="mx-auto mt-7 max-w-[520px] rounded-[22px] bg-white px-7 py-8 shadow-[0_4px_10px_rgba(44,52,60,.08)] sm:px-9">
        <form onSubmit={submit} className="space-y-5">
          <div>
            <FieldLabel right="계정 로그인 및 알림용">이메일 주소 <span className="text-[#c44400]">*</span></FieldLabel>
            <div className="flex gap-2"><input type="email" required autoComplete="email" placeholder="example@danggeun.com" className={inputClass}/><SideButton><span onClick={()=>setCodeSent(true)}>인증번호 발송</span></SideButton></div>
            <p className="mt-2 text-[10px] text-[#7b848c]">발신 소식 및 중요 거래 알림을 수신할 이메일을 입력해주세요.</p>
          </div>

          <div className="rounded-2xl bg-[#eaf1fb] p-3">
            <FieldLabel right={<span className="rounded-full bg-[#f9d9cc] px-2 py-1 font-semibold text-[#d05215]">{codeSent?'02:59':'03:00'}</span>}>인증번호 확인</FieldLabel>
            <div className="flex gap-2"><input inputMode="numeric" maxLength={6} placeholder="6자리 숫자 입력" className="min-w-0 flex-1 rounded-xl bg-white px-4 py-3 text-sm outline-none placeholder:text-[#9aa2a9]"/><button type="button" className="shrink-0 rounded-full bg-[#c44400] px-4 text-[11px] font-semibold text-white">인증 확인</button></div>
          </div>

          <div>
            <FieldLabel>아이디 <span className="text-[#c44400]">*</span></FieldLabel>
            <div className="flex gap-2"><input required minLength={4} maxLength={16} placeholder="영문 소문자, 숫자 조합 4~16자" className={inputClass}/><SideButton>중복확인</SideButton></div>
          </div>

          <div>
            <FieldLabel right="영문, 숫자, 특수문자 조합 8~20자">비밀번호 <span className="text-[#c44400]">*</span></FieldLabel>
            <div className="relative"><input type={pwVisible?'text':'password'} required minLength={8} maxLength={20} autoComplete="new-password" placeholder="영문, 숫자, 특수문자 조합 8~20자" className={`${inputClass} pr-10`}/><button type="button" onClick={()=>setPwVisible(!pwVisible)} className="absolute right-3.5 top-1/2 -translate-y-1/2 text-[#79838b]">{pwVisible?<EyeOff size={15}/>:<Eye size={15}/>}</button></div>
            <div className="mt-2 flex items-center gap-3"><div className="h-1.5 flex-1 rounded-full bg-[#dce5ec]"><div className="h-full w-3/5 rounded-full bg-[#00a887]"/></div><span className="flex items-center gap-1 text-[10px] font-medium text-[#168773]"><CheckCircle2 size={11}/>강력 (보안 좋음)</span></div>
          </div>

          <div>
            <FieldLabel>비밀번호 재확인 <span className="text-[#c44400]">*</span></FieldLabel>
            <PasswordInput placeholder="비밀번호를 한 번 더 입력해주세요" autoComplete="new-password"/>
          </div>

          <div>
            <FieldLabel>닉네임 <span className="text-[#c44400]">*</span></FieldLabel>
            <div className="flex gap-2"><input required minLength={2} maxLength={10} autoComplete="nickname" placeholder="이웃에게 보여질 닉네임 (2~10자)" className={inputClass}/><SideButton>중복확인</SideButton></div>
            <p className="mt-2 text-[10px] text-[#6d757c]">♡ 개성 있고 따뜻한 닉네임을 설정해보세요! (욕설 및 비방 단어 제한)</p>
          </div>

          <div className="space-y-3 rounded-xl bg-[#edf3fb] px-4 py-4 text-[11px] text-[#4d565e]">
            <label className="flex items-center justify-between gap-3 font-medium"><span className="flex items-center gap-2"><input type="checkbox" className="h-4 w-4 accent-[#c44400]"/>당근마켓 서비스 이용약관 전체 동의</span><span className="text-[10px] text-[#c44400]">선택 항목 포함</span></label>
            <label className="flex items-center justify-between"><span className="flex items-center gap-2"><input type="checkbox" required/>서비스 이용약관 동의</span><button type="button" className="text-[10px] underline">전문보기</button></label>
            <label className="flex items-center justify-between"><span className="flex items-center gap-2"><input type="checkbox" required/>개인정보 수집 및 이용 동의</span><button type="button" className="text-[10px] underline">전문보기</button></label>
            <label className="flex items-center justify-between"><span className="flex items-center gap-2"><input type="checkbox" required/>만 14세 이상입니다.</span><span className="text-[10px]">확인</span></label>
            <label className="flex items-center justify-between"><span className="flex items-center gap-2"><input type="checkbox"/>마케팅 정보 수신 동의 (이메일/SMS)</span><button type="button" className="text-[10px] underline">전문보기</button></label>
          </div>

          <button type="submit" className="mt-2 w-full rounded-full bg-[#b93c00] py-3.5 text-sm font-semibold text-white shadow-[0_8px_20px_rgba(185,60,0,.22)]">가입하기 →</button>
          <p className="text-center text-[11px] text-[#7b838a]">이미 계정이 있으신가요? <Link to="/login" className="font-medium text-[#c44400]">로그인하기</Link></p>
        </form>
      </section>

      <aside className="mx-auto mt-6 flex max-w-[520px] items-center gap-3 rounded-2xl bg-[#e4edf6] px-4 py-3">
        <div className="grid h-10 w-10 shrink-0 place-items-center rounded-full bg-[#41e0ba] text-[#096b58]"><ShieldCheck size={18}/></div>
        <div className="min-w-0 flex-1"><b className="text-[10px]">따뜻하고 매너 있는 직거래</b><p className="mt-0.5 text-[9px] text-[#68737c]">실제 배운 이웃 인증으로 안심하고 안전하게 소통할 수 있어요.</p></div>
        <span className="shrink-0 rounded-full bg-[#fff0e8] px-3 py-1.5 text-xs font-semibold text-[#c44400]">36.5°C ♡</span>
      </aside>
    </main>
    <div className="pb-8 pt-5 text-center text-[10px] text-[#c3c9ce]">© Danggeun Market Inc. All rights reserved.</div>
  </Page>
}
