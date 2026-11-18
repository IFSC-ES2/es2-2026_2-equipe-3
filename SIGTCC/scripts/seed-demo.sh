#!/usr/bin/env bash
# Script reprodutível de preparação da massa de dados de demonstração e avaliação (Issue #280).
#
# Configura o cenário previsível de demonstração sobre o banco inicial (migração V5):
#   1. Confere o login dos 3 usuários de teste no Keycloak (aluno, orientador e responsável);
#   2. Atualiza os 2 orientadores ativos com linhas de pesquisa, biografia e vagas disponíveis;
#   3. Confere o vínculo do professor responsável com a Turma_TCC vigente (2026/2);
#   4. Cadastra (ou atualiza, de forma idempotente) os 3 prazos da turma vigente
#      (PROJETO_TCC em +30 dias, TCC1 em +60 dias e TCC2 em +90 dias).
#
# Uso local (apenas preparar cenário limpo para demonstração manual):
#   bash SIGTCC/scripts/seed-demo.sh
#
# Uso local (preparar cenário e validar todos os 8 passos do roteiro da Issue #286):
#   bash SIGTCC/scripts/seed-demo.sh --validar-roteiro
#
# Uso no staging:
#   KEYCLOAK_URL=https://sigtcc.cobbler-albacore.ts.net:10000 \
#   API_URL=https://sigtcc.cobbler-albacore.ts.net:8443 \
#   bash SIGTCC/scripts/seed-demo.sh

set -u

KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8081}"
API_URL="${API_URL:-http://localhost:8080}"
KEYCLOAK_URL="${KEYCLOAK_URL%/}"
API_URL="${API_URL%/}"

ALUNO_EMAIL="${ALUNO_EMAIL:-aluno@example.com}"
ALUNO_SENHA="${ALUNO_SENHA:-aluno123}"
ORIENTADOR_EMAIL="${ORIENTADOR_EMAIL:-professor.orientador@example.com}"
ORIENTADOR_SENHA="${ORIENTADOR_SENHA:-professor123}"
RESPONSAVEL_EMAIL="${RESPONSAVEL_EMAIL:-professor.responsavel@example.com}"
RESPONSAVEL_SENHA="${RESPONSAVEL_SENHA:-professor123}"

TMP="$(mktemp)"
TMP_REQ="$(mktemp)"
trap 'rm -f "$TMP" "$TMP_REQ"' EXIT
PASSOU=0
STATUS=""
CORPO=""
REQUISICAO=""

chamar() {
  local metodo="$1" caminho="$2" token="$3" json="${4:-}"
  local args=(-s -o "$TMP" -w '%{http_code}' -X "$metodo" "$API_URL$caminho")
  [ -n "$token" ] && args+=(-H "Authorization: Bearer $token")
  REQUISICAO="$json"
  if [ -n "$json" ]; then
    printf '%s' "$json" > "$TMP_REQ"
    STATUS="$(curl "${args[@]}" -H "Content-Type: application/json" --data-binary @- < "$TMP_REQ")"
  else
    STATUS="$(curl "${args[@]}")"
  fi
  CORPO="$(cat "$TMP")"
}

esperar() {
  local descricao="$1" esperado="$2"
  if [[ "$STATUS" =~ ^($esperado)$ ]]; then
    echo "  OK    $descricao (HTTP $STATUS)"
    PASSOU=$((PASSOU + 1))
  else
    echo "  FALHA $descricao: esperado HTTP $esperado, recebido '$STATUS'"
    [ -n "$REQUISICAO" ] && echo "        enviado:  $REQUISICAO"
    echo "        resposta: $CORPO"
    echo; echo "Preparação interrompida. $PASSOU etapa(s) concluída(s) antes da falha."
    exit 1
  fi
}

conter() {
  if [[ "$CORPO" == *"$2"* ]]; then
    echo "  OK    $1"
    PASSOU=$((PASSOU + 1))
  else
    echo "  FALHA $1: trecho '$2' não encontrado na resposta"
    echo "        resposta: $CORPO"
    echo; echo "Preparação interrompida. $PASSOU etapa(s) concluída(s) antes da falha."
    exit 1
  fi
}

login() {
  curl -s -X POST "$KEYCLOAK_URL/realms/sigtcc/protocol/openid-connect/token" \
    -d grant_type=password -d client_id=sigtcc-frontend \
    --data-urlencode "username=$1" --data-urlencode "password=$2" \
    | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p'
}

