package it.unical.immobiliare.dto;

import jakarta.validation.constraints.NotNull;

public record AdminBanRequest(@NotNull Boolean bannato) {}
