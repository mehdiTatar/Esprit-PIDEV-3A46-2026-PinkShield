/**
 * PinkShield — AJAX Form Validation
 * Validates form fields on blur and on submit via /validate-field endpoint.
 * Usage: add data-ajax-validate="fieldName" to any input/select/textarea.
 */
const AjaxValidator = (function () {

    const ENDPOINT = '/validate-field';

    /* ── Find the correct container for error placement ──────── */

    /**
     * Returns the element that should receive the error span.
     * Handles nested wrappers like .pw-wrap — climbs up until we find
     * the first ancestor that is NOT a single-purpose wrapper.
     */
    function getContainer(input) {
        let el = input.parentElement;
        // If immediate parent is a small utility wrapper (pw-wrap / select-wrap),
        // use its parent so the error appears outside the wrapper.
        if (el && (el.classList.contains('pw-wrap') || el.classList.contains('select-wrap'))) {
            el = el.parentElement;
        }
        return el;
    }

    /* ── Error display helpers ──────────────────────────────── */

    function getErrorEl(input) {
        const container = getContainer(input);
        let el = container.querySelector(':scope > .ajax-field-error');
        if (!el) {
            el = document.createElement('span');
            el.className = 'ajax-field-error';
            el.setAttribute('role', 'alert');
            el.style.cssText = [
                'display:none',
                'color:#dc3545',
                'font-size:0.8rem',
                'margin-top:4px',
                'font-weight:500',
                'display:none',
            ].join(';');
            container.appendChild(el);
        }
        return el;
    }

    function showError(input, message) {
        input.classList.add('is-invalid');
        input.classList.remove('is-valid');
        const el = getErrorEl(input);
        el.innerHTML = '<i class="fas fa-exclamation-circle" style="margin-right:4px;"></i>' + message;
        el.style.display = 'block';
        // Also apply invalid border to select inside select-wrap
        if (input.parentElement && input.parentElement.classList.contains('select-wrap')) {
            input.classList.add('is-invalid');
        }
    }

    function clearError(input) {
        input.classList.remove('is-invalid');
        input.classList.add('is-valid');
        const container = getContainer(input);
        const el = container.querySelector(':scope > .ajax-field-error');
        if (el) el.style.display = 'none';
    }

    function resetField(input) {
        input.classList.remove('is-invalid', 'is-valid');
        const container = getContainer(input);
        const el = container.querySelector(':scope > .ajax-field-error');
        if (el) el.style.display = 'none';
    }

    /* ── AJAX call ──────────────────────────────────────────── */

    async function validateField(input, extra) {
        const field = input.dataset.ajaxValidate;
        const value = input.value;

        try {
            const res = await fetch(ENDPOINT, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ field, value, extra: extra || '' }),
            });
            if (!res.ok) return true; // don't block on server error
            const data = await res.json();
            if (data.valid) {
                clearError(input);
            } else {
                showError(input, data.error || 'Invalid value.');
            }
            return data.valid;
        } catch {
            return true; // fail-open on network issues
        }
    }

    /* ── Attach blur listeners ──────────────────────────────── */

    function attachBlur(form, extraFn) {
        form.querySelectorAll('[data-ajax-validate]').forEach(input => {
            input.addEventListener('blur', () => {
                // Always validate — even empty values — let the server decide if required
                const extra = extraFn ? extraFn(input) : '';
                validateField(input, extra);
            });

            // Clear error state on input (typing)
            input.addEventListener('input', () => {
                if (input.classList.contains('is-invalid')) {
                    input.classList.remove('is-invalid');
                    const container = getContainer(input);
                    const el = container.querySelector(':scope > .ajax-field-error');
                    if (el) el.style.display = 'none';
                }
            });
        });
    }

    /* ── Intercept submit ────────────────────────────────────── */

    function attachSubmit(form, extraFn, onAllValid) {
        form.addEventListener('submit', async function (e) {
            e.preventDefault();

            const inputs = [...form.querySelectorAll('[data-ajax-validate]')];
            const results = await Promise.all(
                inputs.map(input => {
                    const extra = extraFn ? extraFn(input) : '';
                    return validateField(input, extra);
                })
            );

            const allValid = results.every(Boolean);
            if (allValid) {
                if (typeof onAllValid === 'function') {
                    onAllValid();
                } else {
                    form.removeEventListener('submit', arguments.callee);
                    form.submit();
                }
            } else {
                // Scroll to first error
                const first = form.querySelector('.is-invalid');
                if (first) first.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        });
    }

    /* ── Public API ─────────────────────────────────────────── */

    return { attachBlur, attachSubmit, validateField, showError, clearError };
})();

/* Inject keyframe animation for error messages */
(function () {
    const style = document.createElement('style');
    style.textContent = `
        @keyframes fadeInErr {
            from { opacity: 0; transform: translateY(-4px); }
            to   { opacity: 1; transform: translateY(0); }
        }
        .ajax-field-error {
            display: none;
            color: #dc3545;
            font-size: 0.8rem;
            margin-top: 4px;
            font-weight: 500;
            animation: fadeInErr 0.2s ease;
        }
        /* Override Symfony server-side error display globally */
        .invalid-feedback,
        .form-error,
        .field-error { display: none !important; }
    `;
    document.head.appendChild(style);
})();
