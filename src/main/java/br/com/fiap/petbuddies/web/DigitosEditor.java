package br.com.fiap.petbuddies.web;

import java.beans.PropertyEditorSupport;

/** Telefone e CNPJ chegam com máscara da tela e entram no banco só com dígitos. */
public class DigitosEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String texto) {
        setValue(texto == null ? null : texto.replaceAll("\\D", ""));
    }
}
