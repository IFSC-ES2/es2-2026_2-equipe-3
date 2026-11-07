#!/usr/bin/env bash
# Smoke test por API do ambiente de staging (Issue #220).
#
# Percorre o fluxo principal do TCC usando os usuários de teste do realm:
# login -> solicitação aceita -> prazo -> entrega por link -> parecer.
# Os dados de apoio (solicitação e prazo) são criados pelo próprio script,
# então rode em um banco zerado (docker compose ... down -v).
#
# Uso:
#   bash SIGTCC/scripts/smoke-test.sh
#   KEYCLOAK_URL=https://kc.exemplo.com API_URL=https://api.exemplo.com bash SIGTCC/scripts/smoke-test.sh

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
trap 'rm -f "$TMP"' EXIT
PASSOU=0
STATUS=""
CORPO=""
REQUISICAO=""

# chamar METODO CAMINHO TOKEN [JSON]  ->  preenche STATUS e CORPO
chamar() {
  local metodo="$1" caminho="$2" token="$3" json="${4:-}"
  local args=(-s -o "$TMP" -w '%{http_code}' -X "$metodo" "$API_URL$caminho")
  [ -n "$token" ] && args+=(-H "Authorization: Bearer $token")
  REQUISICAO="$json"
  if [ -n "$json" ]; then
    # Corpo por stdin: no Windows, o mesmo JSON passado em argumento do curl.exe chega
    # corrompido e o backend responde 400 "corpo ilegivel" (comprovado em teste A/B).
    # Por stdin os bytes chegam intactos, inclusive com acentos.
    STATUS="$(printf '%s' "$json" | curl "${args[@]}" -H "Content-Type: application/json" --data-binary @-)"
  else
    STATUS="$(curl "${args[@]}")"
  fi
  CORPO="$(cat "$TMP")"
}

# esperar "descrição" "200|201"  -> confere o STATUS da última chamada
esperar() {
  local descricao="$1" esperado="$2"
  if [[ "$STATUS" =~ ^($esperado)$ ]]; then
    echo "  OK    $descricao (HTTP $STATUS)"
    PASSOU=$((PASSOU + 1))
  else
    echo "  FALHA $descricao: esperado HTTP $esperado, recebido '$STATUS'"
    [ -n "$REQUISICAO" ] && echo "        enviado:  $REQUISICAO"
    echo "        resposta: $CORPO"
    echo; echo "Smoke test interrompido. $PASSOU verificação(ões) passaram antes da falha."
    exit 1
  fi
}

# conter "descrição" "trecho"  -> confere o CORPO da última chamada
conter() {
  if [[ "$CORPO" == *"$2"* ]]; then
    echo "  OK    $1"
    PASSOU=$((PASSOU + 1))
  else
    echo "  FALHA $1: não encontrei '$2' na resposta"
    echo "        resposta: $CORPO"
    echo; echo "Smoke test interrompido. $PASSOU verificação(ões) passaram antes da falha."
    exit 1
  fi
}

# Jackson devolve JSON compacto, então sed basta (sem depender de jq)
numero() { printf '%s' "$CORPO" | sed -n "s/.*\"$1\":\([0-9][0-9]*\).*/\1/p" | head -n1; }

login() {  # login EMAIL SENHA -> imprime o token
  curl -s -X POST "$KEYCLOAK_URL/realms/sigtcc/protocol/openid-connect/token" \
    -d grant_type=password -d client_id=sigtcc-frontend \
    --data-urlencode "username=$1" --data-urlencode "password=$2" \
    | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p'
}

echo "Smoke test -> API: $API_URL | Keycloak: $KEYCLOAK_URL"

echo; echo "1) Login via Keycloak"
TK_ALUNO="$(login "$ALUNO_EMAIL" "$ALUNO_SENHA")"
TK_ORIENTADOR="$(login "$ORIENTADOR_EMAIL" "$ORIENTADOR_SENHA")"
TK_RESPONSAVEL="$(login "$RESPONSAVEL_EMAIL" "$RESPONSAVEL_SENHA")"
for par in "aluno:$TK_ALUNO" "orientador:$TK_ORIENTADOR" "responsável:$TK_RESPONSAVEL"; do
  if [ -z "${par#*:}" ]; then
    echo "  FALHA login do ${par%%:*}: o Keycloak não devolveu token (veja KEYCLOAK_URL, usuário/senha e o CORS/issuer)."
    exit 1
  fi
  echo "  OK    login do ${par%%:*}"; PASSOU=$((PASSOU + 1))
