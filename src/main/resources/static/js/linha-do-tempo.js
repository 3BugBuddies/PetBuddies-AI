// Filtro da linha do tempo da ficha: esconde as entradas de outros tipos, sem ir ao servidor.
document.addEventListener('click', function (evento) {
    var botao = evento.target.closest('[data-filtro]');
    if (!botao) {
        return;
    }
    var filtro = botao.getAttribute('data-filtro');
    document.querySelectorAll('[data-filtro]').forEach(function (b) {
        b.classList.toggle('on', b === botao);
    });
    document.querySelectorAll('.pc-linha-item').forEach(function (item) {
        item.hidden = filtro !== 'TUDO' && item.getAttribute('data-tipo') !== filtro;
    });
});
