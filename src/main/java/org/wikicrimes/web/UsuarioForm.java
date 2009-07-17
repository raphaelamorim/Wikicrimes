package org.wikicrimes.web;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wikicrimes.model.Perfil;
import org.wikicrimes.model.Usuario;
import org.wikicrimes.service.EmailService;
import org.wikicrimes.service.UsuarioService;

public class UsuarioForm extends GenericForm {
	private final Log log = LogFactory.getLog(UsuarioForm.class);

	private String id;
	private Usuario usuario;
	private String cemail = null;
	private UsuarioService service;
	private EmailService emailService;
	private String lat;
	private String lng;
	private String idCrime;
	private String idRelato;
	private String mensagemLogin;
	private String tipoRegistro; 
	private String idUsuarioRedeSocial;
	private String idRedeSocial;
	private String csenha = null;
	private String senhaAtual = null;
	private String nsenha = null;
	private List<SelectItem> estados;
	private String estado;
	private ReCaptchaImpl reCaptcha;
	private String termoUso;
	private boolean concordaTermo = false;
	private Integer vizualizarCrimeOpensocial;

	public ReCaptchaImpl getReCaptcha() {
		return reCaptcha;
	}

	public void setReCaptcha(ReCaptchaImpl reCaptcha) {
		this.reCaptcha = reCaptcha;
	}

	public UsuarioForm() {
		setUsuario((Usuario) this.getSessionScope().get("usuario"));

		estados = getEstadosBrasileiros();
		if (usuario == null)
			usuario = new Usuario();
	}

	public static List<SelectItem> getEstadosBrasileiros() {
		List<SelectItem> listaSelect = new ArrayList<SelectItem>();
		listaSelect.add(new SelectItem("AC", "AC"));
		listaSelect.add(new SelectItem("AL", "AL"));
		listaSelect.add(new SelectItem("AM", "AM"));
		listaSelect.add(new SelectItem("AP", "AP"));
		listaSelect.add(new SelectItem("BA", "BA"));
		listaSelect.add(new SelectItem("CE", "CE"));
		listaSelect.add(new SelectItem("DF", "DF"));
		listaSelect.add(new SelectItem("ES", "ES"));
		listaSelect.add(new SelectItem("GO", "GO"));
		listaSelect.add(new SelectItem("MA", "MA"));
		listaSelect.add(new SelectItem("MG", "MG"));
		listaSelect.add(new SelectItem("MS", "MS"));
		listaSelect.add(new SelectItem("MT", "MT"));
		listaSelect.add(new SelectItem("PA", "PA"));
		listaSelect.add(new SelectItem("PB", "PB"));
		listaSelect.add(new SelectItem("PE", "PE"));
		listaSelect.add(new SelectItem("PI", "PI"));
		listaSelect.add(new SelectItem("PR", "PR"));
		listaSelect.add(new SelectItem("RJ", "RJ"));
		listaSelect.add(new SelectItem("RN", "RN"));
		listaSelect.add(new SelectItem("RO", "RO"));
		listaSelect.add(new SelectItem("RR", "RR"));
		listaSelect.add(new SelectItem("RS", "RS"));
		listaSelect.add(new SelectItem("SC", "SC"));
		listaSelect.add(new SelectItem("SE", "SE"));
		listaSelect.add(new SelectItem("SP", "SP"));
		listaSelect.add(new SelectItem("TO", "TO"));
		return listaSelect;
	}

	public List getEstados() {
		return estados;
	}

	public void setEstados(List estados) {
		this.estados = estados;
	}

	public String getCsenha() {
		return csenha;
	}

