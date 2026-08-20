//Para insertar información en el modal según el registro
document.addEventListener('DOMContentLoaded', function () {
    const confirmModal = document.getElementById('confirmModal');
    if (confirmModal) {
        confirmModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            document.getElementById('modalId').value = button.getAttribute('data-bs-id');
            document.getElementById('modalDescripcion').textContent = button.getAttribute('data-bs-descripcion');
        });
    }
});

setTimeout(() => {
    document.querySelectorAll('.toast').forEach(t => t.classList.remove('show'));
}, 4000);

document.addEventListener('DOMContentLoaded', function () {
    const idModal = document.body.getAttribute('data-reabrir-modal');
    if (idModal) {
        const modalEl = document.getElementById(idModal);
        if (modalEl) {
            new bootstrap.Modal(modalEl).show();
        }
    }
});
