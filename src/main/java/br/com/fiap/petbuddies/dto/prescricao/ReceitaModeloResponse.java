package br.com.fiap.petbuddies.dto.prescricao;

import br.com.fiap.petbuddies.domain.entity.ReceitaModeloEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReceitaModeloResponse {

    private Long id;
    private String titulo;
    private String medicamento;
    private BigDecimal doseMin;
    private BigDecimal doseMax;
    private String unidade;
    private Integer frequenciaDia;
    private Integer duracaoDias;
    private String orientacao;
    private Integer versao;
    private Boolean ativo;
    private Long clinicaId;
    private Long autorId;
    private String autorNome;
    private List<RegraReceitaModeloResponse> regras;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Exige autor e regras já carregados (entity graph do repository); fora da transação o proxy LAZY não abre.
    public static ReceitaModeloResponse from(ReceitaModeloEntity entity) {
        ReceitaModeloResponse dto = new ReceitaModeloResponse();
        dto.id = entity.getId();
        dto.titulo = entity.getTitulo();
        dto.medicamento = entity.getMedicamento();
        dto.doseMin = entity.getFaixaDose().getDoseMin();
        dto.doseMax = entity.getFaixaDose().getDoseMax();
        dto.unidade = entity.getFaixaDose().getUnidade();
        dto.frequenciaDia = entity.getFrequenciaDia();
        dto.duracaoDias = entity.getDuracaoDias();
        dto.orientacao = entity.getOrientacao();
        dto.versao = entity.getVersao();
        dto.ativo = entity.isAtivo();
        dto.clinicaId = entity.getClinica().getId();
        dto.autorId = entity.getAutor().getId();
        dto.autorNome = entity.getAutor().getNome();
        dto.regras = entity.getRegras().stream().map(RegraReceitaModeloResponse::from).toList();
        dto.createdAt = entity.getAuditoria().getCreatedAt();
        dto.updatedAt = entity.getAuditoria().getUpdatedAt();
        return dto;
    }
}
