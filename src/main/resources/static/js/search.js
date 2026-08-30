(function () {
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

    document.querySelectorAll('.search-form').forEach((form) => {
        form.addEventListener('submit', (event) => {
            const field = form.querySelector('input[name="q"]');
            if (!field || !field.value.trim()) {
                event.preventDefault();
            }
        });
    });

    const page = document.querySelector('[data-search-query]');
    const query = page?.dataset.searchQuery?.trim() || '';
    if (query.length <= 1) {
        return;
    }

    const pattern = new RegExp(query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'gi');
    document.querySelectorAll('[data-search-highlight]').forEach((container) => {
        const walker = document.createTreeWalker(container, NodeFilter.SHOW_TEXT);
        const textNodes = [];
        let node;
        while ((node = walker.nextNode())) {
            textNodes.push(node);
        }

        textNodes.forEach((textNode) => {
            const text = textNode.nodeValue || '';
            if (!pattern.test(text)) {
                pattern.lastIndex = 0;
                return;
            }
            pattern.lastIndex = 0;

            const fragment = document.createDocumentFragment();
            let lastIndex = 0;
            text.replace(pattern, (match, offset) => {
                fragment.append(text.slice(lastIndex, offset));
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
    });
})();
