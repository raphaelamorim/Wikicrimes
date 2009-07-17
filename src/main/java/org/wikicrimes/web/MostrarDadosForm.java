
package org.wikicrimes.web;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wikicrimes.model.Confirmacao;
import org.wikicrimes.model.Crime;
import org.wikicrimes.model.Usuario;
import org.wikicrimes.service.ConfirmacaoService;
import org.wikicrimes.service.CrimeService;
import org.wikicrimes.util.Constantes;
import org.wikicrimes.util.Horario;

public class MostrarDadosForm extends GenericForm {

	private final Log log = LogFactory.getLog(MostrarDadosForm.class);

	private Crime crime = null;

	private CrimeService crimeService;

	private Long idCrime;
	
	private Confirmacao confirmacao;
	
	public ConfirmacaoService confirmacaoService;

	public ConfirmacaoService getConfirmacaoService() {
		return confirmacaoService;
	}

	public void setConfirmacaoService(ConfirmacaoService confirmacaoService) {
		this.confirmacaoService = confirmacaoService;
	}

	public MostrarDadosForm() {
		crime = new Crime();
		confirmacao = new Confirmacao();
		confirmacao.setCrime(crime);
	}

	public Crime getCrime() {
		return crime;
	}

	public void setCrime(Crime crime) {
		this.crime = crime;
	}

	public CrimeService getCrimeService() {
		return crimeService;
	}

	public void setCrimeService(CrimeService crimeService) {
		this.crimeService = crimeService;
	}

	public Long getIdCrime() {
		return idCrime;
	}

	public void setIdCrime(Long idCrime) {
		this.idCrime = idCrime;
		if (idCrime != null) {
			crime = (Crime) crimeService.get(idCrime);
			crimeService.atualizaVisualizacoes(crime);
		}
	}
	
	public String simConfirma() {
		confirmacao.setConfirma(Constantes.SIM);
		return confirma();
	}

	public String naoConfirma() {
		confirmacao.setConfirma(Constantes.NAO);
		return confirma();
	}
	protected String confirma() {
		String returnPage = FAILURE;

		try {
			
			if (confirmacao.getIdConfirmacao() != null) {
					confirmacao.setDataConfirmacao(new Date());
				if (confirmacaoService.update(confirmacao)) {

					crimeService.atualizaContador(confirmacao.getConfirma(),
							confirmacao.getCrime());

					addMessage("confirmacao.realizada", "");
					returnPage = SUCCESS;
				} else {
					addMessage("errors.geral",
							"Erro ao tentar realizar uma confirma��o de crime.");
				}
			}
			else {
				Usuario user= (Usuario) this.getSessionScope().get("usuario");
				if (user!=null) {
					confirmacao.setUsuario(user);
					confirmacao.setIndicacaoEmail(false);
//					verifica se foi o usuario que registrou esse crime. Usuario nao pode confirmar o proprio crime que registrou
					//TODO forma correta mas esta com erro
					/*if (confirmacao.getCrime().getUsuario().equals(user)){
						addMessage("confirmacao.recusada.propria",
						"");
						return "";
					}*/
					//TODO forma gambiarra
					List<Crime> crimesUser =crimeService.getByUser(user.getIdUsuario());
					for (Crime crimeUser : crimesUser ){
						
						if (crimeUser.equals(confirmacao.getCrime())){
							addMessage("confirmacao.recusada.propria",
							"");
							return "";
						}
						
					}
					
					/*if (confirmacao.getCrime().getUsuario().equals(user)){
						addMessage("confirmacao.recusada.propria",
						"");
						return "";
					}*/
					//verifica se usuario ja confirmou esse crime
					if (!confirmacaoService.getJaConfirmou(confirmacao)){
						confirmacao.setDataConfirmacao(new Date());
						confirmacaoService.insert(confirmacao);
						crimeService.atualizaContador(confirmacao.getConfirma(),
								confirmacao.getCrime());
						addMessage("confirmacao.realizada", "");
						returnPage = SUCCESS;
					}
					else {
						addMessage("confirmacao.recusada.jarealizada",
						"");
						return "";
					}
				}
				else {
					addMessage("errors.geral",
					"Usuario nulo");
				} 
					
			}

		} catch (Exception e) {
			e.printStackTrace();
			addMessage("errors.geral", e.getMessage());
		}

		return returnPage;
	}

