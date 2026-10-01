import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { describe, test, expect } from "vitest";
import { SolicitarOrientacaoPage } from "./index";

describe("SolicitarOrientacaoPage", () => {
    test("deve renderizar o formulário quando o ID na URL for um número válido", () => {
        render(
            <MemoryRouter initialEntries={["/solicitar/7"]}>
                <Routes>
                    <Route path="/solicitar/:orientadorId" element={<SolicitarOrientacaoPage />} />
                </Routes>
            </MemoryRouter>
        );
        expect(screen.getByText("Conte sua proposta de TCC")).toBeInTheDocument();
    });

    test("deve exibir mensagem de erro quando o ID na URL for inválido (ex: letras)", () => {
        render(
            <MemoryRouter initialEntries={["/solicitar/abc"]}>
                <Routes>
                    <Route path="/solicitar/:orientadorId" element={<SolicitarOrientacaoPage />} />
                </Routes>
            </MemoryRouter>
        );
        expect(screen.getByText("Orientador inválido.")).toBeInTheDocument();
    });
});