done

echo; echo "2) Backend aceita o token e protege as rotas"
chamar GET /api/v1/entregas/minhas ""
esperar "sem token a API recusa" "401"
chamar GET /api/v1/orientadores "$TK_ALUNO"
esperar "com token a API responde" "200"

echo; echo "3) Dados de apoio (solicitação aceita e prazo)"
chamar POST /api/v1/solicitacoes "$TK_ALUNO" \
  '{"orientadorId":1,"curso":"Engenharia de Software","tema":"Computação gráfica","mensagem":"Solicitação criada pelo smoke test."}'
esperar "aluno cria solicitação" "201"
SOLICITACAO_ID="$(numero id)"
chamar GET "/api/v1/solicitacoes/$SOLICITACAO_ID" "$TK_ALUNO"
esperar "aluno lê a solicitação de volta do banco" "200"
conter "acentos preservados na ida e volta (MySQL)" "Solicitação criada pelo smoke test."
chamar PATCH "/api/v1/solicitacoes/$SOLICITACAO_ID/status" "$TK_ORIENTADOR" '{"status":"ACEITA"}'
esperar "orientador aceita a solicitação" "200"
PRAZO="$(date -d '+30 days' +%F 2>/dev/null || date -v+30d +%F)"
chamar POST /api/v1/prazos "$TK_RESPONSAVEL" "{\"tipo\":\"PROJETO_TCC\",\"dataLimite\":\"$PRAZO\"}"
esperar "professor responsável cadastra o prazo" "201"

echo; echo "4) Entrega por link (ADR-0013)"
chamar POST /api/v1/entregas "$TK_ALUNO" '{"tipo":"PROJETO_TCC","urlDocumento":"http://drive.google.com/arquivo"}'
esperar "link http:// é rejeitado" "400|422"
chamar POST /api/v1/entregas "$TK_ALUNO" '{"tipo":"PROJETO_TCC","urlDocumento":"https://drive.google.com/file/d/smoke-test"}'
esperar "link https:// é aceito" "201"
conter "status inicial é SUBMETIDO" '"status":"SUBMETIDO"'
ENTREGA_ID="$(numero id)"

echo; echo "5) Avaliação do orientador"
chamar POST "/api/v1/entregas/$ENTREGA_ID/parecer" "$TK_ALUNO" '{"resultado":"APROVADO"}'
esperar "aluno não pode registrar parecer" "403"
chamar GET "/api/v1/entregas/$ENTREGA_ID" "$TK_RESPONSAVEL"
esperar "professor que não é o orientador não vê a entrega" "403"
chamar GET /api/v1/entregas/orientador "$TK_ORIENTADOR"
esperar "orientador lista as entregas dos orientandos" "200"
conter "a entrega enviada aparece na lista" "\"id\":$ENTREGA_ID"
chamar POST "/api/v1/entregas/$ENTREGA_ID/parecer" "$TK_ORIENTADOR" '{"resultado":"AJUSTES_NECESSARIOS"}'
esperar "ajustes sem comentário são rejeitados" "422"
chamar POST "/api/v1/entregas/$ENTREGA_ID/parecer" "$TK_ORIENTADOR" '{"resultado":"APROVADO","comentario":"Aprovado no smoke test."}'
esperar "orientador aprova a entrega" "200"
chamar POST "/api/v1/entregas/$ENTREGA_ID/parecer" "$TK_ORIENTADOR" '{"resultado":"REPROVADO","comentario":"Segundo parecer."}'
esperar "novo parecer após APROVADO é bloqueado" "409"

echo; echo "6) Aluno acompanha o resultado"
chamar GET /api/v1/entregas/minhas "$TK_ALUNO"
esperar "aluno lista suas entregas" "200"
conter "status refletido para o aluno" '"status":"APROVADO"'

echo; echo "Smoke test concluído: $PASSOU verificações passaram."