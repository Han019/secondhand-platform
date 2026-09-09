import {useLocation} from 'react-router-dom'
import App from './App'
import {LoginPage,SignupPage} from './AuthPages'

export default function RootApp(){
  const {pathname}=useLocation()

  if(pathname==='/login') return <LoginPage/>
  if(pathname==='/signup') return <SignupPage/>

  return <App/>
}
