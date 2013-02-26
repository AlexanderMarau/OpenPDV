package br.com.openpdv.controlador.comandos;

import br.com.openpdv.controlador.core.Conexao;
import br.com.openpdv.controlador.core.CoreService;
import br.com.openpdv.controlador.core.Util;
import br.com.openpdv.modelo.core.EBusca;
import br.com.openpdv.modelo.core.EComandoSQL;
import br.com.openpdv.modelo.core.OpenPdvException;
import br.com.openpdv.modelo.core.Sql;
import br.com.openpdv.modelo.core.filtro.ECompara;
import br.com.openpdv.modelo.core.filtro.FiltroObjeto;
import br.com.openpdv.modelo.ecf.EcfPagamentoTipo;
import br.com.openpdv.modelo.produto.ProdComposicao;
import br.com.openpdv.modelo.produto.ProdEmbalagem;
import br.com.openpdv.modelo.produto.ProdGrade;
import br.com.openpdv.modelo.produto.ProdPreco;
import br.com.openpdv.modelo.produto.ProdProduto;
import br.com.openpdv.modelo.sistema.SisCliente;
import br.com.openpdv.modelo.sistema.SisUsuario;
import br.com.phdss.controlador.PAF;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.log4j.Logger;

/**
 * Classe que realiza a acao de recedor os dados do servidor.
 *
 * @author Pedro H. Lira
 */
public class ComandoReceberDados implements IComando {

    private CoreService service;
    private Logger log;

    public ComandoReceberDados() {
        this.service = new CoreService();
        this.log = Logger.getLogger(ComandoReceberDados.class);
    }