data_futura() {
  local dias="$1"
  date -d "+${dias} days" +%F 2>/dev/null || date -v+"${dias}"d +%F
}

extrair_id_prazo() {
  local tipo="$1"
  printf '%s' "$CORPO" \
    | tr '{' '\n' \
    | grep "\"tipo\":\"$tipo\"" \
    | sed -n 's/.*"id":\([0-9][0-9]*\).*/\1/p' \
    | head -n1
}

garantir_prazo() {
  local tipo="$1" data_limite="$2" token="$3"
  chamar GET /api/v1/prazos "$token"
  local prazo_id
  prazo_id="$(extrair_id_prazo "$tipo")"
  if [ -n "$prazo_id" ]; then
    chamar PATCH "/api/v1/prazos/$prazo_id" "$token" "{\"dataLimite\":\"$data_limite\"}"
    esperar "prazo $tipo atualizado para $data_limite" "200"
  else
    chamar POST /api/v1/prazos "$token" "{\"tipo\":\"$tipo\",\"dataLimite\":\"$data_limite\"}"
    esperar "prazo $tipo cadastrado para $data_limite" "201"
  fi
}

echo "Preparando cenário de demonstração -> API: $API_URL | Keycloak: $KEYCLOAK_URL"

echo; echo "1) Verificando usuários de teste no Keycloak (realm sigtcc)"
TK_ALUNO="$(login "$ALUNO_EMAIL" "$ALUNO_SENHA")"
TK_ORIENTADOR="$(login "$ORIENTADOR_EMAIL" "$ORIENTADOR_SENHA")"
TK_RESPONSAVEL="$(login "$RESPONSAVEL_EMAIL" "$RESPONSAVEL_SENHA")"
for par in "aluno ($ALUNO_EMAIL):$TK_ALUNO" "orientador ($ORIENTADOR_EMAIL):$TK_ORIENTADOR" "responsável ($RESPONSAVEL_EMAIL):$TK_RESPONSAVEL"; do
  if [ -z "${par#*:}" ]; then
    echo "  FALHA login de ${par%%:*}: verifique se o Keycloak está ativo e com o realm importado."
    exit 1
  fi
  echo "  OK    autenticação de ${par%%:*}"; PASSOU=$((PASSOU + 1))
done

echo; echo "2) Configurando orientadores com linhas de pesquisa, biografia e vagas"
chamar PATCH /api/v1/orientadores/1 "$TK_ORIENTADOR" \
  '{"nome":"Professor Orientador","departamento":"DAELN","vagasDisponiveis":3,"biografia":"Docente pesquisador em Engenharia de Software, Arquitetura de Sistemas e Qualidade de Software.","linhasDePesquisa":["Engenharia de Software","Arquitetura de Sistemas","Qualidade de Software"]}'
esperar "orientador 1 (professor.orientador@example.com) configurado" "200"

chamar PATCH /api/v1/orientadores/2 "$TK_RESPONSAVEL" \
  '{"nome":"Professor Responsável pela Turma","departamento":"DAELN","vagasDisponiveis":3,"biografia":"Docente responsável pela coordenação da Turma TCC 2026/2 e pesquisador em Inteligência Artificial e Ciência de Dados.","linhasDePesquisa":["Inteligência Artificial","Ciência de Dados","Engenharia de Software"]}'
esperar "orientador 2 (professor.responsavel@example.com) configurado" "200"

echo; echo "3) Conferindo turma vigente (2026/2) e responsabilidade do professor"
chamar GET /api/v1/professores/me "$TK_RESPONSAVEL"
esperar "consulta de perfil do professor responsável" "200"
conter "professor.responsavel@example.com é responsável pela turma vigente" '"responsavelTurma":true'

echo; echo "4) Cadastrando/atualizando prazos da turma vigente (2026/2)"
PRAZO_PROJETO="$(data_futura 30)"
PRAZO_TCC1="$(data_futura 60)"
PRAZO_TCC2="$(data_futura 90)"

