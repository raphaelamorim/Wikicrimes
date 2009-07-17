package org.wikicrimes.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.wikicrimes.dao.GenericCrudDao;
import org.wikicrimes.model.BaseObject;
import org.wikicrimes.model.CrimeCelular;
import org.wikicrimes.model.UsuarioCelular;

public class ServletInterfaceComunicacao extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4059683118894911147L;
	
	private static ApplicationContext ctx;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		
		response.setContentType("text/plain");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setCharacterEncoding("iso-8859-1");
		
		PrintWriter out = response.getWriter();
		
		String dadosCrime = request.getParameter("dados_crime");
		String dadosUsuario = request.getParameter("dados_usuario");
		ctx = WebApplicationContextUtils.getWebApplicationContext(this
				.getServletContext());
		GenericCrudDao genericCrudDao = (GenericCrudDao) ctx
		.getBean("opensocialDao");
		if(dadosCrime != null && dadosCrime != "undefined" && !dadosCrime.equalsIgnoreCase(""))
			out.print(tratarDadosCrime(dadosCrime, genericCrudDao));
		if(dadosUsuario != null && dadosUsuario != "undefined" && !dadosUsuario.equalsIgnoreCase(""))
			out.println(tratarDadosUsuario(dadosUsuario, genericCrudDao));
		
		out.close();
	}
	
	public String tratarDadosCrime(String dadosCrime, GenericCrudDao genericCrudDao){		
		String [] array = dadosCrime.split(";");
		if( array.length == 8){
			CrimeCelular crc = new CrimeCelular();
			crc.setTipoCrime(array[0]);
			crc.setTurno(array[1]);
			crc.setDescricao(array[2]);
			crc.setLatitude(array[3]);
			crc.setLongitude(array[4]);
			crc.setDataHoraRegistro(array[5]+"_"+array[6]);
			crc.setJaImportado(new Long(0));
			UsuarioCelular usc = new UsuarioCelular();
			usc.setTelefoneCelular(array[7]);
			List<BaseObject> usuarios = (List<BaseObject>) genericCrudDao.find(usc);
			if( usuarios.size() == 0 ){
				genericCrudDao.save(usc);
				crc.setUsuarioCelular((UsuarioCelular)genericCrudDao.find(usc).get(0));
			}else{
				crc.setUsuarioCelular((UsuarioCelular)usuarios.get(0));
			}
			genericCrudDao.save(crc);
			
			return "success -> report crime";
		}else{
			return "failure -> report crime";
		}	
	}
	
	public String tratarDadosUsuario(String dadosUsuario, GenericCrudDao genericCrudDao){
		String [] array = dadosUsuario.split(";");
		if( array.length == 2){
			UsuarioCelular usc = new UsuarioCelular();
			if(array[0] != null && !array[0].equalsIgnoreCase("")){	
				usc.setEmail(array[0]);
				if( genericCrudDao.find(usc).size() > 0 )
					return "failure -> user email already exists";
			}	
			usc.setTelefoneCelular(array[1]);
			usc.setEmail(null);
			if(genericCrudDao.find(usc).size()>0)
				return "failure -> user mobile phone already exists";
			usc.setEmail(array[0]);
			genericCrudDao.save(usc);
			return "success -> register user";
		}else{
			return "failure -> register user";
		}	
		
	}
}