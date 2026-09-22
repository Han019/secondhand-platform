/// <reference types="vite/client" />
export type ProductStatus = 'ON_SALE' | 'RESERVED' | 'SOLD_OUT'
export type Product = {id:number; title:string; price:number; status:ProductStatus; address:string|null; imageUrl:string|null}
export type ProductDetail = Omit<Product,'imageUrl'> & {description:string; latitude:number|null; longitude:number|null; createdAt:string; seller:{id:number; nickname:string; profileImage:string|null; mannerScore:number}; images:{id:number; url:string}[]}
export type ProductPage = {content:Product[]; number:number; totalPages:number; totalElements:number}

const base = import.meta.env?.VITE_API_BASE_URL || ''
const keys = ['accessToken','refreshToken'] as const
const storage = () => localStorage.getItem(keys[1]) ? localStorage : sessionStorage
const token = (key:typeof keys[number]) => storage().getItem(key)

export function saveTokens(tokens:{accessToken:string; refreshToken:string}, remember:boolean) {
  clearTokens()
  const target = remember ? localStorage : sessionStorage
  keys.forEach(key => target.setItem(key, tokens[key]))
}
export function clearTokens() { keys.forEach(key => {localStorage.removeItem(key); sessionStorage.removeItem(key)}) }
export const isLoggedIn = () => Boolean(token('refreshToken'))
export function currentUserId() {
  try { return Number(JSON.parse(atob(token('accessToken')!.split('.')[1].replace(/-/g,'+').replace(/_/g,'/'))).sub) || null }
  catch { return null }
}

async function send<T>(path:string, init:RequestInit={}, auth=false, retry=true):Promise<T> {
  const headers = new Headers(init.headers)
  if (!(init.body instanceof FormData) && init.body) headers.set('Content-Type','application/json')
  if (auth && token('accessToken')) headers.set('Authorization',`Bearer ${token('accessToken')}`)
  const response = await fetch(`${base}${path}`, {...init, headers})
  if (response.status===401 && auth && retry && token('refreshToken')) {
    try {
      const fresh = await send<{accessToken:string}>('/api/auth/refresh',{method:'POST',body:JSON.stringify({refreshToken:token('refreshToken')})})
      storage().setItem('accessToken',fresh.accessToken)
      return send<T>(path,init,auth,false)
    } catch { clearTokens() }
  }
  if (!response.ok) {
    const error = await response.json().catch(()=>null)
    throw new Error(error?.message || `요청에 실패했습니다. (${response.status})`)
  }
  return response.status===204 || response.status===201 && !response.headers.get('content-type') ? undefined as T : response.json()
}

const json = (body:unknown) => JSON.stringify(body)
export const api = {
  login: (loginId:string,password:string) => send<{accessToken:string;refreshToken:string}>('/api/auth/login',{method:'POST',body:json({loginId,password})}),
  signup: (data:{loginId:string;password:string;email:string;nickname:string}) => send<void>('/api/auth/signup',{method:'POST',body:json(data)}),
  available: (field:'login-id'|'nickname', value:string) => send<{isAvailable:boolean}>(`/api/auth/check/${field}?${field==='login-id'?'loginId':'nickname'}=${encodeURIComponent(value)}`),
  logout: () => send<void>('/api/auth/logout',{method:'POST'},true),
  products: (params:URLSearchParams) => send<ProductPage>(`/api/products?${params}`),
  product: (id:string|number) => send<ProductDetail>(`/api/products/${id}`),
  createProduct: (data:{title:string;description:string;price:number;address:string}, images:File[]) => {
    const form = new FormData()
    form.append('product',new Blob([json(data)],{type:'application/json'}))
    images.forEach(image=>form.append('image',image))
    return send<{productId:number}>('/api/products',{method:'POST',body:form},true)
  },
  updateProduct: (id:number,data:{title:string;description:string;price:number}) => send<void>(`/api/products/${id}`,{method:'PATCH',body:json(data)},true),
  changeStatus: (id:number,status:ProductStatus) => send<void>(`/api/products/${id}/status`,{method:'PATCH',body:json({status})},true),
  deleteProduct: (id:number) => send<void>(`/api/products/${id}`,{method:'DELETE'},true),
  uploadImages: (id:number,images:File[]) => {const form=new FormData(); images.forEach(image=>form.append('image',image)); return send(`/api/products/${id}/images`,{method:'POST',body:form},true)},
  deleteImage: (id:number,imageId:number) => send<void>(`/api/products/${id}/images/${imageId}`,{method:'DELETE'},true),
  reorderImages: (id:number,imageIds:number[]) => send<void>(`/api/products/${id}/images/order`,{method:'PATCH',body:json(imageIds)},true),
}
