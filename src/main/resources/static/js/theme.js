(function () {
    const storageKey = 'theme';
    const root = document.documentElement;
    const toggles = document.querySelectorAll('[data-theme-toggle]');

    function currentTheme() {
        return root.dataset.theme === 'light' ? 'light' : 'dark';
    }

    function applyTheme(theme) {
        const normalizedTheme = theme === 'light' ? 'light' : 'dark';
        root.dataset.theme = normalizedTheme;
        toggles.forEach((toggle) => {
            toggle.setAttribute('aria-label', normalizedTheme === 'light' ? '切换到暗色主题' : '切换到亮色主题');
            const icon = toggle.querySelector('[data-theme-icon]');
            if (icon) {
                icon.textContent = normalizedTheme === 'light' ? '☀️' : '🌙';
            }
        });
    }

    toggles.forEach((toggle) => {
        toggle.addEventListener('click', () => {
            const nextTheme = currentTheme() === 'light' ? 'dark' : 'light';
            try {
                window.localStorage.setItem(storageKey, nextTheme);
            } catch (error) {
                // localStorage can be unavailable in restricted browser modes.
            }
            applyTheme(nextTheme);
        });
    });

    applyTheme(currentTheme());
})();