    @Override
    public void executar() throws OpenPdvException {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        WebResource wr;

        try {
            emf = Conexao.getInstancia();
            em = emf.createEntityManager();

            // atualiza os usuarios
            wr = Util.getRest(Util.getConfig().get("sinc.host") + "/usuario");
            List<SisUsuario> usuarios = wr.accept(MediaType.APPLICATION_JSON).get(new GenericType<List<SisUsuario>>() {
            });
            
            em.getTransaction().begin();
            for (SisUsuario usu : usuarios) {
                usu.setSisUsuarioLogin(Util.normaliza(usu.getSisUsuarioLogin()));
                service.salvar(em, usu);
            }
            em.getTransaction().commit();
            log.debug("Dados usuarios recebidos -> " + usuarios.size());

            // atualiza os clientes
            Date daCli = (Date) service.buscar(new SisCliente(), "sisClienteData", EBusca.MAXIMO, null);
            wr = Util.getRest(Util.getConfig().get("sinc.host") + "/cliente");
            List<SisCliente> clientes = wr.queryParam("data", Util.getDataHora(daCli)).accept(MediaType.APPLICATION_JSON).get(new GenericType<List<SisCliente>>() {
            });
            
            em.getTransaction().begin();
            for (SisCliente cli : clientes) {
                try {
                    service.salvar(em, cli);
                } catch (Exception ex) {
                    // caso exista duplicidade nao salva
                }
            }
            em.getTransaction().commit();
            log.debug("Dados clientes recebidos -> " + clientes.size());

            // atualiza os tipos de pagamento
            wr = Util.getRest(Util.getConfig().get("sinc.host") + "/tipo_pagamento/");
            List<EcfPagamentoTipo> tiposPagamento = wr.accept(MediaType.APPLICATION_JSON).get(new GenericType<List<EcfPagamentoTipo>>() {
            });
            
            em.getTransaction().begin();
            for (EcfPagamentoTipo tipo : tiposPagamento) {
                // Identifica se a forma de pagamento e uma das 3 permitidas [Dinheiro, Cheque ou Cartao (TEF = true)]
                if (tipo.getEcfPagamentoTipoDescricao().equalsIgnoreCase("dinheiro")) {
                    tipo.setEcfPagamentoTipoCodigo(Util.getConfig().get("ecf.dinheiro"));
                    service.salvar(em, tipo);
                } else if (tipo.getEcfPagamentoTipoDescricao().equalsIgnoreCase("cheque")) {
                    tipo.setEcfPagamentoTipoCodigo(Util.getConfig().get("ecf.cheque"));
                    service.salvar(em, tipo);
                } else if (tipo.isEcfPagamentoTipoTef()) {
                    tipo.setEcfPagamentoTipoCodigo(Util.getConfig().get("ecf.cartao"));
                    service.salvar(em, tipo);
                }
            }
            em.getTransaction().commit();
            log.debug("Dados tipos pagamento recebidos -> " + tiposPagamento.size());

            // atualiza as embalagens
            wr = Util.getRest(Util.getConfig().get("sinc.host") + "/embalagem");
            List<ProdEmbalagem> embalagens = wr.accept(MediaType.APPLICATION_JSON).get(new GenericType<List<ProdEmbalagem>>() {
            });
            
            em.getTransaction().begin();
            for (ProdEmbalagem emb : embalagens) {
                service.salvar(em, emb);
            }
            log.debug("Dados embalagens recebidos -> " + embalagens.size());
            em.getTransaction().commit();

            // recupera os novos produtos
            int limite = Integer.valueOf(Util.getConfig().get("sinc.limite"));
            int pagina = 0;
            List<ProdProduto> novos;
            List<ProdPreco> precos = new ArrayList<>();
            List<ProdComposicao> comps = new ArrayList<>();
            List<ProdGrade> grades = new ArrayList<>();

            // parametros
            MultivaluedMap<String, String> mm = new MultivaluedMapImpl();
            Integer maxId = (Integer) service.buscar(new ProdProduto(), "prodProdutoId", EBusca.MAXIMO, null);
            mm.putSingle("id", maxId != null ? maxId.toString() : "0");
            mm.putSingle("limite", String.valueOf(limite));

            do {
                mm.putSingle("pagina", String.valueOf(pagina));
                wr = Util.getRest(Util.getConfig().get("sinc.host") + "/produtoNovo");
                novos = wr.queryParams(mm).accept(MediaType.APPLICATION_JSON).get(new GenericType<List<ProdProduto>>() {
                });

                em.getTransaction().begin();
                for (ProdProduto prod : novos) {
                    try {
                        // guarda as sub listas
                        for (ProdPreco pp : prod.getProdPrecos()) {
                            pp.setProdProduto(prod);
                            precos.add(pp);
                        }
                        for (ProdComposicao pc : prod.getProdComposicoes()) {
                            pc.setProdProdutoPrincipal(prod);
                            comps.add(pc);
                        }
                        for(ProdGrade pg : prod.getProdGrades()){
                            pg.setProdProduto(prod);
                            grades.add(pg);
                        }

                        // salva o produto
                        prod.setProdPrecos(null);
                        prod.setProdComposicoes(null);
                        prod.setProdGrades(null);
                        prod.setProdProdutoDescricao(Util.normaliza(prod.getProdProdutoDescricao()));
                        service.salvar(em, prod);
                    } catch (Exception ex) {
                        log.error("Nao salvou o produto com ID = " + prod.getProdProdutoId(), ex);
                    }
                }
                em.getTransaction().commit();
                log.debug("Dados dos produtos novos recebidos da pagina " + pagina);
                pagina++;
            } while (novos.size() == limite);

            em.getTransaction().begin();
            // salva os precos
            for (ProdPreco preco : precos) {
                try {
                    service.salvar(em, preco);
                } catch (Exception ex) {
                    log.error("Nao salvou o preco do produto com ID = " + preco.getProdProduto().getProdProdutoId(), ex);
                }
            }
            // salva os itens
            for (ProdComposicao comp : comps) {
                try {
                    service.salvar(em, comp);
                } catch (Exception ex) {
                    log.error("Nao salvou a composicao do produto com ID = " + comp.getProdProduto().getProdProdutoId(), ex);
                }
            }
            // salva as grades 
            for(ProdGrade grade : grades){
                try {
                    service.salvar(em, grade);
                } catch (Exception ex) {
                    log.error("Nao salvou a grade do produto com ID = " + grade.getProdProduto().getProdProdutoId(), ex);
                }
            }
            em.getTransaction().commit();

            // recupera os produtos atualizados
            pagina = 0;
            List<ProdProduto> atualizados;
            mm.clear();
            Date da = (Date) service.buscar(new ProdProduto(), "prodProdutoAlterado", EBusca.MAXIMO, null);
            mm.putSingle("data", Util.getData(da));
            mm.putSingle("limite", String.valueOf(limite));

            do {
                mm.putSingle("pagina", String.valueOf(pagina));
                wr = Util.getRest(Util.getConfig().get("sinc.host") + "/produtoAtualizado");
                atualizados = wr.queryParams(mm).accept(MediaType.APPLICATION_JSON).get(new GenericType<List<ProdProduto>>() {
                });

                em.getTransaction().begin();
                for (ProdProduto prod : atualizados) {
                    try {
                        // guarda as sub listas
                        precos = prod.getProdPrecos();
                        prod.setProdPrecos(null);
                        comps = prod.getProdComposicoes();
                        prod.setProdComposicoes(null);
                        grades = prod.getProdGrades();
                        prod.setProdGrades(null);

                        // salva o produto
                        prod.setProdProdutoDescricao(Util.normaliza(prod.getProdProdutoDescricao()));
                        service.salvar(em, prod);

                        // salva os precos
                        if (!precos.isEmpty()) {
                            FiltroObjeto fo = new FiltroObjeto("prodProduto", ECompara.IGUAL, prod);
                            Sql sql = new Sql(new ProdPreco(), EComandoSQL.EXCLUIR, fo);
                            service.executar(em, sql);
                            for (ProdPreco preco : precos) {
                                preco.setProdProduto(prod);
                                service.salvar(em, preco);
                            }
                        }

                        // salva os itens
                        if (!comps.isEmpty()) {
                            FiltroObjeto fo1 = new FiltroObjeto("prodProdutoPrincipal", ECompara.IGUAL, prod);
                            Sql sql1 = new Sql(new ProdComposicao(), EComandoSQL.EXCLUIR, fo1);
                            service.executar(em, sql1);
                            for (ProdComposicao comp : comps) {
                                comp.setProdProdutoPrincipal(prod);
                                service.salvar(em, comp);
                            }
                        }
                        
                        // salva as grades
                        if(!grades.isEmpty()){
                            FiltroObjeto fo = new FiltroObjeto("prodProduto", ECompara.IGUAL, prod);
                            Sql sql = new Sql(new ProdGrade(), EComandoSQL.EXCLUIR, fo);
                            service.executar(em, sql);
                            for (ProdGrade grade : grades) {
                                grade.setProdProduto(prod);
                                service.salvar(em, grade);
                            }
                        }
                    } catch (Exception ex) {
                        log.error("Nao atualizou o produto com ID = " + prod.getProdProdutoId(), ex);
                    }
                }
                em.getTransaction().commit();
                log.debug("Dados dos produtos atualizados recebidos da pagina " + pagina);
                pagina++;
            } while (atualizados.size() == limite);

            // se sucesso atualiza no arquivo a data do ultimo recebimento
            PAF.AUXILIAR.setProperty("out.recebimento", Util.getDataHora(new Date()));
            PAF.criptografar();
        } catch (Exception ex) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            log.error("Erro ao receber os dados.", ex);
            throw new OpenPdvException(ex.getMessage());
        } finally {
            if (em != null) {
                em.close();
                emf.close();
            }
        }
    }

    @Override
    public void desfazer() throws OpenPdvException {
        // comando nao aplicavel.
    }
}
