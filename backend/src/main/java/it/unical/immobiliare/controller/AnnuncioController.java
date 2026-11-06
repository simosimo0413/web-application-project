package it.unical.immobiliare.controller;

import it.unical.immobiliare.dao.AnnuncioDAO;
import it.unical.immobiliare.dto.AnnuncioCardDTO;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/annunci")
public class AnnuncioController {

    private final AnnuncioDAO annuncioDAO;

    public AnnuncioController(AnnuncioDAO annuncioDAO) {
        this.annuncioDAO = annuncioDAO;
    }

    @GetMapping
    public List<AnnuncioCardDTO> getAnnunci() {
        return annuncioDAO.trovaAnnunciAttivi();
    }
}