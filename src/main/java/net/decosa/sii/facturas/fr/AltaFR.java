package net.decosa.sii.facturas.fr;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import net.decosa.sii.aeat.DesgloseFacturaRecibidasType;
import net.decosa.sii.aeat.DesgloseFacturaRecibidasType.DesgloseIVA;
import net.decosa.sii.aeat.DesgloseFacturaRecibidasType.InversionSujetoPasivo;
import net.decosa.sii.aeat.DetalleIVARecibida2Type;
import net.decosa.sii.aeat.DetalleIVARecibidaType;
import net.decosa.sii.aeat.FacturaRecibidaType;
import net.decosa.sii.aeat.LRFacturasRecibidasType;
import net.decosa.sii.aeat.RespuestaLRFRecibidasType;
import net.decosa.sii.aeat.RespuestaRecibidaType;
import net.decosa.sii.aeat.SuministroLRFacturasRecibidas;
import net.decosa.sii.ed.DesgloseIVAFR;
import net.decosa.sii.ed.FacturaRecibida;
import net.decosa.sii.facturas.AbstractAltaFacturasSII;
import net.decosa.sii.facturas.Respuesta;
import net.decosa.sii.util.NumberUtils;


@Component
public class AltaFR extends AbstractAltaFacturasSII {
	
	private SuministroLRFacturasRecibidas suministroFR;

	
	
	@SuppressWarnings("unchecked")
	@Override
	protected void procesar() throws Exception {
		// Se prepara la información
		suministroFR = new SuministroLRFacturasRecibidas();
		
		// Cabecera
		suministroFR.setCabecera(getCabeceraAltaSII());
		
		// Facturas
		List<LRFacturasRecibidasType> facturasRecibidas = toFacturasRecibidasSII((List<FacturaRecibida>) facturas);
		suministroFR.getRegistroLRFacturasRecibidas().addAll(facturasRecibidas);		
	}

	
	@Override
	public String getXML(boolean pretty) {
		return getXML(suministroFR, pretty);
	}
	
	
	@Override
	public String getXML() {
		return getXML(suministroFR);
	}

	
	@Override
	public Respuesta enviar(boolean simularEnvio) throws Exception {

		// Envío
		RespuestaLRFRecibidasType respuestaFR = wsSIIRequest.altaFR(suministroFR, simularEnvio);
		
		// Guardar XML de respuesta?
		
		// Procesar respuesta
		return procesarRespuesta(respuestaFR, simularEnvio);
		
	}

	
	private Respuesta procesarRespuesta(RespuestaLRFRecibidasType respuestaFR, boolean simularEnvio) {
		if (simularEnvio) return null;
		
		// Procesar cabecera
		Respuesta respuestaAlta = procesarRespuestaCabecera(respuestaFR.getEstadoEnvio(),
				respuestaFR.getDatosPresentacion(), respuestaFR.getCSV());
		
		// Procesar detalles
		for(RespuestaRecibidaType respuestaLinea: respuestaFR.getRespuestaLinea())
			procesarRespuestaDetalles(respuestaAlta, respuestaLinea.getIDFactura().getNumSerieFacturaEmisor(),
					respuestaLinea.getEstadoRegistro(), respuestaLinea.getDescripcionErrorRegistro(),
					respuestaLinea.getCodigoErrorRegistro());
		
		return respuestaAlta;
	}

	
	private List<LRFacturasRecibidasType> toFacturasRecibidasSII(List<FacturaRecibida> facturasRecibidas) {
		List<LRFacturasRecibidasType> facturasRecibidasSII = new ArrayList<LRFacturasRecibidasType>();
		
		for(FacturaRecibida facturaRecibida: facturasRecibidas) {
			
			// FacturaSII
			LRFacturasRecibidasType facturaRecibidaSII = new LRFacturasRecibidasType();
			
			// Periodo impositivo
			facturaRecibidaSII.setPeriodoImpositivo(facturaRecibida.getPeriodoImpositivo().toPeriodoImpositivoSII());
			
			// idFactura
			facturaRecibidaSII.setIDFactura(facturaRecibida.getIdFactura().getIDFacturaRecibidaType());
			
			// Factura recibida
			FacturaRecibidaType facturaRecibidaType = new FacturaRecibidaType();
			facturaRecibidaType.setTipoFactura(facturaRecibida.getTipoFacturaSII());
			facturaRecibidaType.setClaveRegimenEspecialOTrascendencia(facturaRecibida.getClaveRegimenEspecialOTrascendencia());
			facturaRecibidaType.setDescripcionOperacion(facturaRecibida.getDescripcionOperacionSII());
			facturaRecibidaType.setFechaOperacion(facturaRecibida.getFechaOperacionSII());
			
			// Contraparte
			facturaRecibidaType.setContraparte(facturaRecibida.getContraparte().getPersonaFisicaJuridicaType());
			
			// Desglose factura
			DesgloseFacturaRecibidasType desgloseFactura = new DesgloseFacturaRecibidasType();
			InversionSujetoPasivo isp = null;
			DesgloseIVA iva = null;
			Double quotaDeducible = 0.0;
			
			for(DesgloseIVAFR facturaLinea: facturaRecibida.getDetallesIVA()) {
				
				if (facturaLinea.getIsp()) {
					if (isp == null) isp = new InversionSujetoPasivo();
					
					// Detalle IVA 
					DetalleIVARecibida2Type detalleIVARecibida = new DetalleIVARecibida2Type();
					detalleIVARecibida.setTipoImpositivo("" + facturaLinea.getTipoImpositivo());
					detalleIVARecibida.setBaseImponible("" + facturaLinea.getBaseImponible());
					detalleIVARecibida.setCuotaSoportada("" + facturaLinea.getCuotaSoportada());
					
					isp.getDetalleIVA().add(detalleIVARecibida);
					
				} else {
					if (iva == null) iva = new DesgloseIVA();
					
					// Detalle IVA 
					DetalleIVARecibidaType detalleIVARecibida = new DetalleIVARecibidaType();
					detalleIVARecibida.setTipoImpositivo("" + facturaLinea.getTipoImpositivo());
					detalleIVARecibida.setBaseImponible("" + facturaLinea.getBaseImponible());
					detalleIVARecibida.setCuotaSoportada("" + facturaLinea.getCuotaSoportada());
					quotaDeducible += facturaLinea.getCuotaSoportada();
					
					iva.getDetalleIVA().add(detalleIVARecibida);
				}
			}
			
			if (isp != null) desgloseFactura.setInversionSujetoPasivo(isp);
			if (iva != null) desgloseFactura.setDesgloseIVA(iva);
			facturaRecibidaType.setDesgloseFactura(desgloseFactura);
			
			facturaRecibidaType.setDescripcionOperacion(facturaRecibida.getDescripcionOperacion());
			facturaRecibidaType.setFechaRegContable(facturaRecibida.getFechaRegContableSII());
			facturaRecibidaType.setFechaOperacion(facturaRecibida.getFechaOperacionSII());
			facturaRecibidaType.setCuotaDeducible("" + NumberUtils.round(quotaDeducible));
			
			facturaRecibidaSII.setFacturaRecibida(facturaRecibidaType);
			facturasRecibidasSII.add(facturaRecibidaSII);
		}
		
		return facturasRecibidasSII;
	}

}
