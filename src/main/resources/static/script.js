/* =============================================
   Grand Hotel — script.js
   ============================================= */

// ── Price Calculator ──────────────────────────
function calculatePrice() {
    const checkInInput  = document.getElementById('check_in');
    const checkOutInput = document.getElementById('check_out');
    const roomSelect    = document.getElementById('room_id');
    const priceBox      = document.getElementById('price-estimate-box');
    const calculatedPriceSpan = document.getElementById('calculated_price');
    const daysCountSpan = document.getElementById('days_count');
    const submitBtn     = document.getElementById('submitBtn');

    if (checkInInput && checkInInput.value &&
        checkOutInput && checkOutInput.value &&
        roomSelect    && roomSelect.value) {

        const checkIn  = new Date(checkInInput.value);
        const checkOut = new Date(checkOutInput.value);

        if (checkOut <= checkIn) {
            alert('Check-out date must be after the check-in date!');
            checkOutInput.value = '';
            if (priceBox)   priceBox.style.display = 'none';
            if (submitBtn)  submitBtn.disabled = true;
            return;
        }

        const diffTime = Math.abs(checkOut - checkIn);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

        const selectedOption  = roomSelect.options[roomSelect.selectedIndex];
        const pricePerNight   = parseFloat(selectedOption.getAttribute('data-price'));
        const totalPrice      = diffDays * pricePerNight;

        if (daysCountSpan)        daysCountSpan.textContent       = diffDays;
        if (calculatedPriceSpan)  calculatedPriceSpan.textContent = totalPrice.toFixed(2);
        if (priceBox)             priceBox.style.display  = 'block';
        if (submitBtn)            submitBtn.disabled = false;
    } else {
        if (priceBox)  priceBox.style.display = 'none';
        if (submitBtn) submitBtn.disabled = false;
    }
}

// ── Date Constraints ──────────────────────────
document.addEventListener('DOMContentLoaded', function () {
    const checkInInput  = document.getElementById('check_in');
    const checkOutInput = document.getElementById('check_out');

    if (checkInInput && checkOutInput) {
        const today = new Date().toISOString().split('T')[0];
        checkInInput.setAttribute('min', today);
        checkOutInput.setAttribute('min', today);

        checkInInput.addEventListener('change', function () {
            if (checkInInput.value) {
                const nextDay = new Date(checkInInput.value);
                nextDay.setDate(nextDay.getDate() + 1);
                checkOutInput.setAttribute('min', nextDay.toISOString().split('T')[0]);
            }
        });
    }

    // ── Auto-dismiss Alerts after 5 s ─────────
    document.querySelectorAll('.alert').forEach(function (alert) {
        setTimeout(function () {
            alert.style.transition = 'opacity 0.6s ease';
            alert.style.opacity    = '0';
            setTimeout(function () { alert.style.display = 'none'; }, 650);
        }, 5000);
    });

    // ── Mobile Nav Toggle ─────────────────────
    const navToggle = document.getElementById('navToggle');
    const navLinks  = document.getElementById('navLinks');
    if (navToggle && navLinks) {
        navToggle.addEventListener('click', function () {
            navLinks.classList.toggle('open');
        });
    }

    // ── Intersection Observer Fade-ins ────────
    const observer = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.12 });

    document.querySelectorAll('.fade-in').forEach(function (el) {
        observer.observe(el);
    });
});
