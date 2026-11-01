export const state = {
  gameId: sessionStorage.getItem("enigmaGameId"),
  dashboard: null,
  caseFile: null,
  locations: [],
  suspects: [],
  evidence: []
};

export function setGameId(id) {
  state.gameId = id;
  if (id) sessionStorage.setItem("enigmaGameId", id);
  else sessionStorage.removeItem("enigmaGameId");
}
