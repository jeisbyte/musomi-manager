document.addEventListener('DOMContentLoaded', () => {
    // Initialize Lucide icons on first load
    lucide.createIcons();
    
    // Check for prefers-reduced-motion
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (prefersReducedMotion) {
        document.documentElement.style.setProperty('--transition-duration', '0ms');
    }
});

// Re-initialize icons after HTMX swaps
document.body.addEventListener('htmx:afterSwap', () => {
    lucide.createIcons();
});

// Configure HTMX
document.body.addEventListener('htmx:beforeSwap', function(evt) {
    // Allow swapping for 400, 422 errors (validation errors)
    if (evt.detail.xhr.status === 400 || evt.detail.xhr.status === 422) {
        evt.detail.shouldSwap = true;
        evt.detail.isError = false;
    }
});

document.body.addEventListener('htmx:responseError', function(evt) {
    if (evt.detail.xhr.status === 401 || evt.detail.xhr.responseURL.endsWith('/login')) {
        // Redirect to login on 401 or if response URL is login page
        window.location.href = '/login';
    } else if (evt.detail.xhr.status === 403) {
        window.location.href = '/access-denied';
    } else if (evt.detail.xhr.status >= 500) {
        window.location.href = '/500';
    }
});

// Toast functionality
window.showToast = function(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;
    
    const toast = document.createElement('div');
    toast.className = `p-4 rounded-lg shadow-md border flex items-start gap-3 transition-all duration-300 transform translate-y-2 opacity-0
        ${type === 'success' ? 'bg-emerald-50 border-emerald-200 text-emerald-800' : 
          type === 'error' ? 'bg-red-50 border-red-200 text-red-800' : 
          'bg-brand-50 border-brand-200 text-brand-800'}`;
          
    let iconName = 'info';
    if (type === 'success') iconName = 'check-circle';
    if (type === 'error') iconName = 'alert-circle';
    
    toast.innerHTML = `
        <i data-lucide="${iconName}" class="w-5 h-5 shrink-0 mt-0.5 ${type === 'success' ? 'text-emerald-600' : type === 'error' ? 'text-red-600' : 'text-brand-600'}"></i>
        <div class="flex-grow text-sm font-medium leading-relaxed">${message}</div>
        <button type="button" class="shrink-0 rounded-md p-1 hover:bg-black/5 focus:outline-none focus:ring-2 focus:ring-brand-700" onclick="this.parentElement.remove()">
            <span class="sr-only">Close</span>
            <i data-lucide="x" class="w-4 h-4 text-slate-500"></i>
        </button>
    `;
    
    container.appendChild(toast);
    lucide.createIcons({ root: toast });
    
    // Animate in
    requestAnimationFrame(() => {
        toast.classList.remove('translate-y-2', 'opacity-0');
        toast.classList.add('translate-y-0', 'opacity-100');
    });
    
    // Auto remove after 5s
    setTimeout(() => {
        toast.classList.remove('translate-y-0', 'opacity-100');
        toast.classList.add('translate-y-2', 'opacity-0');
        setTimeout(() => toast.remove(), 300);
    }, 5000);
};

// Expose HTMX trigger for server-sent toasts
document.body.addEventListener('show-toast', function(evt) {
    if (evt.detail && evt.detail.value) {
        showToast(evt.detail.value.message, evt.detail.value.type);
    }
});
