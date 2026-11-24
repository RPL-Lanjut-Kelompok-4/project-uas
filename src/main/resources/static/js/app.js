const $ = (s) => document.querySelector(s);
let conversationId = null;

// ----- navigasi tab -----
document.querySelectorAll('.nav-item').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.nav-item').forEach(b => b.classList.remove('active'));
        document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
        btn.classList.add('active');
        $('#tab-' + btn.dataset.tab).classList.add('active');
    });
});

async function postJson(url, body) {
    const res = await fetch(url, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(body)
    });
    if (!res.ok) throw new Error('HTTP ' + res.status);
    return res.json();
}

function esc(s) {
    return String(s).replace(/[&<>"]/g, c => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;'}[c]));
}

async function loadInfo() {
    const info = await (await fetch('/api/info')).json();
    $('#info-model').textContent = info.model;
    $('#info-chunk').textContent = info.totalChunk;
    $('#info-doc').textContent = info.dokumen.length;
}

// ----- chat -----
function addMsg(cls, html) {
    const div = document.createElement('div');
    div.className = 'msg ' + cls;
    div.innerHTML = html;
    $('#chat-box').appendChild(div);
    $('#chat-box').scrollTop = $('#chat-box').scrollHeight;
    return div;
}

async function sendChat(text) {
    if (!text.trim()) return;
    addMsg('user', esc(text));
    const loading = addMsg('bot loading', 'SiAkad AI sedang mengetik...');
    try {
        const r = await postJson('/api/chat', {conversationId, message: text});
        conversationId = r.conversationId;
        loading.className = 'msg bot';
        loading.innerHTML = esc(r.answer) + `<span class="meta">${esc(r.model)} · ${r.durationMs} ms</span>`;
    } catch (e) {
        loading.className = 'msg bot';
        loading.textContent = 'Terjadi kesalahan: ' + e.message;
    }
}

$('#chat-form').addEventListener('submit', e => {
    e.preventDefault();
    const t = $('#chat-input').value;
    $('#chat-input').value = '';
    sendChat(t);
});
document.querySelectorAll('#tab-chat .chip').forEach(c => c.addEventListener('click', () => sendChat(c.textContent)));

// ----- RAG -----
async function ask(q) {
    if (!q.trim()) return;
    $('#rag-input').value = q;
    $('#rag-result').innerHTML = '<div class="card muted">Mencari di VectorStore...</div>';
    const r = await postJson('/api/rag/ask', {question: q});
    const src = r.sources.map((s, i) =>
        `<div class="source"><b>[${i + 1}] ${esc(s.fileName)}</b> · skor ${s.score == null ? '-' : s.score.toFixed(3)}<br>${esc(s.excerpt)}</div>`).join('');
    $('#rag-result').innerHTML = `
        <div class="card"><h3>Jawaban</h3><div class="answer">${esc(r.answer)}</div>
        <div class="muted" style="margin-top:8px">${r.durationMs} ms</div></div>
        <div class="card"><h3>Sumber dokumen (top-${r.sources.length} chunk)</h3>${src || '<span class="muted">Tidak ada</span>'}</div>`;
}
$('#rag-form').addEventListener('submit', e => { e.preventDefault(); ask($('#rag-input').value); });
document.querySelectorAll('.rag-chip').forEach(c => c.addEventListener('click', () => ask(c.textContent)));

// ----- Quiz -----
$('#quiz-form').addEventListener('submit', async e => {
    e.preventDefault();
    $('#quiz-result').innerHTML = '<div class="card muted">Membuat soal...</div>';
    const quiz = await postJson('/api/quiz', {topic: $('#quiz-topic').value, jumlah: +$('#quiz-n').value});
    const html = quiz.soal.map((s, i) => `
        <div class="q" data-answer="${esc(s.jawaban)}">
            <div class="q-title">${i + 1}. ${esc(s.pertanyaan)}</div>
            ${s.pilihan.map((p, j) => `<span class="opt" data-val="${esc(p)}">${'ABCD'[j]}. ${esc(p)}</span>`).join('')}
        </div>`).join('');
    $('#quiz-result').innerHTML = `<div class="card"><h3>Kuis: ${esc(quiz.topik)} (${quiz.soal.length} soal)</h3>${html}</div>`;
    document.querySelectorAll('.opt').forEach(o => o.addEventListener('click', () => {
        const q = o.closest('.q');
        q.querySelectorAll('.opt').forEach(x => {
            if (x.dataset.val === q.dataset.answer) x.classList.add('correct');
        });
        if (o.dataset.val !== q.dataset.answer) o.classList.add('wrong');
    }));
});

// ----- Upload -----
$('#upload-form').addEventListener('submit', async e => {
    e.preventDefault();
    const f = $('#upload-file').files[0];
    if (!f) return;
    const fd = new FormData();
    fd.append('file', f);
    const res = await fetch('/api/rag/upload', {method: 'POST', body: fd});
    const r = await res.json();
    $('#upload-result').innerHTML = res.ok
        ? `<div class="success">✅ <b>${esc(r.fileName)}</b> berhasil di-index: ${r.jumlahChunk} chunk ditambahkan (total ${r.totalChunk} chunk di VectorStore).</div>`
        : `<div class="card">${esc(r.error)}</div>`;
    loadInfo();
});

loadInfo();
