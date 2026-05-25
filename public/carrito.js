/**
 * carrito.js
 * Lógica interactiva del carrito: cantidades, eliminar, recalcular totales.
 */

function fmt(n) {
    return '$' + n.toLocaleString('es-CO').replace(/,/g, '.');
}

function changeQty(btn, delta) {
    const ctrl = btn.closest('.item-controls');
    const val  = ctrl.querySelector('.qty-val');
    const item = btn.closest('.cart-item');
    let qty = parseInt(val.textContent) + delta;
    if (qty < 1) qty = 1;
    val.textContent = qty;
    const base = parseInt(item.dataset.price);
    item.querySelector('.item-price').textContent = fmt(base * qty);
    recalc();
}

function removeItem(btn) {
    const item = btn.closest('.cart-item');
    item.style.transition = 'opacity .25s, max-height .3s';
    item.style.opacity    = '0';
    item.style.maxHeight  = '0';
    item.style.overflow   = 'hidden';
    setTimeout(() => { item.remove(); recalc(); }, 300);
}

function recalc() {
    const items = document.querySelectorAll('.cart-item');
    let total = 0, count = 0;

    items.forEach(item => {
        const cb = item.querySelector('input[type="checkbox"]');
        if (!cb.checked) return;
        const qty  = parseInt(item.querySelector('.qty-val').textContent);
        const base = parseInt(item.dataset.price);
        total += base * qty;
        count++;
    });

    document.getElementById('subtotalVal').textContent = fmt(total);
    document.getElementById('totalVal').textContent    = fmt(total);
    document.getElementById('itemCount').textContent   = count;

    // Actualiza badge del carrito (se carga después del header)
    const badge = document.getElementById('badge');
    if (badge) badge.textContent = '!';
}

// Inicializar al cargar
recalc();