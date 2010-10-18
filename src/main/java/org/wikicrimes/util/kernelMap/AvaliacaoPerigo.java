package org.wikicrimes.util.kernelMap;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wikicrimes.model.BaseObject;
import org.wikicrimes.model.PontoLatLng;
import org.wikicrimes.service.CrimeService;
import org.wikicrimes.servlet.ServletKernelMap;
import org.wikicrimes.util.NumerosUtil;
import org.wikicrimes.util.rotaSegura.geometria.Ponto;


public class AvaliacaoPerigo{
	
	private CrimeService crimeService; 

	private static final int PROPORCAO_TAMANHO_MAPA = 10; //proporcao do tamanho do mapa de kernel em relacao ao circulo avaliado
	private static final double KM_POR_GRAU = 110.5; //aproximacao do valor medio pra distancia em 1 grau de latitude ou longitude
	private static final int ZOOM = 13;
	private static final int NODE_SIZE = 5;
	private static final double BANDWIDTH = 30;
	
	//condicoes pra avaliacao
	private int minimoCrimes;
	private double amplitudeMinima; //diferenca entre a menor e a maior densidade do mapa de kernel 
	
	public AvaliacaoPerigo(CrimeService crimeService){
		this.crimeService = crimeService;
	}
	
	public AvaliacaoPerigo(CrimeService crimeService, int minimoCrimes, double amplitudeMinima){
		this.crimeService = crimeService;
		this.minimoCrimes = minimoCrimes;
		this.amplitudeMinima = amplitudeMinima;
	}
	
	/**
	 * @param centro : lat lng
	 * @param raio : raio do circulo, em metros 
	 * @param dataInicial : o periodo comeca na dataInicial e termina na data de hoje
	 * @return um double variando de 0 (densidade minima do mapa) a 1 (densidade maxima do mapa)
	 * obs:o "mapa" eh um quadrado grande que contem o circulo.
	 */
	public double avaliarCirculo(PontoLatLng centro, double raio, Date dataInicial){
		
		//conversoes e mapa de kernel
		Ponto centroPixel = new Ponto(centro.toPixel(ZOOM));
		int raioPixel = raioMetrosToPixel(centro, raio);
		int lado = raioPixel*PROPORCAO_TAMANHO_MAPA;
		Rectangle boundsMapa = new Rectangle(centroPixel.x-lado/2, centroPixel.y-lado/2, lado, lado);
		List<Point> crimes = buscaCrimes(boundsMapa, dataInicial);
		if(crimes.size() < minimoCrimes) return -1;
		KernelMap kernel = new KernelMap(NODE_SIZE, BANDWIDTH, boundsMapa, crimes);
		
		//calculo de densidades
		double maxMapa = kernel.getMaxDens();
		double minMapa = kernel.getMinDens();
		double amplitude = maxMapa-minMapa; 
		if(amplitude < amplitudeMinima) return -1;
		double mediaCirculo = densidadeMediaNoCirculo(kernel, centroPixel, raioPixel);
		double normalizada = (mediaCirculo-minMapa)/amplitude;
		return normalizada;
	}
	
	private int raioMetrosToPixel(PontoLatLng centro, double raio){
		double raioLatLng = (raio/1000)/KM_POR_GRAU;
		PontoLatLng pontoPerimetro = new PontoLatLng(centro.lat, centro.lng+raioLatLng); //um ponto qualquer do perimetro, foi escolhido o da direita arbitrariamente
		Ponto pontoPerimetroPixel = new Ponto(pontoPerimetro.toPixel(ZOOM));
		Ponto centroPixel = new Ponto(centro.toPixel(ZOOM));
		int raioPixel = (int)Math.round(Ponto.distancia(pontoPerimetroPixel, centroPixel));
		return raioPixel;
	}
	
	private double densidadeMediaNoCirculo(KernelMap kernel, Ponto centro, int raio){
		int xIni = centro.x - raio;
		int xFin = centro.x + raio;
		int yIni = centro.y - raio;
		int yFin = centro.y + raio;
		double densidadeAcumulada = 0.0;
		int contCelulasCirculo = 0;
		for(int x=xIni; x<=xFin; x+=NODE_SIZE){
			for(int y=yIni; y<=yFin; y+=NODE_SIZE){
				Ponto posicaoCelula = new Ponto(x,y);
				double distanciaAoCentro = Ponto.distancia(posicaoCelula, centro); 
				if(distanciaAoCentro <= raio){
					contCelulasCirculo++;
					densidadeAcumulada += kernel.getDensidade(posicaoCelula);
				}
			}
		}
		return densidadeAcumulada/contCelulasCirculo;
	}
	
	private List<Point> buscaCrimes(Rectangle boundsPixel, Date dataInicial){
		
		Point pixelNO = new Point((int)boundsPixel.getMinX(), (int)boundsPixel.getMinY());
		Point pixelSE = new Point((int)boundsPixel.getMaxX(), (int)boundsPixel.getMaxY());
		PontoLatLng latlngNO = PontoLatLng.fromPixel(pixelNO, ZOOM);
		PontoLatLng latlngSE = PontoLatLng.fromPixel(pixelSE, ZOOM);
		
		
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("norte", latlngNO.lat);
		params.put("sul", latlngSE.lat);
		params.put("leste", latlngSE.lng);
		params.put("oeste", latlngNO.lng);
		params.put("dataInicial", dataInicial);
		params.put("dataFinal", new Date());
		List<BaseObject> crimes = crimeService.filter(params);
		List<Point> crimesPixel = ServletKernelMap.toPixel(crimes, ZOOM);
		
		return crimesPixel;
	}

}
