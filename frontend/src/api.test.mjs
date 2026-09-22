import test from 'node:test'
import assert from 'node:assert/strict'

const fakeStorage = () => {
  const values = new Map()
  return {getItem:key=>values.get(key)??null,setItem:(key,value)=>values.set(key,value),removeItem:key=>values.delete(key)}
}
globalThis.localStorage = fakeStorage()
globalThis.sessionStorage = fakeStorage()
const {api,saveTokens} = await import('./api.ts')

test('만료된 access token을 재발급한 뒤 원래 요청을 다시 보낸다', async () => {
  saveTokens({accessToken:'expired',refreshToken:'valid'},false)
  const calls=[]
  globalThis.fetch=async (url,init) => {
    calls.push({url,init})
    if(url==='/api/auth/refresh') return Response.json({accessToken:'new'})
    if(init.headers.get('Authorization')==='Bearer expired') return new Response(null,{status:401})
    return Response.json({content:[],number:0,totalPages:0,totalElements:0})
  }
  await api.products(new URLSearchParams())
  assert.equal(calls.length,1)
  await api.deleteProduct(3)
  assert.equal(calls[1].init.headers.get('Authorization'),'Bearer expired')
  assert.equal(calls[2].init.headers.get('Authorization'),null)
  assert.equal(calls[3].init.headers.get('Authorization'),'Bearer new')
})