# Atualiza de trás para frente caso já existam prazos futuros cadastrados,
# respeitando a ordem cronológica (PROJETO_TCC < TCC1 < TCC2).
chamar GET /api/v1/prazos "$TK_RESPONSAVEL"
esperar "listagem inicial de prazos da turma vigente" "200"
ID_TCC2="$(extrair_id_prazo "TCC2")"
ID_TCC1="$(extrair_id_prazo "TCC1")"
if [ -n "$ID_TCC2" ]; then
  chamar PATCH "/api/v1/prazos/$ID_TCC2" "$TK_RESPONSAVEL" "{\"dataLimite\":\"$PRAZO_TCC2\"}"
  esperar "ajuste preliminar de TCC2 para $PRAZO_TCC2" "200"
fi
if [ -n "$ID_TCC1" ]; then
  chamar PATCH "/api/v1/prazos/$ID_TCC1" "$TK_RESPONSAVEL" "{\"dataLimite\":\"$PRAZO_TCC1\"}"
  esperar "ajuste preliminar de TCC1 para $PRAZO_TCC1" "200"
fi

garantir_prazo "PROJETO_TCC" "$PRAZO_PROJETO" "$TK_RESPONSAVEL"
garantir_prazo "TCC1" "$PRAZO_TCC1" "$TK_RESPONSAVEL"
garantir_prazo "TCC2" "$PRAZO_TCC2" "$TK_RESPONSAVEL"

echo; echo "5) Conferindo estado final do catálogo e do calendário para a demonstração"
chamar GET /api/v1/orientadores "$TK_ALUNO"
esperar "catálogo de orientadores disponível para o aluno" "200"
conter "linha 'Arquitetura de Sistemas' presente no catálogo" "Arquitetura de Sistemas"
conter "linha 'Inteligência Artificial' presente no catálogo" "Inteligência Artificial"

chamar GET /api/v1/prazos "$TK_ALUNO"
esperar "calendário de prazos visível para o aluno" "200"
conter "prazo PROJETO_TCC registrado" '"tipo":"PROJETO_TCC"'
conter "prazo TCC1 registrado" '"tipo":"TCC1"'
conter "prazo TCC2 registrado" '"tipo":"TCC2"'

