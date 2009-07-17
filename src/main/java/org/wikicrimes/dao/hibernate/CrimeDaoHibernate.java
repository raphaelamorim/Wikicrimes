package org.wikicrimes.dao.hibernate;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.wikicrimes.dao.CrimeDao;
import org.wikicrimes.model.BaseObject;
import org.wikicrimes.model.Crime;
import org.wikicrimes.model.EntidadeCertificadora;
import org.wikicrimes.model.TipoCrime;
import org.wikicrimes.model.TipoLocal;
import org.wikicrimes.model.TipoVitima;

/**
 * 
 */
public class CrimeDaoHibernate extends GenericCrudDaoHibernate implements
		CrimeDao {

	public CrimeDaoHibernate() {
		setEntity(Crime.class);
	}

				
//	public List<BaseObject> filter(final Map parameters) {
//		
//		return (List<BaseObject>) getHibernateTemplate().execute(new HibernateCallback() {
//			public Object doInHibernate(Session session)
//					throws HibernateException {
//
//				Criteria criteria = session.createCriteria(getEntity());
//				if (parameters.get("tipoCrime") != null) {
//					criteria.add(Expression.eq("tipoCrime", parameters.get("tipoCrime")));
//				}
//				if (parameters.get("tipoLocal") != null) {
//					criteria.add(Expression.eq("tipoLocal", parameters.get("tipoLocal")));
//				}
//				if (parameters.get("tipoVitima") != null) {
////					criteria.createAlias("tipoLocal", "tpl").add( Restrictions.eqProperty("tpl.id", parameters.get("tipoVitima")) );
//					TipoCrime tipoCrime = (TipoCrime) parameters.get("tipoCrime");
//					if (parameters.get("tipoLocal") != null || (tipoCrime.getIdTipoCrime() != 5)) {
//						criteria.createCriteria("tipoLocal").add( Restrictions.like("tipoVitima", parameters.get("tipoVitima")) );
//					}
//					else {
//						criteria.add(Expression.eq("tipoVitima", parameters.get("tipoVitima")));
//					}
//				}
//				if (parameters.get("horarioInicial") != null) {
//					criteria.add(Expression.ge("horario", parameters.get("horarioInicial")));
//				}
//				if (parameters.get("horarioFinal") != null) {
//					criteria.add(Expression.le("horario", parameters.get("horarioFinal")));
//				}
//				if (parameters.get("dataInicial") != null) {
//					criteria.add(Expression.ge("data", parameters.get("dataInicial")));
//				}
//				if (parameters.get("dataFinal") != null) {
//					criteria.add(Expression.le("data", parameters.get("dataFinal")));
//				}
//				criteria.add(Expression.eq("status",Crime.ATIVO));
//				criteria.addOrder(Order.desc("data"));
//				if (parameters.get("maxResults") != null)
//					criteria.setMaxResults((Integer) parameters.get("maxResults"));
//				return criteria.list();
//			}
//		});
//	}

	// M�todo que realiza um filtro parametrizado de crimes
	// Usando HQL
	public List<BaseObject> filter(Map parameters) {
		boolean entrouTipoLocal = false;
		String consulta = "from Crime as crime where ";
		List<BaseObject> listaEntidadeCertificadora = (List<BaseObject>) parameters
		.get("entidadeCertificadora");
		if (listaEntidadeCertificadora!=null)
			consulta = "select crime from Crime as crime join crime.confirmacoes as confirmacao where ";

		// TipoCrime
		if (parameters.get("tipoCrime") != null) {
			consulta += "(crime.tipoCrime.idTipoCrime = "
					+ ((TipoCrime) parameters.get("tipoCrime"))
							.getIdTipoCrime() + ") and ";

		}
		
		// Email Usuario(Traz somente crimes registrados por esse usuario)
		if (parameters.get("emailUsuario") != null) {
			consulta += "(crime.usuario.email like '"
					+ (parameters.get("emailUsuario"))
							+ "') and ";

		}

		// TipoLocal
		if (parameters.get("tipoLocal") != null) {
			consulta += "(crime.tipoVitima.idTipoVitima = "
					+ ((TipoVitima) parameters.get("tipoVitima"))
							.getIdTipoVitima() + ") and ";
			entrouTipoLocal = true;
		}

		// TipoVitima e TipoLocal
		if (parameters.get("tipoVitima") != null) {
			TipoCrime tipoCrime = (TipoCrime) parameters.get("tipoCrime");
			if (parameters.get("tipoLocal") != null	|| (tipoCrime.getIdTipoCrime() != 5)) {
				if (parameters.get("tipoLocal")!=null)
					consulta += "(crime.tipoLocal.idTipoLocal = "+ ((TipoLocal) parameters.get("tipoLocal")).getIdTipoLocal() + ") and ";
				else
					consulta += "(crime.tipoVitima.idTipoVitima = "	+ ((TipoVitima) parameters.get("tipoVitima")).getIdTipoVitima() + ") and "; 
			}
			if (!entrouTipoLocal)
				consulta += "(crime.tipoVitima.idTipoVitima = "	+ ((TipoVitima) parameters.get("tipoVitima")).getIdTipoVitima() + ") and ";

		}

		// DataInicial e DataFinal
		if (parameters.get("dataInicial") != null
				&& parameters.get("dataFinal") != null) {
			Date dataInicial = (Date) parameters.get("dataInicial");
			Date dataFinal = (Date) parameters.get("dataFinal");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String stringDataInicial = dateFormat.format(dataInicial);
			String stringDataFinal = dateFormat.format(dataFinal);

			consulta += "(CRI_DATA BETWEEN '" + stringDataInicial + "' and '"
					+ stringDataFinal + "'" + ") and ";
		}
		// HorarioInicial e HorarioFinal
		if (parameters.get("horarioInicial") != null
				&& parameters.get("horarioFinal") != null) {

			consulta += "(CRI_HORARIO >= "
					+ ((Long) parameters.get("horarioInicial")).longValue()
					+ " and CRI_HORARIO <= "
					+ ((Long) parameters.get("horarioFinal")).longValue()
					+ ") and ";

		}

		// Certifica��o de Crimes
		Boolean isCrimeConfirmadoPositivamente = (Boolean) parameters
				.get("crimeConfirmadoPositivamente");
		// Certifica��o por confirma��o
		if (isCrimeConfirmadoPositivamente != null) {
			if (isCrimeConfirmadoPositivamente)
				consulta += "(crime.confirmacoesPositivas > crime.confirmacoesNegativas) and ";
		}
		
		// Certifica��o por Entidade Certificadora
		EntidadeCertificadora entidadeCertificadora = null;
		if (listaEntidadeCertificadora != null)
			if (listaEntidadeCertificadora.size()>0){
			for (int i = 0; i < listaEntidadeCertificadora.size(); i++) {
				entidadeCertificadora = (EntidadeCertificadora) listaEntidadeCertificadora
						.get(i);
				if (listaEntidadeCertificadora.size() == 1)
					consulta += "((crime.usuario.entidadeCertificadora.idEntidadeCertificadora = "
							+ entidadeCertificadora
									.getIdEntidadeCertificadora() + ") or (confirmacao.entidadeCertificadora.idEntidadeCertificadora = " 
									+ entidadeCertificadora
									.getIdEntidadeCertificadora() + ")) and ";
				else { //nao sei para que serve esse else
					if (i == 0)
						consulta += "(crime.usuario.entidadeCertificadora.idEntidadeCertificadora = "
								+ entidadeCertificadora
										.getIdEntidadeCertificadora();
					else if (i == listaEntidadeCertificadora.size() - 1)
						consulta += " or crime.usuario.entidadeCertificadora.idEntidadeCertificadora = "
								+ entidadeCertificadora
										.getIdEntidadeCertificadora()
								+ ") and ";
					else
						consulta += " or crime.usuario.entidadeCertificadora.idEntidadeCertificadora = "
								+ entidadeCertificadora
										.getIdEntidadeCertificadora();
				}

			 }
			}
		
			else {
				//Todas as entidades certificadoras
				consulta += "((crime.usuario.entidadeCertificadora.idEntidadeCertificadora is not null) or (confirmacao.entidadeCertificadora.idEntidadeCertificadora is not null)) and ";
				
			}
		
		//crimes no Viewport
		if (parameters.get("norte")!=null && parameters.get("sul")!=null && parameters.get("leste")!=null && parameters.get("oeste")!=null){
			
			if (Double.parseDouble(parameters.get("leste").toString())> Double.parseDouble(parameters.get("oeste").toString())) {
				//retorna todos os crimes dentro da southwest/northeast boundary
				consulta+=" (crime.longitude< " + parameters.get("leste") + " and crime.longitude> " + parameters.get("oeste") + ") and (crime.latitude<= " + parameters.get("norte") + " and crime.latitude>= " + parameters.get("sul") + ") and ";
			}
			else {
				 //retorna todos os crimes dentro da southwest/northeast boundary
				 //split over the meridian
				consulta+=" (crime.longitude<= " + parameters.get("leste") + " or crime.longitude>= " + parameters.get("oeste") + ") and (crime.latitude<= " + parameters.get("norte") + " and crime.latitude>= " + parameters.get("sul") + ") and ";
			}
			
			
		}
		
		//Status do crime = ATIVO
		consulta+= "(crime.status = 0) and ";
		if (consulta.indexOf("and") == -1)
			consulta = consulta.substring(0, consulta.indexOf("where"));
		else
			consulta = consulta.substring(0, consulta.length() - 4);
		//Ordenar por data de maneira decrescente
		consulta+= "order by CRI_DATA desc";
		if (parameters.get("maxResults") != null)
			getHibernateTemplate().setMaxResults(
					(Integer) parameters.get("maxResults"));
		else  
			getHibernateTemplate().setMaxResults(0);
					
		//System.err.println("\nConsulta: " + consulta + "\n");
		return (List<BaseObject>) getHibernateTemplate().find(consulta);

	}

	public void incrementaContador(Boolean tipo, Long idCrime) {
		String query = "update Crime set ";
		if (tipo) {
			query += "CRI_CONFIRMACOES_POSITIVAS=ifnull(CRI_CONFIRMACOES_POSITIVAS,0)+1 ";
		} else
			query += " CRI_CONFIRMACOES_NEGATIVAS=ifnull(CRI_CONFIRMACOES_NEGATIVAS,0)+1 ";
		query += " where idCrime=" + idCrime;
		Session session = getHibernateTemplate().getSessionFactory().openSession();
		session.createQuery(query).executeUpdate();
		session.close();

	}
	//Atualiza visualizacoes de um crime
	public void incrementaView( Long idCrime) {
		String query = "update Crime set ";
		query += "CRI_VIEW=ifnull(CRI_VIEW,0)+1 ";
		
		
		query += " where idCrime=" + idCrime;
		Session session = getHibernateTemplate().getSessionFactory().openSession();
		session.createQuery(query).executeUpdate();
		session.close();

	}

	public List<Crime> getByUser(Long idUsuario) {
		String query = "from Crime ";

		if (idUsuario != null) {
			query += "where usuario = " + idUsuario;
		}

		return getHibernateTemplate().find(query);
	}

	public Integer getQTDCrimesAtivos() {
		return (Integer) getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session)
							throws HibernateException, SQLException {
						Query query = session
								.createQuery("select count(*) from Crime where status = 0");
						return ((Long) query.iterate().next()).intValue();
					}
				});
	}

	public Integer getQtdCrimesByDateInterval(final int tipoCrime,
			final String dataInicio, final String dataFim) {
		return (Integer) getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session)
							throws HibernateException, SQLException {
						Query query = session
								.createQuery("select count(*) from Crime where tcr_idtipo_crime = "
										+ tipoCrime
										+ " and cri_data > '"
										+ dataInicio
										+ "' and cri_data < '"
										+ dataFim + "'");

						return ((Long) query.iterate().next()).intValue();
					}
				});
	}

	public Integer getQtdCrimesByDateIntervalPais(final int tipoCrime,
			final String dataInicio, final String dataFim,
			final String siglaPais) {
		return (Integer) getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session)
							throws HibernateException, SQLException {
						Query query = session
								.createQuery("select count(*) from Crime where cri_pais = '"
										+ siglaPais
										+ "' and tcr_idtipo_crime = "
										+ tipoCrime
										+ " and cri_data > '"
										+ dataInicio
										+ "' and cri_data < '"
										+ dataFim + "'");

						return ((Long) query.iterate().next()).intValue();
					}
				});
	}

	public Integer getQtdCrimesByDateIntervalEstado(final int tipoCrime,
			final String dataInicio, final String dataFim,
			final String siglaEstado) {
		return (Integer) getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session)
							throws HibernateException, SQLException {
						Query query = session
								.createQuery("select count(*) from Crime where cri_estado = '"
										+ siglaEstado
										+ "' and tcr_idtipo_crime = "
										+ tipoCrime
										+ " and cri_data > '"
										+ dataInicio
										+ "' and cri_data < '"
										+ dataFim + "'");

						return ((Long) query.iterate().next()).intValue();
					}
				});
	}

	public Integer getQtdCrimesByDateIntervalCidade(final int tipoCrime,
			final String dataInicio, final String dataFim,
			final String nomeCidade) {
		return (Integer) getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session)
							throws HibernateException, SQLException {
						Query query = session
								.createQuery("select count(*) from Crime where cri_cidade = '"
										+ nomeCidade
										+ "' and tcr_idtipo_crime = "
										+ tipoCrime
										+ " and cri_data > '"
										+ dataInicio
										+ "' and cri_data < '"
										+ dataFim + "'");

						return ((Long) query.iterate().next()).intValue();
					}
				});
	}

	public List<Crime> getCrimesSemEstatisticas() {

		String query = "from Crime ";

		query += "where status =0 and cri_endereco is null and cri_cidade is null and cri_estado is null and cri_pais is null ";

		return getHibernateTemplate().find(query);

		/*
		 * Crime crime = new Crime(); crime.setStatus(Crime.ATIVO);
		 * crime.setEstado(""); crime.setCidade(""); crime.setPais("");
		 * crime.setEndereco("");
		 * 
		 * return (List<Crime>) getHibernateTemplate().findByExample(crime);
		 */

	}
	public List<Crime> getCrimesSemChave() {
    	
    	String query = "from Crime ";

    	
    	    query += "where cri_chave is null";
    	

    	return getHibernateTemplate().find(query);
    	  		
    	
    }
   /*
    * Metodo para retornar crimes dentro de Viewport
    */
	public List<Crime> getCrimesByViewPort(final double norte, final double sul, final double leste, final double oeste) {
		  List<Crime> crimes=null;
		  crimes = (List<Crime>) getHibernateTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session)
						throws HibernateException { 
					
					Criteria criteria = session.createCriteria(getEntity());
					criteria.add(Expression.le("latitude", norte));
					criteria.add(Expression.ge("latitude", sul));
					criteria.add(Expression.le("longitude", leste));
					criteria.add(Expression.ge("longitude", oeste));
					
					return criteria.list();
				}});
		  
		return  crimes;
	}
    	
	public List<Crime> find(Crime crime){
		return this.getHibernateTemplate().findByExample(crime);
	}
	
	public List<Crime> pesquisarCrime(Crime crime){
		String query = "from Crime c ";

    	
	    query += "where c.descricao like '%"+crime.getDescricao()+"%' and c.status <> 1 order by c.idCrime desc";
	    getHibernateTemplate().setMaxResults(20);

	    return getHibernateTemplate().find(query);
	}
	
	public List<Crime> getCrimesMaisVistos(){
		String query = "from Crime crime where crime.status <> 1 ";

	
	    query += "order by crime.visualizacoes desc";
	    getHibernateTemplate().setMaxResults(5);

	    return getHibernateTemplate().find(query);
	}


	@Override
	public List<Crime> getCrimesMaisComentados() {
		String query = "select c from Crime c , Comentario con" +
				" where c.idCrime = con.crime.idCrime and c.status <> 1";	
		
	    query += " group by c order by count(con) desc";
	    getHibernateTemplate().setMaxResults(5);

	    return getHibernateTemplate().find(query);
	}


	@Override
	public List<Crime> getCrimesMaisConfirmados() {
		String query = "from Crime c where c.status <> 1";		
    	
	    query += "order by c.confirmacoesPositivas desc";
	    getHibernateTemplate().setMaxResults(5);

	    return getHibernateTemplate().find(query);
	}


	@Override
	public void atualizaContadorCometarios(Long idCrime) {
		String query = "update Crime set ";
		query += "CRI_QTD_COMENTARIOS=ifnull(CRI_QTD_COMENTARIOS,0)+1 ";
		
		
		query += " where idCrime=" + idCrime;
		Session session = getHibernateTemplate().getSessionFactory().openSession();
		session.createQuery(query).executeUpdate();
		session.close();
		
	}	
	

}