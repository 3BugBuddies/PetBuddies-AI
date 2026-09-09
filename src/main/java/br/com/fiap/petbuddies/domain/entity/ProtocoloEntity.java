package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.Especie;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "T_PB_PROTOCOLO")
public class ProtocoloEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PROTOCOLO")
    private Long id;

    @Column(name = "NM_NOME", nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_CATEGORIA_PROTOCOLO", nullable = false, length = 50)
    private CategoriaProtocolo categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "ES_ESPECIE", nullable = false, length = 50)
    private Especie especie;

    @Column(name = "AT_ATIVO", nullable = false)
    private boolean ativo = true;

    @Column(name = "DS_DESCRICAO", length = 2000)
    private String descricao;

    @Column(name = "CA_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "protocolo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RegraProtocoloEntity> regras = new ArrayList<>();

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public CategoriaProtocolo getCategoria() { return categoria; }
    public void setCategoria(CategoriaProtocolo categoria) { this.categoria = categoria; }

    public Especie getEspecie() { return especie; }
    public void setEspecie(Especie especie) { this.especie = especie; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<RegraProtocoloEntity> getRegras() { return regras; }
    public void setRegras(List<RegraProtocoloEntity> regras) { this.regras = regras; }
}