	public void setCsenha(String csenha) {
		this.csenha = csenha;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public void setUsuarioService(UsuarioService service) {
		this.service = service;
	}

	public String getIdRelato() {
		return idRelato;
	}

	public void setIdRelato(String idRelato) {
		this.idRelato = idRelato;
	}

	public String edit() {

		if (id != null) {
			// assuming edit
			setUsuario((Usuario) service.get(Long.getLong(id)));
		}

		return "success";
	}

	private boolean validaCaptcha(String desafio, String response) {
		HttpServletRequest request = (HttpServletRequest) FacesContext
				.getCurrentInstance().getExternalContext().getRequest();
		String ip = request.getRemoteAddr();
		ReCaptchaResponse resp = reCaptcha.checkAnswer(ip, desafio, response);

		return resp.isValid();

	}

	public String insert() {
		String returnPage = "failure";

		/*
		 * if(!validaCaptcha(getRequestParameter("recaptcha_challenge_field"),getRequestParameter
		 * ("recaptcha_response_field"))){ addMessage("erro.captcha","");
		 * 
		 * return ""; }
		 */

		// checa se ja existe email cadastrado ou se e convidado
		Usuario userResult = service.getUsuario(getUsuario().getEmail());
		if (userResult != null) {
			// se nao for convidado nao passa
			if (userResult.getPerfil().getIdPerfil() != Perfil.CONVIDADO) {
				addMessage("erro.email.existente", "");
				return null;
			}

			else {
				// se for atualiza id
				getUsuario().setIdUsuario(userResult.getIdUsuario());
			}
		}

		try {
			getUsuario().setIp(this.getIp());
			if (userResult != null) {
				if (userResult.getPerfil().getIdPerfil() != Perfil.CONVIDADO)
					getUsuario().setPerfil(new Perfil(Perfil.USUARIO));
				else
					getUsuario().setPerfil(new Perfil(Perfil.CONVIDADO));
			} else {
				getUsuario().setPerfil(new Perfil(Perfil.USUARIO));
			}
			getUsuario().setLat(new Double(lat));
			getUsuario().setLng(new Double(lng));
			getUsuario().setDataHoraRegistro(new Date());

			if (service.insert(getUsuario())) {
				// addMessage("usuario.registrado", args );
				addMessage("usuario.registrado", getUsuario().getEmail()
						.toString());

				returnPage = "success";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnPage;
	}

	public String update() {
		String returnPage = FAILURE;

		Usuario usuarioBD = (Usuario) service.get(((Usuario) this
				.getSessionScope().get("usuario")).getIdUsuario());
		// autentica senha
		if (getUsuario().getSenha().equals(usuarioBD.getSenha())) {
			// autenticado

			usuarioBD.setDataHoraAlteracao(new Date());
			usuarioBD.setIpAlteracao(getIp());
			usuarioBD.setAniversario(getUsuario().getAniversario());
			usuarioBD.setCidade(getUsuario().getCidade());
			usuarioBD.setEstado(getUsuario().getEstado());
			usuarioBD.setHomepage(getUsuario().getHomepage());
			usuarioBD.setIdiomaPreferencial(getUsuario()
					.getIdiomaPreferencial());
			usuarioBD.setLat(getUsuario().getLat());
			usuarioBD.setLng(getUsuario().getLng());
			usuarioBD.setPais(getUsuario().getPais());
			usuarioBD.setPrimeiroNome(getUsuario().getPrimeiroNome());
			usuarioBD.setUltimoNome(getUsuario().getUltimoNome());
			usuarioBD.setReceberNewsletter(getUsuario().getReceberNewsletter());
			usuarioBD.setTutorAtivado(getUsuario().getTutorAtivado());
			
			if (service.update(usuarioBD)) {
				((Usuario) this.getSessionScope().get("usuario")).setTutorAtivado(getUsuario().getTutorAtivado());
				HttpSession session = (HttpSession)facesContext.getExternalContext().getSession(false);
				FiltroForm form = (FiltroForm)session.getAttribute("filtroForm");
				form.setTutorAtivado(getUsuario().getTutorAtivado());
				System.out.println("[" + new Date() + "] "
						+ usuarioBD.getEmail() + " atualizou seu cadastro...");
				addMessage("usuario.atualizado", getUsuario().getEmail()
						.toString());
				returnPage = SUCCESS;
			}
		} else {
			// senha invalida
			addMessage("usuario.senha.invalida", "");
			returnPage = FAILURE;
		}
		return returnPage;
	}

	public String alteraSenha() {
		String returnPage = FAILURE;
		try {
			if (getNsenha() != null || !getNsenha().equals(" ")) {
				// alterar senha
				String senha = ((Usuario) this.getSessionScope().get("usuario"))
						.getSenha();
				// autentica senha
				if (getSenhaAtual().equals(senha)) {
					// autenticado
					getUsuario().setSenha(getNsenha());
					addMessage("usuario.recupere.sucesso", getUsuario()
							.getEmail().toString());
					getUsuario().setDataHoraAlteracao(new Date());
					getUsuario().setIpAlteracao(getIp());
					if (service.update(getUsuario())) {
						System.out.println("[" + new Date() + "] "
								+ usuario.getEmail()
								+ " atualizou seu cadastro...");
						addMessage("usuario.atualizado", getUsuario()
								.getEmail().toString());
						// TODO enviar email com mudancas
						// emailService.enviarEmailRecuperaSenha(usuario,
						// FacesContext.getCurrentInstance().getViewRoot().getLocale().toString());

						returnPage = SUCCESS;
					} else {
						addMessage("usuario.senha.invalida", "");
						return returnPage;
					}

				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			return "failure";
		}
		return returnPage;
	}

	public String delete() {
		service.delete(getUsuario());
		addMessage("usuario.deleted", getUsuario().getNome());

		return "success";
	}

	public String login() {
		try {

			Usuario usuario = service.login(getUsuario());
			// System.out.println("id sessao: " + getExternalSession().getId());
			if (usuario != null) {

				if (usuario.getConfirmacao().equals(Usuario.TRUE)) {
					
					
					this.getSessionScope().put("usuario", usuario);
					HttpSession session = (HttpSession)facesContext.getExternalContext().getSession(false);
					FiltroForm form = (FiltroForm)session.getAttribute("filtroForm");
					if (form!=null){
						if(form.getTutorAtivado().equalsIgnoreCase("0")){
							if(usuario.getTutorAtivado() == null || usuario.getTutorAtivado().equalsIgnoreCase("1")){
								usuario.setTutorAtivado("0");
								service.update(usuario);
							}	
						}else{
							if(usuario.getTutorAtivado()!= null && usuario.getTutorAtivado().equalsIgnoreCase("0")){
								form.setTutorAtivado("0");
							}
						}
					}
					// addMessage("usuario.loginSucess", "");
					getRequestScope().put("loginMessage", "");
					System.out.println("[" + new Date() + "] "
							+ usuario.getEmail() + " logado..");

					return null;

				} else {
					addMessage("usuario.login.naoconfirmado", "");
					getRequestScope().put("loginMessage",
							getMessage("usuario.login.naoconfirmado", ""));
					System.out.println("[" + new Date() + "]"
							+ usuario.getEmail()
							+ " cadastro nao confirmado...");
					return null;
				}
			} else {
				addMessage("usuario.loginFail", "");
				getRequestScope().put("loginMessage",
						getMessage("usuario.loginFail", ""));
				System.out.println("[" + new Date() + "]"
						+ " Login inexistente ou Senha Invalida");
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String recuperaSenha() {
		String returnPage = FAILURE;
		Usuario usuario = service.getUsuario(getUsuario().getEmail());
		if (usuario != null) {
			emailService.enviarEmailRecuperaSenha(usuario, FacesContext
					.getCurrentInstance().getViewRoot().getLocale().toString());
			addMessage("usuario.recupere.sucesso", getUsuario().getEmail()
					.toString());
			returnPage = SUCCESS;
		} else {
			addMessage("usuario.recupere.falha", getUsuario().getEmail()
					.toString());
		}
		return returnPage;
	}

	public String logout() {
		try {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			Usuario usuario = (Usuario) facesContext.getExternalContext()
					.getSessionMap().remove("usuario");
			if (usuario != null)
				System.out.println("[" + new Date() + "] " + usuario.getEmail()
						+ " logout..");
			addMessage("usuario.logoutSucess", "");
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void clearAll() {
		this.usuario = null;
		getRequestScope().put("loginMessage", "");
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	// Convenience methods ====================================================
	public static String getRequestParameter(String name) {
		return (String) FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get(name);
	}

	public String getIp() {
		HttpServletRequest request = (HttpServletRequest) FacesContext
				.getCurrentInstance().getExternalContext().getRequest();
		String ip = request.getRemoteAddr();
		return ip;
	}

	public String getTermoUso() {
		StringWriter sw = new StringWriter();
		try {
			if (FacesContext.getCurrentInstance().getViewRoot().getLocale()
					.toString().equals("pt")
					|| FacesContext.getCurrentInstance().getViewRoot()
							.getLocale().toString().equals("pt_BR"))
				IOUtils.copy(service.getTxtTermoUso().getInputStream(), sw);
			else if (FacesContext.getCurrentInstance().getViewRoot()
					.getLocale().toString().equals("es"))
				IOUtils.copy(service.getTxtTermoUso_ES().getInputStream(), sw);
			else
				IOUtils.copy(service.getTxtTermoUso_EN().getInputStream(), sw);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
		return sw.toString();
	}

	public void setTermoUso(String termoUso) {
		this.termoUso = termoUso;
	}

	public boolean isConcordaTermo() {
		return concordaTermo;
	}

	public void setConcordaTermo(boolean concordaTermo) {
		this.concordaTermo = concordaTermo;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLng() {
		return lng;
	}

	public void setLng(String lng) {
		this.lng = lng;
	}

	public String loginEfetuado() {
		return null;
	}

	public String getCemail() {
		return cemail;
	}

	public void setCemail(String cemail) {
		this.cemail = cemail;
	}

	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	public String getIdCrime() {
		return idCrime;
	}

	public void setIdCrime(String idCrime) {
		this.idCrime = idCrime;
	}

	public String getIdUsuarioRedeSocial() {
		return idUsuarioRedeSocial;
	}

	public void setIdUsuarioRedeSocial(String idUsuarioRedeSocial) {
		this.idUsuarioRedeSocial = idUsuarioRedeSocial;
	}

	public String getIdRedeSocial() {
		return idRedeSocial;
	}

	public void setIdRedeSocial(String idRedeSocial) {
		this.idRedeSocial = idRedeSocial;
	}

	public Integer getVizualizarCrimeOpensocial() {
		return vizualizarCrimeOpensocial;
	}

	public void setVizualizarCrimeOpensocial(Integer vizualizarCrimeOpensocial) {
		this.vizualizarCrimeOpensocial = vizualizarCrimeOpensocial;
	}

	public String getNsenha() {
		return nsenha;
	}

	public void setNsenha(String nsenha) {
		this.nsenha = nsenha;
	}

	public String getSenhaAtual() {
		return senhaAtual;
	}

	public void setSenhaAtual(String senhaAtual) {
		this.senhaAtual = senhaAtual;
	}

	public String getMensagemLogin() {
		return mensagemLogin;
	}

	public void setMensagemLogin(String mensagemLogin) {
		this.mensagemLogin = mensagemLogin;
	}

	public String getTipoRegistro() {
		return tipoRegistro;
	}

	public void setTipoRegistro(String tipoRegistro) {
		this.tipoRegistro = tipoRegistro;
	}
	
	

}