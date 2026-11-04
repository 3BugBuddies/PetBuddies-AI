// Prévia da condição: o código gerado do nome e a regra de exemplo, sem ir ao servidor.
// A mesma normalização do servidor (CondicaoClinicaService.gerarCodigo).
(function () {
    var nome = document.getElementById('rotulo');
    var unidade = document.getElementById('unidade');
    var codigo = document.getElementById('codigo-gerado');
    var numerica = document.getElementById('previa-numerica');
    var booleana = document.getElementById('previa-booleana');
    if (!nome || !codigo) {
        return;
    }

    function gerarCodigo(texto) {
        return texto.normalize('NFD').replace(/[̀-ͯ]/g, '').toUpperCase()
            .replace(/[^A-Z0-9]+/g, '_').replace(/^_+|_+$/g, '').substring(0, 60);
    }

    function atualizar() {
        var rotulo = nome.value.trim() || 'condição';
        var fixo = codigo.getAttribute('data-fixo');
        codigo.textContent = fixo ? fixo : (gerarCodigo(nome.value) || '—');
        var un = unidade && unidade.value.trim() ? ' ' + unidade.value.trim() : '';
        numerica.textContent = rotulo.toLowerCase() + ' > 39,5' + un + ' → acionar clínica';
        booleana.textContent = rotulo.toLowerCase() + ' → dose mínima';
    }

    nome.addEventListener('input', atualizar);
    if (unidade) {
        unidade.addEventListener('input', atualizar);
    }
    atualizar();
})();
