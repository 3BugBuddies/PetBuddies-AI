// Máscaras de exibição: o servidor guarda só dígitos (ver DigitosBinder).
(function () {
    function telefone(v) {
        var d = v.replace(/\D/g, '').slice(0, 11);
        if (d.length <= 2) return d;
        if (d.length <= 6) return '(' + d.slice(0, 2) + ') ' + d.slice(2);
        if (d.length <= 10) return '(' + d.slice(0, 2) + ') ' + d.slice(2, 6) + '-' + d.slice(6);
        return '(' + d.slice(0, 2) + ') ' + d.slice(2, 7) + '-' + d.slice(7);
    }
    function cnpj(v) {
        var d = v.replace(/\D/g, '').slice(0, 14);
        return d.replace(/^(\d{2})(\d)/, '$1.$2').replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3')
            .replace(/\.(\d{3})(\d)/, '.$1/$2').replace(/(\d{4})(\d)/, '$1-$2');
    }
    var mascaras = { telefone: telefone, cnpj: cnpj };
    document.querySelectorAll('input[data-mascara]').forEach(function (input) {
        var f = mascaras[input.dataset.mascara];
        if (!f) return;
        input.value = f(input.value);
        input.addEventListener('input', function () { input.value = f(input.value); });
    });
})();
