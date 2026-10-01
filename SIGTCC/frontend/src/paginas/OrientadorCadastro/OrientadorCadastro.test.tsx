import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, test, expect } from "vitest";
import { OrientadorCadastroPage } from "./index";

describe("OrientadorCadastroPage", () => {
    test("deve renderizar a página de cadastro corretamente", () => {
        render(
            <MemoryRouter>
                <OrientadorCadastroPage />
            </MemoryRouter>
        );
        expect(screen.getByText("Cadastrar Novo Orientador")).toBeInTheDocument();
    });
});