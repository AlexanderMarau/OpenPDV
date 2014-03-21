package br.com.openpdv.modelo.ecf;

import br.com.openpdv.modelo.core.Dados;
import br.com.openpdv.modelo.core.EDirecao;
import br.com.openpdv.modelo.sistema.SisCliente;
import br.com.openpdv.modelo.sistema.SisUsuario;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Classe que representa a venda do sistama.
 *
 * @author Pedro H. Lira
 */
@Entity
@Table(name = "ecf_venda")
@XmlRootElement
public class EcfVenda extends Dados implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ecf_venda_id")
    private Integer ecfVendaId;
    @Column(name = "ecf_venda_ccf")
    private int ecfVendaCcf;
    @Column(name = "ecf_venda_coo")
    private int ecfVendaCoo;
    @Column(name = "ecf_venda_data")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ecfVendaData;
    @Column(name = "ecf_venda_bruto")
    private Double ecfVendaBruto;
    @Column(name = "ecf_venda_desconto")
    private Double ecfVendaDesconto;
    @Column(name = "ecf_venda_acrescimo")
    private Double ecfVendaAcrescimo;
    @Column(name = "ecf_venda_liquido")
    private Double ecfVendaLiquido;
    @Column(name = "ecf_venda_fechada")
    private boolean ecfVendaFechada;
    @Column(name = "ecf_venda_cancelada")
    private boolean ecfVendaCancelada;
    @Column(name = "ecf_venda_sinc")
    private boolean ecfVendaSinc;
    @Column(name = "ecf_venda_observacao")
    private String ecfVendaObservacao;
    @JoinColumn(name = "sis_usuario_id")
    @ManyToOne
    private SisUsuario sisUsuario;
    @JoinColumn(name = "sis_vendedor_id", referencedColumnName = "sis_usuario_id")
    @ManyToOne
    private SisUsuario sisVendedor;
    @JoinColumn(name = "sis_gerente_id", referencedColumnName = "sis_usuario_id")
    @ManyToOne
    private SisUsuario sisGerente;
    @JoinColumn(name = "ecf_impressora_id")
    @ManyToOne
    private EcfImpressora ecfImpressora;
    @JoinColumn(name = "ecf_z_id")
    @ManyToOne
    private EcfZ ecfZ;
    @JoinColumn(name = "sis_cliente_id")
    @ManyToOne
    private SisCliente sisCliente;
    @OneToMany(mappedBy = "ecfVenda", fetch = FetchType.EAGER)
    private List<EcfVendaProduto> ecfVendaProdutos;
    @OneToMany(mappedBy = "ecfVenda", fetch = FetchType.EAGER)
    private List<EcfPagamento> ecfPagamentos;
    @OneToMany(mappedBy = "ecfVenda", fetch = FetchType.EAGER)
    private List<EcfTroca> ecfTrocas;
    private transient boolean informouCliente;

    /**
     * Construtor padrao
     */
    public EcfVenda() {
        this(0);
    }

    /**
     * Contrutor padrao passando o id
     *
     * @param ecfVendaId o id do registro.
     */
    public EcfVenda(Integer ecfVendaId) {
        super("EcfVenda", "ecfVendaId", "ecfVendaData", EDirecao.DESC);
        this.ecfVendaId = ecfVendaId;
    }

    @Override
    public Integer getId() {
        return ecfVendaId;
    }

    @Override
    public void setId(Integer id) {
        ecfVendaId = id;
    }

    // GETs e SETs
    public Integer getEcfVendaId() {
        return ecfVendaId;
    }

    public void setEcfVendaId(Integer ecfVendaId) {
        this.ecfVendaId = ecfVendaId;
    }

    public int getEcfVendaCcf() {
        return ecfVendaCcf;
    }

    public void setEcfVendaCcf(int ecfVendaCcf) {
        this.ecfVendaCcf = ecfVendaCcf;
    }

    public int getEcfVendaCoo() {
        return ecfVendaCoo;
    }

    public void setEcfVendaCoo(int ecfVendaCoo) {
        this.ecfVendaCoo = ecfVendaCoo;
    }

    public Date getEcfVendaData() {
        return ecfVendaData;
    }

    public void setEcfVendaData(Date ecfVendaData) {
        this.ecfVendaData = ecfVendaData;
    }

    public Double getEcfVendaBruto() {
        return ecfVendaBruto;
    }

    public void setEcfVendaBruto(Double ecfVendaBruto) {
        this.ecfVendaBruto = ecfVendaBruto;
    }

    public Double getEcfVendaDesconto() {
        return ecfVendaDesconto;
    }

    public void setEcfVendaDesconto(Double ecfVendaDesconto) {
        this.ecfVendaDesconto = ecfVendaDesconto;
    }

    public Double getEcfVendaAcrescimo() {
        return ecfVendaAcrescimo;
    }

    public void setEcfVendaAcrescimo(Double ecfVendaAcrescimo) {
        this.ecfVendaAcrescimo = ecfVendaAcrescimo;
    }

    public Double getEcfVendaLiquido() {
        return ecfVendaLiquido;
    }

    public void setEcfVendaLiquido(Double ecfVendaLiquido) {
        this.ecfVendaLiquido = ecfVendaLiquido;
    }

    public boolean getEcfVendaFechada() {
        return ecfVendaFechada;
    }

    public void setEcfVendaFechada(boolean ecfVendaFechada) {
        this.ecfVendaFechada = ecfVendaFechada;
    }

    public boolean getEcfVendaCancelada() {
        return ecfVendaCancelada;
    }

    public void setEcfVendaCancelada(boolean ecfVendaCancelada) {
        this.ecfVendaCancelada = ecfVendaCancelada;
    }

    public boolean isEcfVendaSinc() {
        return ecfVendaSinc;
    }

    public void setEcfVendaSinc(boolean ecfVendaSinc) {
        this.ecfVendaSinc = ecfVendaSinc;
    }

    public EcfZ getEcfZ() {
        return ecfZ;
    }

    public void setEcfZ(EcfZ ecfZ) {
        this.ecfZ = ecfZ;
    }

    public List<EcfVendaProduto> getEcfVendaProdutos() {
        return ecfVendaProdutos;
    }

    public void setEcfVendaProdutos(List<EcfVendaProduto> ecfVendaProdutos) {
        this.ecfVendaProdutos = ecfVendaProdutos;
    }

    public List<EcfPagamento> getEcfPagamentos() {
        return ecfPagamentos;
    }

    public void setEcfPagamentos(List<EcfPagamento> ecfPagamentos) {
        this.ecfPagamentos = ecfPagamentos;
    }

    public SisCliente getSisCliente() {
        return sisCliente;
    }

    public void setSisCliente(SisCliente sisCliente) {
        this.sisCliente = sisCliente;
    }

    public SisUsuario getSisUsuario() {
        return sisUsuario;
    }

    public void setSisUsuario(SisUsuario sisUsuario) {
        this.sisUsuario = sisUsuario;
    }

    public SisUsuario getSisVendedor() {
        return sisVendedor;
    }

    public void setSisVendedor(SisUsuario sisVendedor) {
        this.sisVendedor = sisVendedor;
    }

    public SisUsuario getSisGerente() {
        return sisGerente;
    }

    public void setSisGerente(SisUsuario sisGerente) {
        this.sisGerente = sisGerente;
    }

    public boolean isInformouCliente() {
        return informouCliente;
    }

    public void setInformouCliente(boolean informouCliente) {
        this.informouCliente = informouCliente;
    }

    public String getEcfVendaObservacao() {
        return ecfVendaObservacao;
    }

    public void setEcfVendaObservacao(String ecfVendaObservacao) {
        this.ecfVendaObservacao = ecfVendaObservacao;
    }

    public List<EcfTroca> getEcfTrocas() {
        return ecfTrocas;
    }

    public void setEcfTrocas(List<EcfTroca> ecfTrocas) {
        this.ecfTrocas = ecfTrocas;
    }

    public EcfImpressora getEcfImpressora() {
        return ecfImpressora;
    }

    public void setEcfImpressora(EcfImpressora ecfImpressora) {
        this.ecfImpressora = ecfImpressora;
    }

}
