function calculatePrice() {
    const checkInInput = document.getElementById('check_in');
    const checkOutInput = document.getElementById('check_out');
    const roomSelect = document.getElementById('room_id');
    const priceBox = document.getElementById('price-estimate-box');
    const calculatedPriceSpan = document.getElementById('calculated_price');
    const daysCountSpan = document.getElementById('days_count');
    const submitBtn = document.getElementById('submitBtn');

    if (checkInInput.value && checkOutInput.value && roomSelect.value) {
        const checkIn = new Date(checkInInput.value);
        const checkOut = new Date(checkOutInput.value);
        
        // Ensure check-out is after check-in
        if (checkOut <= checkIn) {
            alert("Check-out date must be after the check-in date!");
            checkOutInput.value = "";
            priceBox.style.display = 'none';
            submitBtn.disabled = true;
            return;
        }

        const diffTime = Math.abs(checkOut - checkIn);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)); 
        
        const selectedOption = roomSelect.options[roomSelect.selectedIndex];
        const pricePerNight = parseFloat(selectedOption.getAttribute('data-price'));
        
        const totalPrice = diffDays * pricePerNight;

        daysCountSpan.textContent = diffDays;
        calculatedPriceSpan.textContent = totalPrice.toFixed(2);
        priceBox.style.display = 'block';
        submitBtn.disabled = false;
    } else {
        priceBox.style.display = 'none';
        submitBtn.disabled = false;
    }
}

// Automatically disable past dates for check-in and check-out
document.addEventListener('DOMContentLoaded', function() {
    const checkInInput = document.getElementById('check_in');
    const checkOutInput = document.getElementById('check_out');
    
    if(checkInInput && checkOutInput) {
        let today = new Date().toISOString().split('T')[0];
        checkInInput.setAttribute('min', today);
        checkOutInput.setAttribute('min', today);
        
        checkInInput.addEventListener('change', function() {
            if(checkInInput.value) {
                let nextDay = new Date(checkInInput.value);
                nextDay.setDate(nextDay.getDate() + 1);
                checkOutInput.setAttribute('min', nextDay.toISOString().split('T')[0]);
            }
        });
    }
});
