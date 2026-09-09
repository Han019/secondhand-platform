import {useState, type FormEvent, type ReactNode} from 'react'
import {Eye,EyeOff,LockKeyhole,Mail,ShieldCheck,UserRound} from 'lucide-react'
import {Link} from 'react-router-dom'

const inputClass='w-full rounded-2xl border border-black/10 bg-[#f6faff] px-4 py-3.5 pl-11 text-sm outline-none transition placeholder:text-[#8b9298] focus:border-[#a73400] focus:bg-white focus:ring-4 focus:ring-[#a73400]/10'

function AuthLayout({title,description,children}:{title:string,description:string,children:ReactNode}){
  return <main className="min-h-screen bg-[#edf4fd] px-5 py-8 text-[#151c23]">
    <div className="mx-auto grid min-h-[calc(100vh-4rem)] max-w-[1040px] overflow-hidden rounded-[32px] bg-white shadow-[0_24px_80px_rgba(21,28,35,.12)] lg:grid-cols-[.92fr_1.08fr]">
      <section className="relative hidden overflow-hidden bg-[#a73400] p-10 text-white lg:flex lg:flex-col">
        <div className="absolute -right-20 -top-20 h-64 w-64 rounded-full bg-white/10"/>
        <div className="absolute -bottom-24 -left-16 h-72 w-72 rounded-full bg-[#ffdbcf]/20"/>
        <Link to="/" className="relative z-10 text-2xl font-bold">당근 <span className="rounded-full bg-[#ffdbcf] px-2.5 py-1 text-base text-[#390c00]">마켓</span></Link>
        <div className="relative z-10 my-auto max-w-sm">
          <span className="inline-flex items-center gap-2 rounded-full bg-white/15 px-3 py-1.5 text-sm"><ShieldCheck size={16}/>우리 동네 안심 중고거래</span>
          <h1 className="mt-5 text-4xl font-bold leading-tight">가까운 이웃과<br/>따뜻한 거래를 시작하세요.</h1>
          <p className="mt-4 leading-7 text-white/75">동네 이웃의 물건을 발견하고, 채팅으로 약속을 잡고, 믿을 수 있는 직거래를 이어갑니다.</p>
        </div>
        <p className="relative z-10 text-sm text-white/60">secondhand platform</p>
      </section>
      <section className="flex items-center justify-center px-6 py-10 sm:px-12 lg:px-16">
        <div className="w-full max-w-md">
          <Link to="/" className="mb-10 inline-flex font-bold text-[#a73400] lg:hidden">당근마켓</Link>
          <h2 className="text-3xl font-bold">{title}</h2>
          <p className="mt-2 text-sm leading-6 text-[#6b7177]">{description}</p>
          {children}
        </div>
      </section>
    </div>
  </main>
}

function Field({label,icon,children}:{label:string,icon:ReactNode,children:ReactNode}){
  return <label className="block">
    <span className="mb-2 block text-sm font-semibold">{label}</span>
    <div className="relative">
      <span className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-[#778087]">{icon}</span>
      {children}
    </div>
  </label>
}

function PasswordField({label,placeholder='비밀번호를 입력해주세요',autoComplete}:{label:string,placeholder?:string,autoComplete:string}){
  const [visible,setVisible]=useState(false)
  return <Field label={label} icon={<LockKeyhole size={18}/>}>
    <input type={visible?'text':'password'} required autoComplete={autoComplete} placeholder={placeholder} className={`${inputClass} pr-11`}/>
    <button type="button" onClick={()=>setVisible(!visible)} aria-label={visible?'비밀번호 숨기기':'비밀번호 보기'} className="absolute right-4 top-1/2 -translate-y-1/2 text-[#778087] hover:text-[#a73400]">
      {visible?<EyeOff size={18}/>:<Eye size={18}/>} 
    </button>
  </Field>
}

export function LoginPage(){
  const submit=(event:FormEvent<HTMLFormElement>)=>event.preventDefault()
  return <AuthLayout title="로그인" description="이메일과 비밀번호로 로그인하고 동네 거래를 이어가세요.">
    <form onSubmit={submit} className="mt-8 space-y-5">
      <Field label="이메일" icon={<Mail size={18}/>}>
        <input type="email" required autoComplete="email" placeholder="example@email.com" className={inputClass}/>
      </Field>
      <PasswordField label="비밀번호" autoComplete="current-password"/>
      <div className="flex items-center justify-between gap-3 text-sm">
        <label className="flex items-center gap-2 text-[#5b5f63]"><input type="checkbox" className="h-4 w-4 accent-[#a73400]"/>로그인 상태 유지</label>
        <button type="button" className="font-medium text-[#a73400] hover:underline">비밀번호를 잊으셨나요?</button>
      </div>
      <button type="submit" className="w-full rounded-2xl bg-[#a73400] px-5 py-3.5 font-semibold text-white shadow-lg shadow-orange-900/15 transition hover:opacity-90">로그인</button>
    </form>
    <p className="mt-8 text-center text-sm text-[#6b7177]">아직 계정이 없으신가요? <Link to="/signup" className="font-semibold text-[#a73400] hover:underline">회원가입</Link></p>
  </AuthLayout>
}

export function SignupPage(){
  const submit=(event:FormEvent<HTMLFormElement>)=>event.preventDefault()
  return <AuthLayout title="회원가입" description="기본 정보를 입력하고 우리 동네 중고거래를 시작하세요.">
    <form onSubmit={submit} className="mt-8 space-y-5">
      <Field label="닉네임" icon={<UserRound size={18}/>}>
        <input type="text" required maxLength={20} autoComplete="nickname" placeholder="사용할 닉네임을 입력해주세요" className={inputClass}/>
      </Field>
      <Field label="이메일" icon={<Mail size={18}/>}>
        <input type="email" required autoComplete="email" placeholder="example@email.com" className={inputClass}/>
      </Field>
      <PasswordField label="비밀번호" placeholder="8자 이상 입력해주세요" autoComplete="new-password"/>
      <PasswordField label="비밀번호 확인" placeholder="비밀번호를 다시 입력해주세요" autoComplete="new-password"/>
      <label className="flex items-start gap-2 text-sm leading-6 text-[#5b5f63]"><input type="checkbox" required className="mt-1 h-4 w-4 shrink-0 accent-[#a73400]"/><span>서비스 이용약관 및 개인정보 처리방침에 동의합니다.</span></label>
      <button type="submit" className="w-full rounded-2xl bg-[#a73400] px-5 py-3.5 font-semibold text-white shadow-lg shadow-orange-900/15 transition hover:opacity-90">회원가입</button>
    </form>
    <p className="mt-8 text-center text-sm text-[#6b7177]">이미 계정이 있으신가요? <Link to="/login" className="font-semibold text-[#a73400] hover:underline">로그인</Link></p>
  </AuthLayout>
}
