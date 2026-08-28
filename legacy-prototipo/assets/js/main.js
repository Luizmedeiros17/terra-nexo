/* =========================================================
   Terra Nexo — scripts do protótipo
   Vanilla JS, sem dependências. Cada módulo só roda se
   encontrar os elementos que lhe interessam na página.
   ========================================================= */
(function () {
  'use strict';

  /* ---------- Helpers ---------- */
  const $ = (sel, ctx = document) => ctx.querySelector(sel);
  const $$ = (sel, ctx = document) => Array.from(ctx.querySelectorAll(sel));

  const brl = (v, compact = false) =>
    new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
      maximumFractionDigits: 0,
      notation: compact ? 'compact' : 'standard',
    }).format(v);

  const icon = (paths, extra = '') =>
    `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" ${extra}>${paths}</svg>`;

  const ICONS = {
    pin: icon('<path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/><circle cx="12" cy="10" r="3"/>'),
    bed: icon('<path d="M3 18v-6a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v6"/><path d="M3 18h18"/><path d="M7 10V7a1 1 0 0 1 1-1h8a1 1 0 0 1 1 1v3"/>'),
    bath: icon('<path d="M4 12h16v3a4 4 0 0 1-4 4H8a4 4 0 0 1-4-4v-3Z"/><path d="M6 12V6a2 2 0 0 1 4 0"/><path d="M7 19l-1 2M17 19l1 2"/>'),
    car: icon('<path d="M5 13l1.5-4.5A2 2 0 0 1 8.4 7h7.2a2 2 0 0 1 1.9 1.5L19 13"/><path d="M4 13h16v4H4z"/><circle cx="7.5" cy="17.5" r="1.2"/><circle cx="16.5" cy="17.5" r="1.2"/>'),
    area: icon('<path d="M4 4h16v16H4z"/><path d="M9 4v3M4 9h3M20 15h-3M15 20v-3"/>'),
    heart: icon('<path d="M12 20s-7-4.4-7-9.2A3.8 3.8 0 0 1 12 8a3.8 3.8 0 0 1 7 2.8C19 15.6 12 20 12 20Z"/>'),
    empty: icon('<circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/>'),
  };

  const prefersReduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  /* ---------- 1. Tema claro/escuro ---------- */
  const Theme = {
    key: 'terranexo:theme',
    init() {
      const saved = localStorage.getItem(this.key);
      const sysDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
      this.apply(saved || (sysDark ? 'dark' : 'light'));

      $$('[data-theme-toggle]').forEach((btn) =>
        btn.addEventListener('click', () => {
          const next = document.documentElement.dataset.theme === 'dark' ? 'light' : 'dark';
          this.apply(next);
          localStorage.setItem(this.key, next);
        })
      );
    },
    apply(mode) {
      document.documentElement.dataset.theme = mode;
      const meta = $('meta[name="theme-color"]');
      if (meta) meta.setAttribute('content', mode === 'dark' ? '#0e1114' : '#0f3b3f');
      $$('[data-theme-toggle]').forEach((b) =>
        b.setAttribute('aria-label', mode === 'dark' ? 'Ativar tema claro' : 'Ativar tema escuro')
      );
    },
  };

  /* ---------- 2. Cabeçalho e menu ---------- */
  function initHeader() {
    const header = $('.header');
    const nav = $('#nav-principal');
    const toggle = $('[data-nav-toggle]');
    if (!header) return;

    const onScroll = () => header.classList.toggle('is-stuck', window.scrollY > 12);
    onScroll();
    window.addEventListener('scroll', onScroll, { passive: true });

    if (!nav || !toggle) return;
    const close = () => {
      nav.classList.remove('is-open');
      toggle.setAttribute('aria-expanded', 'false');
    };
    toggle.addEventListener('click', () => {
      const open = nav.classList.toggle('is-open');
      toggle.setAttribute('aria-expanded', String(open));
    });
    nav.addEventListener('click', (e) => { if (e.target.closest('a')) close(); });
    document.addEventListener('keydown', (e) => { if (e.key === 'Escape') close(); });
    window.addEventListener('resize', () => { if (window.innerWidth > 940) close(); });
  }

  /* ---------- 3. Revelação no scroll ---------- */
  function initReveal() {
    const items = $$('.reveal');
    if (!items.length) return;
    if (prefersReduced || !('IntersectionObserver' in window)) {
      items.forEach((el) => el.classList.add('is-visible'));
      return;
    }
    const io = new IntersectionObserver(
      (entries) => entries.forEach((en) => {
        if (en.isIntersecting) { en.target.classList.add('is-visible'); io.unobserve(en.target); }
      }),
      { rootMargin: '0px 0px -8% 0px', threshold: 0.08 }
    );
    items.forEach((el) => io.observe(el));
  }

  /* ---------- 4. Favoritos ---------- */
  const Favs = {
    key: 'terranexo:favoritos',
    get() { try { return JSON.parse(localStorage.getItem(this.key)) || []; } catch { return []; } },
    has(id) { return this.get().includes(id); },
    toggle(id) {
      const list = this.get();
      const i = list.indexOf(id);
      i > -1 ? list.splice(i, 1) : list.push(id);
      localStorage.setItem(this.key, JSON.stringify(list));
      this.sync();
      return i === -1;
    },
    sync() {
      const n = this.get().length;
      $$('[data-fav-count]').forEach((el) => {
        el.textContent = n ? `(${n})` : '';
      });
    },
    bind(root = document) {
      $$('.card__fav', root).forEach((btn) => {
        const id = btn.dataset.id;
        btn.setAttribute('aria-pressed', String(this.has(id)));
        btn.addEventListener('click', (e) => {
          e.preventDefault();
          btn.setAttribute('aria-pressed', String(this.toggle(id)));
        });
      });
    },
  };

  /* ---------- 5. Card de imóvel ---------- */
  function cardHTML(im, eager = false) {
    const preco = im.operacao === 'aluguel'
      ? `${brl(im.preco)} <small>/mês</small>`
      : brl(im.preco);
    const badges = [
      `<span class="badge badge--brand">${im.operacao === 'venda' ? 'Venda' : 'Aluguel'}</span>`,
      im.exclusivo ? '<span class="badge badge--accent">Exclusivo</span>' : '',
    ].join('');

    const specs = [
      im.quartos ? `<li>${ICONS.bed}<span>${im.quartos} <span class="sr-only">quartos</span>quartos</span></li>` : '',
      im.banheiros ? `<li>${ICONS.bath}<span>${im.banheiros} banheiros</span></li>` : '',
      im.vagas ? `<li>${ICONS.car}<span>${im.vagas} vagas</span></li>` : '',
      `<li>${ICONS.area}<span>${im.areaTexto || `${im.area} m²`}</span></li>`,
    ].join('');

    return `
      <article class="card reveal" itemscope itemtype="https://schema.org/Residence">
        <div class="card__media">
          <img src="${im.fotos[0]}" alt="${im.titulo} — ${im.bairro}, ${im.cidade}"
               width="900" height="675" loading="${eager ? 'eager' : 'lazy'}" decoding="async" itemprop="photo">
          <div class="card__badges">${badges}</div>
          <button class="card__fav" type="button" data-id="${im.id}"
                  aria-pressed="false" aria-label="Salvar ${im.titulo} nos favoritos">${ICONS.heart}</button>
        </div>
        <div class="card__body">
          <p class="card__price">${preco}</p>
          <h3 class="card__title">
            <a href="imovel.html?id=${im.slug}" itemprop="name">${im.titulo}</a>
          </h3>
          <p class="card__loc" itemprop="address" itemscope itemtype="https://schema.org/PostalAddress">
            ${ICONS.pin}<span><span itemprop="addressLocality">${im.bairro}</span>, <span itemprop="addressRegion">${im.cidade}</span></span>
          </p>
          <ul class="card__specs">${specs}</ul>
        </div>
      </article>`;
  }

  function renderCards(container, lista, eagerFirst = 0) {
    container.innerHTML = lista.map((im, i) => cardHTML(im, i < eagerFirst)).join('');
    Favs.bind(container);
    initReveal();
  }

  /* ---------- 6. Home: destaques e bairros ---------- */
  function initHome() {
    const grid = $('[data-destaques]');
    if (grid && window.IMOVEIS) {
      renderCards(grid, IMOVEIS.filter((i) => i.destaque).slice(0, 6), 3);
    }

    const bairros = $('[data-bairros]');
    if (bairros && window.BAIRROS) {
      bairros.innerHTML = BAIRROS.map((b) => `
        <a class="district" href="imoveis.html?bairro=${encodeURIComponent(b.nome)}">
          <img src="${b.foto}" alt="Vista do bairro ${b.nome}" width="1100" height="800" loading="lazy" decoding="async">
          <div><h3>${b.nome}</h3><span>${b.qtd} imóveis disponíveis</span></div>
        </a>`).join('');
    }

    // Abas da busca (venda / aluguel)
    $$('[data-search-tab]').forEach((tab) => {
      tab.addEventListener('click', () => {
        $$('[data-search-tab]').forEach((t) => {
          t.classList.toggle('is-active', t === tab);
          t.setAttribute('aria-selected', String(t === tab));
        });
        const hidden = $('#busca-operacao');
        if (hidden) hidden.value = tab.dataset.searchTab;
      });
    });
  }

  /* ---------- 7. Listagem com filtros ---------- */
  function initListagem() {
    const grid = $('[data-resultados]');
    const form = $('[data-filtros]');
    if (!grid || !window.IMOVEIS) return;

    const params = new URLSearchParams(location.search);
    const state = {
      operacao: params.get('operacao') || 'todos',
      tipo: params.getAll('tipo'),
      bairro: params.get('bairro') || '',
      q: params.get('q') || '',
      quartos: Number(params.get('quartos') || 0),
      max: Number(params.get('max') || 7000000),
      ordem: 'relevancia',
    };

    // Reflete a URL no formulário
    if (form) {
      const op = form.querySelector(`[name="operacao"][value="${state.operacao}"]`);
      if (op) op.checked = true;
      const q = form.querySelector('[name="q"]');
      if (q) q.value = state.bairro || state.q;
      const max = form.querySelector('[name="max"]');
      if (max) max.value = state.max;
      state.tipo.forEach((t) => {
        const el = form.querySelector(`[name="tipo"][value="${t}"]`);
        if (el) el.checked = true;
      });
      const qt = form.querySelector(`[name="quartos"][value="${state.quartos}"]`);
      if (qt) qt.checked = true;
    }

    const readForm = () => {
      if (!form) return;
      const fd = new FormData(form);
      state.operacao = fd.get('operacao') || 'todos';
      state.tipo = fd.getAll('tipo');
      state.quartos = Number(fd.get('quartos') || 0);
      state.max = Number(fd.get('max') || 7000000);
      state.q = (fd.get('q') || '').toString().trim();
      state.bairro = '';
    };

    const filtrar = () => {
      const termo = (state.q || state.bairro).toLowerCase();
      let out = IMOVEIS.filter((im) => {
        if (state.operacao !== 'todos' && im.operacao !== state.operacao) return false;
        if (state.tipo.length && !state.tipo.includes(im.tipo)) return false;
        if (state.quartos && im.quartos < state.quartos) return false;
        // Aluguel e venda têm ordens de grandeza distintas: o teto só filtra venda.
        if (im.operacao === 'venda' && im.preco > state.max) return false;
        if (termo) {
          const alvo = `${im.titulo} ${im.bairro} ${im.cidade} ${im.tipo} ${im.resumo}`.toLowerCase();
          if (!alvo.includes(termo)) return false;
        }
        return true;
      });

      const ordens = {
        'preco-asc': (a, b) => a.preco - b.preco,
        'preco-desc': (a, b) => b.preco - a.preco,
        'area-desc': (a, b) => b.area - a.area,
        relevancia: (a, b) => Number(b.destaque) - Number(a.destaque),
      };
      out = out.sort(ordens[state.ordem] || ordens.relevancia);

      if (!out.length) {
        grid.innerHTML = `
          <div class="empty">${ICONS.empty}
            <h3>Nenhum imóvel encontrado</h3>
            <p>Tente ampliar a faixa de preço ou remover alguns filtros.</p>
          </div>`;
      } else {
        renderCards(grid, out, 3);
      }

      const count = $('[data-count]');
      if (count) count.innerHTML = `<strong>${out.length}</strong> ${out.length === 1 ? 'imóvel encontrado' : 'imóveis encontrados'}`;
    };

    if (form) {
      form.addEventListener('input', (e) => {
        if (e.target.name === 'max') {
          const out = $('[data-range-out]');
          if (out) out.textContent = `até ${brl(Number(e.target.value), true)}`;
        }
        readForm();
        filtrar();
      });
      form.addEventListener('submit', (e) => { e.preventDefault(); readForm(); filtrar(); });
      const limpar = $('[data-limpar]');
      if (limpar) limpar.addEventListener('click', () => {
        form.reset();
        const out = $('[data-range-out]');
        if (out) out.textContent = 'até R$ 7 mi';
        readForm();
        filtrar();
      });
    }

    const sort = $('[data-ordem]');
    if (sort) sort.addEventListener('change', () => { state.ordem = sort.value; filtrar(); });

    const rangeOut = $('[data-range-out]');
    if (rangeOut) rangeOut.textContent = `até ${brl(state.max, true)}`;

    filtrar();
  }

  /* ---------- 8. Página de detalhe ---------- */
  function initDetalhe() {
    const root = $('[data-imovel]');
    if (!root || !window.IMOVEIS) return;

    const slug = new URLSearchParams(location.search).get('id');
    const im = IMOVEIS.find((i) => i.slug === slug || i.id === slug);
    // Sem parâmetro válido a página mantém o conteúdo estático já renderizado no HTML.
    if (!im || im.slug === root.dataset.imovel) return;

    const set = (sel, html) => { const el = $(sel); if (el) el.innerHTML = html; };
    const preco = im.operacao === 'aluguel' ? `${brl(im.preco)} <small>/mês</small>` : brl(im.preco);

    document.title = `${im.titulo} — ${im.bairro}, ${im.cidade} | Terra Nexo`;
    const areaTxt = im.areaTexto || `${im.area} m²`;
    const desc = `${im.tipo} para ${im.operacao} em ${im.bairro}, ${im.cidade}: ${areaTxt}${im.quartos ? `, ${im.quartos} quartos` : ''}${im.vagas ? `, ${im.vagas} vagas` : ''}. ${im.resumo}`.slice(0, 158);
    $$('meta[name="description"], meta[property="og:description"], meta[name="twitter:description"]').forEach((m) => m.setAttribute('content', desc));
    $$('meta[property="og:title"], meta[name="twitter:title"]').forEach((m) => m.setAttribute('content', document.title));
    $$('meta[property="og:image"], meta[name="twitter:image"]').forEach((m) => m.setAttribute('content', im.fotos[0]));
    const canon = $('link[rel="canonical"]');
    if (canon) canon.href = `https://www.terranexo.com.br/imovel.html?id=${im.slug}`;
    const ogUrl = $('meta[property="og:url"]');
    if (ogUrl) ogUrl.setAttribute('content', `https://www.terranexo.com.br/imovel.html?id=${im.slug}`);

    set('[data-campo="titulo"]', im.titulo);
    set('[data-campo="preco"]', preco);
    set('[data-campo="endereco"]', `${ICONS.pin} ${im.endereco} — ${im.bairro}, ${im.cidade}/${im.uf}`);
    set('[data-campo="resumo"]', im.resumo);
    set('[data-campo="breadcrumb"]', im.titulo);
    set('[data-campo="ref"]', im.id.toUpperCase());
    set('[data-campo="condominio"]', im.condominio ? brl(im.condominio) : '—');
    set('[data-campo="iptu"]', im.iptu ? brl(im.iptu) : '—');
    set('[data-campo="ano"]', im.ano || '—');
    set('[data-campo="tipo"]', im.tipo);

    const specs = [
      { i: 'area', v: im.areaTexto || `${im.area} m²`, l: im.areaTexto ? 'Área total' : 'Área útil' },
      { i: 'bed', v: im.quartos || '—', l: 'Quartos' },
      { i: 'bath', v: im.banheiros || '—', l: 'Banheiros' },
      { i: 'car', v: im.vagas || '—', l: 'Vagas' },
    ];
    set('[data-campo="specs"]', specs.map((s) => `<div class="spec">${ICONS[s.i]}<strong>${s.v}</strong><span>${s.l}</span></div>`).join(''));

    set('[data-campo="comodidades"]', im.comodidades.map((c) =>
      `<li>${icon('<path d="m5 12 4.5 4.5L19 7"/>')}<span>${c}</span></li>`).join(''));

    const gal = $('[data-campo="galeria"]');
    if (gal) {
      gal.innerHTML = im.fotos.slice(0, 5).map((f, i) => `
        <a href="${f}" target="_blank" rel="noopener">
          <img src="${f}" alt="${im.titulo} — foto ${i + 1}" width="900" height="675"
               loading="${i === 0 ? 'eager' : 'lazy'}" decoding="async" fetchpriority="${i === 0 ? 'high' : 'auto'}">
          ${i === 4 ? `<span class="gallery__more">+${Math.max(im.fotos.length - 5, 8)} fotos</span>` : ''}
        </a>`).join('');
    }

    // Atualiza o JSON-LD do imóvel
    const ld = $('#ld-imovel');
    if (ld) {
      ld.textContent = JSON.stringify({
        '@context': 'https://schema.org',
        '@type': ['Residence', 'Product'],
        name: im.titulo,
        description: im.resumo,
        sku: im.id,
        image: im.fotos,
        url: `https://www.terranexo.com.br/imovel.html?id=${im.slug}`,
        numberOfRooms: im.quartos,
        numberOfBathroomsTotal: im.banheiros,
        yearBuilt: im.ano || undefined,
        floorSize: { '@type': 'QuantitativeValue', value: im.area, unitCode: 'MTK' },
        address: {
          '@type': 'PostalAddress',
          streetAddress: im.endereco,
          addressLocality: im.cidade,
          addressRegion: im.uf,
          postalCode: im.cep,
          addressCountry: 'BR',
        },
        amenityFeature: im.comodidades.map((c) => ({ '@type': 'LocationFeatureSpecification', name: c, value: true })),
        offers: {
          '@type': 'Offer',
          price: im.preco,
          priceCurrency: 'BRL',
          availability: 'https://schema.org/InStock',
          businessFunction: im.operacao === 'aluguel'
            ? 'https://schema.org/LeaseOut'
            : 'https://schema.org/Sell',
        },
      }, null, 2);
    }

    // Similares
    const sim = $('[data-similares]');
    if (sim) {
      const lista = IMOVEIS.filter((x) => x.id !== im.id && (x.bairro === im.bairro || x.tipo === im.tipo)).slice(0, 3);
      renderCards(sim, lista.length ? lista : IMOVEIS.filter((x) => x.id !== im.id).slice(0, 3));
    }
  }

  /* ---------- 9. Formulários ---------- */
  function initForms() {
    $$('form[data-validate]').forEach((form) => {
      form.setAttribute('novalidate', '');

      const showError = (field, msg) => {
        const wrap = field.closest('.field');
        if (!wrap) return;
        wrap.classList.toggle('is-invalid', Boolean(msg));
        const slot = wrap.querySelector('.field__error');
        if (slot) slot.textContent = msg || '';
        field.setAttribute('aria-invalid', String(Boolean(msg)));
      };

      const validate = (field) => {
        const v = field.value.trim();
        let msg = '';
        if (field.required && !v) msg = 'Campo obrigatório.';
        else if (field.type === 'email' && v && !/^[^\s@]+@[^\s@]+\.[a-z]{2,}$/i.test(v)) msg = 'Informe um e-mail válido.';
        else if (field.type === 'tel' && v && v.replace(/\D/g, '').length < 10) msg = 'Informe um telefone com DDD.';
        showError(field, msg);
        return !msg;
      };

      $$('input, textarea, select', form).forEach((f) => {
        if (f.type === 'checkbox' || f.type === 'hidden') return;
        f.addEventListener('blur', () => validate(f));
        f.addEventListener('input', () => { if (f.closest('.field')?.classList.contains('is-invalid')) validate(f); });
      });

      // Máscara simples de telefone
      const tel = form.querySelector('input[type="tel"]');
      if (tel) tel.addEventListener('input', () => {
        const d = tel.value.replace(/\D/g, '').slice(0, 11);
        tel.value = d.length <= 10
          ? d.replace(/(\d{0,2})(\d{0,4})(\d{0,4})/, (_, a, b, c) => [a && `(${a}`, a.length === 2 ? ') ' : '', b, c && `-${c}`].join('')).trim()
          : d.replace(/(\d{2})(\d{5})(\d{0,4})/, '($1) $2-$3');
      });

      form.addEventListener('submit', (e) => {
        e.preventDefault();
        const campos = $$('input, textarea, select', form).filter((f) => f.type !== 'checkbox' && f.type !== 'hidden');
        const ok = campos.map(validate).every(Boolean);
        const consent = form.querySelector('input[type="checkbox"][required]');
        if (consent && !consent.checked) {
          consent.focus();
          return;
        }
        if (!ok) {
          form.querySelector('.is-invalid input, .is-invalid textarea, .is-invalid select')?.focus();
          return;
        }
        const msg = form.querySelector('.form-msg');
        if (msg) {
          msg.classList.add('is-visible');
          msg.focus?.();
        }
        form.reset();
        // Protótipo: nenhuma requisição é enviada.
        console.info('[Terra Nexo] Formulário validado — integrar com o CRM aqui.');
      });
    });
  }

  /* ---------- 10. Busca da home ---------- */
  function initBuscaHome() {
    const form = $('[data-busca]');
    if (!form) return;
    form.addEventListener('submit', (e) => {
      e.preventDefault();
      const fd = new FormData(form);
      const p = new URLSearchParams();
      for (const [k, v] of fd.entries()) if (v) p.set(k, v);
      location.href = `imoveis.html?${p.toString()}`;
    });
  }

  /* ---------- 11. Ano no rodapé ---------- */
  function initMisc() {
    $$('[data-ano]').forEach((el) => (el.textContent = new Date().getFullYear()));
    // Fallback de imagem: evita quadro quebrado se o CDN estiver indisponível.
    document.addEventListener('error', (e) => {
      const img = e.target;
      if (img.tagName !== 'IMG' || img.dataset.fallback) return;
      img.dataset.fallback = '1';
      img.src = 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(
        `<svg xmlns="http://www.w3.org/2000/svg" width="900" height="675"><defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#0f3b3f"/><stop offset="1" stop-color="#b9884b"/></linearGradient></defs><rect width="900" height="675" fill="url(#g)"/><text x="50%" y="52%" text-anchor="middle" font-family="Georgia,serif" font-size="42" fill="rgba(255,255,255,.85)">Terra Nexo</text></svg>`
      );
    }, true);
  }

  /* ---------- Boot ---------- */
  Theme.init();
  document.addEventListener('DOMContentLoaded', () => {
    initHeader();
    initHome();
    initListagem();
    initDetalhe();
    initForms();
    initBuscaHome();
    initMisc();
    Favs.bind();
    Favs.sync();
    initReveal();
  });
})();