	/**
	 * @TODO MELHORAR CODIGO
	 * @return
	 */
	public String getQuantidade() {
		Long quantidade = getCrime().getQuantidade();
		if (quantidade != null) {
			if (quantidade.equals(new Long(1))) {
				return "1";
			}
			if (quantidade.equals(new Long(2))) {
				return "2";
			}
			if (quantidade.equals(new Long(3))) {
				return "3";
			}
			if (quantidade.equals(new Long(4))) {
				return "4";
			}
			if (quantidade.equals(new Long(5))) {
				return "5";
			}
			if (quantidade.equals(new Long(6))) {
				return "6";
			}
			if (quantidade.equals(new Long(7))) {
				return "7";
			}
			if (quantidade.equals(new Long(8))) {
				return "8";
			}
			if (quantidade.equals(new Long(9))) {
				return "9";
			}
			if (quantidade.equals(new Long(10))) {
				return "10";
			}
			if (quantidade.equals(new Long(11))) {
				return "Mais de 10";
			}
		}
		
		return "0";
	}
	
	public String getFaixaEtaria() {
		Long faixaEtaria = getCrime().getFaixaEtaria();
		if (faixaEtaria != null) {
			if (faixaEtaria.equals(new Long(1))) {
				return "Menor";
			}
			if (faixaEtaria.equals(new Long(2))) {
				return "At� 25 Anos";
			}
			if (faixaEtaria.equals(new Long(3))) {
				return "Maior que 25 anos";
			}
		}
		
		return null;
	}
	
	public String getSexo() {
		Long sexo = getCrime().getSexo();
		if (sexo != null) {
			if (sexo.equals(new Long(1))) {
				return "Masculino";
			}
			if (sexo.equals(new Long(0))) {
				return "Feminino";
			}
		}
		
		return null;
	}
	
	public String getQtdMasculino() {
		Long quantidade = getCrime().getQtdMasculino();
		if (quantidade != null) {
			if (quantidade.equals(new Long(0))) {
				return "0";
			}
			if (quantidade.equals(new Long(1))) {
				return "1";
			}
			if (quantidade.equals(new Long(2))) {
				return "2";
			}
			if (quantidade.equals(new Long(3))) {
				return "3";
			}
			if (quantidade.equals(new Long(4))) {
				return "4";
			}
			if (quantidade.equals(new Long(5))) {
				return "5";
			}
			if (quantidade.equals(new Long(6))) {
				return "6";
			}
			if (quantidade.equals(new Long(7))) {
				return "7";
			}
			if (quantidade.equals(new Long(8))) {
				return "8";
			}
			if (quantidade.equals(new Long(9))) {
				return "9";
			}
			if (quantidade.equals(new Long(10))) {
				return "10";
			}
			if (quantidade.equals(new Long(11))) {
				return "Mais de 10";
			}
		}
		
		return null;
	}
	
	public String getQtdFeminino() {
		Long quantidade = getCrime().getQtdFeminino();
		if (quantidade != null) {
			if (quantidade.equals(new Long(0))) {
				return "0";
			}
			if (quantidade.equals(new Long(1))) {
				return "1";
			}
			if (quantidade.equals(new Long(2))) {
				return "2";
			}
			if (quantidade.equals(new Long(3))) {
				return "3";
			}
			if (quantidade.equals(new Long(4))) {
				return "4";
			}
			if (quantidade.equals(new Long(5))) {
				return "5";
			}
			if (quantidade.equals(new Long(6))) {
				return "6";
			}
			if (quantidade.equals(new Long(7))) {
				return "7";
			}
			if (quantidade.equals(new Long(8))) {
				return "8";
			}
			if (quantidade.equals(new Long(9))) {
				return "9";
			}
			if (quantidade.equals(new Long(10))) {
				return "10";
			}
			if (quantidade.equals(new Long(11))) {
				return "Mais de 10";
			}
		}
		
		return null;
	}
	