if [ "${1:-}" = "--validar-roteiro" ]; then
  echo; echo "6) Validando os 8 passos do Roteiro da Demonstração (docs/ROTEIRO-DEMONSTRACAO.md)"

  # Passo 1: Controle de acesso sem token
  chamar GET /api/v1/entregas/minhas ""
  esperar "Passo 1: rota protegida rejeita acesso sem token" "401"

  # Passo 2: Catálogo e filtros combinados (US02 e US03)
  chamar GET "/api/v1/orientadores?area=Arquitetura%20de%20Sistemas&temVagas=true" "$TK_ALUNO"
  esperar "Passo 2: filtro exato por 'Arquitetura de Sistemas' + temVagas=true" "200"
  conter "Passo 2: retorna Professor Orientador" '"email":"professor.orientador@example.com"'

  chamar GET "/api/v1/orientadores?area=Computacao%20Quantica" "$TK_ALUNO"
  esperar "Passo 2: filtro sem correspondência retorna lista vazia" "200"
  conter "Passo 2: estado vazio []" "[]"

  # Passo 3: Solicitação de orientação pelo aluno (US04)
  chamar GET "/api/v1/solicitacoes/orientador/1?status=PENDENTE" "$TK_ORIENTADOR"
  PEND_ID="$(printf '%s' "$CORPO" | sed -n 's/^\[{"id":\([0-9][0-9]*\).*/\1/p' | head -n1)"
  if [ -n "$PEND_ID" ]; then
    chamar PATCH "/api/v1/solicitacoes/$PEND_ID/status" "$TK_ORIENTADOR" \
      '{"status":"RECUSADA","justificativa":"Limpeza preliminar para validacao do roteiro."}'
  fi
  chamar POST /api/v1/solicitacoes "$TK_ALUNO" \
    '{"orientadorId":1,"curso":"Engenharia de Software","tema":"Arquitetura de Microsserviços e Observabilidade em Sistemas Acadêmicos","mensagem":"Olá professor, gostaria de desenvolver meu TCC na linha de Arquitetura de Sistemas aplicando padrões de resiliência."}'
  esperar "Passo 3: aluno envia solicitação de orientação" "201"
  SOL_ID="$(printf '%s' "$CORPO" | sed -n 's/^{"id":\([0-9][0-9]*\).*/\1/p' | head -n1)"
  conter "Passo 3: solicitação nasce PENDENTE" '"status":"PENDENTE"'

  # Passo 4: Aceite pelo orientador e decremento de vagas via Observer (US04)
  chamar PATCH "/api/v1/solicitacoes/$SOL_ID/status" "$TK_ORIENTADOR" '{"status":"ACEITA"}'
  esperar "Passo 4: orientador aceita a solicitação" "200"
  conter "Passo 4: status transita para ACEITA" '"status":"ACEITA"'
  chamar GET /api/v1/orientadores/1 "$TK_ORIENTADOR"
  esperar "Passo 4: consulta de vagas do orientador 1" "200"
  conter "Passo 4: Observer decrementou vagasDisponiveis de 3 para 2" '"vagasDisponiveis":2'

  # Passo 5: Gestão do calendário pelo professor responsável (US05)
  chamar GET /api/v1/prazos "$TK_RESPONSAVEL"
  ID_PROJ="$(extrair_id_prazo "PROJETO_TCC")"
  chamar PATCH "/api/v1/prazos/$ID_PROJ" "$TK_RESPONSAVEL" '{"dataLimite":"2025-01-10"}'
  esperar "Passo 5: data limite no passado é rejeitada" "400"
  PRAZO_AJUSTADO="$(data_futura 25)"
  chamar PATCH "/api/v1/prazos/$ID_PROJ" "$TK_RESPONSAVEL" "{\"dataLimite\":\"$PRAZO_AJUSTADO\"}"
  esperar "Passo 5: professor responsável atualiza prazo de PROJETO_TCC" "200"

  # Passo 6: Submissão de entrega por link externo HTTPS pelo aluno (US06)
  chamar POST /api/v1/entregas "$TK_ALUNO" \
    '{"tipo":"PROJETO_TCC","urlDocumento":"http://drive.google.com/file/d/1A2B3C-projeto-tcc-v1/view"}'
  esperar "Passo 6: link http:// inseguro é rejeitado" "400"
  chamar POST /api/v1/entregas "$TK_ALUNO" \
    '{"tipo":"PROJETO_TCC","urlDocumento":"https://drive.google.com/file/d/1A2B3C-projeto-tcc-v1/view"}'
  esperar "Passo 6: aluno submete link https:// válido" "201"
  ENT_ID="$(printf '%s' "$CORPO" | sed -n 's/^{"id":\([0-9][0-9]*\).*/\1/p' | head -n1)"
  conter "Passo 6: entrega criada com status SUBMETIDO" '"status":"SUBMETIDO"'

  # Passo 7: Avaliação com parecer AJUSTES_NECESSARIOS pelo orientador (US07)
  chamar POST "/api/v1/entregas/$ENT_ID/parecer" "$TK_ORIENTADOR" '{"resultado":"AJUSTES_NECESSARIOS"}'
  esperar "Passo 7: parecer AJUSTES_NECESSARIOS sem comentário é rejeitado" "422"
  chamar POST "/api/v1/entregas/$ENT_ID/parecer" "$TK_ORIENTADOR" \
    '{"resultado":"AJUSTES_NECESSARIOS","comentario":"A fundamentação teórica está consistente, mas é necessário detalhar o diagrama de componentes C3 na seção 4 antes da aprovação."}'
  esperar "Passo 7: orientador registra parecer AJUSTES_NECESSARIOS" "200"
  conter "Passo 7: status da entrega muda para AJUSTES_NECESSARIOS" '"status":"AJUSTES_NECESSARIOS"'

  # Passo 8: Reenvio pelo aluno e aprovação final pelo orientador (US06 e US07)
  chamar POST /api/v1/entregas "$TK_ALUNO" \
    '{"tipo":"PROJETO_TCC","urlDocumento":"https://drive.google.com/file/d/1A2B3C-projeto-tcc-v2-revisado/view"}'
  esperar "Passo 8: aluno reenvia entrega após ajustes" "200"
  conter "Passo 8: status volta para SUBMETIDO" '"status":"SUBMETIDO"'
  chamar POST "/api/v1/entregas/$ENT_ID/parecer" "$TK_ORIENTADOR" \
    '{"resultado":"APROVADO","comentario":"Ajustes no diagrama C3 verificados. Projeto de TCC aprovado!"}'
  esperar "Passo 8: orientador registra parecer final APROVADO" "200"
  conter "Passo 8: status final APROVADO" '"status":"APROVADO"'
fi

echo; echo "Cenário de demonstração pronto ($PASSOU verificações concluídas com sucesso)."
