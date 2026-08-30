(function () {
    const overlay = document.querySelector('[data-search-overlay]');
    const input = document.querySelector('[data-search-input]');
    const toggle = document.querySelector('[data-search-toggle]');
    const close = document.querySelector('[data-search-close]');

    function openSearch() {
        if (!overlay) return;
        overlay.hidden = false;
        document.body.classList.add('search-open');
        input?.focus();
    }

    function closeSearch() {
        if (!overlay) return;
        overlay.hidden = true;
        document.body.classList.remove('search-open');
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
