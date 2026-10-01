import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, test, expect, vi } from "vitest";
import { OrientadorListagemPage } from "./index";

vi.mock("../../utils/orientadorService", () => ({
    listarOrientadores: vi.fn().mockResolvedValue([]),
}));

describe("OrientadorListagemPage", () => {
    test("deve renderizar a página do catálogo corretamente", () => {
        render(
            <MemoryRouter>
                <OrientadorListagemPage />
            </MemoryRouter>
        );
        expect(screen.getByText("Catálogo de Orientadores")).toBeInTheDocument();
    });
});