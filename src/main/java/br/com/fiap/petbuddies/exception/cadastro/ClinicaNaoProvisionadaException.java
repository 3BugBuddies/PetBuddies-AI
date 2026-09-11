package br.com.fiap.petbuddies.exception.cadastro;

public class ClinicaNaoProvisionadaException extends RuntimeException {
    public ClinicaNaoProvisionadaException() {
        super("Nenhuma clínica provisionada: rode a migration de seed antes de registrar veterinário.");
    }
}
