// 简单的端点检测脚本，根据 API.md 列表逐个请求并打印结果
// 运行：node scripts/check_endpoints.js

const API_BASE = process.env.VITE_API_BASE || 'http://localhost:8080'
const timeoutMs = 8000

const endpoints = [
  { name: 'co-comment', method: 'GET', path: (id) => `/api/recommend/co-comment/${id}` },
  { name: 'reply', method: 'GET', path: (id) => `/api/recommend/reply/${id}` },
  { name: 'shared-video', method: 'GET', path: (id) => `/api/recommend/shared-video/${id}` },
  { name: 'category', method: 'GET', path: (id) => `/api/recommend/category/${id}` },
  { name: 'theme', method: 'GET', path: (id) => `/api/recommend/theme/${id}` },
  { name: 'user-behavior', method: 'GET', path: (id) => `/api/recommend/user-behavior/${id}` },
  { name: 'common-up', method: 'GET', path: (id) => `/api/recommend/common-up/${id}` },
  { name: 'favorite-similarity', method: 'GET', path: (id) => `/api/recommend/favorite-similarity/${id}` },

  { name: 'filter.same-up', method: 'GET', path: () => `/api/filter/same-up?upId=1&durationOption=-1` },
  { name: 'filter.same-tag', method: 'GET', path: () => `/api/filter/same-tag?tagId=1&durationOption=-1` },
  { name: 'filter.same-up-video-count', method: 'POST', path: () => `/api/filter/same-up-video-count`, body: { upId: 1, ratioOption: 0 } },
  { name: 'filter.same-tag-video-count', method: 'GET', path: () => `/api/filter/same-tag-video-count?tagId=1&ratioOption=0` },
  { name: 'filter.follow-time', method: 'GET', path: () => `/api/filter/follow-time?userId=1&upId=1` },
  { name: 'filter.night-owl', method: 'GET', path: () => `/api/filter/night-owl?option=0` },
  { name: 'filter.user-activity', method: 'GET', path: () => `/api/filter/user-activity?option=0` },
  { name: 'filter.deep-video', method: 'GET', path: () => `/api/filter/deep-video?videoId=1&option=0` },
  { name: 'filter.series', method: 'GET', path: () => `/api/filter/series?tagId=1` }
]

async function fetchWithTimeout(url, options = {}) {
  const controller = new AbortController()
  const id = setTimeout(() => controller.abort(), timeoutMs)
  try {
    const res = await fetch(url, { ...options, signal: controller.signal })
    clearTimeout(id)
    return res
  } catch (err) {
    clearTimeout(id)
    throw err
  }
}

async function check() {
  console.log(`Checking API base: ${API_BASE}`)
  const results = []
  for (const ep of endpoints) {
    const sampleUserId = 1
    const url = API_BASE + (typeof ep.path === 'function' ? ep.path(sampleUserId) : ep.path)
    try {
      let res
      if (ep.method === 'GET') {
        res = await fetchWithTimeout(url, { method: 'GET' })
      } else if (ep.method === 'POST') {
        res = await fetchWithTimeout(url, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(ep.body || {}) })
      }
      const ct = res.headers.get('content-type') || ''
      let body = null
      if (ct.includes('application/json')) {
        body = await res.json()
      } else {
        body = await res.text()
      }
      results.push({ name: ep.name, url, ok: res.ok, status: res.status, type: ct, sample: Array.isArray(body) ? `Array(${body.length})` : (typeof body) })
      console.log(`[OK] ${ep.name} ${res.status} ${url} -> ${results[results.length-1].sample}`)
    } catch (err) {
      results.push({ name: ep.name, url, ok: false, error: String(err) })
      console.error(`[ERR] ${ep.name} ${url} -> ${err.message || err}`)
    }
  }

  console.log('\nSummary:')
  for (const r of results) {
    if (r.ok) console.log(` - ${r.name}: OK ${r.status} (${r.sample || r.type})`)
    else console.log(` - ${r.name}: FAIL (${r.error})`)
  }

  const failed = results.filter(r => !r.ok)
  if (failed.length > 0) process.exitCode = 2
}

check()
