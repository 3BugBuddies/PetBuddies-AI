package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.Especie;
import br.com.fiap.petbuddies.domain.enums.Porte;
import br.com.fiap.petbuddies.domain.enums.Sexo;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "protocolo")
public class ProtocoloEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaProtocolo categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Especie especie;

    @Enumerated(EnumType.STRING)
    private Porte porte;

    @Enumerated(EnumType.STRING)
    private Sexo sexo;

    private Boolean castrado;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column
    private Integer idadeMinMeses;

    @Column
    private Integer idadeMaxMeses;

    @Column
    private String descricao;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @OneToMany(mappedBy = "protocolo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EventoProtocoloEntity> eventos = new ArrayList<>();

    @PrePersist
    private void prePersist() {
        criadoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public CategoriaProtocolo getCategoria() { return categoria; }
    public void setCategoria(CategoriaProtocolo categoria) { this.categoria = categoria; }

    public Especie getEspecie() { return especie; }
    public void setEspecie(Especie especie) { this.especie = especie; }

    public Porte getPorte() { return porte; }
    public void setPorte(Porte porte) { this.porte = porte; }

    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }

    public Boolean getCastrado() { return castrado; }
    public void setCastrado(Boolean castrado) { this.castrado = castrado; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public Integer getIdadeMinMeses() { return idadeMinMeses; }
    public void setIdadeMinMeses(Integer idadeMinMeses) { this.idadeMinMeses = idadeMinMeses; }

    public Integer getIdadeMaxMeses() { return idadeMaxMeses; }
    public void setIdadeMaxMeses(Integer idadeMaxMeses) { this.idadeMaxMeses = idadeMaxMeses; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDateTime getCriadoEm() { return criadoEm; }

    public List<EventoProtocoloEntity> getEventos() { return eventos; }
    public void setEventos(List<EventoProtocoloEntity> eventos) { this.eventos = eventos; }
}
