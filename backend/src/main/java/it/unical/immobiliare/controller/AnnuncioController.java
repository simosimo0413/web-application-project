package it.unical.immobiliare.controller;

import it.unical.immobiliare.dao.AnnuncioDAO;
import it.unical.immobiliare.dto.AnnuncioCardDTO;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/annunci")
public class AnnuncioController {

    private final AnnuncioDAO annuncioDAO;

    public AnnuncioController(AnnuncioDAO annuncioDAO) {
        this.annuncioDAO = annuncioDAO;
    }

    @GetMapping
    public List<AnnuncioCardDTO> getAnnunci(
            @RequestParam(required = false) String citta,
            @RequestParam(required = false) BigDecimal prezzoMax,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String tipoContratto) {

            return annuncioDAO.trovaAnnunciAttivi(
                citta,
                prezzoMax,
                categoria,
                tipoContratto
            );
    }
}