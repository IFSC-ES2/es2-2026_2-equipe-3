## **[Correção]**

**Problema:** Estão faltando alguns atributos na classe `OrientadorRequestDTO` para cumprir o contrato de dados estabelecido para a **US02**, em decorrência disso o salvamento do `Orientador` não está completo.

**Motivo:** No DTO `OrientadorRequestDTO` constam apenas os atributos `nome`, `email` e `vagas`, porém no contrato de dados foram estabelecidos: `nome`, `email`, `departamento`, `linhasDePesquisa`, `vagasDisponiveis` e `biografia`. Além da ausência dos campos, há uma divergência de nomenclatura no campo de vagas (`vagas` no DTO vs `vagasDisponiveis` no contrato). Modelo dos dados em JSON esperado:

```json
{
  "nome": "Dr. Adriano Lima",
  "email": "adriano.lima@ifsc.edu.br",
  "departamento": "DAE - Câmpus São José",
  "linhasDePesquisa": [
    "Engenharia de Software",
    "Qualidade de Software",
    "Sistemas Distribuídos"
  ],
  "vagasDisponiveis": 3,
  "biografia": "Professor com foco em processos de software, métricas e metodologias ágeis."
}
```

**Alternativa:**
Para cumprir o contrato de dados, as seguintes alterações devem ser feitas:
* **Refatoração do DTO:** Renomear o atributo `vagas` para `vagasDisponiveis` na classe `OrientadorRequestDTO` para garantir que o JSON esteja no padrão definido.
* **Inclusão de Atributos:** Adicionar os atributos faltantes no DTO: `departamento`, `biografia` e `linhasDePesquisa`.
* **Bean Validation:** Aplicar as anotações de validação nos novos atributose alterar a validação do campo de vagas de `@Min(value = 1)` para `@Min(value = 0)`, respeitando os limites estipulados no Dicionário de Dados.
* **Atualização da Camada de Serviço:** Modificar o método cadastrar no `OrientadorService` para que ele leia os novos dados do DTO, instancie o `Orientador` e o `PerfilOrientador` com essas informações, e converta a lista de strings em entidades `LinhaPesquisa`, atrelando tudo antes de chamar o `repository.save()`.