package it.unical.immobiliare.controller;

import it.unical.immobiliare.dao.AnnuncioDAO;
import it.unical.immobiliare.dto.AnnuncioCardDTO;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

import it.unical.immobiliare.dto.AnnuncioDettaglioDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

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
    @GetMapping("/{id}")
    public ResponseEntity<AnnuncioDettaglioDTO> getAnnuncioPerId(
        @PathVariable Long id) {
    return annuncioDAO.trovaAnnuncioPerId(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}