	public String getEmbedded(){
		HttpSession session = (HttpSession)facesContext.getExternalContext().getSession(false);
		FiltroForm form = (FiltroForm)session.getAttribute("filtroForm");
		if(form == null){
			ResourceBundle bundle = ResourceBundle.getBundle("messages", (Locale)facesContext.getExternalContext().getRequestLocale());	
			return bundle.getString("webapp.sessaoexpirada");	
		}else{			
			String tipoCrimeUrl="";
			String tipoVitimaUrl="";
			String tipoLocalUrl="";
			String dataInicialUrl="";
			String dataFinalUrl="";
			String horarioInicialUrl="";
			String horarioFinalUrl="";
			String entidadeCertificadoraUrl="";
			String crimesConfirmadosPositivamenteUrl = "";
			if(form.getTipoCrime()!=null){
				tipoCrimeUrl = form.getTipoCrime().toString();
			}
			if(form.getTipoVitima()!=null){
				tipoVitimaUrl=form.getTipoVitima().toString();
			}
			if(form.getTipoLocal()!=null){
				tipoLocalUrl=form.getTipoLocal().toString();			
			}
			if(form.getDataInicial()!=null){
				GregorianCalendar gc= new GregorianCalendar();
				gc.setTimeInMillis(form.getDataInicial().getTime());
				String dia = gc.get(GregorianCalendar.DATE)+"";
				if(dia.length()==1)
					dia = "0"+dia;
				String mes = (gc.get(GregorianCalendar.MONTH)+1)+"";
				if(mes.length()==1)
					mes = "0"+mes;
				dataInicialUrl = dia+","+mes+","+gc.get(GregorianCalendar.YEAR);
			}
			if(form.getDataFinal()!=null){
				GregorianCalendar gc= new GregorianCalendar();
				gc.setTimeInMillis(form.getDataFinal().getTime());
				String dia = gc.get(GregorianCalendar.DATE)+"";
				if(dia.length()==1)
					dia = "0"+dia;
				String mes = (gc.get(GregorianCalendar.MONTH)+1)+"";
				if(mes.length()==1)
					mes = "0"+mes;
				dataFinalUrl = dia+","+mes+","+gc.get(GregorianCalendar.YEAR);
			}
			if(form.getHorarioInicial()!=null){
				horarioInicialUrl = form.getHorarioInicial().toString();
			}
			if(form.getHorarioFinal()!=null){
				horarioFinalUrl = form.getHorarioFinal().toString();
			}
			if(form.getEntidadeCertificadora()!=null){
				if(form.getCrimeConfirmadoEntCer())
					entidadeCertificadoraUrl = form.getEntidadeCertificadora().toString();
				else{
					entidadeCertificadoraUrl = "";
				}
			}
			if(form.getCrimeConfirmadoPositivamente()!=null){
				if(form.getCrimeConfirmadoPositivamente())
					crimesConfirmadosPositivamenteUrl = "true";
			}
			 	   		
			return "lat="+crime.getLatitude()+"&lng="+crime.getLongitude()+"&tc="+tipoCrimeUrl+"&tv="+tipoVitimaUrl+"&tl="+tipoLocalUrl+"&di="+dataInicialUrl+"&df="+dataFinalUrl+"&hi="+horarioInicialUrl+"&hf="+horarioFinalUrl+"&ic="+crime.getChave()+"&ec="+entidadeCertificadoraUrl+"&cp="+crimesConfirmadosPositivamenteUrl;
			
		}
		
	}
	
	public String getHorario() {
		Long horario = getCrime().getHorario();
		if (horario != null) {
			Iterator it = Horario.iterator();
			while (it.hasNext()) {
				Horario h = (Horario) it.next();
				Long value = new Long(h.ord());
				if (value.equals(horario)) {
					return h.toString();
				}
			}
		}

		return null;
	}
	
	public String atualizaCrime() {
		
		
		String returnPage = "failure";
		
		crimeService.update(crime);
	   // emailService.sendMailChanges(crime ,FacesContext.getCurrentInstance().getViewRoot().getLocale().toString());
		returnPage = "sucesso";
		

		return returnPage;
	}

}