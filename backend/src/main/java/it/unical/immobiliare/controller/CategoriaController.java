package it.unical.immobiliare.controller;

import it.unical.immobiliare.dao.CategoriaDAO;
import it.unical.immobiliare.dto.CategoriaResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categorie")
public class CategoriaController {

    private final CategoriaDAO categoriaDAO;

    public CategoriaController(CategoriaDAO categoriaDAO) {
        this.categoriaDAO = categoriaDAO;
    }

    @GetMapping
    public List<CategoriaResponse> tutteLeCategorie() {
        return categoriaDAO.trovaTutte();
    }
}