package net.decosa.sii.facturas;

import net.decosa.sii.aeat.CabeceraSii;
import net.decosa.sii.aeat.ClaveTipoComunicacionType;
import net.decosa.sii.aeat.PersonaFisicaJuridicaESType;


public abstract class AbstractAltaFacturasSII extends AbstractFacturasSII {

	
	protected CabeceraSii getCabeceraAltaSII() {
		PersonaFisicaJuridicaESType personaFisicaJuridicaESType = new PersonaFisicaJuridicaESType();
		personaFisicaJuridicaESType.setNIF(cif);
		personaFisicaJuridicaESType.setNombreRazon(nombreEmpresa.toUpperCase());
		
		CabeceraSii cabeceraSii = new CabeceraSii();
		cabeceraSii.setIDVersionSii(siiVersion);
		cabeceraSii.setTipoComunicacion(ClaveTipoComunicacionType.A_0);
		cabeceraSii.setTitular(personaFisicaJuridicaESType);
		
		return cabeceraSii;
	}
	
}
