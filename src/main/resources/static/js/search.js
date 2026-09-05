(function () {
    // ============ 覆盖层开合（保留原逻辑） ============
    const overlay = document.querySelector('[data-search-overlay]');
    const input = document.querySelector('[data-search-input]');
    const toggle = document.querySelector('[data-search-toggle]');
    const close = document.querySelector('[data-search-close]');
    let closeTimer = null;

    function openSearch() {
        if (!overlay) return;
        clearTimeout(closeTimer);
        overlay.hidden = false;
        document.body.classList.add('search-open');
        requestAnimationFrame(() => requestAnimationFrame(() => {
            overlay.classList.add('is-open');
            input?.focus();
        }));
    }

    function closeSearch() {
        if (!overlay || overlay.hidden) return;
        overlay.classList.remove('is-open');
        document.body.classList.remove('search-open');
        closeTimer = window.setTimeout(() => {
            if (!overlay.classList.contains('is-open')) {
                overlay.hidden = true;
                resetLive();
            }
        }, 300);
    }

    toggle?.addEventListener('click', openSearch);
    close?.addEventListener('click', closeSearch);
    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') {
            closeSearch();
        }
    });

    // ============ 表单提交拦截（空查询不提交） ============
    document.querySelectorAll('.search-form').forEach((form) => {
        form.addEventListener('submit', (event) => {
            const field = form.querySelector('input[name="q"]');
            if (!field || !field.value.trim()) {
                event.preventDefault();
            }
        });
    });

    // ============ 文本节点高亮（抽出复用） ============
    function markTextNodes(container, query) {
        const pattern = new RegExp(query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'gi');
        const walker = document.createTreeWalker(container, NodeFilter.SHOW_TEXT);
        const textNodes = [];
        let node;
        while ((node = walker.nextNode())) {
            textNodes.push(node);
        }

        textNodes.forEach((textNode) => {
            const text = textNode.nodeValue || '';
            pattern.lastIndex = 0;
            if (!pattern.test(text)) {
                pattern.lastIndex = 0;
                return;
            }
            pattern.lastIndex = 0;

            const fragment = document.createDocumentFragment();
            let lastIndex = 0;
            text.replace(pattern, (match, offset) => {
                if (offset > lastIndex) {
                    fragment.append(text.slice(lastIndex, offset));
                }
                const mark = document.createElement('mark');
                mark.className = 'search-mark';
                mark.textContent = match;
                fragment.append(mark);
                lastIndex = offset + match.length;
                return match;
            });
            fragment.append(text.slice(lastIndex));
            textNode.parentNode.replaceChild(fragment, textNode);
            pattern.lastIndex = 0;
        });
    }

    // ============ 即时搜索（覆盖层内渐进增强） ============
    const panel = document.querySelector('.search-panel');
    const live = document.createElement('div');
    live.className = 'search-live';
    live.hidden = true;
    panel?.appendChild(live);

    let debounceTimer = null;
    let controller = null;
    const TYPE_LABELS = { project: '项目', post: '笔记', resource: '资源' };
    const GROUPS = [['project', '项目'], ['post', '笔记'], ['resource', '资源']];

    function liveItem(result) {
        const li = document.createElement('li');
        li.className = 'search-live-item';

        const link = document.createElement('a');
        link.className = 'search-live-link';
        link.href = result.url;

        const type = document.createElement('span');
        type.className = 'search-live-type';
        type.textContent = TYPE_LABELS[result.type] || result.type;

        const title = document.createElement('span');
        title.className = 'search-live-title';
        title.textContent = result.title || '';

        const summary = document.createElement('span');
        summary.className = 'search-live-summary';
        summary.textContent = result.summary || '';

        link.append(type, title, summary);
        li.appendChild(link);
        return li;
    }

    function renderLive(data, query) {
        live.textContent = '';
        if (!data || !data.total) {
            const empty = document.createElement('p');
            empty.className = 'search-live-empty';
            empty.textContent = '没有找到「' + query + '」相关的内容';
            live.appendChild(empty);
            live.hidden = false;
            return;
        }

        GROUPS.forEach(([key, label]) => {
            const items = data.results.filter((r) => r.type === key);
            if (!items.length) return;
            const heading = document.createElement('p');
            heading.className = 'search-live-group-title';
            heading.textContent = label;
            live.appendChild(heading);
            const ul = document.createElement('ul');
            ul.className = 'search-live-list';
            items.forEach((r) => ul.appendChild(liveItem(r)));
            live.appendChild(ul);
        });
        markTextNodes(live, query);
        live.hidden = false;
    }

    function resetLive() {
        clearTimeout(debounceTimer);
        if (controller) {
            controller.abort();
            controller = null;
        }
        live.hidden = true;
        live.textContent = '';
    }

    input?.addEventListener('input', () => {
        clearTimeout(debounceTimer);
        const q = input.value.trim();
        if (q.length < 2) {
            if (controller) {
                controller.abort();
                controller = null;
            }
            live.hidden = true;
            return;
        }
        debounceTimer = window.setTimeout(async () => {
            if (controller) {
                controller.abort();
            }
            controller = new AbortController();
            try {
                const res = await fetch('/api/search?q=' + encodeURIComponent(q), {
                    signal: controller.signal,
                    headers: { 'Accept': 'application/json' }
                });
                if (!res.ok) {
                    throw new Error('HTTP ' + res.status);
                }
                const data = await res.json();
                renderLive(data, q);
            } catch (err) {
                if (err.name !== 'AbortError') {
                    live.hidden = true;
                }
            }
        }, 300);
    });

    // ============ 搜索页结果高亮（保留原逻辑） ============
    const page = document.querySelector('[data-search-query]');
    const query = page?.dataset.searchQuery?.trim() || '';
    if (query.length <= 1) {
        return;
    }

    document.querySelectorAll('[data-search-highlight]').forEach((container) => {
        markTextNodes(container, query);
    });
})();
